package py.com.personal.oauth2.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

import py.com.personal.oauth2.rest.client.dao.OAuserInterface;

/**
 * The persistent class for the Usuario database table.
 */
@Entity
@Table(name="oauser")
@JsonAutoDetect({JsonMethod.NONE})
public class OAuser implements OAuserInterface, Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private int blocked; //1=true, 0=false
	private boolean active;

	@Temporal(TemporalType.TIMESTAMP)
	private Date blockDate;
	
	private long attempts; //cantidad de intentos
	
	private String mail;
	
	private String oneTimePin;

	@Column(unique=true)
	private String name; //user name
	
	private String fullName;

	private String secret;

	private String salt; //utilizado para modificar el password
	
	@JsonIgnore
	@OneToMany(mappedBy = "oAuser",cascade=CascadeType.REMOVE)
	private List<UserScope> userScopes;
	
    @JsonIgnore
	@OneToMany(mappedBy = "oAuser",cascade=CascadeType.REMOVE)
	private List<Session> sessions;

	@JsonIgnore
	@OneToMany(mappedBy = "oAuser",cascade=CascadeType.ALL)
	private List<UserClientAction> clientActions;
	
	@JsonIgnore
	@OneToMany(mappedBy = "oAuser",cascade=CascadeType.REMOVE)
	private List<ExternalAccount> externalAccounts; 
	
	@JsonProperty
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getBlocked() {
		return blocked;
	}
	
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
	
	@JsonProperty
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@JsonIgnore
	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	@JsonIgnore
	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	@JsonProperty
	public Date getBlockDate() {
		return blockDate;
	}

	public void setBlockDate(Date blockDate) {
		this.blockDate = blockDate;
	}

	@JsonProperty
	public long getAttempts() {
		return attempts;
	}

	public void setAttempts(long attempts) {
		this.attempts = attempts;
	}
	
    @JsonProperty
	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

    @JsonProperty
	public String getOneTimePin() {
		return oneTimePin;
	}

	public void setOneTimePin(String oneTimePin) {
		this.oneTimePin = oneTimePin;
	}

    @JsonProperty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@JsonIgnore
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public List<UserScope> getUserScopes() {
		return userScopes;
	}

	public void setUserScopes(List<UserScope> userScopes) {
		this.userScopes = userScopes;
	}

	public List<Session> getSessions() {
		return sessions;
	}

	public void setSessions(List<Session> sessions) {
		this.sessions = sessions;
	}

	public List<UserClientAction> getClientActions() {
		return clientActions;
	}

	public void setClientActions(List<UserClientAction> clientActions) {
		this.clientActions = clientActions;
	}

	public List<ExternalAccount> getExternalAccounts() {
		return externalAccounts;
	}

	public void setExternalAccounts(List<ExternalAccount> externalAccounts) {
		this.externalAccounts = externalAccounts;
	}
}
