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
package gov.nist.itl.ssd.wipp.backend.argo.workflows.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import gov.nist.itl.ssd.wipp.backend.argo.workflows.plugin.Plugin;
import gov.nist.itl.ssd.wipp.backend.argo.workflows.plugin.PluginIO;
import gov.nist.itl.ssd.wipp.backend.argo.workflows.spec.*;
import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandlerService;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.Workflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Converts workflow configuration to Argo workflow spec file (YAML)
 *
 * @author Philippe Dessauw <philippe.dessauw at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 * @author Samia Benjida <samia.benjida at nist.gov>
 */
@Component
public class WorkflowConverter {
    private Workflow workflow;
    private Map<Job, List<String>> jobsDependencies;
    private Map<Job, Plugin> jobsPlugins;

    private static final String wippDataVolumeName = "wipp-data-volume";

    private static final Logger LOGGER = Logger.getLogger(WorkflowConverter.class.getName());

    @Autowired
    private CoreConfig coreConfig;

    @Autowired
    private DataHandlerService dataHandlerService;


    private HashMap<String, String> generateMetadata() {
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("generateName", this.workflow.getName().toLowerCase() + "-");

        return metadata;
    }

    private List<ArgoVolume> generateSpecVolumes() {
        ArrayList<ArgoVolume> argoVolumeList = new ArrayList<>();
        ArgoVolume inputArgoVolume = new ArgoVolume(wippDataVolumeName, 
				coreConfig.getWippDataPVCName());
        argoVolumeList.add(inputArgoVolume);

        return argoVolumeList;
    }

    private ArgoTemplatePluginContainer generateTemplatePluginContainer(
            String containerId,
            List<String> parameters,
            String jobId
    ) {
        ArgoTemplatePluginContainer container = new ArgoTemplatePluginContainer();

        container.setImage(containerId);

        // Setup the container arguments
        List<String> argoPluginContainerArgs = new ArrayList<>();
        for (String parameter : parameters) {
            argoPluginContainerArgs.add("--" + parameter);
            argoPluginContainerArgs.add("{{inputs.parameters." + parameter + "}}");
        }

        container.setArgs(argoPluginContainerArgs);


        ArrayList<Map<String, Object>> volumeMounts = new ArrayList<>();

        // Setup the volume mounts for the input data
        HashMap<String, Object> inputDataVolumeMount = new HashMap<>();
        inputDataVolumeMount.put("mountPath", coreConfig.getContainerInputsMountPath());
        inputDataVolumeMount.put("name", wippDataVolumeName);
        inputDataVolumeMount.put("readOnly", true);
        volumeMounts.add(inputDataVolumeMount);

        // Setup the volume mounts for the output data
        HashMap<String, Object> outputDataVolumeMount = new HashMap<>();
        outputDataVolumeMount.put("mountPath", this.getOutputMountPath(jobId));
        outputDataVolumeMount.put("name", wippDataVolumeName);
        outputDataVolumeMount.put("subPath", getOutputMountSubPath(jobId));
        outputDataVolumeMount.put("readOnly", false);
        volumeMounts.add(outputDataVolumeMount);
        container.setVolumeMounts(volumeMounts);

        return container;
    }

    private ArgoTemplatePlugin generateTemplatePlugin(Plugin plugin, List<String> parameters, String jobId) {
        ArgoTemplatePlugin argoTemplatePlugin = new ArgoTemplatePlugin();
        argoTemplatePlugin.setName(plugin.getIdentifier() + "-" + jobId);

        // Add the plugin parameters as required by the image
        HashMap<String, List<NameValueParam>> argoTemplateInputs = new HashMap<>();
        List<NameValueParam> argoTemplateArgs = new ArrayList<>();

        // Add the plugin's outputs to the list of parameters coming from the job's configuration
        for (PluginIO output : plugin.getOutputs()) {
            parameters.add(output.getName());
        }

        // Add all the parameters to the Argo plugin template
        for (String parameter : parameters) {
            argoTemplateArgs.add(new NameValueParam(parameter));
        }

        argoTemplateInputs.put("parameters", argoTemplateArgs);
        argoTemplatePlugin.setInputs(argoTemplateInputs);

        argoTemplatePlugin.setContainer(
                this.generateTemplatePluginContainer(
                        plugin.getContainerId(),
                        parameters,
                        jobId
                )
        );
        return argoTemplatePlugin;
    }

