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
package gov.nist.itl.ssd.wipp.backend.images.imagescollection;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;

import gov.nist.itl.ssd.wipp.backend.images.imagescollection.images.Image;
import gov.nist.itl.ssd.wipp.backend.images.imagescollection.metadatafiles.MetadataFile;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 *
 * @author Antoine Vandecreme
 */
public class ImagesCollectionRepositoryImpl
        implements ImagesCollectionRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void updateImagesCaches(String imagesCollectionId) {
    	MongoCollection<Document> collection = mongoTemplate.getCollection(
                mongoTemplate.getCollectionName(Image.class));
    	
    	List<Document> pipeline = Arrays.asList(
                new Document("$match",
                        new Document("imagesCollection",
                                imagesCollectionId)),
                new Document("$group",
                		new Document()
	                        .append("_id", "aggregation")
	                        .append("numberOfImages", new BasicDBObject("$sum", 1))
	                        .append("imagesTotalSize",
	                                new Document("$sum", "$fileSize"))
	                        .append("numberOfImportingImages",
	                                new Document("$sum",
	                                        new Document("$cond",
	                                                Arrays.asList(
	                                                        "$importing",
	                                                        1,
	                                                        0))))
	                        .append("numberOfImportErrors",
	                                new Document("$sum",
	                                        new Document("$cond",
	                                                Arrays.asList(
	                                                        "$importError",
	                                                        1,
	                                                        0))))
				));
    	AggregateIterable<Document> output = collection.aggregate(pipeline);
        int numberOfImages = 0;
        long imagesTotalSize = 0;
        int numberOfImportingImages = 0;
        int numberOfImportErrors = 0;
        
        Iterator<Document> iterator = output.iterator();
        
        if (iterator.hasNext()) {
        	Document dbo = iterator.next();
            numberOfImages = dbo.getInteger("numberOfImages");
            imagesTotalSize = dbo.getLong("imagesTotalSize");
            numberOfImportingImages = dbo.getInteger("numberOfImportingImages");
            numberOfImportErrors = dbo.getInteger("numberOfImportErrors");
        }
        
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("id").is(imagesCollectionId)),
                new Update()
                .set("numberOfImages", numberOfImages)
                .set("imagesTotalSize", imagesTotalSize)
                .set("numberImportingImages", numberOfImportingImages)
                .set("numberOfImportErrors", numberOfImportErrors),
                ImagesCollection.class);
    }

    @Override
    public void updateMetadataFilesCaches(String imagesCollectionId) {
    	MongoCollection<Document> collection = mongoTemplate.getCollection(
                mongoTemplate.getCollectionName(MetadataFile.class));
    	
    	List<Document> pipeline = Arrays.asList(
                new Document("$match",
                        new Document("imagesCollection",
                                imagesCollectionId)),
                new Document("$group",
                		new Document()
	                		.append("_id", "aggregation")
	                        .append("numberOfMetadataFiles",
	                                new Document("$sum", 1))
	                        .append("metadataFilesTotalSize",
	                                new Document("$sum", "$fileSize"))
                ));
    	AggregateIterable<Document> output = collection.aggregate(pipeline);
        
    	int numberOfMetadataFiles = 0;
        long metadataFilesTotalSize = 0;
        Iterator<Document> iterator = output.iterator();
        if (iterator.hasNext()) {
        	Document dbo = iterator.next();
            numberOfMetadataFiles = dbo.getInteger("numberOfMetadataFiles");
            metadataFilesTotalSize = dbo.getLong("metadataFilesTotalSize");
        }
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("id").is(imagesCollectionId)),
                new Update()
                .set("numberOfMetadataFiles", numberOfMetadataFiles)
                .set("metadataFilesTotalSize", metadataFilesTotalSize),
                ImagesCollection.class);
    }

}
