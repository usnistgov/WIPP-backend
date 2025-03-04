/**
 * NIST-developed software is provided by NIST as a public service. You may
 * use, copy, and distribute copies of the software in any medium, provided
 * that you keep intact this entire notice. You may improve, modify, and create
 * derivative works of the software or any portion of the software, and you may
 * copy and distribute such modifications or works. Modified works should carry
 * a notice stating that you changed the software and should note the date and
 * nature of any such change. Please explicitly acknowledge the National
 * Institute of Standards and Technology as the source of the software.
 *
 * NIST-developed software is expressly provided "AS IS." NIST MAKES NO
 * WARRANTY OF ANY KIND, EXPRESS, IMPLIED, IN FACT, OR ARISING BY OPERATION OF
 * LAW, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT, AND DATA ACCURACY. NIST
 * NEITHER REPRESENTS NOR WARRANTS THAT THE OPERATION OF THE SOFTWARE WILL BE
 * UNINTERRUPTED OR ERROR-FREE, OR THAT ANY DEFECTS WILL BE CORRECTED. NIST
 * DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OF THE
 * SOFTWARE OR THE RESULTS THEREOF, INCLUDING BUT NOT LIMITED TO THE
 * CORRECTNESS, ACCURACY, RELIABILITY, OR USEFULNESS OF THE SOFTWARE.
 *
 * You are solely responsible for determining the appropriateness of using and
 * distributing the software and you assume all risks associated with its use,
 * including but not limited to the risks and costs of program errors,
 * compliance with applicable laws, damage to or loss of data, programs or
 * equipment, and the unavailability or interruption of operation. This
 * software is not intended to be used in any situation where a failure could
 * cause risk of injury or damage to property. The software developed by NIST
 * employees is not subject to copyright protection within the United States.
 */
package gov.nist.itl.ssd.wipp.backend.data.aimodel;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nist.itl.ssd.wipp.backend.core.model.computation.Plugin;
import gov.nist.itl.ssd.wipp.backend.core.model.computation.PluginIO;
import gov.nist.itl.ssd.wipp.backend.core.model.computation.PluginRepository;
import gov.nist.itl.ssd.wipp.backend.core.model.data.BaseDataHandler;
import gov.nist.itl.ssd.wipp.backend.data.aimodelcard.AiModelCard;
import gov.nist.itl.ssd.wipp.backend.data.aimodelcard.AiModelCardRepository;
import gov.nist.itl.ssd.wipp.backend.data.tensorboard.TensorBoardLogsController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobExecutionException;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 * @author Antoine Meyer <antoine.meyer at nist.gov>
 */
@Component("aiModelDataHandler")
@Qualifier("tensorflowModelDataHandler")
public class AiModelDataHandler extends BaseDataHandler implements DataHandler {

	@Autowired
	CoreConfig config;

    @Autowired
    private AiModelRepository aiModelRepository;

    @Autowired
    private PluginRepository wippPluginRepository;

    @Autowired
    private TensorBoardLogsController tensorBoardLogsController;

    @Autowired
    private AiModelCardRepository modelCardRepository;

	@Override
	public void importData(Job job, String outputName) throws JobExecutionException {
        // Get plugin
        Plugin plugin = wippPluginRepository.findById(job.getWippExecutable()).orElse(null);
        assert plugin != null;

        AiModel aiModel = new AiModel(job, outputName);
        // Set owner to job owner
        aiModel.setOwner(job.getOwner());
        // Set TM to private
        aiModel.setPubliclyShared(false);
        // Set framework
        setFramework(plugin, aiModel);
        aiModelRepository.save(aiModel);

        File trainedModelFolder = new File(config.getAiModelsFolder(), aiModel.getId());
        trainedModelFolder.mkdirs();

        File tempOutputDir = getJobOutputTempFolder(job.getId(), outputName);
        boolean success = tempOutputDir.renameTo(trainedModelFolder);
        if (!success) {
            aiModelRepository.delete(aiModel);
            throw new JobExecutionException("Cannot move ai model to final destination.");
        }
        setOutputId(job, outputName, aiModel.getId());

        // Create & save Model Card
        AiModelCard mc = new AiModelCard(aiModel, job, plugin);
        // Fill with TensorboardLogs data
        try {
            // Declare id
            String id = job.getId();
            // Declare variables
            List<List<String>> data;
            Float startTime, endTime, time, epochs, maxAccuracy, minLoss;
            for(String type : new String[]{"train", "test"})
            {
                setAccuracy(type, id, mc);
                setLoss(type, id, mc);
            }
        } catch (IOException e) { throw new RuntimeException(e); }

        // Save
        modelCardRepository.save(mc);
	}

