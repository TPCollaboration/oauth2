package py.com.personal.oa2.model;

import java.io.Serializable;

public class ExternalAccountId implements Serializable{

	private String id;
	
	private String type;
	
	public ExternalAccountId() {
		super();
	}
	
	public ExternalAccountId(String id, String type) {
		super();
		this.id = id;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ExternalAccountId)) {
			return false;
		}
		ExternalAccountId castOther = (ExternalAccountId) other;
		return (this.id == castOther.id) && (this.type == castOther.type);
	}

    @Override
    public int hashCode() {
        return id.hashCode() + type.hashCode();
    }
 
}
