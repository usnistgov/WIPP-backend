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

import java.util.Objects;

/**
 *
 * Thread safe
 *
 * @author Antoine Vandecreme <antoine.vandecreme at nist.gov>
 */
public class FlowFile {

    private final int flowChunkSize;
    private final long flowTotalSize;
    private final String flowIdentifier;
    private final String flowFilename;
    private final String flowRelativePath;
    private final int nbChunks;

    public FlowFile(int flowChunkSize, long flowTotalSize,
            String flowIdentifier, String flowFilename,
            String flowRelativePath) {
        if (flowChunkSize <= 0) {
            throw new FlowjsException("Flow chunck size must be > 0.");
        }
        if (flowTotalSize <= 0) {
            throw new FlowjsException("Flow total size must be > 0.");
        }
        if (flowIdentifier == null || flowIdentifier.isEmpty()) {
            throw new FlowjsException("Flow identifier must not be empty.");
        }
        if (flowFilename == null || flowFilename.isEmpty()) {
            throw new FlowjsException("Flow filename must not be empty.");
        }
        if (flowRelativePath == null || flowRelativePath.isEmpty()) {
            throw new FlowjsException("Flow relative path must not be empty.");
        }

        this.flowChunkSize = flowChunkSize;
        this.flowTotalSize = flowTotalSize;
        this.flowIdentifier = flowIdentifier;
        this.flowFilename = flowFilename;
        this.flowRelativePath = flowRelativePath;

        // Math.floor because the last chunck is merged with the previous one
        // by flow.js
        int nbChunks = (int) Math.floor(flowTotalSize / (double) flowChunkSize);
        if (nbChunks == 0) {
            // We have at least one chunck though...
            nbChunks = 1;
        }
        this.nbChunks = nbChunks;
    }

    public FlowFile(FlowFile flowFile) {
        this(flowFile.flowChunkSize, flowFile.flowTotalSize,
                flowFile.flowIdentifier, flowFile.flowFilename,
                flowFile.flowRelativePath);
    }

    public int getNbChunks() {
        return nbChunks;
    }

    public int getFlowChunkSize() {
        return flowChunkSize;
    }

    public long getFlowTotalSize() {
        return flowTotalSize;
    }

    public String getFlowIdentifier() {
        return flowIdentifier;
    }

    public String getFlowFilename() {
        return flowFilename;
    }

    public String getFlowRelativePath() {
        return flowRelativePath;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.flowChunkSize;
        hash = 17 * hash + (int) (this.flowTotalSize
                ^ (this.flowTotalSize >>> 32));
        hash = 17 * hash + Objects.hashCode(this.flowIdentifier);
        hash = 17 * hash + Objects.hashCode(this.flowFilename);
        hash = 17 * hash + Objects.hashCode(this.flowRelativePath);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FlowFile other = (FlowFile) obj;
        if (this.flowChunkSize != other.flowChunkSize) {
            return false;
        }
        if (this.flowTotalSize != other.flowTotalSize) {
            return false;
        }
        if (!Objects.equals(this.flowIdentifier, other.flowIdentifier)) {
            return false;
        }
        if (!Objects.equals(this.flowFilename, other.flowFilename)) {
            return false;
        }
        return Objects.equals(this.flowRelativePath, other.flowRelativePath);
    }

}
