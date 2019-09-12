package gov.nist.itl.ssd.wipp.backend.core.model.computation;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

@IdExposed
@Document(collection = "computation")
@CompoundIndexes({
    @CompoundIndex(
        def = "{'name': 1, 'version': 1}",
        name = "unique_name",
        unique = true
    )
})
public abstract class Computation {
	@Id
	private String id;

	private String name;
	
	private String version;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private Date creationDate = new Date();
	
	public Computation() {
	}
	
	public Computation(String name, String version) {
		this.name = name;
		this.version = version;
	}

	@JsonIgnore
	public String getIdentifier() {
		String identifier = name + "-" + version;
		return identifier.toLowerCase().replaceAll("[^a-z0-9\\-]", "-");
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof Computation))
			return false;

		Computation otherExecutable = (Computation) other;
		if (!otherExecutable.getName().equals(this.getName()))
			return false;
		if (!otherExecutable.getVersion().equals(this.getVersion()))
			return false;

		return true;
	}
}
