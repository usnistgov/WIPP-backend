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
package gov.nist.itl.ssd.wipp.backend.data.aimodelcard;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.data.aimodelcard.bioimageio.Authors;
import gov.nist.itl.ssd.wipp.backend.data.aimodelcard.bioimageio.BioImageIo;
import gov.nist.itl.ssd.wipp.backend.data.aimodelcard.bioimageio.Cite;
import gov.nist.itl.ssd.wipp.backend.data.aimodelcard.tensorflow.*;
import gov.nist.itl.ssd.wipp.backend.data.aimodelcard.huggingface.HuggingFace;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AI Model Card controller
 *
 * @author Antoine Meyer <antoine.meyer at nist.gov>
 */
@RestController
@Tag(name="AiModelCard Entity")
@RequestMapping(CoreConfig.BASE_URI + "/aiModelCards/{id}/export")
public class AiModelCardController {

    @Autowired
    AiModelCardRepository aiModelCardRepository;

    private AiModelCard getAiModelCard(String id) {
        // Get
        Optional<AiModelCard> omc = aiModelCardRepository.findById(id);
        if(!omc.isPresent()){
            throw new ResourceNotFoundException("ModelCard not found.");
        }
        AiModelCard mc = omc.get();
        return mc;
    }

    private byte[] convertIntoBytes(Object obj) throws IOException {
        byte[] bytes = new byte[0];
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            bytes = mapper.writeValueAsString(obj).getBytes();
        }
        catch (JsonGenerationException | JsonMappingException e) { e.printStackTrace(); }
        return bytes;
    }

    private HttpHeaders setupResponseHead(String filename) {
        HttpHeaders head = new HttpHeaders();
        head.add(
                "content-disposition",
                "attachment; filename=\"" + filename
        );
        List<String> exposedHead = List.of("content-disposition");
        head.setAccessControlExposeHeaders(exposedHead);
        return head;
    }

    private Map<String, String> addFullUrlToId(Map<String, String> map) {
        if (map != null) {
            map.replaceAll((k, v) -> v = "/images-collection/" + v);
        }
        return map;
    }

    @RequestMapping(
            value = "tensorflow",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('admin') or @aiModelCardSecurity.checkAuthorize(#id, false)")
    public ResponseEntity<byte[]> tensorflow(@PathVariable("id") String id) throws IOException
    {
        AiModelCard mc = getAiModelCard(id);

        // Convert ModelCard object to Tensorflow ModelCard
        Tensorflow tf = new Tensorflow();
        tf.setModelDetails(new ModelDetails(
                mc.getName(),
                mc.getDescription(),
                new Version(mc.getVersion()),
                new Owners(mc.getAuthor()),
                mc.getLicense(),
                mc.getCitation()
        ));
        tf.setModelParameters(new ModelParameters());
        tf.setConsiderations(new Considerations());

        byte[] bytes = convertIntoBytes(tf);

        HttpHeaders head = setupResponseHead("WIPP_AIModelCard_Tensorflow.json");

        return ResponseEntity
                .ok()
                .headers(head)
                .contentType(MediaType.APPLICATION_JSON)
                .contentLength(bytes.length)
                .body(bytes);
    }

    @RequestMapping(
            value = "huggingface",
            method = RequestMethod.GET,
            produces = "application/x-yaml"
    )
    @PreAuthorize("hasRole('admin') or @aiModelCardSecurity.checkAuthorize(#id, false)")
    public ResponseEntity<byte[]> huggingface(@PathVariable("id") String id) throws IOException
    {
        AiModelCard mc = getAiModelCard(id);

        // Convert ModelCard object to HuggingFace ModelCard
        HuggingFace hf = new HuggingFace(
                mc.getId(),
                mc.getDescription(),
                mc.getLicense(),
                mc.getCitation(),
                mc.getAuthor()
        );
        hf.setModel_type(mc.getOperationType());
        hf.setTraining_data(addFullUrlToId(mc.getTrainingData()));
        hf.setTesting_metrics(mc.getTesting());

        byte[] bytes = convertIntoBytes(hf);

        HttpHeaders head = setupResponseHead("WIPP_AIModelCard_Huggingface.yaml");

        return ResponseEntity
                .ok()
                .headers(head)
                .contentType(MediaType.valueOf("application/yaml"))
                .contentLength(bytes.length)
                .body(bytes);
    }

    @RequestMapping(
            value = "bioimageio",
            method = RequestMethod.GET,
            produces = "application/x-yaml"
    )
    @PreAuthorize("hasRole('admin') or @aiModelCardSecurity.checkAuthorize(#id, false)")
    public ResponseEntity<byte[]> bioimageio(@PathVariable("id") String id) throws IOException
    {
        AiModelCard mc = getAiModelCard(id);

        // Fill-in
        Authors[] authors = new Authors[1];
        authors[0] = new Authors(mc.getAuthor());

        Cite[] cites = new Cite[1];
        cites[0] = new Cite(mc.getCitation());

        // Convert ModelCard object to Bioimageio ModelCard
        BioImageIo bii = new BioImageIo(
                authors,
                cites,
                mc.getDescription(),
                mc.getLicense(),
                mc.getName(),
                mc.getVersion(),
                mc.getOperationType()
        );
        bii.setTimestamp(mc.getDate());
        bii.setTraining_data(addFullUrlToId(mc.getTrainingData()));

        byte[] bytes = convertIntoBytes(bii);

        HttpHeaders head = setupResponseHead("WIPP_AIModelCard_BioImageIo.yaml");

        return ResponseEntity
                .ok()
                .headers(head)
                .contentType(MediaType.valueOf("application/yaml"))
                .contentLength(bytes.length)
                .body(bytes);
    }

    @RequestMapping(
            value = "cdcs",
            method = RequestMethod.GET
    )
    @PreAuthorize("hasRole('admin') or @aiModelCardSecurity.checkAuthorize(#id, false)")
    public ResponseEntity<byte[]> cdcs(@PathVariable("id") String id) throws IOException
    {
        AiModelCard mc = getAiModelCard(id);
        mc.setTrainingData(addFullUrlToId(mc.getTrainingData()));

        byte[] bytes = convertIntoBytes(mc);

        HttpHeaders head = setupResponseHead("WIPP_AIModelCard_CDCS.json");

        return ResponseEntity
                .ok()
                .headers(head)
                .contentType(MediaType.APPLICATION_JSON)
                .contentLength(bytes.length)
                .body(bytes);
    }

}
