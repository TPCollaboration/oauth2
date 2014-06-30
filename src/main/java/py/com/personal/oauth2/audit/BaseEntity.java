package py.com.personal.oauth2.audit;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Deprecated
@MappedSuperclass
public class BaseEntity {

	@Temporal(value = TemporalType.TIMESTAMP)
	private Date creationDate;
	
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date lastModificationDate;
	
	@PrePersist
	public void setCreaTionDate(){
		this.creationDate = Calendar.getInstance().getTime();
	}

	@PreUpdate
	public void setLastModificationDate(){
		this.lastModificationDate = Calendar.getInstance().getTime();
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastModificationDate() {
		return lastModificationDate;
	}

	public void setLastModificationDate(Date lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}
}
