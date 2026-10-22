package gov.nist.itl.ssd.wipp.backend.data.imagescollection;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.metadatafiles.MetadataFileHandler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Controller for import of NexusLims images collections
 * @author Mylene Simon <mylene.simon at nist.gov>
 */

@RestController
@Tag(name="ImagesCollection Entity")
@RequestMapping(CoreConfig.BASE_URI + "/imagesCollections/nexuslims-import")
public class ImagesCollectionNexusLimsImportController {

    private final ImagesCollectionNexusLimsImportService nexusLimsService;

    public ImagesCollectionNexusLimsImportController(ImagesCollectionNexusLimsImportService nexusLimsService) {
        this.nexusLimsService = nexusLimsService;
    }

    @RequestMapping(value = "",
            method = RequestMethod.POST,
            produces = "application/json")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> importRecord(@RequestBody NexusLimsImportRequest request) {
        try {
            // Call your service which fetches, unmarshalls, transforms, and prints the data
            List<String> result = nexusLimsService.importExperimentDataByPid(request.pid());
            
            // Returns the exact structure as a JSON response body
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            // If the PID isn't found or API fails, return a 500 status with the error message
            e.printStackTrace(); 
            return ResponseEntity.internalServerError().build();
        }
    }

}

record NexusLimsImportRequest(String pid) {}