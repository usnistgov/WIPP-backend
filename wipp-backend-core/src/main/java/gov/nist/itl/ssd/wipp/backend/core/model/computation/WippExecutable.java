package gov.nist.itl.ssd.wipp.backend.core.model.computation;

import org.springframework.data.mongodb.core.mapping.Document;

import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;

@IdExposed
@Document(collection = "executable")
public abstract class WippExecutable {

	private String name;
	
	private String version;
	
	public WippExecutable() {
	}
	
	public WippExecutable(String name, String version) {
		this.name = name;
		this.version = version;
	}

	public String getIdentifier() {
		return name + "-" + version;
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

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof WippExecutable))
			return false;

		WippExecutable otherExecutable = (WippExecutable) other;
		if (!otherExecutable.getName().equals(this.getName()))
			return false;
		if (!otherExecutable.getVersion().equals(this.getVersion()))
			return false;

		return true;
	}
}
