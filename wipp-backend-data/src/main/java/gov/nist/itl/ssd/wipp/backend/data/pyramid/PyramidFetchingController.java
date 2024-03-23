package gov.nist.itl.ssd.wipp.backend.data.pyramid;


import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import loci.formats.ome.OMEXMLMetadata;
import ome.units.quantity.Length;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.IntegerRange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.NumberRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.nist.isg.pyramidio.DeepZoomImageReader;
import gov.nist.isg.pyramidio.stitching.MistStitchedImageReader;
import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataDownloadToken;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataDownloadTokenRepository;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobRepository;
import gov.nist.itl.ssd.wipp.backend.core.rest.DownloadUrl;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ForbiddenException;
import gov.nist.itl.ssd.wipp.backend.core.utils.FilenameConverter;
import gov.nist.itl.ssd.wipp.backend.core.utils.IdentityFilenameConverter;
import gov.nist.itl.ssd.wipp.backend.core.utils.PatternFilenameConverter;
import gov.nist.itl.ssd.wipp.backend.core.utils.SecurityUtils;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollectionRepository;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageHandler;
import gov.nist.itl.ssd.wipp.backend.data.pyramid.timeslices.PyramidTimeSliceRepository;
import gov.nist.itl.ssd.wipp.backend.data.stitching.StitchingVector;
import gov.nist.itl.ssd.wipp.backend.data.stitching.StitchingVectorRepository;
import gov.nist.itl.ssd.wipp.backend.data.stitching.timeslices.StitchingVectorTimeSlice;
import gov.nist.itl.ssd.wipp.backend.data.stitching.timeslices.StitchingVectorTimeSliceRepository;


