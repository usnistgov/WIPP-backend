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
package gov.nist.itl.ssd.wipp.backend.data.aimodelcard.huggingface;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document
public class HuggingFace {
    private String model_id;
    private String model_summary;
    private String model_description;
    private String developers;
    private List<String> model_type;
    private String language;
    private String license;
    private String direct_use;
    private String out_of_scope_use;
    private String bias_risks_limitations;
    private String bias_recommendations;
    private String get_started_code;
    private Map<String, String> training_data;
    private String training_regime;
    private String testing_data;
    private String testing_factors;
    private Map<String, Float> testing_metrics;
    private String results;
    private String results_summary;
    private String citation_bibtex;
    private String model_card_authors;
    private String model_card_contact;

    public HuggingFace(String mi, String md, String li, String cb, String mca) {
        this.model_id = mi;
        this.model_description = md;
        this.license = li;
        this.citation_bibtex = cb;
        this.model_card_authors = mca;
    }

    public String getModel_id() { return model_id; }
    public String getModel_summary() { return model_summary; }
    public String getModel_description() { return model_description; }
    public String getDevelopers() {return developers;}
    public List<String> getModel_type() { return model_type; }
    public String getLanguage() { return language; }
    public String getLicense() {return license;}
    public String getDirect_use() {return direct_use;}
    public String getOut_of_scope_use() {return out_of_scope_use;}
    public String getBias_risks_limitations() {return bias_risks_limitations;}
    public String getBias_recommendations() {return bias_recommendations;}
    public String getGet_started_code() {return get_started_code;}
    public Map<String, String> getTraining_data() {return training_data;}
    public String getTraining_regime() { return training_regime; }
    public String getTesting_data() { return testing_data; }
    public String getTesting_factors() { return testing_factors; }
    public Map<String, Float> getTesting_metrics() { return testing_metrics; }
    public String getResults() { return results; }
    public String getResults_summary() { return results_summary; }
    public String getCitation_bibtex() { return citation_bibtex; }
    public String getModel_card_authors() { return model_card_authors; }
    public String getModel_card_contact() { return model_card_contact; }

    public void setModel_type(List<String> model_type) { this.model_type = model_type; }
    public void setTraining_data(Map<String, String> td) { this.training_data = td; }
    public void setTesting_metrics(Map<String, Float> tm) { this.testing_metrics = tm; }
}
