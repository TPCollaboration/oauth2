package py.com.personal.oauth2.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The persistent class for the Sesion_Usuario database table.
 * 
 */
@Entity
public class Session implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String accessToken;

	private String redirectUrl;
	
	private String remoteAddr;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;//time when the session started

	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime; //time when the session should end.
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date finalization; //time when the session was terminated.
	
	@ManyToOne
	private OAuser oAuser;
	
	private int active;
	
	@ManyToMany
	private List<Scope> scopes;//list of scopes associated with this session, normally inherited from the user.
	
	@ManyToOne
	private Client client;

	public Session() {
	}

	public String getAccessToken() {
		return this.accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}	

	public String getRedirectUrl() {
		return this.redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	public Date getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	public Date getEndTime() {
		return this.endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	public Date getFinalization() {
		return finalization;
	}

	public void setFinalization(Date finalization) {
		this.finalization = finalization;
	}

	public OAuser getUser() {
		return oAuser;
	}

	public void setUser(OAuser oAuser) {
		this.oAuser = oAuser;
	}
	
	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public String getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
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

	public List<Scope> getScopes() {
		return scopes;
	}

	public void setScopes(List<Scope> scopes) {
		this.scopes = scopes;
	}
	
	@Override
	public String toString() {
		String scopes = "";		
		Scope scope = null;
		Iterator<Scope> iter = this.scopes.iterator();
		while(iter.hasNext()){
			scope = iter.next();
			scopes = scopes.concat(scope.getName());
			scopes = scopes.concat(" ");
		}
		if(scopes != null && scopes.length()>0){
			scopes = scopes.substring(0, scopes.lastIndexOf(" "));
			scopes = "["+ scopes + "]";
		}
		return scopes;
	}
}
