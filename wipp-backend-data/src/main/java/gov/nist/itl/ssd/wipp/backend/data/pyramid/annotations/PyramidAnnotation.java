package gov.nist.itl.ssd.wipp.backend.data.pyramid.annotations;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;

@Document
@IdExposed
public class PyramidAnnotation {
	
    @Id
    private String id;
    
	private final String name;
	
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date creationDate;

    public PyramidAnnotation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
