package py.com.personal.oauth2.rest.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.resteasy.spi.BadRequestException;

import py.com.personal.oauth2.client.facebook.FacebookClient;
import py.com.personal.oauth2.config.Messages;
import py.com.personal.oauth2.data.OAuthDataAccess;
import py.com.personal.oauth2.data.OAuthRepository;
import py.com.personal.oauth2.model.Access;
import py.com.personal.oauth2.model.Action;
import py.com.personal.oauth2.model.Client;
import py.com.personal.oauth2.model.ClientUserId;
import py.com.personal.oauth2.model.ExternalAccount;
import py.com.personal.oauth2.model.OAuser;
import py.com.personal.oauth2.model.Scope;
import py.com.personal.oauth2.model.Session;
import py.com.personal.oauth2.model.UserScope;
import py.com.personal.oauth2.rest.client.UserResourceInterface;
import py.com.personal.oauth2.rest.client.dao.ListResponse;
import py.com.personal.oauth2.rest.client.dao.OA2Response;
import py.com.personal.oauth2.util.OA2Logger;
import py.com.personal.oauth2.util.Utils;

import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.User;

@SuppressWarnings("deprecation")
@Stateless
public class UserResource implements UserResourceInterface{
		
	@Inject
	private OAuthDataAccess dataAccess;
	
	@Context
	private UriInfo uriInfo;
	
	@Context
	private HttpServletRequest httpRequest;

	@Inject
	@OAuthRepository
	private EntityManager em;	
	
	@SuppressWarnings("deprecation")
	@Inject
	private OA2Logger oa2logger;
	
	@Inject
	private Logger logger;
		
