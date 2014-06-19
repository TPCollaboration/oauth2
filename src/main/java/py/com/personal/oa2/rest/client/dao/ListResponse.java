package py.com.personal.oa2.rest.client.dao;

import java.util.List;

public class ListResponse<T> {
	
	private List<T> data;
	
	private int first;
	
	private int limit;
	
	public ListResponse() {

	}

	public ListResponse(List<T> data, int first, int limit) {
		super();
		this.data = data;
		this.first = first;
		this.limit = limit;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public int getFirst() {
		return first;
	}

	public void setFirst(int first) {
		this.first = first;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}
}
