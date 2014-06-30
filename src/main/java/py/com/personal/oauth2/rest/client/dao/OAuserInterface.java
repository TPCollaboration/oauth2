package py.com.personal.oauth2.rest.client.dao;

import java.util.Date;


public interface OAuserInterface {

	public long getId();

	public void setId(long id);

	public boolean isBlocked();

	public void setBlocked(boolean blocked);
	
	public boolean isActive();

	public void setActive(boolean active);

	public Date getBlockDate();

	public void setBlockDate(Date blockDate);

	public long getAttempts();

	public void setAttempts(long attempts);
	
	public String getMail();

	public void setMail(String mail);

	public String getOneTimePin();

	public void setOneTimePin(String oneTimePin);

	public String getName();

	public void setName(String name);
}
