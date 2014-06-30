package py.com.personal.oauth2.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entity implementation class for Entity: UserMigration
 *
 */
@Entity
public class UserMigration implements Serializable {

	
	private static final long serialVersionUID = 1L;

	public UserMigration() {
		super();
	}
	
	@Id
	private String userName;
   
	private int migrated;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date migrationDate;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String username) {
		this.userName = username;
	}

	public int getMigrated() {
		return migrated;
	}

	public void setMigrated(int migrated) {
		this.migrated = migrated;
	}

	public Date getMigrationDate() {
		return migrationDate;
	}

	public void setMigrationDate(Date migrationDate) {
		this.migrationDate = migrationDate;
	}
	
}
