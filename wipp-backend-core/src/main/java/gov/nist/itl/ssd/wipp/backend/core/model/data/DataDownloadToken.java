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
package gov.nist.itl.ssd.wipp.backend.core.model.data;

import java.util.Date;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Random token for data download URL generation
 * Automatically expires after 1 hour
 * 
 * @author Mylene Simon <mylene.simon at nist.gov>
 *
 */
@Document
public class DataDownloadToken {

	@Id
    private String id;
	
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@Indexed(name="tokenCreationDateIndex", expireAfterSeconds=3600)
    private Date creationDate;
	
	private String dataId;
	
	private String token;
	
	public DataDownloadToken(String dataId) {
		this.creationDate = new Date();	
		this.dataId = dataId;
		this.token = UUID.randomUUID().toString();
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public String getDataId() {
		return dataId;
	}

	public String getToken() {
		return token;
	}

	public String getId() {
		return id;
	}
}
