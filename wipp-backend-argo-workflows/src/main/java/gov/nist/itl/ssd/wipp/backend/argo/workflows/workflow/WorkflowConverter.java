package gov.nist.itl.ssd.wipp.backend.argo.workflows.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import gov.nist.itl.ssd.wipp.backend.argo.workflows.plugin.Plugin;
import gov.nist.itl.ssd.wipp.backend.argo.workflows.spec.*;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.Workflow;
import gov.nist.itl.ssd.wipp.backend.core.model.workflow.WorkflowStatus;

import java.io.File;
import java.util.*;


/**
 *
 *
 * @author Philippe Dessauw <philippe.dessauw at nist.gov>
 */
public class WorkflowConverter {
    private Workflow workflow;
    private Map<Job, List<String>> jobsDependencies;
    private Map<Job, Plugin> jobsPlugins;
    private String imagesCollectionsFolder;

    private static final String dataVolumeName = "data-volume";

    public WorkflowConverter(
        Workflow workflow,
        Map<Job, List<String>> jobsDependencies,
        Map<Job, Plugin> jobsPlugins,
        String imagesCollectionsFolder
    ) {
        this.workflow = workflow;
        this.jobsDependencies = jobsDependencies;
        this.jobsPlugins = jobsPlugins;
        this.imagesCollectionsFolder = imagesCollectionsFolder;
    }

    private HashMap<String, String> generateMetadata() {
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("generatedName", this.workflow.getName());

        return metadata;
    }

    private List<ArgoVolume> generateSpecVolumes() {
        HashMap<String, String> hostPath = new HashMap<>();
        hostPath.put("path", this.imagesCollectionsFolder);
        hostPath.put("type", "Directory");

        ArrayList<ArgoVolume> argoVolumeList = new ArrayList<>();
        ArgoVolume argoVolume = new ArgoVolume(dataVolumeName, hostPath);
        argoVolumeList.add(argoVolume);

        return argoVolumeList;
    }

    private ArgoTemplatePluginContainer generateTemplatePluginContainer(
        String containerId,
        Set<String> parameters
    ) {
        ArgoTemplatePluginContainer container = new ArgoTemplatePluginContainer();

        container.setImage(containerId);

        // Setup the container arguments
        List<String> argoPluginContainerArgs = new ArrayList<>();
        for(String parameter: parameters) {
            argoPluginContainerArgs.add("--" + parameter);
            argoPluginContainerArgs.add("{{ inputs.parameters." + parameter + " }}");
        }
        container.setArgs(argoPluginContainerArgs);

        // Setup the volume for the data
        ArrayList<Map<String, String>> volumeMounts = new ArrayList<>();

        HashMap<String, String> dataVolume = new HashMap<>();
        dataVolume.put("mountPath", "/data");
        dataVolume.put("name", dataVolumeName);

        volumeMounts.add(dataVolume);
        container.setVolumeMounts(volumeMounts);

        return container;
    }

    private ArgoTemplatePlugin generateTemplatePlugin(Plugin plugin, Set<String> parameters) {
        ArgoTemplatePlugin argoTemplatePlugin = new ArgoTemplatePlugin();
        argoTemplatePlugin.setName(plugin.getIdentifier());

        // Add the plugin parameters as required by the image
        HashMap<String, List<NameValueParam>> argoTemplateInputs = new HashMap<>();
        List<NameValueParam> argoTemplateArgs = new ArrayList<>();

        for(String parameter: parameters) {
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

        // Browse the parameter to setup plugin parameters
        for(String key: job.getParameters().keySet()) {
            NameValueParam workflowParams = new NameValueParam(key, job.getParameter(key));

            argoWorkflowArgs.add(workflowParams);
        }

        argoTemplateWorkflowParams.put("parameters", argoWorkflowArgs);
        argoTemplateWorkflowTask.setArguments(argoTemplateWorkflowParams);

        argoTemplateWorkflowTask.setDependencies(this.jobsDependencies.get(job));
        return argoTemplateWorkflowTask;
    }

    private List<ArgoAbstractTemplate> generateSpecTemplates() {
        List<ArgoAbstractTemplate> argoTemplates = new ArrayList<>();
        List<ArgoTemplateWorkflowTask> argoTemplateWorkflowTasks = new ArrayList<>();

        // Keep track of the included plugins
        List<String> includedPlugins = new ArrayList<>();

        for(Job job: this.jobsDependencies.keySet()) {
            // Get the plugin used for the job
            Plugin plugin = this.jobsPlugins.get(job);

            // Add plugin template if it has not been included yet
            if(!includedPlugins.contains(plugin.getIdentifier())) {
                argoTemplates.add(this.generateTemplatePlugin(plugin, job.getParameters().keySet()));
                includedPlugins.add(plugin.getIdentifier());  // Update included plugin list
            }

            // Add the task to the workflow
            argoTemplateWorkflowTasks.add(this.generateTemplateWorkflowTask(job, plugin));
        }

        argoTemplates.add(new ArgoTemplateWorkflow(argoTemplateWorkflowTasks));

        return argoTemplates;
    }

    private ArgoWorkflowSpec generateSpec() {
        ArgoWorkflowSpec argoWorkflowSpec = new ArgoWorkflowSpec();

        argoWorkflowSpec.setTemplates(this.generateSpecTemplates());
        argoWorkflowSpec.setVolumes(this.generateSpecVolumes());

        return argoWorkflowSpec;
    }

    public void convert(String workflowFilePath) throws Exception {
        YAMLFactory yamlFactory = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper(yamlFactory);

        ArgoWorkflow argoWorkflow = new ArgoWorkflow();

        argoWorkflow.setMetadata(this.generateMetadata());
        argoWorkflow.setSpec(this.generateSpec());

        File workflowFile = new File(workflowFilePath);
        mapper.writeValue(workflowFile, argoWorkflow);

        this.workflow.setStatus(WorkflowStatus.SUBMITTED);
    }

    public Workflow getWorkflow() {
        return this.workflow;
    }
}
