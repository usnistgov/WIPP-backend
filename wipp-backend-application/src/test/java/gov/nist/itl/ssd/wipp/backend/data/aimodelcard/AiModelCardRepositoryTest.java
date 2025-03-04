
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

import gov.nist.itl.ssd.wipp.backend.Application;
import gov.nist.itl.ssd.wipp.backend.app.SecurityConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.computation.Plugin;
import gov.nist.itl.ssd.wipp.backend.core.model.computation.PluginIO;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.data.aimodel.AiModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

/**
 * Collection of tests for {@link AiModelCardRepository} exposed methods
 *
 * @author Antoine Meyer <antoine.meyer at nist.gov>
 */
@SpringBootTest(
        classes = { Application.class, SecurityConfig.class },
        properties = { "spring.data.mongodb.port=0", "de.flapdoodle.mongodb.embedded.version=6.0.5"}
)
public class AiModelCardRepositoryTest {

    @Autowired
    AiModelCardRepository aiModelCardRepository;

    AiModelCard aiModelCardA, aiModelCardB;

    @BeforeEach
    public void setUp() {
        // Clear embedded database
        aiModelCardRepository.deleteAll();

        // Setup A
        AiModel aiModelA = new AiModel("AI Model A");
        aiModelA.setOwner("user A");

        Job jobA = new Job();
        jobA.setName("job A");
        Map<String, String> paramsA = new HashMap<String, String>();
        paramsA.put("paramA1", "paramAvalue1");
        jobA.setParameters(paramsA);

        Plugin pluginA = new Plugin();
        pluginA.setName("org/test-plugin-A");
        pluginA.setVersion("1.0.0");

        // Create and save aiModelCardA
        aiModelCardA = new AiModelCard(aiModelA, jobA, pluginA);
        aiModelCardA = aiModelCardRepository.save(aiModelCardA);

        // Setup B
        AiModel aiModelB = new AiModel("AI Model B");

        Job jobB = new Job();
        jobB.setName("job B");
        Map<String, String> paramsB = new HashMap<String, String>();
        paramsB.put("DIRparamB1", "paramBvalue1");
        paramsB.put("paramB2", "paramBvalue2");
        jobB.setParameters(paramsB);

        Plugin pluginB = new Plugin();
        pluginB.setCitation("example-citation");
        pluginB.setOperationType(Arrays.asList("augmentation", "segmentation"));
        PluginIO plugin = new PluginIO();
        plugin.setName("outputDir");
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("architecture", "Unet");
        plugin.setOptions(options);
        List<PluginIO> list = new ArrayList<>();
        list.add(plugin);
        pluginB.setOutputs(list);

        // Create and save aiModelCardB
        aiModelCardB = new AiModelCard(aiModelB, jobB, pluginB);
        aiModelCardB = aiModelCardRepository.save(aiModelCardB);
    }

    @Test
    public void checkSmall() {
        Assertions.assertEquals(aiModelCardA.getName(), "AI Model A");
        Assertions.assertEquals(aiModelCardA.getOwner(), "user A");
        Assertions.assertNull(aiModelCardA.getFramework());
        Assertions.assertNull(aiModelCardA.getOperationType());
        Assertions.assertEquals(aiModelCardA.getArchitecture(), "N/A");
    }

    @Test
    public void checkComplete() {
        Assertions.assertEquals(aiModelCardB.getName(), "AI Model B");
        Assertions.assertEquals(aiModelCardB.getTrainingData().get("DIRparamB1"), "paramBvalue1");
        Assertions.assertEquals(aiModelCardB.getTrainingParameters().get("paramB2"), "paramBvalue2");
        Assertions.assertEquals(aiModelCardB.getCitation(), "example-citation");
        Assertions.assertEquals(aiModelCardB.getOperationType(), Arrays.asList("augmentation", "segmentation"));
        Assertions.assertEquals(aiModelCardB.getArchitecture(), "Unet");
    }

}

