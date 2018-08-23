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

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import gov.nist.itl.ssd.wipp.backend.images.imagescollection.images.Image;
import gov.nist.itl.ssd.wipp.backend.images.imagescollection.metadatafiles.MetadataFile;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
        DBCollection collection = mongoTemplate.getCollection(
                mongoTemplate.getCollectionName(Image.class));
        List<DBObject> pipeline = Arrays.asList(
                new BasicDBObject("$match",
                        new BasicDBObject("imagesCollection",
                                imagesCollectionId)),
                new BasicDBObject("$group",
                        BasicDBObjectBuilder.start()
                        .add("_id", "aggregation")
                        .add("numberOfImages", new BasicDBObject("$sum", 1))
                        .add("imagesTotalSize",
                                new BasicDBObject("$sum", "$fileSize"))
                        .add("numberOfImportingImages",
                                new BasicDBObject("$sum",
                                        new BasicDBObject("$cond",
                                                Arrays.asList(
                                                        "$importing",
                                                        1,
                                                        0))))
                        .add("numberOfImportErrors",
                                new BasicDBObject("$sum",
                                        new BasicDBObject("$cond",
                                                Arrays.asList(
                                                        "$importError",
                                                        1,
                                                        0))))
                        .get()
                ));
        AggregationOutput output = collection.aggregate(pipeline);
        int numberOfImages = 0;
        long imagesTotalSize = 0;
        int numberOfImportingImages = 0;
        int numberOfImportErrors = 0;
        Iterator<DBObject> iterator = output.results().iterator();
        if (iterator.hasNext()) {
            DBObject dbo = iterator.next();
            numberOfImages = (int) dbo.get("numberOfImages");
            imagesTotalSize = (long) dbo.get("imagesTotalSize");
            numberOfImportingImages = (int) dbo.get("numberOfImportingImages");
            numberOfImportErrors = (int) dbo.get("numberOfImportErrors");
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
        DBCollection collection = mongoTemplate.getCollection(
                mongoTemplate.getCollectionName(MetadataFile.class));
        List<DBObject> pipeline = Arrays.asList(
                new BasicDBObject("$match",
                        new BasicDBObject("imagesCollection",
                                imagesCollectionId)),
                new BasicDBObject("$group",
                        BasicDBObjectBuilder.start()
                        .add("_id", "aggregation")
                        .add("numberOfMetadataFiles",
                                new BasicDBObject("$sum", 1))
                        .add("metadataFilesTotalSize",
                                new BasicDBObject("$sum", "$fileSize"))
                        .get()
                ));
        AggregationOutput output = collection.aggregate(pipeline);
        int numberOfMetadataFiles = 0;
        long metadataFilesTotalSize = 0;
        Iterator<DBObject> iterator = output.results().iterator();
        if (iterator.hasNext()) {
            DBObject dbo = iterator.next();
            numberOfMetadataFiles = (int) dbo.get("numberOfMetadataFiles");
            metadataFilesTotalSize = (long) dbo.get("metadataFilesTotalSize");
        }
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("id").is(imagesCollectionId)),
                new Update()
                .set("numberOfMetadataFiles", numberOfMetadataFiles)
                .set("metadataFilesTotalSize", metadataFilesTotalSize),
                ImagesCollection.class);
    }

}
