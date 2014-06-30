package py.com.personal.oauth2.model;

import java.io.Serializable;

import javax.persistence.Embeddable;

/**
 * The primary key class for the Aplicacion_Usuario database table.
 */
@Embeddable
public class ClientUserId implements Serializable {

	// default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	public ClientUserId() {
		super();
	}
	
	public ClientUserId(long client, long oAuser) {
		super();
		this.client = client;
		this.oAuser = oAuser;
	}

	private long client;
	
	private long oAuser;
	
	public long getClient() {
		return client;
	}

	public void setClient(long client) {
		this.client = client;
	}

	public long getUserId() {
		return oAuser;
	}

	public void setUser(long user) {
		this.oAuser = user;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ClientUserId)) {
			return false;
		}
		ClientUserId castOther = (ClientUserId) other;
		return (this.oAuser == castOther.oAuser) && (this.client == castOther.client);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + ((int) (this.client ^ (this.client >>> 32)));
		hash = hash * prime + ((int) (this.oAuser ^ (this.oAuser >>> 32)));

		return hash;
	}

}
