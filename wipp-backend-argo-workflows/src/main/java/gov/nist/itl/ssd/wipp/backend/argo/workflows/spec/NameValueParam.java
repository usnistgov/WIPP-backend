package gov.nist.itl.ssd.wipp.backend.argo.workflows.spec;

import com.fasterxml.jackson.annotation.JsonInclude;

public class NameValueParam {
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String value;

    public NameValueParam(String name) {
        this.name = name;
    }

    public NameValueParam(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
