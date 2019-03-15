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
package gov.nist.itl.ssd.wipp.backend.data.utils.flowjs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 */
public abstract class FlowjsController {

    @Autowired
    private FlowFileStorage flowFileStorage;

    private static final Logger logger = Logger.getLogger(FlowjsController.class.getName());

    protected interface Parameters {
    }

    protected FlowFile getFlowFile(HttpServletRequest request,
            Parameters parameters) {
        int flowChunkSize = Integer.parseInt(
                request.getParameter("flowChunkSize"));
        long flowTotalSize = Long.parseLong(
                request.getParameter("flowTotalSize"));
        String flowIdentifier = request.getParameter("flowIdentifier");
        String flowFilename = request.getParameter("flowFilename");
        String flowRelativePath = request.getParameter("flowRelativePath");
        return new FlowFile(flowChunkSize, flowTotalSize, flowIdentifier,
                flowFilename, flowRelativePath);
    }

    protected int getFlowChunckNumber(HttpServletRequest request) {
        return Integer.parseInt(request.getParameter("flowChunkNumber"));
    }

    protected abstract File getTempUploadDir(FlowFile flowFile);

    protected abstract File getUploadDir(FlowFile flowFile);

    protected void isChunckUploaded(HttpServletRequest request,
            HttpServletResponse response, Parameters parameters)
            throws IOException {
        try {
            FlowFile flowFile = getFlowFile(request, parameters);
            int flowChunkNumber = getFlowChunckNumber(request);
            if (flowFileStorage.isChunckUploaded(flowFile, flowChunkNumber)) {
                //This chunk has already been uploaded.
                response.getWriter().print("Uploaded.");
            } else {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (FlowjsException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(ex.getMessage());
        }
    }

    protected abstract void onUploadFinished(FlowFile flowFile, Path tempPath)
            throws IOException;

    protected void uploadChunck(HttpServletRequest request,
            HttpServletResponse response, Parameters parameters)
            throws IOException {
        try {
            FlowFile flowFile = getFlowFile(request, parameters);
            int flowChunkNumber = getFlowChunckNumber(request);
            File tempDir = getTempUploadDir(flowFile);
            tempDir.mkdirs();
            File file = new File(tempDir, flowFile.getFlowFilename());
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                //Seek to position
                raf.seek((flowChunkNumber - 1) * flowFile.getFlowChunkSize());

                //Save to file
                InputStream is = request.getInputStream();
                long readed = 0;
                long content_length = request.getContentLength();
                byte[] bytes = new byte[1024 * 100];
                while (readed < content_length) {
                    int r = is.read(bytes);
                    if (r < 0) {
                        break;
                    }
                    raf.write(bytes, 0, r);
                    readed += r;
                }
            }

            flowFileStorage.setChunckUploaded(flowFile, flowChunkNumber);

            if (flowFileStorage.isUploadFinished(flowFile)) {
                onUploadFinished(flowFile, file.toPath());
                flowFileStorage.removeFlowFile(flowFile);
                response.getWriter().print("All finished.");
            } else {
                response.getWriter().print("Upload");
            }
        } catch (FlowjsException ex) {
            logger.log(Level.WARNING, "", ex);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(ex.getMessage());
        }
    }
}
