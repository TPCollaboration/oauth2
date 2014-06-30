package py.com.personal.oauth2.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;

@Entity
@IdClass(value=ExternalAccountId.class)
public class ExternalAccount {

	@Id
	private String id;
	
	@Id
	private String type;
	
	@ManyToOne
	private OAuser oAuser;
	
	private String token;
	
	public ExternalAccount() {
		super();
	}
	
	public ExternalAccount(String id, String type, OAuser oAuser) {
		super();
		this.id = id;
		this.type = type;
		this.oAuser = oAuser;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public OAuser getoAuser() {
		return oAuser;
	}

	public void setoAuser(OAuser oAuser) {
		this.oAuser = oAuser;
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * add new external account types here: twitter, google, etc.
	 * 
	 * @author florense
	 *
	 */
	public enum TYPES {
		facebook ("facebook"),
		webmail ("webmail");

	    private final String name;       

	    private TYPES(String s) {
	        name = s;
	    }

	    public boolean equalsName(String otherName){
	        return (otherName == null)? false:name.equals(otherName);
	    }

	    public String toString(){
	       return name;
	    }

	}
}
