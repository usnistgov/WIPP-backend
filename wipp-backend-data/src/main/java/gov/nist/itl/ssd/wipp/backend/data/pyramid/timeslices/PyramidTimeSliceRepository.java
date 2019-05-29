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
package gov.nist.itl.ssd.wipp.backend.data.pyramid.timeslices;

import com.google.common.io.Files;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.services.OMEXMLService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Antoine Vandecreme
 */
@Component
public class PyramidTimeSliceRepository {

	@Autowired
	private CoreConfig config;

	public PyramidTimeSlice findOne(String pyramidId, String timeSliceId) {
		File dziFile = getDziFile(pyramidId, timeSliceId);
		if (!dziFile.exists()) {
			return null;
		}
		return new PyramidTimeSlice(timeSliceId);
	}

	public List<File> getAllDziFiles(String pyramidId) {
		File[] dziFiles = new File(config.getPyramidsFolder(), pyramidId)
				.listFiles((File dir, String name) -> name.endsWith(".dzi"));
		if (dziFiles == null) {
			throw new RuntimeException("Can not read pyramid folder "
					+ config.getPyramidsFolder());
		}
		return Arrays.asList(dziFiles);
	}

	public List<File> getDziFiles(String pyramidId,
			SortedSet<Integer> timeSlicesNumbers) {
		return findAll(pyramidId).stream()
				.filter(pts -> timeSlicesNumbers.contains(
						new Integer(pts.getName())))
				.map(pts -> getDziFile(pyramidId, pts.getName()))
				.collect(Collectors.toList());
	}

	public List<PyramidTimeSlice> findAll(String pyramidId) {
		return getAllDziFiles(pyramidId).stream().map(f -> {
			String fileName = f.getName();
			// Remove .dzi at the end of the filename.
			String sliceName = fileName.substring(0, fileName.length() - 4);
			return new PyramidTimeSlice(sliceName);
		}).collect(Collectors.toList());
	}

	public List<File> getAllOmeFiles(String pyramidId) {
		File[] omeFiles = new File(config.getPyramidsFolder(), pyramidId)
				.listFiles((File dir, String name) -> name.endsWith(".ome.xml"));
		if (omeFiles == null) {
			throw new RuntimeException("Can not read pyramid folder "
					+ config.getPyramidsFolder());
		}
		return Arrays.asList(omeFiles);
	}

	public OMEXMLMetadata getOmeXmlMetadata(String pyramidId)
			throws IOException {
		List<PyramidTimeSlice> pts = findAll(pyramidId);
		if (pts.isEmpty()) {
			throw new RuntimeException(
					"No time slice found for pyramid " + pyramidId);
		}
		return getOmeXmlMetadata(pyramidId, pts.get(0).getName());
	}

	public OMEXMLMetadata getOmeXmlMetadata(String pyramidId,
			String timeSliceId) throws IOException {
		try {
			ServiceFactory factory = new ServiceFactory();
			OMEXMLService service = factory.getInstance(OMEXMLService.class);
			return service.createOMEXMLMetadata(
					Files.asCharSource(
							getOmeFile(pyramidId, timeSliceId),
							Charset.forName("UTF-8")).read());
		} catch (DependencyException | ServiceException ex) {
			throw new IOException(ex);
		}
	}

	private File getDziFile(String pyramidId, String timeSliceId) {
		return new File(
				new File(config.getPyramidsFolder(), pyramidId),
				timeSliceId + ".dzi");
	}

	private File getOmeFile(String pyramidId, String timeSliceId) {
		return new File(
				new File(config.getPyramidsFolder(), pyramidId),
				timeSliceId + ".ome.xml");
	}


}
