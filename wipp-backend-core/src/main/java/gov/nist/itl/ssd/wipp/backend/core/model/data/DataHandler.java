package gov.nist.itl.ssd.wipp.backend.core.model.data;

        import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;


public abstract class DataHandler {

    public abstract void importData(Job job, String outputName) throws Exception;

    public abstract String exportDataAsParam(String value);
    //TODO: handle chained jobs and collection validity check
}
