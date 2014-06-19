package py.com.personal.oa2.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * The persistent class for the Resource database table.
 */
@Entity
public class Resource implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private String name;

	@Column(unique=true)
	private String url;

	// bi-directional many-to-one association to Accion
	@JsonIgnore
	@OneToMany(mappedBy = "resource", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	private List<Action> actions;

	public Resource() {
	}

	public long getId() {
		return this.id;
	}

	public void setId(long recursoId) {
		this.id = recursoId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<Action> getActions() {
		return this.actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	
	@Override
	public String toString() {
		if(this.name != null){
			return this.name + ": " + this.url; 
		}else{
			return this.url;
		}
	}
	
}