/*
 * This software was developed at the National Institute of Standards and
 * Technology by employees of the Federal Government in the course of
 * their official duties. Pursuant to title 17 Section 105 of the United
 * States Code this software is not subject to copyright protection and is
 * in the public domain. This software is an experimental system. NIST assumes
 * no responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or
 * any other characteristic. We would appreciate acknowledgement if the
 * software is used.
 */
package gov.nist.itl.ssd.wipp.backend.data.visualization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.data.pyramid.Pyramid;
import gov.nist.itl.ssd.wipp.backend.data.pyramid.PyramidRepository;
import gov.nist.itl.ssd.wipp.backend.data.pyramid.timeslices.PyramidTimeSlice;
import gov.nist.itl.ssd.wipp.backend.data.pyramid.timeslices.PyramidTimeSliceRepository;
import gov.nist.itl.ssd.wipp.backend.data.visualization.manifest.Manifest;
import io.swagger.annotations.Api;

/**
 * Controller for downloading visualization, returns a ZIP folder with pyramids + manifest
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Controller
@Api(tags="Visualization Entity")
@RequestMapping(CoreConfig.BASE_URI + "/visualizations/{visualizationId}/download")
public class VisualizationDownloadController {

	@Autowired
	CoreConfig config;
	
	@Autowired
    private VisualizationRepository visualizationRepository;
	
	@Autowired
    private PyramidRepository pyramidRepository;
	
	@Autowired
    private PyramidTimeSliceRepository pyramidTimeSliceRepository;
	
	@RequestMapping(
            value = "",
            method = RequestMethod.GET,
            produces = "application/zip")
	// We make sure the user trying to download the visualization has the right to access it
	@PreAuthorize("@securityServiceData.checkAuthorizeVisualizationId(#visualizationId, false)")
    public void get(
            @PathVariable("visualizationId") String visualizationId,
            HttpServletResponse response) throws IOException {
        
		Visualization visualization = null;
		Optional<Visualization> optionalVisualization = visualizationRepository.findById(visualizationId);
		
		if (!optionalVisualization.isPresent()) {
			throw new ResourceNotFoundException(
					"Visualization " + visualizationId + " not found.");
		} else {
			visualization = optionalVisualization.get();
		}
		
		Manifest manifest = visualization.getManifest();
		
		if (manifest == null) {
			throw new ResourceNotFoundException(
					"No manifest found for visualization " + visualizationId + ".");
		}
		
        response.setHeader("Content-disposition",
                "attachment;filename=" + visualization.getName() + ".zip");

        ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
        
        // add pyramids used for the visualization
        List<String> pyramidIds = getListOfPyramidIdsAndCurateManifest(manifest);
        for (int i=0; i < pyramidIds.size(); i++) {
        	addPyramidToZipOutputStream(zos, pyramidIds.get(i));
        }
        
        // add manifest and README file
        try (PrintWriter printWriter = new PrintWriter(zos)) {
        	// manifest (curated with generic paths for the pyramids URLs)
            zos.putNextEntry(new ZipEntry("/visualization/manifest.json"));
        	ObjectMapper mapper = new ObjectMapper();
        	mapper.disable(MapperFeature.USE_ANNOTATIONS);
			printWriter.write(mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(manifest));
			printWriter.flush();
			// README
	        zos.putNextEntry(new ZipEntry("/visualization/README.txt"));
	        printWriter.write(generateREADME());
        }
        
    }
	
	/**
	 * Retrieves list of pyramidIds used for the visualization from manifest,
	 * and curates manifest to be used outside of WIPP
	 * @param manifest Visualization manifest
	 * @return List of pyramidIds
	 */
	private List<String> getListOfPyramidIdsAndCurateManifest(Manifest manifest) {
		
		List<String> pyramidIds = new ArrayList<String>();
		
		if(manifest != null && manifest.getLayersGroups() != null) {
    		manifest.getLayersGroups().forEach(layersGroup -> {
    			if (layersGroup != null && layersGroup.getLayers() != null) {
    				layersGroup.getLayers().forEach(layer -> {
    					// get the pyramidId for the layer and add it to the list of pyramids to export
    					String pyramidId = layer.getBaseUrl();
    					if (! pyramidIds.contains(pyramidId)) {
    						pyramidIds.add(pyramidId);
    					}
    					// curate pyramid base url and fetching info for export
    					layer.setBaseUrl("data/pyramids/" + pyramidId);
    					layer.setFetching(null); // no fetching available for standalone wdzt instance
    				});
    			}
    		});
    	}
		return pyramidIds;
	}
	
	/**
	 * Add pyramid files (tiles, dzi and ome.xml metadata) to the ZipOutputStream
	 * @param zos
	 * @param pyramidId
	 * @throws IOException
	 */
	private void addPyramidToZipOutputStream(ZipOutputStream zos, String pyramidId) throws IOException {
		
		Pyramid pyramid = null;
		Optional<Pyramid> optionalPyramid = pyramidRepository.findById(
				pyramidId);
		if (!optionalPyramid.isPresent()) {
		    throw new ResourceNotFoundException(
		            "Pyramid " + pyramidId + " not found.");
		} else {
			pyramid = optionalPyramid.get();
		}
		
		List<PyramidTimeSlice> ptsList = pyramidTimeSliceRepository.findAll(pyramid.getId());
		for (PyramidTimeSlice pts : ptsList) {
			String sliceNumber = pts.getName();
			// add DZI file
			File dziFile = new File(
	                new File(config.getPyramidsFolder(), pyramid.getId()),
	                sliceNumber + ".dzi");
			addFileToZos(zos, dziFile, "/visualization/pyramids/" + pyramidId + "/" + sliceNumber + ".dzi");

			// add OME XML file
	        File omeXmlFile = new File(
	                new File(config.getPyramidsFolder(), pyramid.getId()),
	                sliceNumber + ".ome.xml");
	        addFileToZos(zos, omeXmlFile, "/visualization/pyramids/" + pyramidId + "/" + sliceNumber + ".ome.xml");

			// add pyramid tiles
	        File tilesFolder = new File(
	        		new File(config.getPyramidsFolder(), pyramid.getId()), 
	        		sliceNumber + "_files");
	        String pathSuffixInZip = "/visualization/pyramids/" + pyramidId + "/" + tilesFolder.getName();
	        File[] levels = tilesFolder.listFiles(File::isDirectory);
	        for (File level : levels) {
	        	String levelName = level.getName();
	        	File[] tiles = level.listFiles(File::isFile);
	        	for (File tile : tiles) {
	        		addFileToZos(zos, tile, pathSuffixInZip + "/" + levelName + "/" + tile.getName());
	        	}
	        }
		}
		
		
	}
	
	/**
	 * Generates README file
	 * @return
	 */
	private String generateREADME() {
        StringBuilder sb = new StringBuilder();
        sb.append("PYRAMID VISUALIZATION").append('\n');
        sb.append("Check out the instructions at https://hub.docker.com/r/wipp/wdzt to visualize the pyramids ");
        sb.append("using the WDZT Docker.");
        sb.append('\n');
        return sb.toString();
    }
	
	/**
	 * Adds a file to the ZipOutputStream
	 * @param zos
	 * @param file
	 * @param entryName
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void addFileToZos(ZipOutputStream zos, File file, String entryName) throws FileNotFoundException, IOException {
		zos.putNextEntry(new ZipEntry(entryName));
	       try (InputStream is = new FileInputStream(file)) {
	            IOUtils.copyLarge(is, zos);
	        }
	}
}

