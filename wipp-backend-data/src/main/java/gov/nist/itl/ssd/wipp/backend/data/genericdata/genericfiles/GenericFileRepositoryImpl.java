package gov.nist.itl.ssd.wipp.backend.data.genericdata.genericfiles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at labshare.org>
*/
public class GenericFileRepositoryImpl implements GenericFileRepositoryCustom {
	
   @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void deleteByGenericData(String genericData) {
        mongoTemplate.remove(Query.query(
                Criteria.where("genericData").is(genericData)),
        		GenericFile.class);
    }

    @Override
    public void deleteByGenericDataAndFileName(String genericData,
            String fileName) {
        mongoTemplate.remove(Query.query(
                Criteria.where("genericData").is(genericData)
                .and("fileName").is(fileName)),
        		GenericFile.class);
    }

}
