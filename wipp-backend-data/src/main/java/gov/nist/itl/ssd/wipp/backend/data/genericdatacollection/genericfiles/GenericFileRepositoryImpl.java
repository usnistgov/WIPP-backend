package gov.nist.itl.ssd.wipp.backend.data.genericdatacollection.genericfiles;

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
    public void deleteByGenericDataCollection(String genericDataCollection) {
        mongoTemplate.remove(Query.query(
                Criteria.where("genericDataCollection").is(genericDataCollection)),
        		GenericFile.class);
    }

    @Override
    public void deleteByGenericDataCollectionAndFileName(String genericDataCollection,
            String fileName) {
        mongoTemplate.remove(Query.query(
                Criteria.where("genericDataCollection").is(genericDataCollection)
                .and("fileName").is(fileName)),
        		GenericFile.class);
    }

}
