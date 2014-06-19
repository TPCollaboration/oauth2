package py.com.personal.oa2.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

@Entity
public class Client implements Serializable {
	private static final long serialVersionUID = 1L;
	
/**	 client
     A program that establishes connections for the purpose of sending
     requests.
     http://tools.ietf.org/html/rfc2616#page-9
*/

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id; //valor autogenerado
	
	private String name; //nombre de la app
	 
	private String url; //url de la app a donde se tiene que ser redirigido el cliente

	private String clientId; //id unico del cliente

	@JsonIgnore
	private String secret;

	private String description;

	private Integer exipresIn; //maximo tiempo expiracion del accessToken
	
	private boolean resourceServer; //true si este cliente es un resource server.
	
	@OneToMany
	private List<Resource> resources; //resources que pertenecen a este si es un resource server
	
	@JsonIgnore
	@OneToMany(mappedBy="client")
	private List<UserClientAction> userClientActions;
	
	
	@JsonIgnore
	@ManyToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	private List<Scope> scopes; //scopes que tiene el cliente.
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date blockDate;

	private int blocked; //1=true, 0=false

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	@JsonIgnore
	public int getBlocked() {
		return blocked;
	}
	
	@JsonIgnore
	public void setBlocked(int blocked) {
		this.blocked = blocked;
	}
	
	@JsonProperty
	@Transient
	public boolean isBlocked() {
		if(this.blocked == 1){
			return true;
		}else{
			return false;
		}	
	}
	
	@JsonProperty
	@Transient
	public void setBlocked(boolean blocked) {
		if(blocked){
			this.blocked = 1;
		}else{
			this.blocked = 0;
		}	
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String clientSecret) {
		this.secret = clientSecret;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getExipresIn() {
		return exipresIn;
	}

	public void setExipresIn(Integer exipresIn) {
		this.exipresIn = exipresIn;
	}

	public Date getBlockDate() {
		return blockDate;
	}

	public void setBlockDate(Date blockDate) {
		this.blockDate = blockDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<UserClientAction> getUserClientActions() {
		return userClientActions;
	}

	public void setUserClientActions(List<UserClientAction> clientActions) {
		this.userClientActions = clientActions;
	}

	public List<Scope> getScopes() {
		return scopes;
	}

	public void setScopes(List<Scope> scopes) {
		this.scopes = scopes;
	}

	public boolean isResourceServer() {
		return resourceServer;
	}

	public void setResourceServer(boolean resourceServer) {
		this.resourceServer = resourceServer;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resrouces) {
		this.resources = resrouces;
	}
}