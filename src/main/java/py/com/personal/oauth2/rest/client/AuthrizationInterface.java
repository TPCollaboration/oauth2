package py.com.personal.oauth2.rest.client;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.cache.Cache;
import org.jboss.resteasy.annotations.cache.NoCache;

import py.com.personal.oauth2.rest.client.dao.AccessTokenResponse;
import py.com.personal.oauth2.rest.client.dao.ErrorResponse;

@Path("/auth/")
@Produces("application/json")
@RequestScoped
public interface AuthrizationInterface {

	/**
	 * 
	 * @param grantType
	 * @param responseType
	 * @param clientId
	 * @param redirectUri
	 * @param state
	 * @return 
	 */
	@GET
	@Path("/authorize")
	@NoCache
	@Cache(noStore=true)
	public Response authorize(
			@QueryParam("grant_type") String grantType,
			@QueryParam("response_type") String responseType,
			@QueryParam("client_id") String clientId,
			@QueryParam("redirect_uri") String redirectUri,
			@QueryParam("state") String state);
	
	/**
	 * 
	 * @param grantType
	 * @param user
	 * @param password
	 * @param responseType
	 * @param clientId
	 * @param redirectUri
	 * @param state
	 * @return {@link AccessTokenResponse} si es correcto.
	 *  sino {@link ErrorResponse}
	 */
	@POST
	@Path("/token")
	@NoCache
	@Cache(noStore=true)
	@Produces("application/json")
	public Response authenticate(
			@FormParam("grant_type") String grantType,
			@FormParam("user") String user,
			@FormParam("password") String password,
			@FormParam("response_type") String responseType,
			@FormParam("client_id") String clientId,
			@FormParam("redirect_uri") String redirectUri,
			@FormParam("state") String state,
			@FormParam("token") String token);
	
	/**
	 * 
	 * @param user
	 * @param token
	 * @param client_id
	 * @param url
	 * @param method
	 * @return String "OK" si es valido.
	 */
	@POST
	@Path("/token/validate")
	@Produces("application/json")
	public Response validateToken(
			@FormParam("user")String user,
			@FormParam("token")String token,
			@FormParam("client_id")String client_id,//client_id from the app calling the API
			@FormParam("url")String url,
			@FormParam("method")String method);
		
	@POST
	@Path("/logout")
	public Response logout();
	
}

