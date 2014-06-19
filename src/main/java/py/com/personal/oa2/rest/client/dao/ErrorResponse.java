package py.com.personal.oa2.rest.client.dao;

public class ErrorResponse {
/** 
 * 
 *  http://tools.ietf.org/html/rfc6749#section-4.2.2.1
	 error
         REQUIRED.  A single ASCII [USASCII] error code from the
         following:
         invalid_request
               The request is missing a required parameter, includes an
               invalid parameter value, includes a parameter more than
               once, or is otherwise malformed.
         unauthorized_client
               The client is not authorized to request an authorization
               code using this method.
         access_denied
               The resource owner or authorization server denied the
               request.
         unsupported_response_type
               The authorization server does not support obtaining an
               authorization code using this method.
         invalid_scope
               The requested scope is invalid, unknown, or malformed.
         server_error
               The authorization server encountered an unexpected
               condition that prevented it from fulfilling the request.
               (This error code is needed because a 500 Internal Server
               Error HTTP status code cannot be returned to the client
               via a HTTP redirect.)
         temporarily_unavailable
               The authorization server is currently unable to handle
               the request due to a temporary overloading or maintenance
               of the server.  (This error code is needed because a 503
               Service Unavailable HTTP status code cannot be returned
               to the client via a HTTP redirect.)
         Values for the "error" parameter MUST NOT include characters
         outside the set %x20-21 / %x23-5B / %x5D-7E.
   error_description
         OPTIONAL.  A human-readable ASCII [USASCII] text providing
         additional information, used to assist the client developer in
         understanding the error that occurred.
         Values for the "error_description" parameter MUST NOT include
         characters outside the set %x20-21 / %x23-5B / %x5D-7E.
   error_uri
         OPTIONAL.  A URI identifying a human-readable web page with
         information about the error, used to provide the client
         developer with additional information about the error.
         Values for the "error_uri" parameter MUST conform to the URI-
         Reference syntax, and thus MUST NOT include characters outside
         the set %x21 / %x23-5B / %x5D-7E.
   state
         REQUIRED if a "state" parameter was present in the client
         authorization request.  The exact value received from the
         client.
*/
	
	private String error;	
	private String error_description;
	private String error_uri;
	private String state;
	
	public ErrorResponse() {
		super();
		//TODO: remove this constructor
	}
	
	public ErrorResponse(String error) {
		super();
		this.error = error;
	}
	
	public ErrorResponse(String error, String state) {
		super();
		this.error = error;
		this.state = state;
	}
	
	public ErrorResponse(String error, String error_description,
			String error_uri, String state) {
		super();
		this.error = error;
		this.error_description = error_description;
		this.error_uri = error_uri;
		this.state = state;
	}
	
	public String getError() {
		return error;
	}
	
	public void setError(String error) {
		this.error = error;
	}
	
	public String getError_description() {
		return error_description;
	}
	
	public void setError_description(String error_description) {
		this.error_description = error_description;
	}
	
	public String getError_uri() {
		return error_uri;
	}
	
	public void setError_uri(String error_uri) {
		this.error_uri = error_uri;
	}
	
	public String getState() {
		return state;
	}
	
	/**
	 * REQUIRED if a "state" parameter was present in the client
     *    authorization request.  The exact value received from the
     *    client. 
	 * @param state
	 */
	public void setState(String state) {
		this.state = state;
	}
	
	
	public enum Error {
		/**
		 *  The request is missing a required parameter, includes an
	     *         invalid parameter value, includes a parameter more than
	     *        once, or is otherwise malformed.
		 */	
		INVALID_REQUEST("invalid_request"),
		/**
	    *           The client is not authorized to request an authorization
	     *          code using this method.
		 */
		UNAUTHORIZED_CLIENT("unauthorized_client"),
		/**
		 * The resource owner or authorization server denied the
         *      request.
		 */
		
		ACCESS_DENIED("access_denied"),
		
		/**
		 *   The authorization server does not support obtaining an
		          access token using this method.
		 */
		  UNSUPPORTED_RESPONSE_TYPE("unsupported_response_type"),
		      
		  /**
		   * The authorization server encountered an unexpected
          condition that prevented it from fulfilling the request.
          (This error code is needed because a 500 Internal Server
          Error HTTP status code cannot be returned to the client
          via a HTTP redirect.)
          
		   */
          SERVER_ERROR("server_error"),
          
          /**
           *      The authorization server is currently unable to handle
          the request due to a temporary overloading or maintenance
          of the server.  (This error code is needed because a 503
          Service Unavailable HTTP status code cannot be returned
          to the client via a HTTP redirect.)
           */
          TEMPORARILY_UNAVAILABLE("temporarily_unavailable");
     
		
		private final String error;
		
		private Error(String error) {
			this.error = error;
		}
		
		@Override
		public String toString() {
			return error;
		}
	}
}
