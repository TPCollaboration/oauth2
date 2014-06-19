package py.com.personal.oa2.model;

import java.io.Serializable;

import javax.persistence.Embeddable;


@Embeddable
public class UserScopeId implements Serializable {

	// default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	private long userId;

	private long scopeId;
	
	private long clientId;

	public UserScopeId() {
	}

	public long getUserId() {
		return this.userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getScopeId() {
		return this.scopeId;
	}

	public void setScopeId(long scopeId) {
		this.scopeId = scopeId;
	}
	
	public long getClientId() {
		return clientId;
	}

	public void setClientId(long clientId) {
		this.clientId = clientId;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof UserScopeId)) {
			return false;
		}
		UserScopeId castOther = (UserScopeId) other;
		return (this.userId == castOther.userId) && (this.scopeId == castOther.scopeId)
				&& (this.clientId == castOther.clientId);
	}
	
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + ((int) (this.userId ^ (this.userId >>> 32)));
		hash = hash * prime + ((int) (this.scopeId ^ (this.scopeId >>> 32)));
		hash = hash * prime + ((int) (this.clientId ^ (this.clientId >>> 32)));
		
		return hash;
	}

}