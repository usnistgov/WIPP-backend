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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Controller;

/**
 * Thread safe
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 */
@Controller
public class FlowFileStorage {

    private final ConcurrentMap<FlowFile, Set<Integer>> flowFilesUploadedChunks
            = new ConcurrentHashMap<>();

    public boolean isChunckUploaded(FlowFile flowFile, int chunckNumber) {
        Set<Integer> uploadedChuncks = flowFilesUploadedChunks.get(flowFile);
        return uploadedChuncks != null
                && uploadedChuncks.contains(chunckNumber);
    }

    public void setChunckUploaded(FlowFile flowFile, int chunckNumber) {
        if (chunckNumber <= 0 || chunckNumber > flowFile.getNbChunks()) {
            throw new FlowjsException("Invalid chunck number " + chunckNumber);
        }
        Set<Integer> uploadedChuncks = Collections.newSetFromMap(
                new ConcurrentHashMap<Integer, Boolean>(
                        flowFile.getNbChunks()));
        Set<Integer> uploadedChuncksTmp = flowFilesUploadedChunks.putIfAbsent(
                flowFile, uploadedChuncks);

        if (uploadedChuncksTmp != null) {
            uploadedChuncks = uploadedChuncksTmp;
        }
        uploadedChuncks.add(chunckNumber);
    }

    public boolean isUploadFinished(FlowFile flowFile) {
        Set<Integer> uploadedChuncks = flowFilesUploadedChunks.get(flowFile);
        return uploadedChuncks != null
                && uploadedChuncks.size() == flowFile.getNbChunks();
    }

    public void removeFlowFile(FlowFile flowFile) {
        flowFilesUploadedChunks.remove(flowFile);
    }
}
