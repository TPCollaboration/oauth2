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
 * Permission to execute an action, granted by the resource owner(end oAuser), to a client.
 *
 */
@Entity
public class UserClientAction implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public UserClientAction() {
		super();
	}
	
	public UserClientAction(UserClientActionId id, Client client, int active) {
		super();
		this.id = id;
		this.client = client;
		this.active = active;
	}
	
	@EmbeddedId
	private UserClientActionId id;

	@ManyToOne
	@JoinColumn(name="clientid", insertable = false, updatable = false)
	private Client client;
	
	@ManyToOne
	@JoinColumn(name="actionid",insertable = false, updatable = false)
	private Action action;

	@ManyToOne
	@JoinColumn(name="userid", insertable = false, updatable = false)
	private OAuser oAuser;

	private int active;

	@Temporal(TemporalType.TIMESTAMP)
	private Date endDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date starDate;

	public UserClientActionId getId() {
		return id;
	}

	public void setId(UserClientActionId id) {
		this.id = id;
	}
	
	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}
	
	@JsonIgnore
	public OAuser getUser() {
		return oAuser;
	}

	public void setUser(OAuser oAuser) {
		this.oAuser = oAuser;
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

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getStarDate() {
		return starDate;
	}

	public void setStarDate(Date starDate) {
		this.starDate = starDate;
	}

}
