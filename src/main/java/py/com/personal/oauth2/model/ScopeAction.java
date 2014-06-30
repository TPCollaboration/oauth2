package py.com.personal.oauth2.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Relaciona un Scope con un Action.
 * Tiene fecha de inicio y fin para casos especiales donde se le quiere
 * dar un permiso temporal a un rol. 
 * 
 */
@Entity
public class ScopeAction implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private ScopeActionId id;

	private int active;

	private Timestamp endDate;

	private Timestamp startDate;

	// bi-directional many-to-one association to Action
	@ManyToOne
	@JoinColumn(name = "actionid", insertable = false, updatable = false)
	private Action action;

	// bi-directional many-to-one association to clientScope
	@ManyToOne
	@JoinColumn(name = "scopeid", insertable = false, updatable = false)
	private Scope scope;

	public ScopeAction() {
	}

	public ScopeActionId getId() {
		return this.id;
	}

	public void setId(ScopeActionId id) {
		this.id = id;
	}

	public int getActive() {
		return this.active;
	}

	public void setActive(int estado) {
		this.active = estado;
	}
	
	@JsonProperty
	@Transient
	public boolean isActive() {
		if(this.active == 1){
			return true;
		}else{
			return false;
		}	
	}
	
	@JsonProperty
	@Transient
	public void setActive(boolean active) {
		if(active){
			this.active = 1;
		}else{
			this.active = 0;
		}	
	}

	public Timestamp getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Timestamp fechaFin) {
		this.endDate = fechaFin;
	}

	public Timestamp getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Timestamp fechaInicio) {
		this.startDate = fechaInicio;
	}

	public Action getAction() {
		return this.action;
	}

	public void setAction(Action accion) {
		this.action = accion;
	}

	public Scope getScope() {
		return this.scope;
	}

	public void setScope(Scope rolAplicacion) {
		this.scope = rolAplicacion;
	}

}