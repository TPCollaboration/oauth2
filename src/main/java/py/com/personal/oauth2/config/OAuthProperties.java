package py.com.personal.oauth2.config;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import py.com.personal.oauth2.data.OAuthRepository;
import py.com.personal.oauth2.model.Configuration;

@Named
@ApplicationScoped
public class OAuthProperties  implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3365635338836105404L;
	
	private Properties properties;
	
	@Inject
	@OAuthRepository
	private EntityManager em;
	
	public static final String version = "v1";	
	public static final String loginFormUrl = "loginFormUrl";
	public static final String MiMundoAPI = "MiMundoAPI";
	public static final String MiMundoWEB = "MiMundoWEB";
	public static final String WebMailUrl = "WebMailUrl";
	public static final String PortalCautivo = "PortalCautivo";
	public static final String LDAPClient = "LDAPClient";
	public static final String AuthenticateUrl = "AuthenticateUrl";
	public static final String buildTimestamp = "build.timestamp";

	public OAuthProperties(){
		super();
	}
	
	@PostConstruct
	public void init(){
		properties = new Properties();
		this.put(LDAPClient,"2dlQe8tWAEw=");
		this.put(PortalCautivo,"4kVfBA==");
		loadProperties();
		loadResources();
		this.properties.put("version", OAuthProperties.version);
	}
	
	//verifies that a property exists and that 
	//the received value corresponds to the key 
	public boolean validateProperty(String key, String value){
		String storedValue = properties.getProperty(key);
		if(storedValue != null && storedValue.equals(value)){
			return true;
		}		
		return false;
	}
	
	public void loadProperties(){
		
		List<Configuration> configurations = 
				em.createQuery("select c from Configuration c")
				.getResultList();
		
		for(Configuration c : configurations){
			this.properties.put(c.getName(), c.getValue());
		}
	}
	
	private void loadResources(){
		put(buildTimestamp, ResourceBundelLocator.getString(buildTimestamp));
	}
	
	public Object get(Object key){
		return this.properties.get(key);
	}
	
	public void put(Object key, Object value){
		this.properties.put(key, value);
	}
		
	public String getProperty(String key){
		return this.properties.getProperty(key);
	}
}
