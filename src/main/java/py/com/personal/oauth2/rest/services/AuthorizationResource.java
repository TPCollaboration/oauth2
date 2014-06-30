package py.com.personal.oauth2.rest.services;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.spi.InternalServerErrorException;

import py.com.personal.oauth2.client.facebook.FacebookClient;
import py.com.personal.oauth2.config.OAuthProperties;
import py.com.personal.oauth2.data.OAuthDataAccess;
import py.com.personal.oauth2.data.OAuthRepository;
import py.com.personal.oauth2.model.Client;
import py.com.personal.oauth2.model.ExternalAccount;
import py.com.personal.oauth2.model.ExternalAccountId;
import py.com.personal.oauth2.model.OAuser;
import py.com.personal.oauth2.model.Scope;
import py.com.personal.oauth2.rest.client.AuthrizationInterface;
import py.com.personal.oauth2.rest.client.dao.AccessTokenResponse;
import py.com.personal.oauth2.rest.client.dao.ErrorResponse;
import py.com.personal.oauth2.rest.client.dao.OA2Response;
import py.com.personal.oauth2.rest.client.dao.OA2Status;
import py.com.personal.oauth2.rest.client.dao.ErrorResponse.Error;
import py.com.personal.oauth2.security.SessionHandler;
import py.com.personal.oauth2.util.OA2Logger;
import py.com.personal.oauth2.util.TokenGenerator;
import py.com.personal.oauth2.util.Utils;

import com.restfb.exception.FacebookOAuthException;


/**
 * <p>This class manages the Oauth 2 authentication flows.</p>
 * It is fully compliant with Oauth 2.0 specs
 * 
 * For more information see {@link URL http://tools.ietf.org/html/rfc6749}
 * 
 * @author florense
 *
 */

public class AuthorizationResource implements AuthrizationInterface {

	private static final String IMPLICIT_GRANT = "implicit";
	
	private static final String CLIENT_CREDENTIALS_GRANT = "client_credentials";
	
	private static final String  MSISDN_GRANT = "msisdn";
	
	private static final String  LDAP_GRANT = "ldap";
	
	private static final String FACEBOOK_GRANT = "facebook";
	
	//maximum attempts for failed logins.
	private static final int MAX_ATTEMPTS = 3;

	private static final String USER_BLOCKED_ERROR = "Usuario bloqueado.";

	private static final String LOGIN_FAILED_ERROR = "Error de autenticacion.";
	
	private static final String INTERNAL_SERVER_ERROR = "Error interno del servidor.";
	
	@Inject
	private TokenGenerator tokenGenerator;
	
	@Inject
	private OAuthDataAccess dataAccess;
	
	@Inject
	private SessionHandler sessionHandler;
	
	@Inject
	private OAuthProperties oAuthProperties;
	
	@Inject
	@OAuthRepository
	private EntityManager em;
	
	@Context
	private HttpServletRequest httpRequest;
	
	@Inject
	protected UserTransaction utx;

	@Inject
	private OA2Logger oa2logger;
	
	@Inject
	private Logger logger;
	
	private OAuser oAuser = null;
	private Client client = null;
	private String fragment = null;
	
	@Override
	public Response authorize(
			String grantType,
			String responseType,
			String clientId,
			String redirectUri,
			String state){
			
		//TODO: validate redirect_uri
		if (grantType == null || grantType.equals("") || grantType.equalsIgnoreCase(IMPLICIT_GRANT)){
			
			//redirect to oauth login form location with params
			String fragment = 
					"grant_type="+grantType
					+"&response_type="+responseType
					+"&client_id="+clientId
					+"&redirect_uri="+redirectUri
					+"&state="+ state;
			
			redirectUri = oAuthProperties.get(OAuthProperties.loginFormUrl) + "?"+fragment;
					
			return createResponseRedirect(redirectUri, null, null, state);
			
		}else if(grantType.equals("code_grant")){
			//TODO
			return null;	
		}else{
			return createErrorResponse(Status.BAD_REQUEST, 
					state, Error.INVALID_REQUEST.toString(), 
					"grant_type invalido o nulo.");
		}
	}
	
