package py.com.personal.oauth2.model;

import java.io.Serializable;

import javax.persistence.Embeddable;

/**
 * The primary key class for the Rol_Accion database table.
 */
@Embeddable
public class ScopeActionId implements Serializable {

	// default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	private long scopeId;

	private long actionId;

	public ScopeActionId() {
	}

	public long getScopeId() {
		return this.scopeId;
	}

	public void setScopeId(long rolId) {
		this.scopeId = rolId;
	}

	public long getActionId() {
		return this.actionId;
	}

	public void setActionId(long actionId) {
		this.actionId = actionId;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ScopeActionId)) {
			return false;
		}
		ScopeActionId castOther = (ScopeActionId) other;
		return (this.scopeId == castOther.scopeId) && (this.actionId == castOther.actionId);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + ((int) (this.scopeId ^ (this.scopeId >>> 32)));
		hash = hash * prime + ((int) (this.actionId ^ (this.actionId >>> 32)));
		return hash;
	}
}