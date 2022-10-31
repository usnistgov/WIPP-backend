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
package gov.nist.itl.ssd.wipp.backend.core.model.computation;

/**
 * Hardware resource requirements for the plugin
 *
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
public class PluginResourceRequirements {

    // Minimum RAM in mebibytes (Mi)
    Long ramMin;

    // Minimum number of CPU cores
    Long coresMin;

    // Advanced Vector Extensions (AVX) CPU capability required
    boolean cpuAVX = false;

    // Advanced Vector Extensions 2 (AVX2) CPU capability required
    boolean cpuAVX2 = false;

    // GPU/accelerator required
    boolean gpu = false;

    // GPU Cuda-related requirements
    PluginResourceCudaRequirements cudaRequirements;

    public Long getRamMin() {
        return ramMin;
    }

    public void setRamMin(Long ramMin) {
        this.ramMin = ramMin;
    }

    public Long getCoresMin() {
        return coresMin;
    }

    public void setCoresMin(Long coresMin) {
        this.coresMin = coresMin;
    }

    public boolean isCpuAVX() {
        return cpuAVX;
    }

    public void setCpuAVX(boolean cpuAVX) {
        this.cpuAVX = cpuAVX;
    }

    public boolean isCpuAVX2() {
        return cpuAVX2;
    }

    public void setCpuAVX2(boolean cpuAVX2) {
        this.cpuAVX2 = cpuAVX2;
    }

    public boolean isGpu() {
        return gpu;
    }

    public void setGpu(boolean gpu) {
        this.gpu = gpu;
    }

    public PluginResourceCudaRequirements getCudaRequirements() {
        return cudaRequirements;
    }

    public void setCudaRequirements(PluginResourceCudaRequirements cudaRequirements) {
        this.cudaRequirements = cudaRequirements;
    }
}