	@Override
	public Response authenticate(
			String grantType,
			String user,
			String password,
			String responseType,
			String clientId,
			String redirectUri,
			String state,
			String token){
		
		if (grantType == null || grantType.equals("") || grantType.equalsIgnoreCase(IMPLICIT_GRANT)){
			
			return implicitGrant(grantType, responseType, clientId, redirectUri, state, user, password);
			
		}else if(grantType.equalsIgnoreCase("code_grant")){
			//TODO:
		}else if(grantType.equalsIgnoreCase("password")){
		
			return resourceOwnerPasswordCredentialsGrant(user, password);
			
		}else if(grantType.equalsIgnoreCase(CLIENT_CREDENTIALS_GRANT)){
			
			return clientCredentialsGrant(clientId, password, state);
	
		}else if(grantType.equalsIgnoreCase(FACEBOOK_GRANT)){
			
			return facebookLogin(grantType, responseType, clientId, redirectUri, state, token);
		
		}else{
			//TODO: return error invalid request
			return createErrorResponse(Status.BAD_REQUEST, state, Error.INVALID_REQUEST.toString(), "grant_type invalido.");
		}
		return createErrorResponse(Status.BAD_REQUEST, state, Error.INVALID_REQUEST.toString(), "grant_type invalido o nulo.");
	}
	
/**
	   response_type
	         REQUIRED.  Value MUST be set to "token".
	   client_id
	         REQUIRED.  The client identifier as described in Section 2.2.
	   redirect_uri
	         OPTIONAL.  As described in Section 3.1.2.
	   scope
	         OPTIONAL.  The scope of the access request as described by
	         Section 3.3.
	   state
	         RECOMMENDED.  An opaque value used by the client to maintain
	         state between the request and callback.  The authorization
	         server includes this value when redirecting the user-agent back
	         to the client.  The parameter SHOULD be used for preventing
	         cross-site request forgery as described in Section 10.12.
	         
	           GET /authorize?response_type=token&client_id=s6BhdRkqt3&state=xyz
        &redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb HTTP/1.1
	*/
	
//	 (A)  The client initiates the flow by directing the resource owner's
//        user-agent to the authorization endpoint.  The client includes
//        its client identifier, requested scope, local state, and a
//        redirection URI to which the authorization server will send the
//        user-agent back once access is granted (or denied).	
//	(B) The authorization server authenticates the resource owner (via
//	        the user-agent) and establishes whether the resource owner
//	        grants or denies the client's access request.
//	(C)  Assuming the resource owner grants access, the authorization
//	        server redirects the user-agent back to the client using the
//	        redirection URI provided earlier.  The redirection URI includes
//	        the access token in the URI fragment.
	
//TODO: Error:
//	 (1) If the request fails due to a missing, invalid, or mismatching
//	   redirection URI, or if the client identifier is missing or invalid,
//	   the authorization server SHOULD inform the resource owner of the
//	   error, and MUST NOT automatically redirect the user-agent to the
//	   invalid redirection URI.

//	 (2) If the resource owner denies the access request or if the request
//	   fails for reasons other than a missing or invalid redirection URI,
//	   the authorization server informs the client by adding the following
//	   parameters to the fragment component of the redirection URI using the
//	   "application/x-www-form-urlencoded" format, per Appendix B:
	
//   (3)??? http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-5.2 ????
	
