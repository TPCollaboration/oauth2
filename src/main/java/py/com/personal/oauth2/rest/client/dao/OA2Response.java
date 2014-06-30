package py.com.personal.oauth2.rest.client.dao;

public class OA2Response {


	private Integer code;
	private String message;

	public OA2Response() {
	}

	public OA2Response(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String mensaje) {
		this.message = mensaje;
	}

}
