/*
 * This software was developed at the National Institute of Standards and
 * Technology by employees of the Federal Government in the course of
 * their official duties. Pursuant to title 17 Section 105 of the United
 * States Code this software is not subject to copyright protection and is
 * in the public domain. This software is an experimental system. NIST assumes
 * no responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or
 * any other characteristic. We would appreciate acknowledgement if the
 * software is used.
 */
package gov.nist.itl.ssd.wipp.backend.data.genericdata;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;

import gov.nist.itl.ssd.wipp.backend.data.genericdata.genericfiles.GenericFile;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
*/
public class GenericDataRepositoryImpl implements GenericDataRepositoryCustom{
	
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public void updateGenericFileCaches(String genericDataId) {
		MongoCollection<Document> collection = mongoTemplate.getCollection(
				mongoTemplate.getCollectionName(GenericFile.class));

		List<Document> pipeline = Arrays.asList(
				new Document("$match",
						new Document("genericData",
								genericDataId)),
				new Document("$group",
						new Document()
								.append("_id", "aggregation")
								.append("numberOfFiles",
										new Document("$sum", 1))
								.append("genericFileTotalSize",
										new Document("$sum", "$fileSize"))
				));
		AggregateIterable<Document> output = collection.aggregate(pipeline);

		int numberOfGenericFiles = 0;
		long genericFileTotalSize = 0;
		Iterator<Document> iterator = output.iterator();
		if (iterator.hasNext()) {
			Document dbo = iterator.next();
			numberOfGenericFiles = dbo.getInteger("numberOfFiles");
			genericFileTotalSize = dbo.getLong("genericFileTotalSize");
		}
		mongoTemplate.updateFirst(
				Query.query(Criteria.where("id").is(genericDataId)),
				new Update()
						.set("numberOfFiles", numberOfGenericFiles)
						.set("genericFileTotalSize", genericFileTotalSize),
				GenericData.class);
	}

}