	/**
   	 *  http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.2
   	 *  
	 * @param responseType
	 * @param clientId
	 * @param redirectUri
	 * @param state
	 */
	private Response implicitGrant(
			String grantType,
			String responseType,
			String clientId,
			String redirectUri,
			String state,
			String user,
			String password){

		boolean authorized = false;
		
		try {
			user = Utils.validateUsername(user);
			Response validationResponse = validateLoginParams(grantType, responseType, 
					clientId, redirectUri, state, user, password);
			if(validationResponse != null){
				
				if(validationResponse.getEntity() != null){
					return validationResponse;
				}
				return Response.status(Status.OK).entity(new ErrorResponse("error", 
						"Por favor verifique que los parametros de autenticación sean validos.", null, state)
				).build();
				//return validationResponse;
			}

			if(oAuser.getBlocked() == 1){				
				ErrorResponse errorResponse = new ErrorResponse(Error.ACCESS_DENIED.toString(), "Usuario bloqueado.", null, state);
				fragment = "error=" + errorResponse.getError()
						+"&error_description="+errorResponse.getError_description();
				oa2logger.logError(httpRequest,Level.WARNING, "implicit grant: "+ 
						errorResponse.getError_description());
				return createResponseRedirect(redirectUri, fragment, errorResponse, state);
			}		
			//verify that the client has permission(clientAction) over the user's resources.
			if(!dataAccess.hasClientAccessToUserResources(clientId, user) ){
				//(2)
				ErrorResponse errorResponse = new ErrorResponse(Error.ACCESS_DENIED.toString(), 
						"El cliente no tiene permisos sobre los recursos del usuario.", null, state);
				fragment = "error=" + errorResponse.getError()
						+"&error_description="+errorResponse.getError_description();
				oa2logger.logError(httpRequest,Level.WARNING, "implicit grant: "+ 
						errorResponse.getError_description());

				//return createResponseRedirect(redirectUri, fragment, errorResponse, state);
				return Response.status(Status.UNAUTHORIZED).entity(errorResponse).build();
			}else{
				//verify that user and password are correct and match.
				try {
					if(grantType.equalsIgnoreCase(IMPLICIT_GRANT)){
						authorized = tokenGenerator.checkPassword(oAuser.getSalt() 
								+ password, oAuser.getSecret());
					}else if(grantType.equalsIgnoreCase(FACEBOOK_GRANT)){
						authorized = tokenGenerator.checkPassword(oAuser.getSalt() 
								+ password, oAuser.getSecret());
					}
					
				} catch (Exception e) {
					//(3)
					e.printStackTrace();
					oa2logger.logError(httpRequest,Level.SEVERE, e);
					return createErrorResponse(Status.INTERNAL_SERVER_ERROR, state, 
							Error.SERVER_ERROR.name(), Error.SERVER_ERROR.name());
				}
			}
			//if the user is authorized we generate the accessTokenResponse
			if(authorized){	
				Response r = createUserSession(oAuser, client, null, state, redirectUri);
				if(r.getStatus() != 200){
					return Response.status(Status.OK).entity(r.getEntity()).build();
				}
				return r;
				//return createUserSession(oAuser, client, state, redirectUri);
			}else{
				//not authorized
				//(2)
				ErrorResponse errorResponse = new ErrorResponse(Error.ACCESS_DENIED.toString());
				oAuser.setAttempts(oAuser.getAttempts() + 1);
				if(oAuser.getAttempts() == MAX_ATTEMPTS){
					oAuser.setBlocked(1);
					oAuser.setBlockDate(Calendar.getInstance().getTime());
					
					logger.info("Usuario " + oAuser.getName() + " bloqueado por intentos fallidos," + 
							" client_id:" + client.getClientId());
					
					errorResponse.setError_description(USER_BLOCKED_ERROR);
				}else{
					errorResponse.setError_description(LOGIN_FAILED_ERROR);
				}
				fragment = "error=" + errorResponse.getError()
						+"&error_description="+errorResponse.getError_description();
				
				//return createResponseRedirect(redirectUri, fragment, error, state);
				return Response.status(Status.UNAUTHORIZED).entity(errorResponse).build();
			}
		} catch (Exception e) {			
			e.printStackTrace();
			oa2logger.logError(httpRequest,Level.WARNING, "implicit grant: "+ e);			
			return createErrorResponse(Status.INTERNAL_SERVER_ERROR, state, 
					Error.SERVER_ERROR.name(), Error.SERVER_ERROR.name());
		}
	}
	