/**
 * Inspired by ProbingSamplingResource in RestletDeepZoom project
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@RestController
@Tag(name="Pyramid Entity")
@RequestMapping(CoreConfig.BASE_URI + "/pyramids/{pyramidId}/fetching")
public class PyramidFetchingController {

	 @Autowired
	    private CoreConfig appConfig;

	    @Autowired
	    private PyramidRepository pyramidRepository;

	    @Autowired
	    private PyramidTimeSliceRepository pyramidTimeSliceRepository;
	    
	    @Autowired
	    private JobRepository jobRepository;
	    
	    @Autowired
	    private StitchingVectorRepository stitchingVectorRepository;

	    @Autowired
	    private StitchingVectorTimeSliceRepository stitchingVectorTimeSliceRepository;

	    @Autowired
	    private ImagesCollectionRepository tilesCollectionRepository;

	    @Autowired
	    private ImageHandler tileRepository;
	    
	    @Autowired
	    private DataDownloadTokenRepository dataDownloadTokenRepository;

	    @RequestMapping(
	            value = "request",
	            method = RequestMethod.GET,
	            produces = "application/json")
		@PreAuthorize("hasRole('admin') or @pyramidSecurity.checkAuthorize(#pyramidId, false)")
	    public DownloadUrl request(@PathVariable("pyramidId") String pyramidId,
	            @RequestParam("x") int x,
	            @RequestParam("y") int y,
	            @RequestParam("width") int width,
	            @RequestParam("height") int height,
	            @RequestParam("zoom") double zoom,
	            @RequestParam(
	                    value = "frames",
	                    defaultValue = "") String frames,
	            @RequestParam(
	                    value = "framesOffset",
	                    defaultValue = "0") int framesOffset,
	            HttpServletResponse response, 
	            HttpServletRequest request) throws IOException {

	        Optional<Pyramid> optionalPyramid = pyramidRepository.findById(pyramidId);

	        if (!optionalPyramid.isPresent()) {
	            throw new ResourceNotFoundException(
	                    "Can not find pyramid " + pyramidId);
	        }
	        
	        // Generate download token
			DataDownloadToken downloadToken = new DataDownloadToken(pyramidId);
			dataDownloadTokenRepository.save(downloadToken);

			// Generate and send unique download URL
			String queryString = request.getQueryString();
			String params = "?" + (queryString == null ? "" : queryString + "&") + "token=" + downloadToken.getToken();
			String downloadLink = linkTo(PyramidFetchingController.class, pyramidId) + params;
			return new DownloadUrl(downloadLink);
	    }
	    
	    @RequestMapping(
	            value = "",
	            method = RequestMethod.GET,
	            produces = "application/zip")
	    public void fetch(@PathVariable("pyramidId") String pyramidId,
	            @RequestParam("x") int x,
	            @RequestParam("y") int y,
	            @RequestParam("width") int width,
	            @RequestParam("height") int height,
	            @RequestParam("zoom") double zoom,
	            @RequestParam(
	                    value = "frames",
	                    defaultValue = "") String frames,
	            @RequestParam(
	                    value = "framesOffset",
	                    defaultValue = "0") int framesOffset,
	            @RequestParam("token") String token,
	            HttpServletResponse response) throws IOException {

	    	// Load security context for system operations
	    	SecurityUtils.runAsSystem();
	    	
	    	// Check validity of download token
	    	Optional<DataDownloadToken> downloadToken = dataDownloadTokenRepository.findByToken(token);
	    	if (!downloadToken.isPresent() || !downloadToken.get().getDataId().equals(pyramidId)) {
	    		System.out.println("Invalid download token.");
	    		throw new ForbiddenException("Invalid download token.");
	    	}
	    	System.out.println("Continue download");
	    	// Check existence of pyramid
	        Pyramid pyramid = null;
	        Optional<Pyramid> optionalPyramid = pyramidRepository.findById(pyramidId);

	        if (optionalPyramid.isPresent()) {
	            pyramid = optionalPyramid.get();
	        } else {
	            throw new ResourceNotFoundException(
	                    "Can not find pyramid " + pyramidId);
	        }

	        if (width * height * zoom * zoom > appConfig.getFetchingPixelsMax()) {
	            throw new ClientException(
	                    "Requested number of pixels per image too big.");
	        }

	        response.setHeader("Content-disposition",
	                "attachment;filename=" + pyramid.getId() + ".zip");

	        Rectangle region = new Rectangle(x, y, width, height);
	        SortedSet<Integer> framesNumbers = integersRangeParser(
	                frames, framesOffset);
	        String provenance = getDataProvenance(
	                pyramid, x, y, width, height, zoom, frames, framesOffset);

	        // Cookie necessary for use with
	        // https://github.com/johnculviner/jquery.fileDownload
	        // Added as late as possible because we ideally want to write it only
	        // if successful. However we must add it before opening the response
	        // stream!
	        Cookie cookie = new Cookie("fileDownload", "true");
	        cookie.setPath("/");
	        response.addCookie(cookie);

	        ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
	        if (pyramid.getJob() != null && zoom == 1) {
	            fillStreamFromTilesCollection(pyramid, region, framesNumbers, zos);
	        } else {
	            fillStreamFromPyramid(pyramidId, region, zoom, framesNumbers, zos);
	        }
	        zos.putNextEntry(new ZipEntry("dataProvenance.txt"));
	        try (PrintWriter printWriter = new PrintWriter(zos)) {
	            printWriter.print(provenance);
	        }
	        
	        // Clear security context after system operations
	        SecurityContextHolder.clearContext();
	    }

	    private void fillStreamFromTilesCollection(Pyramid pyramid, Rectangle region,
	            SortedSet<Integer> framesNumbers, ZipOutputStream zos)
	            throws IOException {
	        String pyramidJobId = pyramid.getJob();
//	        PyramidJob pyramidJob = null;
//	        Optional<PyramidJob> optionalPyramidJob = pyramidJobRepository.findById(pyramidJobId);
	        
	        Job pyramidJob = null;
	        Optional<Job> optionalPyramidJob = jobRepository.findById(pyramidJobId);

	        if(optionalPyramidJob.isPresent()) {
	            pyramidJob = optionalPyramidJob.get();
	        } else {
	            throw new ClientException("Error while retrieving pyramid configuration, pyramid building job not found.");
	        }

	        String tilesCollectionId = pyramidJob.getParameter("input");
	        ImagesCollection tilesCollection = null;
	        Optional<ImagesCollection> optionalImagesCollection = tilesCollectionRepository.findById(
	                tilesCollectionId);

	        if(optionalImagesCollection.isPresent()) {
	            tilesCollection = optionalImagesCollection.get();
	        } else {
	            throw new ClientException("Error while retrieving pyramid configuration, images collection not found.");
	        }

	        // TODO: Add Intensity Scaling
//	        String sourceJob = tilesCollection.getSourceJob();
//	        if (sourceJob != null) {
//	            IntensityScalingJob iScalingJob = null;
//	            Optional<IntensityScalingJob> optionalIntensityScalingJob = intensityScalingJobRepository
//	                    .findById(sourceJob);
//	            if (optionalIntensityScalingJob.isPresent()) {
//	                iScalingJob = optionalIntensityScalingJob.get();
//	                tilesCollectionId = iScalingJob.getInputTilesCollection();
//	            }
//	        }
	        
	        File tilesFolder = tileRepository.getFilesFolder(tilesCollectionId);
	        //TODO: retrieve the SV id
	        //String stitchingVectorId = pyramidJob.getStitchingVector();
	        String stitchingVectorId = "";
	        StitchingVector stitchingVector = null;
	        Optional<StitchingVector>  optionalStitchingVector = stitchingVectorRepository.findById(
	                stitchingVectorId);
	        if(optionalStitchingVector.isPresent()) {
	            stitchingVector = optionalStitchingVector.get();
	        } else {
	            throw new ClientException("Error while retrieving pyramid configuration, stitching vector not found.");
	        }

	        //TODO: handle the patterns
//	        String inputTilesPattern = pyramidJob.getInputTilesPattern();
//	        String stitchedTilesPattern = pyramidJob.getStitchedTilesPattern();
	        String inputTilesPattern = "";
	        String stitchedTilesPattern = "";
	        
	        FilenameConverter converter = StringUtils.isEmpty(inputTilesPattern)
	                || StringUtils.isEmpty(stitchedTilesPattern)
	                ? new IdentityFilenameConverter()
	                : new PatternFilenameConverter(
	                        stitchedTilesPattern, inputTilesPattern);

	        for (StitchingVectorTimeSlice timeSlice : stitchingVector.getTimeSlices()) {
	            int sliceNumber = timeSlice.getSliceNumber();
	            if (framesNumbers != null && !framesNumbers.contains(sliceNumber)) {
	                continue;
	            }
	            File globalPositionsFile = stitchingVectorTimeSliceRepository
	                    .getGlobalPositionsFile(stitchingVectorId, sliceNumber);
	            MistStitchedImageReader reader = new MistStitchedImageReader(
	                    globalPositionsFile, tilesFolder, converter::convert);
	            BufferedImage extract = reader.read(region);

	            String name = sliceNumber + ".tif";
	            ZipEntry entry = new ZipEntry(name);
	            zos.putNextEntry(entry);
	            ImageIO.write(extract, "tif", zos);
	        }
	    }

	    private void fillStreamFromPyramid(String pyramidId, Rectangle region,
	            double zoom, SortedSet<Integer> framesNumbers, ZipOutputStream zos)
	            throws IOException {
	        List<File> dziFiles = getDziFiles(pyramidId, framesNumbers);
	        for (File dziFile : dziFiles) {
	            DeepZoomImageReader dzir = new DeepZoomImageReader(dziFile);
	            BufferedImage extract = dzir.getRegion(region, zoom);

	            String nameNoExt = FilenameUtils.removeExtension(dziFile.getName());
	            ZipEntry entry = new ZipEntry(nameNoExt + "." + dzir.getFormat());
	            zos.putNextEntry(entry);
	            ImageIO.write(extract, dzir.getFormat(), zos);
	        }
	    }

	    private String getDataProvenance(Pyramid pyramid, int x, int y, int width,
	            int height, double zoom, String frames, int framesOffset) {
	        StringBuilder sb = new StringBuilder();
	        sb.append("pyramid: ").append(pyramid.getName()).append(' ')
	                .append(pyramid.getId()).append('\n');
	        sb.append("x: ").append(x).append('\n');
	        sb.append("y: ").append(y).append('\n');
	        sb.append("width: ").append(width).append('\n');
	        sb.append("height: ").append(height).append('\n');
	        sb.append("zoom: ").append(zoom).append('\n');
	        sb.append("frames: ").append(frames).append('\n');
	        sb.append("framesOffset: ").append(framesOffset).append('\n');
	        sb.append("scale: ").append(getScale(pyramid, zoom)).append('\n');
	        return sb.toString();
	    }

	    private String getScale(Pyramid pyramid, double zoom) {
	        OMEXMLMetadata metadata;
	        try {
	            metadata = pyramidTimeSliceRepository.getOmeXmlMetadata(
	                    pyramid.getId());
	        } catch (IOException ex) {
	            Logger.getLogger(PyramidFetchingController.class.getName()).log(
	                    Level.SEVERE,
	                    "Can not get OMEXMLMetadata for pyramid " + pyramid.getId(),
	                    ex);
	            return "";
	        }
	        Length length = metadata.getPixelsPhysicalSizeX(0);
	        double scale = 1.0 / length.value().doubleValue();
	        String symbol = length.unit().getSymbol();
	        return scale * zoom + " " + "pixels/" + symbol;
	    }

	    private List<File> getDziFiles(String pyramidId, SortedSet<Integer> framesNumbers) {
	        if (framesNumbers == null) {
	            return pyramidTimeSliceRepository.getAllDziFiles(pyramidId);
	        }
	        return pyramidTimeSliceRepository.getDziFiles(pyramidId, framesNumbers);
	    }

	    /**
	     * Parse a range of integers. For example, 1-3,6,8-12 will return the
	     * collection 1,2,3,6,8,9,10,11,12
	     *
	     * @param range the range to parse
	     * @return the sorted integers forming the range
	     * @throws IllegalArgumentException if the range format is incorrect
	     */
	    private static SortedSet<Integer> integersRangeParser(
	            String range, int framesOffset) {
	        if (range == null || range.isEmpty()) {
	            return null;
	        }

	        SortedSet<Integer> result = new TreeSet<>();

	        try {
	            for (String interval : range.split(",")) {
	                String[] intervalSplit = interval.split("-");
	                switch (intervalSplit.length) {
	                    case 1:
	                        if (!intervalSplit[0].isEmpty()) {
	                            result.add(new Integer(intervalSplit[0])
	                                    + framesOffset);
	                        }
	                        break;
	                    case 2:
	                        int first = Integer.parseInt(intervalSplit[0]);
	                        int last = Integer.parseInt(intervalSplit[1]);
	                        IntegerRange nr = IntegerRange.of(first, last);
	                        for (Integer i = nr.getMinimum();
	                                i <= nr.getMaximum();
	                                i++) {
	                            result.add(i + framesOffset);
	                        }
	                        break;
	                    default:
	                        throw new IllegalArgumentException("Invalid range "
	                                + range);
	                }
	            }
	            return result;
	        } catch (NumberFormatException e) {
	            throw new IllegalArgumentException("Invalide range " + range, e);
	        }
	    }

	
}
