package py.com.personal.oauth2.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;

@Entity
public class Access implements Serializable {
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	@EmbeddedId
	private ClientUserId id;

	@Transient
	private String userName; //user name
	
	@Transient
	private String mail;
	
	private Long count;

	@Temporal(TemporalType.TIMESTAMP)
	private Date lastLogin;

	private String lastIp;

	@Transient
	private List<String> scopes;
	
	public ClientUserId getId() {
		return id;
	}

	public void setId(ClientUserId id) {
		this.id = id;
	}
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String name) {
		this.userName = name;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date ultimoLogin) {
		this.lastLogin = ultimoLogin;
	}

	public String getLastIp() {
		return lastIp;
	}

	public void setLastIp(String ultimoIp) {
		this.lastIp = ultimoIp;
	}

	public List<String> getScopes() {
		return scopes;
	}

	public void setScopes(List<String> scopes) {
		this.scopes = scopes;
	}

}
