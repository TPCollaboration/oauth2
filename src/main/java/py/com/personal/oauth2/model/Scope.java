package py.com.personal.oauth2.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.codehaus.jackson.annotate.JsonIgnore;

import py.com.personal.oauth2.rest.client.dao.ScopeInterface;

/**
 * http://tools.ietf.org/html/rfc6749#section-3.3
 */
@Entity
public class Scope implements ScopeInterface, Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private String name;

	private Boolean active;

	@JsonIgnore
	@OneToMany(mappedBy="scope", cascade=CascadeType.ALL)
	private List<ScopeAction> scopeActions;
		
	public Scope() {
	}

	public long getId() {
		return this.id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String description) {
		this.name = description;
	}

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean status) {
		this.active = status;
	}

	public List<ScopeAction> getScopeActions() {
		return scopeActions;
	}

	public void setScopeActions(List<ScopeAction> scopeActions) {
		this.scopeActions = scopeActions;
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
}