    private ArgoTemplateWorkflowTask generateTemplateWorkflowTask(
            Job job,
            Plugin plugin
    ) {
        ArgoTemplateWorkflowTask argoTemplateWorkflowTask = new ArgoTemplateWorkflowTask();
        argoTemplateWorkflowTask.setName(job.getName());
        argoTemplateWorkflowTask.setTemplate(plugin.getIdentifier() + "-" + job.getId());

        Map<String, List<NameValueParam>> argoTemplateWorkflowParams = new HashMap<>();
        List<NameValueParam> argoWorkflowArgs = new ArrayList<>();

        // Create job temp output folder
        File tempJobFolder = new File(coreConfig.getJobsTempFolder(),
                job.getId());

        tempJobFolder.mkdirs();

        // Add output folder to parameters
        for (PluginIO output : plugin.getOutputs()) {
            // Create job output subfolders
            File outputSubFolder = new File(tempJobFolder, output.getName());
            outputSubFolder.mkdirs();
            NameValueParam outputParam = new NameValueParam(output.getName(), this.getOutputMountPath(job.getId()) + "/" + output.getName());
            argoWorkflowArgs.add(outputParam);
        }

        // Browse the parameter to setup plugin parameters
        Map<String, String> jobParams = job.getParameters();
        // TODO: handle job params validity at job's creation
        for (PluginIO input : plugin.getInputs()) {
            if (jobParams.containsKey(input.getName())) {
                String paramValue = jobParams.get(input.getName());
                String paramName = input.getName();
                String paramType = input.getType();

                DataHandler dataHandler = dataHandlerService.getDataHandler(paramType);
                paramValue = dataHandler.exportDataAsParam(paramValue);

                NameValueParam workflowParams = new NameValueParam(paramName, paramValue);
                argoWorkflowArgs.add(workflowParams);
            }
        }

        argoTemplateWorkflowParams.put("parameters", argoWorkflowArgs);
        argoTemplateWorkflowTask.setArguments(argoTemplateWorkflowParams);

        argoTemplateWorkflowTask.setDependencies(this.jobsDependencies.get(job));
        return argoTemplateWorkflowTask;
    }

    private ArgoTemplateExitHandler generateTemplateExitHandler() {
        ArgoTemplateExitHandler argoTemplateExitHandler = new ArgoTemplateExitHandler();
        argoTemplateExitHandler.setName("exit-handler");

        argoTemplateExitHandler.setContainer(
                this.generateTemplateExitHandlerContainer()
        );

        return argoTemplateExitHandler;
    }

    private ArgoTemplateExitHandlerContainer generateTemplateExitHandlerContainer() {
        ArgoTemplateExitHandlerContainer container = new ArgoTemplateExitHandlerContainer();

        container.setImage("byrnedo/alpine-curl:latest");

        String url = WebMvcLinkBuilder.linkTo(
                WorkflowExitController.class, workflow.getId())
                .withRel("exit").getHref();
        LOGGER.log(Level.INFO, "workflow url: " + url);

        List<String> args = new ArrayList<>();
        args.add("-X");
        args.add("POST");
        args.add("-H");
        args.add("Content-Type:application/json");
        args.add("-d");
        args.add("{{workflow.status}}");
        args.add(url);
        args.add("-v");
        args.add("-k");
        container.setArgs(args);

        return container;
    }
    /**
     * Get and parse nodeSelector labels for all jobs within workflow. 
     * Can be overridden by nodeSelector in container template.
     * Expects nodeSelector to be formatted as key-value pairs split by semi-colons (eg. "key1:value1;key2:value2;").
     * @return nodeSelector labels as a Map
     */
    private Map<String, String> generateNodeSelector() {
        Map<String, String> nodeSelector = new HashMap<>();
        String[] labels = coreConfig.getWorkflowNodeSelector().split(";");

        for(String label : labels) {
            String[] temp = label.split(":");
            if(temp.length == 2) {
                nodeSelector.put(temp[0], temp[1]);
            }
        }
        return nodeSelector;
    }

