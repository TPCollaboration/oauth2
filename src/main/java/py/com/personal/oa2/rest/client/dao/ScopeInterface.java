package py.com.personal.oa2.rest.client.dao;

import java.math.BigInteger;


public interface ScopeInterface {

	public long getId();
	
	public void setId(long id);

	public String getName();

	public void setName(String name);

	public Boolean isActive();

	public void setActive(Boolean active);

}