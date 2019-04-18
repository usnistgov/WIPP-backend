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
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.WorkflowStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Converts workflow configuration to Argo workflow spec file (YAML) and submits workflow
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

    private static final String dataVolumeName = "data-volume";

    private static final Logger LOGGER = Logger.getLogger(WorkflowConverter.class.getName());

    @Autowired
    private CoreConfig coreConfig;

    @Autowired
    private DataHandlerService dataHandlerService;


    private HashMap<String, String> generateMetadata() {
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("generateName", this.workflow.getName().toLowerCase());

        return metadata;
    }

    private List<ArgoVolume> generateSpecVolumes() {
        HashMap<String, String> hostPath = new HashMap<>();
        hostPath.put("path", coreConfig.getStorageRootFolder());
        //hostPath.put("type", "Directory");

        ArrayList<ArgoVolume> argoVolumeList = new ArrayList<>();
        ArgoVolume argoVolume = new ArgoVolume(dataVolumeName, hostPath);
        argoVolumeList.add(argoVolume);

        return argoVolumeList;
    }

    private ArgoTemplatePluginContainer generateTemplatePluginContainer(
            String containerId,
            List<String> parameters
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

        // Setup the volume for the data
        ArrayList<Map<String, String>> volumeMounts = new ArrayList<>();

        HashMap<String, String> dataVolume = new HashMap<>();
        dataVolume.put("mountPath", coreConfig.getContainerMountPath());
        dataVolume.put("name", dataVolumeName);

        volumeMounts.add(dataVolume);
        container.setVolumeMounts(volumeMounts);

        return container;
    }

    private ArgoTemplatePlugin generateTemplatePlugin(Plugin plugin, List<String> parameters) {
        ArgoTemplatePlugin argoTemplatePlugin = new ArgoTemplatePlugin();
        argoTemplatePlugin.setName(plugin.getIdentifier());

        // Add the plugin parameters as required by the image
        HashMap<String, List<NameValueParam>> argoTemplateInputs = new HashMap<>();
        List<NameValueParam> argoTemplateArgs = new ArrayList<>();

        // Add the plugin's outputs to the list of parameters coming from the job's configuration
        for(PluginIO output : plugin.getOutputs()) {
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
                        parameters
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
        argoTemplateWorkflowTask.setTemplate(plugin.getIdentifier());

        Map<String, List<NameValueParam>> argoTemplateWorkflowParams = new HashMap<>();
        List<NameValueParam> argoWorkflowArgs = new ArrayList<>();

        // Create job temp output folder
        String jobTempFolder = coreConfig.getJobsTempFolder();
        File tempJobFolder = new File(jobTempFolder, job.getId());
        tempJobFolder.mkdirs();

        // Add output folder to parameters
        for (PluginIO output : plugin.getOutputs()) {
            // Create job temp output folder
            File outputFolder = new File(tempJobFolder, output.getName());
            outputFolder.mkdirs();
            NameValueParam outputParam = new NameValueParam(output.getName(),
                    outputFolder.getAbsolutePath().replaceFirst(coreConfig.getStorageRootFolder(),coreConfig.getContainerMountPath()));
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

        String url = ControllerLinkBuilder.linkTo(
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
        container.setArgs(args);

        return container;
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
            if (!includedPlugins.contains(plugin.getIdentifier())) {
                List<String> parameterNames = new ArrayList<String>(job.getParameters().keySet());
                argoTemplates.add(this.generateTemplatePlugin(plugin, parameterNames));
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

        // Launch separate submission of the argo workflow
        List<String> builderCommands = new ArrayList<>();
        Collections.addAll(builderCommands, coreConfig.getWorflowBinary().split(" "));
        builderCommands.add("submit");
        builderCommands.add(workflowFilePath);

        ProcessBuilder builder = new ProcessBuilder(builderCommands);
        builder.inheritIO();
        Process process;
        try {
            process = builder.start();
            int exitCode = process.waitFor();
            assert exitCode == 0;

            this.workflow.setStatus(WorkflowStatus.SUBMITTED);
        } catch (IOException ex) {
            this.workflow.setStatus(WorkflowStatus.ERROR);
            LOGGER.log(Level.WARNING, "Cannot start workflow ", ex);

        }
    }

    public Workflow getWorkflow() {
        return this.workflow;
    }
}