    /**
     * Get and parse node tolerations for all jobs within workflow.
     * Expects tolerations to be formatted as key-operator-value-effect groups split by semi-colons (eg. "key1:operator1:value1:effect1;").
     * Operator options are limited to "Equal" and "Exists", effect options are limited to "NoSchedule", "PreferNoSchedule", and "NoExecute".
     * Optional parameters can be skipped with appropriate colon placeholders (eg. "key1:operator1::").
     * @return tolerations labels as a List of Maps per rule
     */
    private List<Map<String, String>> generateTolerations() {
        final String[] mapKeys = {"key", "operator", "value", "effect"};
        List<Map<String, String>> tolerations = new ArrayList<>();
        String[] labels = coreConfig.getWorkflowTolerations().split(";");

        for(String label : labels) {
            String[] temp = label.split(":", -1);
            Map<String, String> rule = new HashMap<>();
            if(temp.length == 4) {
                for(int i=0; i < mapKeys.length; i++) {
                    if(!temp[i].isEmpty()) {
                        rule.put(mapKeys[i], temp[i]);
                    }
                }
                tolerations.add(rule);
            }
        }
        return tolerations;
    }

    private List<ArgoAbstractTemplate> generateSpecTemplates() {
        List<ArgoAbstractTemplate> argoTemplates = new ArrayList<>();
        List<ArgoTemplateWorkflowTask> argoTemplateWorkflowTasks = new ArrayList<>();

        // Keep track of the included plugins
        List<String> includedPlugins = new ArrayList<>();

        for (Job job : this.jobsDependencies.keySet()) {
            // Get the plugin used for the job
            Plugin plugin = this.jobsPlugins.get(job);

            // Add plugin template if it has not been included yet
            if (!includedPlugins.contains(plugin.getIdentifier() + "-" + job.getId())) {
                List<String> parameterNames = new ArrayList<String>(job.getParameters().keySet());
                argoTemplates.add(this.generateTemplatePlugin(plugin, parameterNames, job.getId()));
                includedPlugins.add(plugin.getIdentifier());  // Update included plugin list
            }

            // Add the task to the workflow
            argoTemplateWorkflowTasks.add(this.generateTemplateWorkflowTask(job, plugin));
        }

        argoTemplates.add(new ArgoTemplateWorkflow(argoTemplateWorkflowTasks));

        // Add exit handler template
        argoTemplates.add(this.generateTemplateExitHandler());

        return argoTemplates;
    }

    private ArgoWorkflowSpec generateSpec() {
        ArgoWorkflowSpec argoWorkflowSpec = new ArgoWorkflowSpec();

        argoWorkflowSpec.setNodeSelector(this.generateNodeSelector());
        argoWorkflowSpec.setTolerations(this.generateTolerations());
        argoWorkflowSpec.setTemplates(this.generateSpecTemplates());
        argoWorkflowSpec.setVolumes(this.generateSpecVolumes());

        return argoWorkflowSpec;
    }

    public void convert(Workflow workflow, Map<Job, List<String>> jobsDependencies, Map<Job, Plugin> jobsPlugins,
                        String workflowFilePath) throws Exception {

        this.workflow = workflow;
        this.jobsDependencies = jobsDependencies;
        this.jobsPlugins = jobsPlugins;

        YAMLFactory yamlFactory = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper(yamlFactory);

        ArgoWorkflow argoWorkflow = new ArgoWorkflow();

        argoWorkflow.setMetadata(this.generateMetadata());
        argoWorkflow.setSpec(this.generateSpec());

        File workflowFile = new File(workflowFilePath);
        mapper.writeValue(workflowFile, argoWorkflow);
    }

    /**
     * Get job work folder path in container
     * @param jobId
     * @return the path of the job work folder
     */
    private String getOutputMountPath(String jobId){
        return coreConfig.getContainerOutputsMountPath() + "/" + jobId;
    }
    
    /**
     * Get data volume mount sub path for job work folder 
     * (relative to root in data volume)
     * @param jobId
     * @return the sub path of the data volume to mount 
     */
    private String getOutputMountSubPath(String jobId){
        return new File(coreConfig.getJobsTempFolder(), jobId).getAbsolutePath()
                .replaceFirst(coreConfig.getStorageRootFolder() + "/", "");
    }
}
