package gov.nist.itl.ssd.wipp.backend.data.genericdata;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import gov.nist.itl.ssd.wipp.backend.data.csvCollection.csv.Csv;
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
public class GenericDataRepositoryImpl implements GenericDataRepositoryCustom {
	
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public void updateGenericFilesCaches(String genericDataId) {
		
		MongoCollection<Document> collection = mongoTemplate.getCollection(
				mongoTemplate.getCollectionName(Csv.class));

		List<Document> pipeline = Arrays.asList( 
				new Document("$match",
						new Document("genericData",
								genericDataId)),
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
		long totalSize = 0;
		Iterator<Document> iterator = output.iterator();
		if (iterator.hasNext()) {
			Document dbo = iterator.next();
			numberOfGenericFiles = dbo.getInteger("numberOfFiles");
			totalSize = dbo.getLong("totalSize");
		}
		mongoTemplate.updateFirst(
				Query.query(Criteria.where("id").is(genericDataId)),
				new Update()
						.set("numberOfFiles", numberOfGenericFiles)
						.set("totalSize", totalSize),
				GenericData.class);
		
	}

}
