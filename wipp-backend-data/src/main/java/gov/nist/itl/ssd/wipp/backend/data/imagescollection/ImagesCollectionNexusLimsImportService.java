package gov.nist.itl.ssd.wipp.backend.data.imagescollection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.xml.transform.stream.StreamSource;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.data.nexuslims.*;

/**
 * Service class for NexusLims import
 @author Mylene Simon <mylene.simon at nist.gov>
 **/
@Service
public class ImagesCollectionNexusLimsImportService {

    CoreConfig config;
    private ImagesCollectionLocalImporter localImporter;

    // RestClient for NexusLims-CDCS
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImagesCollectionNexusLimsImportService(CoreConfig config, ImagesCollectionLocalImporter localImporter) {
        this.config = config;
        this.localImporter = localImporter;
        
        this.restClient = RestClient.builder()
            .baseUrl(config.getNexusLimsUrl()) 
            .build();
    }
    /**
     * Posts a JSON query for a specific Experiment PID, extracts the ID, 
     * and downloads the corresponding experiment XML document.
     * 
     * @param targetPid The PID string to query
     * @return The list of images organized by experiment activities
     */
    public List<String> importExperimentDataByPid(String targetPid) {
        
        try {
            // 1. Create PID search payload {"query": {"Experiment.@pid": targetPid}}
            Map<String, String> criteria = Map.of("Experiment.@pid", targetPid);
            String innerQueryJsonString = objectMapper.writeValueAsString(criteria);
            Map<String, Object> queryPayload = Map.of("query", innerQueryJsonString);
            String queryPayloadString = String.format("{\"query\":{\"Experiment.@pid\":\"%s\"}}", targetPid);

            // 2. Post JSON payload to query endpoint and read JSON tree response
            JsonNode jsonResponse = restClient.post()
                    .uri("/rest/data/query/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(queryPayload)
                    .retrieve()
                    .body(JsonNode.class);


            // 3. Check JSON response
            if (jsonResponse == null || 
                !jsonResponse.has("results") || 
                jsonResponse.get("results").isEmpty()) {
                throw new RuntimeException("No records found for PID: " + targetPid);
            }

            // 4. Extract the raw XML string from the "xml_content" field of the first result index
            JsonNode firstResult = jsonResponse.get("results").get(0);
            if (!firstResult.has("xml_content")) {
                throw new RuntimeException("The 'xml_content' field is missing from the search result payload.");
            }
            String rawXmlDocument = firstResult.get("xml_content").asText();

            // 5. Unmarshall the raw XML string directly using JAXB and a StringReader
            JAXBContext context = JAXBContext.newInstance(Experiment.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            StreamSource source = new StreamSource(new StringReader(rawXmlDocument));
            JAXBElement<Experiment> root = unmarshaller.unmarshal(source, Experiment.class);
            Experiment experiment = root.getValue();
            
            // 6. Extract experiment activity image information from experiment
            Map<String, List<String>> activities = transformToActivityCollections(experiment);

            // 7. Import experiment activities as images collections
            List <String> activityNames = new ArrayList<>();
            activities.forEach((activityName, imageLocations) -> {
                activityNames.add(activityName);
                this.localImporter.importFromNexusLims(activityName, imageLocations, targetPid);
            });

            // Return activity/collection names
            return activityNames;

        } catch (JAXBException e) {
            throw new RuntimeException("Failed to unmarshall embedded 'xml_content' document via JAXB", e);
        } catch (Exception e) {
            throw new RuntimeException("Error processing NexusLIMS transaction pipeline", e);
        }
    }

    private Map<String, List<String>> transformToActivityCollections(Experiment experiment) {
        String title = experiment.getTitle();
        if (experiment.getAcquisitionActivity() == null) return Collections.emptyMap();

        return experiment.getAcquisitionActivity().stream().collect(Collectors.toMap(
            activity -> title + "-Activity" + activity.getSeqno(),
            activity -> {
                List<String> images = new ArrayList<>();
                if (activity.getDataset() != null) {
                    images = activity.getDataset().stream()
                        .filter(ds -> {
                            if (ds.getType() == null) return false;
                            String datasetType = String.valueOf(ds.getType()).trim();
                            return "image".equalsIgnoreCase(datasetType);
                        })
                        .map(ds -> ds.getLocation())
                        .filter(Objects::nonNull) // Safe check to prevent null paths
                        .collect(java.util.stream.Collectors.toList());
                }
                return images;
            }
        ));
    }

}
