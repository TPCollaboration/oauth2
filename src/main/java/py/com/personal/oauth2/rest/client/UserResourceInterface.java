package py.com.personal.oauth2.rest.client;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import py.com.personal.oauth2.rest.client.dao.AccessInterface;
import py.com.personal.oauth2.rest.client.dao.OAuserInterface;
import py.com.personal.oauth2.rest.client.dao.ScopeInterface;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public interface UserResourceInterface {
	
	/**
	 *  Crea un nuevo usuario
	 *  
	 * @param username
	 * @param email
	 * @param password
	 * @param verification
	 * @return {@link OAuserInterface}
	 */
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response create(@FormParam("username") String username,
			@FormParam("email") String email,
			@FormParam("password") String password,
			@FormParam("verification") String verification);
	
	@GET
	@Path("/")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response findUsers(@QueryParam("username") String username, 
			@QueryParam("first")int first,
			@QueryParam("limit")int limit);
	
	/**
	 * 
	 * Obtiene un usuario
	 * 
	 * @param username
	 * @return {@link OAuserInterface}
	 */
	@GET
	@Path("/{username}")
	public Response getUserByName(@PathParam("username") String username);
	
	/**
	 * Elimina un usuario.
	 * 
	 * @param username
	 * @return 200 OK
	 */
	@DELETE
	@Path("/{username}")
	public Response delete(@PathParam("username") String username);

	/**
	 * Activa un usuario.
	 * 
	 * @param username
	 */
	@PUT
	@Path("/{username}/activate")
	public Response activate(@PathParam("username") String username);

	/**
	 * Bloquea un usuario.
	 * 
	 * @param username
	 * @return {@link OAuserInterface}
	 */
	@PUT
	@Path("/{username}/lock")
	public Response lock(@PathParam("username") String username);
	
	/**
	 * Desbloquea un usuario.
	 * 
	 * @param username
	 * @return {@link OAuserInterface}
	 */
	@PUT
	@Path("/{username}/unlock")
	public Response unlock(@PathParam("username") String username);

	/**
	 * Actualiza la clave.
	 * 
	 * @param username
	 * @param password
	 * @param verification
	 * @return 200 OK
	 */
	@PUT
	@Path("/{username}/password")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response updatePassword(@PathParam("username") String username,
			@FormParam("password") String password,
			@FormParam("verification") String verification);
	
	/**
	 * 
	 * @param username
	 * @param clientId
	 * @param scopeIds
	 * @return 200 OK
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{username}/clients/{client_id}/scopes")
	public Response addScopes(@PathParam("username") String username, 
			@PathParam("client_id") String clientId,
			List<String> scopeNames); 
	//si el cliente es mimundo se insertan directamente en la tabla UserClientAction

	/**
	 * Obtiene los scopes de un usuario. Solo se deben obtener los roles
	 * correspondientes al clientId
	 * 
	 * @param username
	 * @param clientId
	 * @return List<{@link ScopeInterface}> 
	 */
	@GET
	@Path("/{username}/clients/{client_id}/scopes")
	public Response getUserScopes(@PathParam("username") String username, 
			@PathParam("client_id") String clientId);

	/**
	 * Modifica los roles de un usuario. Todos los roles anteriores son 
	 * eliminados y reemplazados por la nueva lista de roles.
	 * 
	 * @param username
	 * @param clientId
	 * @param scopesId
	 * @return 200 OK
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{username}/clients/{client_id}/scopes")
	public Response modifyUserScopes(@PathParam("username") String username, 
			@PathParam("client_id") String clientId,
			List<String> scopeNames);  //actualiza los roles del usuario a la lista recibida

	/**
	 * Elimina los scopes de un usuario.
	 * 
	 * @param username
	 * @param clientId
	 * @param scopesId
	 * @return 200 OK
	 */
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{username}/clients/{client_id}/scopes")
	public Response removeUserScopes(@PathParam("username") String username, 
			@PathParam("client_id") String clientId,
			List<String> removedScopes);  //actualiza los roles del usuario a la lista recibida

	/**
	 * Obtiene datos del ultimo acceso del usuario a la aplicacion.
	 * 
	 * @param username
	 * @param clientId
	 * @return {@link AccessInterface}
	 */
	@GET
	@Path("/info")
	public Response getUserInfo(@QueryParam("client_id") String clientId,
			@QueryParam("token") String token);
	
	/**
	 * Permissions given by the user to the client.
	 * 	
	 * @param username
	 * @param clientId
	 * @param actionIds
	 * @return 200 OK
	 */
	@POST
	@Path("/{username}/clients/{client_id}/actions")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUserClientActions(@PathParam("username") String username,
			@PathParam("client_id") String clientId, List<Long> actionIds);

	/**
	 * 	Vincula una cuenta de OAuth con una de un sistema externo para login. 
	 *  account_type="facebook" para vincular con Facebook.
	 *  
	 * @param username
	 * @param clientId
	 * @param accountType
	 * @param token
	 * @return 200 - OK
	 */
	@POST
	@Path("/{username}/external-accounts")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response linkExternalAccount(
			@PathParam("username") String username,
			@FormParam("type") String accountType,
			@FormParam("token") String token);
	
	@PUT
	@Path("/{username}/external-accounts/delete")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response unlinkExternalAccount(
			@PathParam("username") String username,
			@FormParam("type") String accountType);
	
	@GET
	@Path("/{username}/external-accounts/validate")
	public Response hasExternalAccount(
			@PathParam("username") String username, 
			@QueryParam("type") String accountType);
		
}
