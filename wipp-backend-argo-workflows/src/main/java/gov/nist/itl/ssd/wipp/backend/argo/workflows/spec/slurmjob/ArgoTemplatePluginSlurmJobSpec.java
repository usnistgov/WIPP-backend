/**
 * 
 */
package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec.slurmjob;

import java.util.List;

/**
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
public class ArgoTemplatePluginSlurmJobSpec {

	private String batch;
	
	public void generateBatch(String containerId,
            List<String> parameters,
            String jobId,
            String wippSlurmInputDataPath,
            String wippSlurmOutputDataPath,
            String containerInputsMountPath,
            String containerOutputsMountPath) {
		// create list of args for container
		String containerArgs = "";
		for (String parameter : parameters) {
            containerArgs += (" --" + parameter);
            containerArgs += (" {{inputs.parameters." + parameter + "}}");
        }
		// generate batch script
		String batchScript = "#!/bin/sh\n"
				+ "#SBATCH --nodes=1\n"
				+ "#SBATCH --gres=gpu:1\n"
				+ "#SBATCH --output wipp-" + jobId + ".out\n"
				+ "srun singularity run --nv "
				+ "--bind " + wippSlurmInputDataPath + ":" + containerInputsMountPath + ":ro "
				+ "--bind " + wippSlurmOutputDataPath + ":" + containerOutputsMountPath + ":rw "
				+ "docker://" + containerId + containerArgs;
		
		// set batch script
		this.setBatch(batchScript);
	};
	
	public String getBatch() {
		return batch;
	}
	public void setBatch(String batch) {
		this.batch = batch;
	}
	
	
}
