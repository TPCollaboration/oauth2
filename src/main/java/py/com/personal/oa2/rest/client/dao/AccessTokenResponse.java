package py.com.personal.oa2.rest.client.dao;

public class AccessTokenResponse {
	
/** 
	An example successful response:

     HTTP/1.1 200 OK
     Content-Type: application/json;charset=UTF-8
     Cache-Control: no-store
     Pragma: no-cache

     {
       "access_token":"2YotnFZFEjr1zCsicMWpAA",
       "token_type":"example",
       "expires_in":3600,
       "refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA",
       "example_parameter":"example_value"
     }
 */
	
	/** 
	 * REQUIRED.  The access token issued by the authorization server.
	 */
	private String access_token;

	/**
	 * REQUIRED.  The type of the token issued as described in
	 *    Section 7.1.  Value is case insensitive. 
	 */
	private String token_type;
	
	/**
	 * RECOMMENDED.  The lifetime in seconds of the access token.  For
	     example, the value "3600" denotes that the access token will
	     expire in one hour from the time the response was generated.
	     If omitted, the authorization server SHOULD provide the
	     expiration time via other means or document the default value
	 */
	private long expires_in;
	
	/**
	 *  OPTIONAL, if identical to the scope requested by the client,
	     otherwise REQUIRED.  The scope of the access token as described
	     by Section 3.3.
	 */	
	private String scope;

	/**
	 * REQUIRED if the "state" parameter was present in the client
	     authorization request.  The exact value received from the
	     client.
	 */
	private String state;

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getToken_type() {
		return token_type;
	}

	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}

	public long getExpires_in() {
		return expires_in;
	}

	public void setExpires_in(long expires_in) {
		this.expires_in = expires_in;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
}
