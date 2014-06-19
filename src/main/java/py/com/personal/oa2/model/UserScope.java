package py.com.personal.oa2.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The persistent class for the Rol_Usuario database table.
 */
@Entity
public class UserScope implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private UserScopeId id;

	private int active;

	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date starDate;
	
	// bi-directional many-to-one association to Usuario
	@ManyToOne
	@JoinColumn(name = "userId", insertable = false, updatable = false)
	private OAuser oAuser;

	// bi-directional many-to-one association to RolAplicacion
	@ManyToOne
	@JoinColumn(name = "scopeId", insertable = false, updatable = false)
	private Scope scope;
	
	@ManyToOne
	@JoinColumn(name = "clientid", insertable = false, updatable = false)
	private Client client;

	public UserScope() {
	}

	public UserScopeId getId() {
		return this.id;
	}

	public void setId(UserScopeId id) {
		this.id = id;
	}

	@JsonIgnore
	public int getActive() {
		return active;
	}
	
	@JsonIgnore
	public void setActive(int active) {
		this.active = active;
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

	public Date getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Date fechaFin) {
		this.endDate = fechaFin;
	}

	public Date getStarDate() {
		return this.starDate;
	}

	public void setStarDate(Date fechaInicio) {
		this.starDate = fechaInicio;
	}

	public OAuser getoAuser() {
		return oAuser;
	}

	public void setoAuser(OAuser oAuser) {
		this.oAuser = oAuser;
	}

	public Scope getScope() {
		return this.scope;
	}
	
	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}
}