	private Response validateLoginParams(
				String grantType,
				String responseType,
				String clientId,
				String redirectUri,
				String state,
				String user,
				String password) throws Exception{

		client = dataAccess.fetchClientByClientId(clientId);
		if(client == null){
			oa2logger.logError(httpRequest,Level.SEVERE, "invalid client_id");
			return createErrorResponse(Status.BAD_REQUEST, state, Error.INVALID_REQUEST.toString(), "invalid client_id");
		}
		
		if(responseType == null || !responseType.equalsIgnoreCase("token")){
			ErrorResponse errorResponse = new ErrorResponse(Error.INVALID_REQUEST.toString(), 
					"response_type must be token.", null, state);
			
			fragment = "error="+ errorResponse.getError()
					+"&error_description="+errorResponse.getError_description();
			
			oa2logger.logError(httpRequest, Level.WARNING, "implicit grant: "+ 
					errorResponse.getError_description());
			
			return createResponseRedirect(redirectUri, fragment, errorResponse, state);
		}
		
		if(user == null || user.length() == 0){
			//(2) missing user				
			ErrorResponse errorResponse = new ErrorResponse(Error.INVALID_REQUEST.toString(), "missing user.", null, state);
			fragment = "error=" + errorResponse.getError()
					+"&error_description="+errorResponse.getError_description();
			oa2logger.logError(httpRequest,Level.WARNING, "implicit grant: "+ errorResponse.getError_description());
			return createResponseRedirect(redirectUri, fragment, errorResponse, state);
		}		
	
		//verify that the user exists
		oAuser = dataAccess.fetchUserByName(user);
		
		if(oAuser == null){
			ErrorResponse errorResponse = new ErrorResponse(Error.ACCESS_DENIED.toString(), 
					"El usuario " + user +" no existe.", null, state);
			fragment = "error=" + errorResponse.getError()
					+"&error_description="+errorResponse.getError_description();
			logger.info("implicit grant: "+ errorResponse.getError_description());
			return createResponseRedirect(redirectUri, fragment, errorResponse, state);
		}
		if(oAuser.getBlocked() == 1){				
			ErrorResponse errorResponse = new ErrorResponse(Error.ACCESS_DENIED.toString(), "Usuario bloqueado.", null, state);
			fragment = "error=" + errorResponse.getError()
					+"&error_description="+errorResponse.getError_description();
			oa2logger.logError(httpRequest,Level.WARNING, "implicit grant: "+ errorResponse.getError_description());
			return createResponseRedirect(redirectUri, fragment, errorResponse, state);
		}
		return null;	
	}
	
	// Client Credentials Grant
	// http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.4

	// Request
	// POST /token HTTP/1.1
	// Host: server.example.com
	// Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
	// Content-Type: application/x-www-form-urlencoded
	
	// Response
	// HTTP/1.1 200 OK
	// Content-Type: application/json;charset=UTF-8
	// Cache-Control: no-store
	// Pragma: no-cache

	// {
	// "access_token":"2YotnFZFEjr1zCsicMWpAA",
	// "token_type":"example",
	// "expires_in":3600,
	// "example_parameter":"example_value"
	// }

