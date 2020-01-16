package gov.nist.itl.ssd.wipp.backend.core.rest.authorization;

/**
 * Created by gerardin on 4/6/17.
 */
public interface Owned {

    public String getOwner();

    public void setOwner(String owner);
}
