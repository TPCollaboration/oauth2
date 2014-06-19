package py.com.personal.oa2.rest.client.dao;

import java.util.Date;
import java.util.List;


public interface AccessInterface {

	public String getUserName();

	public void setUserName(String name);
	
	public String getMail();

	public void setMail(String mail);
	
	public Long getCount();

	public void setCount(Long count);

	public Date getLastLogin();

	public void setLastLogin(Date ultimoLogin);

	public String getLastIp();

	public void setLastIp(String ultimoIp);
	
	public List<String> getScopes();

	public void setScopes(List<String> scopes);

}
