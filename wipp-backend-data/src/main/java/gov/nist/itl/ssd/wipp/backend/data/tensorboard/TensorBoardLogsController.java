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
package gov.nist.itl.ssd.wipp.backend.data.tensorboard;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

/**
 * TensorBoard controller
 *
 * @author Antoine Meyer <antoine.meyer at nist.gov>
 */
@RestController
@Tag(name="TensorboardLogs Entity")
@RequestMapping(CoreConfig.BASE_URI + "/tensorboardLogs/{id}")
public class TensorBoardLogsController {

    @Autowired
    CoreConfig config;

    @Autowired
    TensorboardLogsRepository tensorboardLogsRepository;

    /**
     * Download TensorFlow results (through TensorBoard instance) as CSV file
     * @param id
     * @throws IOException
     */
    @RequestMapping(value = "export/csv", method = RequestMethod.GET)
    @PreAuthorize("hasRole('admin') or @aiModelCardSecurity.checkAuthorize(#id, false)")
    public void exportCSV(@PathVariable("id") String id) throws IOException
    {
        // Define
        List<String> tags = Arrays.asList("accuracy", "loss");
        List<String> types = Arrays.asList("test", "train");

        // Get
        Optional<TensorboardLogs> otl = tensorboardLogsRepository.findById(id);
        if(!otl.isPresent()){
            throw new ResourceNotFoundException("TensorboardLogs not found.");
        }
        String run = otl.get().getName();

        // Creation of CSV files
        for (String tag : tags) {
            for(String type : types) {
                URL website = new URL(
                        config.getTensorboardUri() + "/data/plugin/scalars/scalars"
                                + "?tag=" + tag + "&run=" + run
                                + "/" + type + "&format=csv");
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(
                        config.getTensorboardLogsFolder()
                                + "/" + run + "/" + type + "/" + tag + ".csv");
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
        }
    }

    /**
     * Retrieves data from CSV selected by TensorBoardLog id, data type and tag
     * @param id
     * @param type
     * @param tag
     * @return
     * @throws IOException
     */
    @RequestMapping(
            value = "get/csv",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('admin') or @aiModelCardSecurity.checkAuthorize(#id, false)")
    public List<List<String>> getCSV(@PathVariable("id") String id, String type, String tag) throws IOException
    {
        // Get file name
        Optional<TensorboardLogs> otl = tensorboardLogsRepository.findById(id);
        if(!otl.isPresent()){
            throw new ResourceNotFoundException("TensorboardLogs not found.");
        }
        String run = otl.get().getName();

        // Get file content
        String filename = config.getTensorboardLogsFolder() + "/" + run + "/" + type + "/" + tag + ".csv";
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                records.add(Arrays.asList(values));
            }

            // Data returned
            return records;
        } catch (FileNotFoundException fnfe) {
            // No data because no file
            return null;
        }
    }

}
