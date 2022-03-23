package gov.nist.itl.ssd.wipp.backend.data.genericdatacollection;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import gov.nist.itl.ssd.wipp.backend.data.genericdatacollection.genericfiles.GenericFile;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at labshare.org>
*/
public class GenericDataCollectionRepositoryImpl implements GenericDataCollectionRepositoryCustom {
	
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public void updateGenericFilesCaches(String genericDataCollectionId) {
		
		MongoCollection<Document> collection = mongoTemplate.getCollection(
				mongoTemplate.getCollectionName(GenericFile.class));

		List<Document> pipeline = Arrays.asList( 
				new Document("$match",
						new Document("genericDataCollection",
								genericDataCollectionId)),
				new Document("$group",
						new Document()
								.append("_id", "aggregation")
								.append("numberOfFiles",
										new Document("$sum", 1))
								.append("fileTotalSize",
										new Document("$sum", "$fileSize"))
				));
		AggregateIterable<Document> output = collection.aggregate(pipeline);

		int numberOfGenericFiles = 0;
		long fileTotalSize = 0;
		Iterator<Document> iterator = output.iterator();
		if (iterator.hasNext()) {
			Document dbo = iterator.next();
			numberOfGenericFiles = dbo.getInteger("numberOfFiles");
			fileTotalSize = dbo.getLong("fileTotalSize");
		}
		mongoTemplate.updateFirst(
				Query.query(Criteria.where("id").is(genericDataCollectionId)),
				new Update()
						.set("numberOfFiles", numberOfGenericFiles)
						.set("fileTotalSize", fileTotalSize),
				GenericDataCollection.class);
		
	}

}