	private Response clientCredentialsGrant(String clientId, String secret, String state){
		
		if(clientId == null){
			return Response.status(Status.BAD_REQUEST)
					.entity(new OA2Response(400, "client_id nulo." ))
					.build();
		}
		
		Client client = dataAccess.fetchClientByClientId(clientId);
		
		if(client == null){
			return Response.status(Status.NOT_FOUND)
					.entity(new OA2Response(404, "El cliente no existe." ))
					.build();
		}
		
		try {
			
			boolean isMatch = tokenGenerator.checkPassword(secret, client.getSecret());			
			if (!isMatch) {
				return Response.status(Status.UNAUTHORIZED)
						.entity(new OA2Response(401, "Autenticacion fallida.")).build();	
			}
			AccessTokenResponse entity = sessionHandler.createSession(client, state, 
					httpRequest.getRemoteAddr());
			entity.setToken_type("token");

			return Response.ok(entity).build();
			
		} catch (Exception e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(500, "Internal server Error."))
					.build();
		}
	}
	
	private Response facebookLogin(
			String grantType,
			String responseType,
			String clientId,
			String redirectUri,
			String state,
			String token){
		
		try{
			String fragment = null;
			ErrorResponse error = null;
			Client client = dataAccess.fetchClientByClientId(clientId);
			
			if(client == null){
				//(1) missing client
				fragment = "error=" + Error.UNAUTHORIZED_CLIENT.toString();
				error = new ErrorResponse(Error.UNAUTHORIZED_CLIENT.toString());
				oa2logger.logError(httpRequest,Level.WARNING, 
						"Facebook grant: "
						+ "client_id: " + clientId 
						+ "unauthorized.");
				return createResponseRedirect(redirectUri, fragment, error, state);
			}
						
			if(!dataAccess.isRedirectURIValid(redirectUri, clientId)){
				fragment = "error=" + Error.INVALID_REQUEST.toString();
				
				oa2logger.logError(httpRequest,Level.WARNING, "implicit grant: redirect_uri invalid.");
				error =  new ErrorResponse(Error.INVALID_REQUEST.toString());
				return createResponseRedirect(redirectUri, fragment, error, state);
			}
//			
			if(responseType == null || !responseType.equalsIgnoreCase("token")){
				fragment = "error=" + Error.INVALID_REQUEST.toString();
				oa2logger.logError(httpRequest,Level.WARNING, "implicit grant: response_type invalid.");
				error =  new ErrorResponse(Error.INVALID_REQUEST.toString());
				return createResponseRedirect(redirectUri, fragment, error, state);
			}

			String facebookId = FacebookClient.getInstance(token).getFacebookId();
			ExternalAccount ea = em.find(ExternalAccount.class, new ExternalAccountId(facebookId, 
					ExternalAccount.TYPES.facebook.name()));
			
			if(ea != null){	
				return createUserSession(ea.getoAuser(), client, null, state, redirectUri);
			}else{
				//redirecciona a la pagina de creacion de usuarios de MiMundo
				String redirrect_uri = redirectUri
						+ "#requires_new_user=true"
						+ "&token="+token;
				URI uri = new URI(redirrect_uri);
				OA2Response oa2r = new OA2Response(302,redirrect_uri);
				return Response.ok(uri).entity(oa2r).build();
				//return Response.temporaryRedirect(redirect).build();
			}
			
		} catch (FacebookOAuthException e) {
			e.printStackTrace();
			oa2logger.logError(httpRequest,Level.WARNING, "facebook login: " + e);
			return createErrorResponse(Status.INTERNAL_SERVER_ERROR, state, e.getLocalizedMessage(), null);
		}catch (Exception e) {
			e.printStackTrace();
			oa2logger.logError(httpRequest,Level.WARNING, "facebook login: " + e);
			return createErrorResponse(Status.INTERNAL_SERVER_ERROR, state, "Internal Server Error.", null);
		}

	//	return createResponseRedirect(redirectUri, null, new ErrorResponse(Error.INVALID_REQUEST.toString()), state);
	}
	
	//this method should be authorized only to trusted endpoints.
	@Override
	public Response validateToken(
			String user,
			String token,
			String client_id,//client_id from the app calling the API
			String url,
			String method){

		try{			
			OA2Response result = sessionHandler.validateSession(client_id, user, token, url, method);
			return Response.ok(result).build();
			
		}catch(Exception e){
			e.printStackTrace();
			oa2logger.logError(httpRequest, Level.SEVERE, e.getLocalizedMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(500, "Internal server Error."))
					.build();
		}
	}
	
	public Response logout(){
		
		String token =  httpRequest.getHeader("authorization");
		if(token == null || token.length() == 0){
			return createErrorResponse(Status.BAD_REQUEST, "logout", 
					Error.INVALID_REQUEST.toString(), "Token nulo o invalido.");
		}
		try{
			OA2Response r = sessionHandler.deactivateSession(token);
			return Response.ok().entity(r).build();
		}catch(Exception e){
			e.printStackTrace();
			oa2logger.logError(httpRequest, Level.SEVERE, e.getLocalizedMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(500, "Internal server Error."))
					.build();
		}
	}

	//http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-4.3

	// Request	
//		POST /token HTTP/1.1
//	    Host: server.example.com
//	    Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
//	    Content-Type: application/x-www-form-urlencoded
	//
//	    grant_type=password&username=johndoe&password=A3ddj3w
		
//		Response:
//	    HTTP/1.1 200 OK
//	    Content-Type: application/json;charset=UTF-8
//	    Cache-Control: no-store
//	    Pragma: no-cache
	//