	@Override
	public Response findUsers(String userName, int first, int limit){
		try {
			List<OAuser> users = dataAccess.findUsers(userName, first, limit);
			
			return Response.ok().
					entity(new ListResponse<OAuser>(users, first, limit)).build();

		} catch (Exception e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
							"Internal server Error."))
					.build();
		}
	}

	@Override
	public Response create(String username, String email,
			String password, String verification) {
		
		try {
			
			username = Utils.validateUsername(username);
			
			if(verification != null && !password.equals(verification)){
				
				logger.info(
						"Username " + username
						+ "Mensaje: " + Messages.INVALID_PASSWORDS);
				
				return Response.status(Status.BAD_REQUEST).
						entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, 
								Messages.INVALID_PASSWORDS))
						.build();
			}
			
			OAuser oAuser = dataAccess.fetchUserByName(username);
			if(oAuser != null ){
				
				logger.info(
						"Username " + username
						+ " - Mensaje: " + Messages.USER_ALREADY_EXISTS);
				
				return Response.status(Status.BAD_REQUEST).
						entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, 
								Messages.USER_ALREADY_EXISTS)).build();
			}
						
			oAuser = dataAccess.createUser(username, email, password);
			
			URI uri = uriInfo.getRequestUriBuilder().path(UserResourceInterface.class, "getUserByName")
					.build(oAuser.getId());
			
			logger.info(
					"Username " + username
					+ "Mensaje: " + Messages.CREATED);
			
			return Response.created(uri).entity(oAuser).build();

		} catch (BadRequestException e){
			oa2logger.logError(httpRequest, Level.WARNING, e.getMessage());
			return Response.status(Status.BAD_REQUEST).
					entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, e.getMessage()))
					.build();
		}catch (Exception e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Internal server Error."))
					.build();
		}
	}
	
	@Override
	public Response getUserByName(String userName){
		try {			
			OAuser user = dataAccess.fetchUserByName(userName);
			
			if(user == null){		
					
				oa2logger.logError(httpRequest, Level.INFO, Messages.USER_NOT_FOUND);
					
				return Response.status(Status.NOT_FOUND).
							entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND, 
									Messages.USER_NOT_FOUND))
							.build();
			}
			
			return Response.ok().entity(user).build();
			
		} catch (Exception e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
							"Internal server Error."))
					.build();
		}
	}
	
	@Override
	public Response activate(String userName) {
		try {
			OAuser user = dataAccess.fetchUserByName(userName);
			if(user == null){		
				oa2logger.logError(httpRequest, Level.INFO, Messages.USER_NOT_FOUND);
				
				return Response.status(Status.NOT_FOUND).
						entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND, 
								Messages.USER_NOT_FOUND))
						.build();
			}
			user.setActive(true);
			em.merge(user);
			
			logger.info(
					"Username " + userName
					+ "Mensaje: " + Messages.ACTIVATED);
			
			return Response.ok().entity(user).build();
		} catch (Exception e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
							"Internal server Error."))
					.build();
		}
	}

	@Override
	public Response lock(String userName) {
		try {
			OAuser user = dataAccess.fetchUserByName(userName);
			if(user == null){		
				oa2logger.logError(httpRequest, Level.INFO, Messages.USER_NOT_FOUND);
				return Response.status(Status.NOT_FOUND).
						entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND, 
								Messages.USER_NOT_FOUND)).build();
			}
			user.setBlocked(true);
			em.merge(user);
			
			logger.info(
					"Username " + userName
					+ "Mensaje: " + "Bloqueado.");
			
			return Response.ok().entity(user).build();
		} catch (Exception e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
							"Internal server Error.")).build();
		}
	}

	@Override
	public Response unlock(String userName) {
		try {
			OAuser user = dataAccess.fetchUserByName(userName);
			
			if(user == null){		
				oa2logger.logError(httpRequest, Level.INFO, Messages.USER_NOT_FOUND);
				return Response.status(Status.NOT_FOUND).
						entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND, 
								Messages.USER_NOT_FOUND)).build();
			}
			
			user.setBlocked(false);
			em.merge(user);
			
			logger.info(
					"Username " + userName
					+ "Mensaje: " + "Desbloqueado.");
			
			return Response.ok().entity(user).build();
		} catch (Exception e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
							"Internal server Error.")).build();
		}
	}

	@Override
	public Response updatePassword(String username, String password,
			String verification) {
		try {			
			OAuser user = dataAccess.fetchUserByName(username);
			
			if(user == null){		
				oa2logger.logError(httpRequest, Level.INFO, Messages.USER_NOT_FOUND);
				return Response.status(Status.NOT_FOUND).
						entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND, 
								Messages.USER_NOT_FOUND)).build();
			}
			
			if(verification != null && password.equals(verification)){
				user.setSecret(password);
				user = dataAccess.generatePassword(user);
				em.merge(user);
				
				logger.info(
						"Username " + username
						+ "Mensaje: " + "Actualizado exitosamente.");
				
				return Response.ok().build();
			}else{
				oa2logger.logError(httpRequest, Level.WARNING, Messages.INVALID_PASSWORDS);
				return Response.status(Status.BAD_REQUEST).
						entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, Messages.INVALID_PASSWORDS))
						.build();
			}
			
		} catch (Exception e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server Error."))
					.build();
		}
	}

	@Override
	public Response getUserScopes(String username, String clientId) {
		try{
			
			OAuser user = dataAccess.fetchUserByName(username);
			if(user == null){		
				oa2logger.logError(httpRequest, Level.INFO, Messages.USER_NOT_FOUND);
				return Response.status(Status.NOT_FOUND).
						entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND, 
								Messages.USER_NOT_FOUND)).build();
			}
			Client client = dataAccess.fetchClientByClientId(clientId);
			if(client == null){
				oa2logger.logError(httpRequest, Level.INFO, Messages.CLIENT_NOT_FOUND);
				return Response.status(Status.NOT_FOUND).
						entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND, 
								Messages.CLIENT_NOT_FOUND)).build();	
			}
			
			List<Scope> result = dataAccess.getUserScopesByClient(username, clientId);
			return Response.ok(result).build();
			
		} catch (Exception e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Internal server Error.")).build();
		}
	}
	

	@Override
	public Response addScopes(String username, String clientId, List<String> scopeNames) {
	
		try {			
			scopeNames = removeRepeated(scopeNames);			
			OAuser user = dataAccess.fetchUserByName(username);
			
			if(user == null){		
				oa2logger.logError(httpRequest, Level.INFO, "No se encontro el usuario " + username + ".");
				return Response.status(Status.NOT_FOUND).
						entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND, 
								Messages.USER_NOT_FOUND)).build();
			}
			Client client = dataAccess.fetchClientByClientId(clientId);
			if(client == null){
				oa2logger.logError(httpRequest, Level.INFO, "No se encontro el cliente " + clientId + ".");
				return Response.status(Status.NOT_FOUND).
						entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND, 
								Messages.CLIENT_NOT_FOUND)).build();	
			}
			
			dataAccess.addScopeNamesToUser(user, clientId, scopeNames);
			return Response.ok().build();
			
		} catch (Exception e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server Error."))
					.build();
		}
	}

	@Override
	public Response modifyUserScopes(String username, 
			String clientId, List<String> newScopeList) {
			try{				
				newScopeList = removeRepeated(newScopeList);
				OAuser user = dataAccess.fetchUserByName(username);				
				if(user == null){		
					oa2logger.logError(httpRequest, Level.INFO, Messages.USER_NOT_FOUND);
					return Response.status(Status.NOT_FOUND).
							entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND, 
									Messages.USER_NOT_FOUND)).build();
				}
				Client client = dataAccess.fetchClientByClientId(clientId);
				if(client == null){
					oa2logger.logError(httpRequest, Level.INFO, Messages.CLIENT_NOT_FOUND);
					return Response.status(Status.NOT_FOUND).
							entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND, 
									Messages.CLIENT_NOT_FOUND)).build();	
				}
				
				dataAccess.updateUserScopes(user, clientId, newScopeList);
				
				return Response.ok().build();
			} catch (Exception e) {
				oa2logger.logError(httpRequest, Level.SEVERE, e);
				return Response.status(Status.INTERNAL_SERVER_ERROR).
						entity(new OA2Response(
								HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
								"Internal server Error."))
						.build();
			}
	}
	
	@Override
	public Response removeUserScopes(String username, String clientId,
		List<String> removedScopes){
		
		removedScopes = removeRepeated(removedScopes);
		OAuser user = dataAccess.fetchUserByName(username);
		
		if(user == null){		
			oa2logger.logError(httpRequest, Level.INFO, Messages.USER_NOT_FOUND);
			return Response.status(Status.NOT_FOUND).
					entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND, 
							Messages.USER_NOT_FOUND)).build();
		}
		
		Client client = dataAccess.fetchClientByClientId(clientId);
		if(client == null){
			oa2logger.logError(httpRequest, Level.INFO, Messages.CLIENT_NOT_FOUND);
			return Response.status(Status.NOT_FOUND).
					entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND, 
							Messages.CLIENT_NOT_FOUND)).build();	
		}
		
		List<String> clientScopeIds = dataAccess.getClientScopeNames(clientId);
		List<UserScope> removedUserScopes = new ArrayList<UserScope>();
		
		try {
			for(UserScope us : user.getUserScopes()){
				if(removedScopes.contains(us.getScope().getName()) && 
						clientScopeIds.contains(us.getScope().getName())){
					removedUserScopes.add(us);
					em.remove(us);
				}
			}
			user.getUserScopes().removeAll(removedUserScopes);
			em.merge(user);
			em.flush();
			
			return Response.ok().build();
		} catch (Exception e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
							"Internal server Error."))
					.build();
		}	
	}

	@Override
	public Response getUserInfo(String clientId, String token) {
		OAuser user = null;
		ClientUserId id = null;
		Access access = null;
		Client client = null;
		Session session = null;
		
		try {
		
			if(token == null || clientId == null){
				return Response.status(Status.BAD_REQUEST)
						.entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, 
								"token o client_id nulo.")).build();
			}
			
			client = dataAccess.fetchClientByClientId(clientId);
			session = dataAccess.getActiveSession(token);
			
			if(session == null){
				return Response.status(Status.GONE)
				.entity(new OA2Response(HttpServletResponse.SC_GONE,
						"No se encontraron sesiones activas para el token.")).build();
			}
			if(session.getUser() == null){
				return Response.status(Status.NO_CONTENT)
				.entity(new OA2Response(HttpServletResponse.SC_NO_CONTENT,
						"No hay informacion para este usuario.")).build();
			}
			if(client == null){
				return Response.status(Status.NOT_FOUND)
						.entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND, 
								"El cliente no existe." )).build();
			}
		
			user = session.getUser();
			id = new ClientUserId(client.getId(), user.getId());
			access = em.find(Access.class, id);
			access.setUserName(user.getName());
			access.setMail(user.getMail());
			access.setScopes(new ArrayList<String>());
			
			for(Scope scope : dataAccess.getTokenScopes(token)){
				access.getScopes().add(scope.getName());
			}
			
			return Response.ok(access).build();
			
		} catch (Exception e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
							"Internal server Error.")).build();
		}
	}
	
	@Override
	public Response delete(String username) {
		try {
			OAuser user = dataAccess.fetchUserByName(username);
			
			if(user == null){		
				oa2logger.logError(httpRequest, Level.INFO, Messages.USER_NOT_FOUND);
				return Response.status(Status.NOT_FOUND).
						entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND, 
								Messages.USER_NOT_FOUND)).build();
			}
			
			em.remove(user);
			List<UserScope> userScopes = user.getUserScopes();
			for (UserScope userScope : userScopes) {
				em.remove(userScope);
			}
			List<Session> sesiones = user.getSessions();
			for (Session sesion : sesiones) {
				em.remove(sesion);
			}
			//TODO: informacion de auditoria de eliminacion
			return Response.ok().build();
		} catch (Exception e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Internal server Error.")).build();
		}
	}	
	
	@Override
	public Response createUserClientActions(String username, String clientId,
			List<Long> actionIds){
		
		try {
			OA2Response response = dataAccess.createUserClientActions(username, 
					clientId, actionIds);
			if(response.getCode() == HttpServletResponse.SC_NOT_FOUND){
				
				return Response.status(Status.NOT_FOUND).
						entity(response).build();				
			}else{
				return Response.ok().build();
			}
			

		} catch (Exception e) {			
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server Error."))
					.build();
		}
	}
	
	@Override
	public Response linkExternalAccount(String username, 
					String accountType, 
					String token) {
		
		if(username == null || accountType == null || token == null){
			return Response.status(Status.BAD_REQUEST)
					.entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST,
							"Username, client_id o token nulo."))
					.build();
		}
		try{
			if(accountType.equalsIgnoreCase("facebook")){
				OAuser user = dataAccess.fetchUserByName(username);
				if(user == null){
					return Response.status(Status.NOT_FOUND)
							.entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND,
									" No se encontro el usuario."))
							.build();
				}

				User fbUser = FacebookClient.getInstance(token).getUser();
				
				if(fbUser == null){
					return Response.status(Status.BAD_REQUEST)
							.entity("Token de facebook invalido o expirado.")
							.build();
				}
				
				ExternalAccount ea = new ExternalAccount(fbUser.getId(), 
						ExternalAccount.TYPES.facebook.name(), user);
				
				em.persist(ea);
				return Response.ok().build();
			}else{
				return Response.status(Status.BAD_REQUEST)
						.entity("account_type invalido.")
						.build();
			}
		} catch (FacebookOAuthException e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
							e.getLocalizedMessage()))
					.build();
		}catch (Exception e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server Error."))
					.build();
		}
	}
	
	@Override
	public Response unlinkExternalAccount(String username, 
			String type) {
		
		if(username == null || type == null ){
			return Response.status(Status.BAD_REQUEST)
					.entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST,
							"Username o tipo de cuenta nulo."))
					.build();
		}
		try{
			if(type.equalsIgnoreCase("facebook")){
				OAuser user = dataAccess.fetchUserByName(username);
				if(user == null){
					return Response.status(Status.NOT_FOUND)
							.entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND,
									Messages.USER_NOT_FOUND))
							.build();
				}
				
				
				List<ExternalAccount> accounts = em.createQuery("select ea "
						+ "from ExternalAccount ea where "
						+ "ea.oAuser = :user "
						+ "and ea.type = :type")
						.setParameter("user", user)
						.setParameter("type", type)
						.getResultList();
				
				if(accounts.size() > 0){
					for(ExternalAccount account : accounts){
						em.remove(account);
					}
					em.flush();
					return Response.ok().build();
				}else{
					return Response.status(Status.BAD_REQUEST)
							.entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST,
											"Las cuentas no estan asociadas."))
							.build();
				}
				
			}else{
				return Response.status(Status.BAD_REQUEST)
						.entity(
								new OA2Response(HttpServletResponse.SC_BAD_REQUEST,
										"account_type invalido."))
						.build();
			}
		} catch (FacebookOAuthException e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server Error."))
					.build();
		}catch (Exception e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server Error."))
					.build();
		}
	}
	
	@Override
	public Response hasExternalAccount(String username, 
			String type) {
		
		if(username == null || type == null ){
			return Response.status(Status.BAD_REQUEST)
					.entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST,
							"Username o tipo de cuenta nulo."))
					.build();
		}
		try{
			if(type.equalsIgnoreCase("facebook")){
				OAuser user = dataAccess.fetchUserByName(username);
				if(user == null){
					return Response.status(Status.NOT_FOUND)
							.entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND,
									Messages.USER_NOT_FOUND)).build();
				}
				
				List<ExternalAccount> accounts = em.createQuery("select ea "
						+ "from ExternalAccount ea where "
						+ "ea.oAuser = :user "
						+ "and ea.type = :type")
						.setParameter("user", user)
						.setParameter("type", type)
						.getResultList();
				
				if(accounts.size() > 0){
					ExternalAccount ea = accounts.get(0);
					return Response.ok(ea.getId()).build();
				}else{
					return Response.ok(false).build();
				}
				
			}else{
				return Response.status(Status.BAD_REQUEST)
						.entity(
								new OA2Response(HttpServletResponse.SC_BAD_REQUEST,
										"account_type invalido."))
						.build();
			}
		} catch (FacebookOAuthException e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server Error."))
					.build();
		}catch (Exception e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server Error."))
					.build();
		}
	}
	
	@PUT
	@Path("/{username}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("username") String username) throws Exception {
		try {
			OAuser user = dataAccess.fetchUserByName(username);
			validate(user, null);
			em.merge(user);
			
			return Response.ok().build();
		} catch (Exception e) {
			oa2logger.logError(httpRequest, Level.SEVERE, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server Error."))
					.build();
		}
	}

		
	@GET
	@Path("/{username}/actions")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUserActions(@PathParam("username") String userName){
		ArrayList<Action> result = new ArrayList<Action>();
		
		String sql = "select a.id, a.active, a.httpmethod, a.name, a.url, a.resourceid from scope as s "
				+ "join userscope as us on(s.id=us.scopeid) "
				+ "join oauser as u on(us.userid=u.id) "
				+ "join scopeaction as sa on(sa.scopeid=us.scopeid) "
				+ "join action as a on (a.id=actionid) where u.name= :username";
		
		Query q = em.createNativeQuery(sql, Action.class);
		
		q.setParameter("username", userName);
		result = (ArrayList<Action>) q.getResultList();
		
		return Response.ok().entity(result).build();
	}
	

	private void validate(OAuser user, String verification) {
		
		if(verification != null && !user.getSecret().equals(verification)){
			throw new BadRequestException(Messages.INVALID_PASSWORDS);
		}
		if (Utils.isEmpty(user.getName())) {
			throw new BadRequestException("El nombre de usuario no puede ser vacio");
		}
		if (Utils.isEmpty(user.getMail())) {
			throw new BadRequestException("La direccion de correo no puede ser vacia");
		}
		if (Utils.isEmpty(user.getSecret())) {
			throw new BadRequestException("La clave no puede ser vacia");
		}
		OAuser oAuser = dataAccess.fetchUserByName(user.getName());
		if(oAuser != null ){
			throw new BadRequestException(Messages.USER_ALREADY_EXISTS);
		}
	}
	
	private List<String> removeRepeated(List<String> stringList){
		HashSet<String> hs = new HashSet<String>();
		hs.addAll(stringList);
		stringList.clear();
		stringList.addAll(hs);
		return stringList;
	}
}