    private void setFramework(Plugin plugin, AiModel aiModel) {
        // search for output where "name" == "outputDir"
        PluginIO outputDir = null;
        for (PluginIO output : plugin.getOutputs()) {
            if(Objects.equals(output.getName(), "outputDir")) {
                outputDir = output;
            }
        }
        // get framework data from this output
        if(outputDir!=null && !outputDir.getOptions().isEmpty()) {
            aiModel.setFramework(outputDir.getOptions().get("framework").toString());
        } else {
            aiModel.setFramework("N/A");
        }
    }

    private void setLoss(String type, String id, AiModelCard mc) throws IOException {
        Float minLoss;
        List<List<String>> data;
        data = tensorBoardLogsController.getCSV(id, type, "loss");
        data.removeFirst();
        // Find min
        minLoss = 1000f;
        for(List<String> e : data){
            Float c = Float.parseFloat(e.getLast());
            if( c < minLoss) { minLoss = c; }
        }
        // Add data
        if(type.equals("train")){
            mc.addTrainingEntries("minLoss", minLoss);
        } else {
            mc.addTestingEntries("minLoss", minLoss);
        }
    }

    private void setAccuracy(String type, String id, AiModelCard mc) throws IOException {
        Float startTime;
        Float time;
        List<List<String>> data;
        Float epochs;
        Float maxAccuracy;
        Float endTime;
        data = tensorBoardLogsController.getCSV(id, type, "accuracy");
        // Get time
        startTime = Float.parseFloat(data.get(1).getFirst());
        endTime = Float.parseFloat(data.getLast().getFirst());
        time = endTime - startTime;
        // Get epoch
        epochs = Float.parseFloat(data.getLast().get(1));
        // Find max
        data.removeFirst();
        maxAccuracy = -1f;
        for(List<String> e : data){
            Float c = Float.parseFloat(e.getLast());
            if( c > maxAccuracy) { maxAccuracy = c; }
        }
        // Add data
        if(type.equals("train")){
            mc.addTrainingEntries("time", time);
            mc.addTrainingEntries("epochs", epochs);
            mc.addTrainingEntries("maxAccuracy", maxAccuracy);
        }else{
            mc.addTestingEntries("time", time);
            mc.addTestingEntries("epochs", epochs);
            mc.addTestingEntries("maxAccuracy", maxAccuracy);
        }
    }

    @Override
    public String exportDataAsParam(String value) {
        String aiModelId = value;
        String aiModelPath;

        // check if the input of the job is the output of another job and if so return the associated path
        String regex = "\\{\\{ (.*)\\.(.*) \\}\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(aiModelId);
        if (m.find()) {
            String jobId = m.group(1);
            String outputName = m.group(2);
            aiModelPath = getJobOutputTempFolder(jobId, outputName).getAbsolutePath();
        }
        // else return the path of the AI model
        else {
            File aiModelFolder = new File(config.getAiModelsFolder(), aiModelId);
            aiModelPath = aiModelFolder.getAbsolutePath();

        }
        aiModelPath = aiModelPath.replaceFirst(config.getStorageRootFolder(),config.getContainerInputsMountPath());
        return aiModelPath;

    }
	
	@Override
    public void setDataToPublic(String value) {
    	Optional<AiModel> optAiModel = aiModelRepository.findById(value);
        if(optAiModel.isPresent()) {
            AiModel aiModel = optAiModel.get();
            if (!aiModel.isPubliclyShared()) {
                aiModel.setPubliclyShared(true);
                aiModelRepository.save(aiModel);
            }
        }
    }

}
