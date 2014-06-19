package py.com.personal.oa2.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The persistent class for the Accion database table.
 */
@Entity
public class Action implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(unique=true)
	private String name;

	private int active;
	
	private String httpMethod;
	
	private String url;
	
	private int publicAction; //determina si la accion se entra dentro del esquema de seguridad

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "resourceId")
	private Resource resource;

	// bi-directional many-to-one association to RolAccion
	@JsonIgnore
	@OneToMany(mappedBy = "action")
	private List<ScopeAction> scopeActions; //las acciones que se pueden ejecutar dentro de ese scope

	public Action() {
	}

	public long getId() {
		return this.id;
	}

	public void setId(long accionId) {
		this.id = accionId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Resource getResource() {
		return this.resource;
	}

	public void setResource(Resource recurso) {
		this.resource = recurso;
	}

	public List<ScopeAction> getScopeActions() {
		return this.scopeActions;
	}

	public void setScopeActions(List<ScopeAction> rolAcciones) {
		this.scopeActions = rolAcciones;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String metodoHttp) {
		this.httpMethod = metodoHttp;
	}

	public int isPublic() {
		return publicAction;
	}

	public void setPublic(int publicAction) {
		this.publicAction = publicAction;
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
	
}