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
package gov.nist.itl.ssd.wipp.backend.data.utils.tiledtiffs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageUploadController;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.formats.FormatTools;
import loci.formats.meta.IMetadata;
import loci.formats.out.OMETiffWriter;
import loci.formats.services.OMEXMLService;
import loci.formats.codec.CompressionType;

/**
 * Inspired from https://docs.openmicroscopy.org/bio-formats/5.9.1/developers/tiling.html
 * This class reads a full image and use an OME-Tiff writer to automatically write out the image in a tiled format.
 *
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
public class TiledOmeTiffConverter {

	private static final Logger LOG = Logger.getLogger(TiledOmeTiffConverter.class.getName());

	private ImageReader reader;
	private OMETiffWriter writer;
	private String inputFile;
	private String outputFile;
	private int tileSizeX;
	private int tileSizeY;

	public TiledOmeTiffConverter(String inputFile, String outputFile, int tileSizeX, int tileSizeY) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.tileSizeX = tileSizeX;
		this.tileSizeY = tileSizeY;
	}

	public void init() throws DependencyException, FormatException, IOException, ServiceException {
		// construct the object that stores OME-XML metadata
		ServiceFactory factory = new ServiceFactory();
		OMEXMLService service = factory.getInstance(OMEXMLService.class);
		IMetadata omexml = service.createOMEXMLMetadata();

		// set up the reader and associate it with the input file
		reader = new ImageReader();
		reader.setOriginalMetadataPopulated(true);
		reader.setMetadataStore(omexml);
		reader.setId(inputFile);

		// important to delete because OME uses RandomAccessFile
		Path outputPath = Paths.get(outputFile);
		outputPath.toFile().delete();

		// set up the writer and associate it with the output file
		writer = new OMETiffWriter();
		writer.setMetadataRetrieve(omexml);
		writer.setInterleaved(reader.isInterleaved());

		// set the tile size height and width for writing
		this.tileSizeX = writer.setTileSizeX(tileSizeX);
		this.tileSizeY = writer.setTileSizeY(tileSizeY);

		writer.setId(outputFile);

		// WIPP stores compressed images
		writer.setCompression(CompressionType.LZW.getCompression());
	}

	// Read the input file as a plain image and write it into a tiled format
	public void readWriteTiles() throws FormatException, DependencyException, ServiceException, IOException {
		byte[] buf = new byte[FormatTools.getPlaneSize(reader)];

		// WIPP handles 2D images only, the image series are set to 0 in our case 
		buf = reader.openBytes(0);
		writer.saveBytes(0, buf);
	}

	// Close the file reader and writer.
	public void cleanup() {
		try {
			reader.close();
		}
		catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to close reader.",e);
		}
		try {
			writer.close();
		}
		catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to close writer.",e);
		}
	}

}
