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

package gov.nist.itl.ssd.wipp.backend.data.imagescollection.tags;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Samia Benjida <samia.benjida at nist.gov>
 */

@Document
public class Tag {

    @Id
    private String id;

    @Indexed(unique = false, sparse = true)
    private String tagName;

    public Tag() {
    }

    public Tag(String id, String tagName) {
        this.tagName = tagName;
        this.id = id;
    }

    public String getTagName() {
        return tagName;
    }


    public String getId() {
        return id;
    }

}
