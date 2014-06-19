package py.com.personal.oa2.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Configuration {

	@Id
	private String name;
	
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
