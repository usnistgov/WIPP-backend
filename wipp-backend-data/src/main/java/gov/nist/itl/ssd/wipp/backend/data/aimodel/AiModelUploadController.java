package gov.nist.itl.ssd.wipp.backend.data.aimodel;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Antoine Meyer <antoine.meyer at nist.gov>
 */
@RestController
@Tag(name="AiModel Entity")
@RequestMapping(CoreConfig.BASE_URI + "/aiModels/{aiModelId}/upload")
public class AiModelUploadController {

    @Autowired
    CoreConfig config;

    @Autowired
    AiModelRepository aiModelRepository;

    @RequestMapping(value="", method= RequestMethod.POST)
    @PreAuthorize("isAuthenticated()")
    public AiModel upload(
            @RequestParam("file") MultipartFile file,
            @PathVariable("aiModelId") String aiModelId
    ) throws IOException {
        // (0) Check existence of AI model
        Optional<AiModel> model = aiModelRepository.findById(aiModelId);
        if (model.isEmpty()) {
            throw new ClientException("AI model " + aiModelId + " not found.");
        }

        // (1) Create ai-model folder in WIPP-plugins
        File aiModelFolder = new File(config.getAiModelsFolder(), aiModelId);
        boolean success = aiModelFolder.mkdirs();
        if (!success) {
            throw new ClientException("AI model folder not created.");
        }

        // (2) Un-zip file
        ZipInputStream zis = new ZipInputStream(file.getInputStream());

        // (3) Move files into folder
        Path path = aiModelFolder.toPath();
        for (ZipEntry entry; (entry = zis.getNextEntry()) != null; ) {
            Path resolvedPath = path.resolve(entry.getName());
            if (!entry.isDirectory()) {
                Files.createDirectories(resolvedPath.getParent());
                Files.copy(zis, resolvedPath);
            } else {
                Files.createDirectories(resolvedPath);
            }
        }
        return model.get();
    }

}
