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
package gov.nist.itl.ssd.wipp.backend.data.utils.zarr;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import org.apache.commons.io.FilenameUtils;

import com.bc.zarr.ArrayParams;
import com.bc.zarr.Compressor;
import com.bc.zarr.CompressorFactory;
import com.bc.zarr.DataType;
import com.bc.zarr.ZarrArray;
import com.bc.zarr.ZarrGroup;
import loci.common.Constants;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.formats.FormatTools;
import loci.formats.meta.IMetadata;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.services.OMEXMLService;
import ucar.ma2.InvalidRangeException;

/**
 * This class reads an image and converts it to OME ZARR format.
 *
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
public class OmeZarrConverter {

	private static final Logger LOG = Logger.getLogger(OmeZarrConverter.class.getName());
	
	private static final String OME_NGFF_VERSION = "0.3";
	private static final String METADATA_FILE = "METADATA.ome.xml";
	
	private static final int CHUNK_SIZE_3D = 64;

	private ImageReader reader;
	private ZarrArray zarrArray;
	private String inputFile;
	private String outputFile;
	private int tileSizeX;
	private int tileSizeY;

	public OmeZarrConverter(String inputFile, String outputFile, int tileSizeX, int tileSizeY) {
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

		// set up the reader 
		reader = new ImageReader();
		// force reader to not group in multi-file formats to treat each file individually
	    reader.setGroupFiles(false);
		// set metadata 
		reader.setOriginalMetadataPopulated(true);
		reader.setMetadataStore(omexml);
		// set input file
		reader.setId(inputFile);

		// important to delete because OME uses RandomAccessFile
		Path outputPath = Paths.get(outputFile);
		outputPath.toFile().delete();

		// set chunk size
		int tileSizeZ = 1;
		if (reader.getSizeZ() > 1) {
			tileSizeX = tileSizeY = tileSizeZ = CHUNK_SIZE_3D;
		}
		int[] chunks = new int[] {1, 1, tileSizeZ, tileSizeY, tileSizeX};
		
		// create output zarr array
		this.create(outputFile, omexml, chunks);
		
		// write original metadata
		this.saveOmeMetadata(outputFile, omexml, service);
		
		// write data
		this.readWriteTiles();

	}
	
	// Read the input file tile by tile and save as zarr chunks
	public void readWriteTiles() throws FormatException, DependencyException, ServiceException, IOException {
		int bpp = FormatTools.getBytesPerPixel(reader.getPixelType());
		int tilePlaneSize = tileSizeX * tileSizeY * reader.getRGBChannelCount() * bpp;
		byte[] buf = new byte[tilePlaneSize];

		// set the current series to 0
		reader.setSeries(0);
	    
	    // convert each image plane in the current series 
		for (int image=0; image<reader.getImageCount(); image++) {
			int width = reader.getSizeX();
			int height = reader.getSizeY();
			int[] zctCoord = reader.getZCTCoords(image);
	
			// Determined the number of tiles to read and write
			int nXTiles = width / tileSizeX;
			int nYTiles = height / tileSizeY;
			if (nXTiles * tileSizeX != width) nXTiles++;
			if (nYTiles * tileSizeY != height) nYTiles++;
	
			for (int y=0; y<nYTiles; y++) {
				for (int x=0; x<nXTiles; x++) {
					
					int tileX = x * tileSizeX;
					int tileY = y * tileSizeY;
					
					int effTileSizeX = (tileX + tileSizeX) < width ? tileSizeX : width - tileX;
					int effTileSizeY = (tileY + tileSizeY) < height ? tileSizeY : height - tileY;
	
					buf = reader.openBytes(image, tileX, tileY, effTileSizeX, effTileSizeY);
					this.saveBytes(buf, new int[] { 1, 1, 1, effTileSizeY, effTileSizeX },
							new int[] { zctCoord[2], zctCoord[1], zctCoord[0], tileY, tileX });
				}
			}
		}
	}
	
	public void saveBytes(Object data, int[] shape, int[] offset) throws FormatException, IOException {
	    if (zarrArray != null) {
	      try {	    	
	        ByteBuffer buf = ByteBuffer.wrap((byte[])data).order(this.zarrArray.getByteOrder());
	        Object tileData = this.getDataArrayFromByteBuffer(buf, this.zarrArray.getDataType(), shape);
	        zarrArray.write(tileData, shape, offset);
	      } catch (InvalidRangeException e) {
	        throw new FormatException(e);
	      }
	    }
	    else throw new IOException("No Zarr file opened");
	  }
	
	// Create Zarr array
	public void create(String file, MetadataRetrieve meta, int[] chunks) throws IOException {
	    int seriesCount = meta.getImageCount();
	    
	    if (seriesCount > 1) {
	      LOG.warning("Series are ignored during image conversion.");
	    }
	    
	    ArrayParams params = new ArrayParams();
	    params.chunks(chunks);
	    Compressor bloscCompressor = CompressorFactory.create("blosc");
	    params.compressor(bloscCompressor);
	    
	    boolean isLittleEndian = !meta.getPixelsBigEndian(0);
	    if (isLittleEndian) {
	      params.byteOrder(ByteOrder.LITTLE_ENDIAN);
	    }

	    // Dimensions order: TCZYX
	    String[] zarrDimensions = {"t", "c", "z", "y", "x"};
		int[] dimensions = new int[] { reader.getSizeT(), reader.getSizeC(), reader.getSizeZ(), reader.getSizeY(),
				reader.getSizeX() };
	    int [] shape = dimensions; 
	    params.shape(shape);
	    
	    // Get Zarr pixel type
	    int pixelType = FormatTools.pixelTypeFromString(meta.getPixelsType(0).toString());
	    DataType zarrPixelType = getZarrPixelType(pixelType);
	    params.dataType(zarrPixelType);
	    
	    // Create group attributes
	    Map<String, Object> grpAttrs = new HashMap<>();	    
	    List<Map<String, Object>> multiscales = new ArrayList<Map<String, Object>>();
	    Map<String, Object> multiscale = new HashMap<String, Object>();
	    multiscale.put("version", OME_NGFF_VERSION);
	    multiscale.put("name", FilenameUtils.getName(outputFile));
	    List<Map<String, String>> datasets = new ArrayList<Map<String, String>>();
	    datasets.add(Collections.singletonMap("path", "0"));
	    multiscale.put("datasets", datasets);
	    multiscale.put("axes", zarrDimensions);
	    multiscales.add(multiscale);
	    grpAttrs.put("multiscales", multiscales);
	    
	    // Create array attributes
	    Map<String, Object> arrAttrs = new HashMap<>();
	    arrAttrs.put("_ARRAY_DIMENSIONS", zarrDimensions); // for xarray compatibility

	    // Create Zarr group and array
	    ZarrGroup root = ZarrGroup.create(outputFile, grpAttrs);
	    zarrArray = root.createArray("0", params, arrAttrs);
	    
	  }
	
	// Write original OME metadata
	public void saveOmeMetadata(String file, MetadataRetrieve meta, OMEXMLService service)
			throws IOException, ServiceException {
		String xml = service.getOMEXML(meta);
		// write the original OME-XML to a file inside OME sub-folder to be consistent with bioformats2raw
		Path metadataPath = Paths.get(file, "OME");
		if (!Files.exists(metadataPath)) {
			Files.createDirectories(metadataPath);
		}
		Path omeXmlFile = metadataPath.resolve(METADATA_FILE);
		Files.write(omeXmlFile, xml.getBytes(Constants.ENCODING)); 
	  }
	
	// Convert pixel type to Zarr pixel type
	private DataType getZarrPixelType(int pixType) {
	    DataType pixelType = null;
	      switch(pixType) {
	        case FormatTools.INT8:
	          pixelType = DataType.i1;
	          break;
	        case FormatTools.INT16:
	          pixelType = DataType.i2;
	          break;
	        case FormatTools.INT32:
	          pixelType = DataType.i4;
	          break;
	        case FormatTools.UINT8:
	          pixelType = DataType.u1;
	          break;
	        case FormatTools.UINT16:
	          pixelType = DataType.u2;
	          break;
	        case FormatTools.UINT32:
	          pixelType = DataType.u4;
	          break;
	        case FormatTools.FLOAT:
	          pixelType = DataType.f4;
	          break;
	        case FormatTools.DOUBLE:
	          pixelType = DataType.f8;
	          break;
	      }
	      return(pixelType);
	  }
	
	// Convert bytes to destination format and return as array
	private Object getDataArrayFromByteBuffer(ByteBuffer byteBuffer, DataType dataType, int[] shape) {
        final int size = IntStream.of(shape).reduce((a, b) -> a * b).orElse(0);
        switch (dataType) {
            case i1:
            case u1:
            	byte[] byteData = new byte[size];
            	byteBuffer.get(byteData);
                return byteData;
            case i2:
            case u2:
            	short[] shortData = new short[size];
            	byteBuffer.asShortBuffer().get(shortData);
                return shortData;
            case i4:
            case u4:
            	int[] intData = new int[size];
            	byteBuffer.asIntBuffer().get(intData);
                return intData;
            case i8:
                long[] longData = new long[size];
            	byteBuffer.asLongBuffer().get(longData);
                return longData;
            case f4:
            	float[] floatData = new float[size];
            	byteBuffer.asFloatBuffer().get(floatData);
                return floatData;
            case f8:
            	double[] doubleData = new double[size];
            	byteBuffer.asDoubleBuffer().get(doubleData);
                return doubleData;
        }
        return null;
    }

	// Close the file reader and writer.
	public void cleanup() {
		try {
			reader.close();
		}
		catch (IOException e) {
			LOG.log(Level.WARNING, "Failed to close reader.",e);
		}
	}

}
