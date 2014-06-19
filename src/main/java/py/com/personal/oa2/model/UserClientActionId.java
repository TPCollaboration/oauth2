package py.com.personal.oa2.model;

import java.io.Serializable;

import javax.persistence.Embeddable;


@Embeddable
public class UserClientActionId implements Serializable {

	// default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	private long userId;
	
	private long clientId;

	private long actionId;
	

	public UserClientActionId() {
	}

	public UserClientActionId(long userId, long clientId, long actionId) {
		super();
		this.userId = userId;
		this.clientId = clientId;
		this.actionId = actionId;
	}



	public long getClientId() {
		return this.clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public long getActionId() {
		return this.actionId;
	}

	public void setActionId(long scopeId) {
		this.actionId = scopeId;
	}
	
	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof UserClientActionId)) {
			return false;
		}
		UserClientActionId castOther = (UserClientActionId) other;
		return (this.clientId == castOther.clientId) && (this.actionId == castOther.actionId)
		&& (this.userId == castOther.userId);
	}
	
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + ((int) (this.clientId ^ (this.clientId >>> 32)));
		hash = hash * prime + ((int) (this.actionId ^ (this.actionId >>> 32)));
		hash = hash * prime + ((int) (this.userId ^ (this.userId >>> 32)));
		
		return hash;
	}
}