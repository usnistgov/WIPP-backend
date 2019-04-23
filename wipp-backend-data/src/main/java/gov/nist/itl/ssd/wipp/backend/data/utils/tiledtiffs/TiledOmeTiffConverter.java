package gov.nist.itl.ssd.wipp.backend.data.utils.tiledtiffs;

/*
 * #%L
 * OME Bio-Formats package for reading and converting biological file formats.
 * %%
 * Copyright (C) 2005 - 2017 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
      System.err.println("Failed to close reader.");
      e.printStackTrace();
    }
    try {
      writer.close();
    }
    catch (IOException e) {
      System.err.println("Failed to close writer.");
      e.printStackTrace();
    }
  }

}