//	    {
//	      "access_token":"2YotnFZFEjr1zCsicMWpAA",
//	      "token_type":"example",
//	      "expires_in":3600,
//	      "refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA",
//	      "example_parameter":"example_value"
//	    }
	
	public Response resourceOwnerPasswordCredentialsGrant(String username,String password){

		throw new InternalServerErrorException("Not implemented.");

	}
	

	private Response createUserSession(OAuser oAuser, Client client, List<Scope> scopes, String state, String redirectUri){
		AccessTokenResponse accessToken=null;
		String fragment = "";
		try {
			if(oAuser == null){
				accessToken = sessionHandler.createSession(client,scopes, state, httpRequest.getRemoteAddr());
			}else{
				accessToken = sessionHandler.createSession(oAuser, client,scopes, state, httpRequest.getRemoteAddr());
			}
			accessToken.setToken_type("Bearer");			
			fragment = "access_token=" + accessToken.getAccess_token() +
					"&state="+accessToken.getState() +
					"&token_type="+accessToken.getToken_type() +
					"&expires_in="+accessToken.getExpires_in() +
					"&scope="+accessToken.getScope();
			
		} catch (SecurityException e) {
			e.printStackTrace();
			oa2logger.logError(httpRequest,Level.WARNING, "createUserSession: " + e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new ErrorResponse(Error.SERVER_ERROR.name(), 
							INTERNAL_SERVER_ERROR, null, state )).build();
		} catch (IllegalStateException e) {
			e.printStackTrace();
			oa2logger.logError(httpRequest,Level.WARNING, "createUserSession: " + e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new ErrorResponse(Error.SERVER_ERROR.name(), 
							INTERNAL_SERVER_ERROR, null, state )).build();
		} catch (NotSupportedException e) {
			e.printStackTrace();
			oa2logger.logError(httpRequest,Level.WARNING, "createUserSession: " + e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new ErrorResponse(Error.SERVER_ERROR.name(), 
							INTERNAL_SERVER_ERROR, null, state )).build();
		} catch (SystemException e) {
			e.printStackTrace();
			oa2logger.logError(httpRequest,Level.WARNING, "createUserSession: " + e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new ErrorResponse(Error.SERVER_ERROR.name(), 
							INTERNAL_SERVER_ERROR, null, state )).build();
		} catch (RollbackException e) {
			e.printStackTrace();
			oa2logger.logError(httpRequest,Level.WARNING, "createUserSession: " + e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new ErrorResponse(Error.SERVER_ERROR.name(), 
							INTERNAL_SERVER_ERROR, null, state )).build();
		} catch (HeuristicMixedException e) {
			e.printStackTrace();
			oa2logger.logError(httpRequest,Level.WARNING, "createUserSession: " + e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new ErrorResponse(Error.SERVER_ERROR.name(), 
							INTERNAL_SERVER_ERROR, null, state )).build();
		} catch (HeuristicRollbackException e) {
			e.printStackTrace();
			oa2logger.logError(httpRequest,Level.WARNING, "createUserSession: " + e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new ErrorResponse(Error.SERVER_ERROR.name(), 
							INTERNAL_SERVER_ERROR, null, state )).build();
		} 		
		return createResponseRedirect(redirectUri, fragment, accessToken, state);
	}
		
	private Response createResponseRedirect(String redirectUri, String fragment, Object entity, String state){
		
		try {
		
			if(fragment != null){
				fragment = encodeFragment(fragment);
				redirectUri = redirectUri+"#"+fragment;
			}
			
			return Response.temporaryRedirect(new URI(redirectUri))
					.status(OA2Status.FOUND)
					.entity(entity)
					.cacheControl(createCacheControl())
					.build();	
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
			oa2logger.logError(httpRequest,Level.WARNING, "createResponseRedirect: " + e);
			return Response.status(Status.BAD_REQUEST)
					.entity(new ErrorResponse(Error.INVALID_REQUEST.name(), 
							"Url malformada.", null, state )).build();
		} 
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			oa2logger.logError(httpRequest,Level.WARNING, "createResponseRedirect: " + e);
			return Response.status(Status.BAD_REQUEST)
					.entity(new ErrorResponse(Error.INVALID_REQUEST.name(), 
							"UnsupportedEncodingException", null, state )).build();
		} catch (Exception e) {
			e.printStackTrace();
			oa2logger.logError(httpRequest,Level.WARNING, "createResponseRedirect: " + e);
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new ErrorResponse(Error.SERVER_ERROR.name(), 
							"Error interno del servidor.", null, state )).build();
		}
	}
	
	private String encodeFragment(String fragment) throws UnsupportedEncodingException{

		String[] split = fragment.split("&");
		String encoded ="";
		for(String s : split){
			String key = s.substring(0, s.indexOf("=")+1);
			String value = s.substring(s.indexOf("=")+1);
			encoded += key + URLEncoder.encode(value, "UTF-8")+"&";
		}
		encoded = encoded.substring(0, encoded.lastIndexOf("&"));
		return encoded;
	}
	
	//http://tools.ietf.org/html/draft-ietf-oauth-v2-31#section-5.2
	private Response createErrorResponse(Status status, String state, String error, String error_description){
		
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setError(error);
		
		if(error_description == null){
			error_description = status.getReasonPhrase();
		}
		
		errorResponse.setError_description(error_description);
		errorResponse.setState(state);
		
		return Response.status(status).entity(errorResponse).build();
	}
		
	private CacheControl createCacheControl(){
		CacheControl cacheControl = new CacheControl();
		cacheControl.setMaxAge(SessionHandler.EXPIRES_IN);
		cacheControl.setNoCache(true);
		cacheControl.setNoStore(true);
		return cacheControl;
	}
	
}
