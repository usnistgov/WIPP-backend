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
package gov.nist.itl.ssd.wipp.backend.data.aimodelcard.bioimageio;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Document
public class BioImageIo {
    /***************** ATTRIBUTE(S) *****************/
    private List<String> type;
    private Authors[] authors;
    private Cite[] cite;
    private String description;
    private String documentation;
    private List<Inputs> inputs;
    private String license;
    private String name;
    private List<Outputs> outputs;
    private Weights weights;
    private String id;
    private String maintainers;
    private Date timestamp;
    private Map<String, String> training_data;
    private Date version;

    /***************** CONSTRUCTOR(S) *****************/
    public BioImageIo(Authors[] aut, Cite[] cit, String des, String lic,
                      String nam, Date ver, List<String> typ) {
        this.authors = aut;
        this.cite = cit;
        this.description = des;
        this.license = lic;
        this.name = nam;
        this.version = ver;
        this.type = typ;
    }

    /***************** METHOD(S) *****************/
    public List<String> getType() {return type;}
    public Authors[] getAuthors() {return authors;}
    public Cite[] getCite() {return cite;}
    public String getDescription() {return description;}
    public String getDocumentation() {return documentation;}
    public List<Inputs> getInputs() {return inputs;}
    public String getLicense() {return license;}
    public String getName() {return name;}
    public List<Outputs> getOutputs() {return outputs;}
    public Weights getWeights() {return weights;}
    public String getId() {return id;}
    public String getMaintainers() {return maintainers;}
    public Date getTimestamp() {return timestamp;}
    public Map<String, String> getTraining_data() {return training_data;}
    public Date getVersion() {return version;}

    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setTraining_data(Map<String, String> td){ this. training_data = td; }
}
