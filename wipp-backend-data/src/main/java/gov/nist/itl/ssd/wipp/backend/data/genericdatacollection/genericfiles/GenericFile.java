package gov.nist.itl.ssd.wipp.backend.data.genericdatacollection.genericfiles;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.ManualRef;
import gov.nist.itl.ssd.wipp.backend.data.genericdatacollection.GenericDataCollection;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at labshare.org>
*/


@Document
@CompoundIndexes({
	@CompoundIndex(
			name = "collection_filename",
			def = "{'genericDataCollection': 1, 'fileName': 1}",
			unique = true)
})
public class GenericFile {

	@Id
	@JsonIgnore
	private String id;

	@Indexed
	@ManualRef(GenericDataCollection.class)
	private String genericDataCollection;

	private String fileName;

	private String originalFileName;

	private long fileSize;

	private boolean importing;

	private String importError;
	
    
	public GenericFile() {
    }
	
	public GenericFile(String genericDataCollection, String fileName, String originalFileName, long fileSize,
			boolean isImporting) {
		this(genericDataCollection, fileName, originalFileName, fileSize, isImporting, null);
	}

	public GenericFile(String genericDataCollection, String fileName, String originalFileName, long fileSize,
			boolean isImporting, String importError) {
		this.genericDataCollection = genericDataCollection;
		this.fileName = fileName;
		this.originalFileName = originalFileName;
		this.fileSize = fileSize;
		this.importing = isImporting;
		this.importError = importError;
	}
    
    public String getId() {
        return id;
    }
    
    @JsonIgnore
    public String getGenericDataCollection() {
        return genericDataCollection;
    }

    public String getFileName() {
        return fileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isImporting() {
        return importing;
    }

    public String getImportError() {
        return importError;
    }

    public void setImporting(boolean importing) {
        this.importing = importing;
    }

    public void setImportError(String importError) {
        this.importError = importError;
    }
	
}
