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
package gov.nist.itl.ssd.wipp.backend.data.csvCollection;

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
 * @author Samia Benjida <samia.benjida at nist.gov>
 */
public class CsvCollectionRepositoryImpl
		implements CsvCollectionRepositoryCustom {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public void updateCsvCaches(String csvCollectionId) {
		MongoCollection<Document> collection = mongoTemplate.getCollection(
				mongoTemplate.getCollectionName(Csv.class));

		List<Document> pipeline = Arrays.asList(
				new Document("$match",
						new Document("csvCollection",
								csvCollectionId)),
				new Document("$group",
						new Document()
								.append("_id", "aggregation")
								.append("numberOfCsvFiles",
										new Document("$sum", 1))
								.append("csvTotalSize",
										new Document("$sum", "$fileSize"))
				));
		AggregateIterable<Document> output = collection.aggregate(pipeline);

		int numberOfCsvFiles = 0;
		long csvTotalSize = 0;
		Iterator<Document> iterator = output.iterator();
		if (iterator.hasNext()) {
			Document dbo = iterator.next();
			numberOfCsvFiles = dbo.getInteger("numberOfCsvFiles");
			csvTotalSize = dbo.getLong("csvTotalSize");
		}
		mongoTemplate.updateFirst(
				Query.query(Criteria.where("id").is(csvCollectionId)),
				new Update()
						.set("numberOfCsvFiles", numberOfCsvFiles)
						.set("csvTotalSize", csvTotalSize),
				CsvCollection.class);
	}

}
