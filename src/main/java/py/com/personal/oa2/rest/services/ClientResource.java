package py.com.personal.oa2.rest.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.spi.InternalServerErrorException;
import org.jboss.resteasy.spi.NotFoundException;

import py.com.personal.oa2.config.Messages;
import py.com.personal.oa2.data.OAuthDataAccess;
import py.com.personal.oa2.data.OAuthRepository;
import py.com.personal.oa2.model.Client;
import py.com.personal.oa2.model.Scope;
import py.com.personal.oa2.rest.client.dao.OA2Response;
import py.com.personal.oa2.util.TokenGenerator;
import py.com.personal.oa2.util.Utils;

@Path("/clients")
@Produces("application/json")
@Consumes("application/json")
@Stateless
public class ClientResource {

	@Inject
	private TokenGenerator tokenGenerator;
	
	@Inject
	private OAuthDataAccess dataAccess;
	
	@Inject
	protected UserTransaction utx;
	
	@Inject
	@OAuthRepository
	private EntityManager em;
	
	@Context
	private HttpServletRequest httpRequest;
	
	@Inject
	private Logger logger;
	
	@POST
	@Path("/")
	public Response create(Client client) throws Exception {
		try {
			Response validation = validate(client);
			if(validation != null){
				return validation;
			}
		
			client.setExipresIn(34560); //24 hs
			String clientId = tokenGenerator.generateRandomSalt(8);
			client.setClientId(clientId);
			
			String clientSecret = tokenGenerator.generateRandomSalt(10);
			client.setSecret(tokenGenerator.generatePassword(clientSecret));

			em.persist(client);
			em.flush();
			//return the client secret so the developer can put it on his application.
			//TODO: return client_id, clientSecret
			
			String responseString = "{"
					+ "clientId: " + client.getClientId() + ", " 
					+ "secret: " + clientSecret 
					+ "}";
			
			logger.info(
					"Client id: " + clientId
					+ "Mensaje: " + Messages.CREATED);
			
			return Response.created(new URI("/clients/" + client.getId()))
					.entity(responseString)
					.build();

		} catch (Exception e) {
			logger.severe("ClientResource " + httpRequest.getRemoteAddr() +" " + e.getLocalizedMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Internal server Error."))
					.build();
		}
	}
	
	@GET
	@Path("/")
	public Response findClients(){
		//TODO: pagination
//		CriteriaQuery<Client> criteriaQuery = CriteriaBuilder
		try{
			List<Client> clients = em.createQuery("select c from Client c")
					.getResultList();
					
			return Response.ok().entity(clients).build();
			
		}catch (Exception e) {
			throw new InternalServerErrorException(e.getMessage());
		}
	}
	
	@POST
	@Path("/{clientId}/scopes/")
	public Response addScopesToClient(@PathParam("clientId") String clientId,
		List<String> scopeNames){
		try {
			//TODO: validar que el cliente que llama al servicio es el mismo al que se agregan los scopes.
			Client client = dataAccess.fetchClientByClientId(clientId);
			Scope scope = null;
			
			if(client.getScopes() == null){
				client.setScopes(new ArrayList<Scope>());
			}
			
			for (String scopeName : scopeNames) {
				scope = dataAccess.getScopeByName(scopeName);
				if(scope != null){
					//TODO: controlar que no se agregue 2 o mas veces el mismo scope.
					em.merge(scope);
					client.getScopes().add(scope);
				}else{
					logger.warning("No se agrego el scope " 
							+ scopeName + "al cliente "
							+ clientId 
							+ ": no existe el scope.");
				}
			}

			em.merge(client);
			em.flush();
			
			return Response.ok().build();
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("ClientResource " + httpRequest.getRemoteAddr() +" " + e.getLocalizedMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Internal server Error."))
					.build();
		}
	}
	
	@PUT
	@Path("/{clientId}/scopes/")
	public Response modifyClientScopes(@PathParam("clientId") String clientId,
		List<String> scopeNames){
		try {
			//TODO: validar que el cliente que llama al servicio es el mismo 
			// al que se agregan los scopes.
			Client client = dataAccess.fetchClientByClientId(clientId);
			Scope scope = null;
			client.setScopes(new ArrayList<Scope>());
			for (String scopeName : scopeNames) {
				scope = dataAccess.getScopeByName(scopeName);
				
				if(scope != null){
					em.merge(scope);
					client.getScopes().add(scope);
				}else{
					logger.warning("modifyClientScopes: No existe el scope: " + scopeName);
				}
			}

			em.merge(client);
			em.flush();
			return Response.ok().build();
		} catch (Exception e) {
			logger.severe("ClientResource " + httpRequest.getRemoteAddr() +" " 
					+ e.getLocalizedMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Internal server Error."))
					.build();
		}
	}
	
	@GET
	@Path("/{clientId:[1-9][0-9]*}/users")
	public Response getClientUsers(@PathParam("clientId") long clientId){
		//TODO
		
		return Response.status(Status.SERVICE_UNAVAILABLE).build();
	}
	
	@GET
	@Path("/{clientId:[1-9][0-9]*}")
	public Response fetch(@PathParam("clientId") long clientId) throws Exception {
		try {
			Client client = fetchClient(clientId);
			
			return Response.ok(client).build();
		} catch (Exception e) {
			throw new InternalServerErrorException(e.getMessage());
		}
	}

	@PUT
	@Path("/{clientId:[1-9][0-9]*}")
	public Response update(@PathParam("clientId") long clientId,
			Client client) throws Exception {
		try {
			Client existingClient = fetchClient(clientId);
			Response validation = validate(existingClient);
			
			if(validation != null){
				return validation;
			}
			
			existingClient.setName(client.getName());
			existingClient.setDescription(client.getDescription());
			existingClient.setUrl(client.getUrl());
			
			em.merge(existingClient);
			
			logger.info(
					"Client id: " + clientId
					+ "Mensaje: " + Messages.UPDATED);
			
			return Response.ok().build();
		} catch (Exception e) {
			logger.severe("ClientResource " + httpRequest.getRemoteAddr() +" " 
					+ e.getLocalizedMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Internal server Error."))
					.build();
		}
	}

	@PUT
	@Path("/{clientId:[1-9][0-9]*}/bloquear")
	public Response bloquear(@PathParam("clientId") long clientId) throws Exception {
		try {
			Client client = fetchClient(clientId);
			client.setBlockDate(Utils.obtenerFechaActual());
			client.setBlocked(true);
			em.merge(client);
			return Response.ok().build();
		} catch (Exception e) {
			logger.severe("ClientResource " + httpRequest.getRemoteAddr() +" " 
					+ e.getLocalizedMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Internal server Error."))
					.build();
		}
	}

	@PUT
	@Path("/{clientId:[1-9][0-9]*}/desbloquear")
	public Response desbloquear(@PathParam("clientId") long clientId) throws Exception {
		try {
			Client aplicacion = fetchClient(clientId);
			aplicacion.setBlockDate(null);
			aplicacion.setBlocked(false);
			em.merge(aplicacion);
			return Response.ok().build();
		} catch (Exception e) {
			throw new InternalServerErrorException(e.getMessage());
		}
	}
	
	@PUT
	@Path("/{clientId}/reset-secret")
	public Response resetSecret(@PathParam("clientId") String clientId){
		try {
			Client client = dataAccess.fetchClientByClientId(clientId);
			
			String clientSecret = tokenGenerator.generateRandomSalt(6);
			client.setSecret(tokenGenerator.generatePassword(clientSecret));

			em.merge(client);
			em.flush();
			
			return Response.ok().entity(clientSecret).build();

		} catch (Exception e) {
			logger.severe("ClientResource " + httpRequest.getRemoteAddr() +" " 
					+ e.getLocalizedMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Internal server Error."))
					.build();
		}
	}
	
	private Client fetchClient(long clientId) {
		Client client = em.find(Client.class, clientId);
		if (client == null) {
			throw new NotFoundException("Cliente no encontrado");
		}
		return client;
	}

	private Response validate(Client client) {
		if (Utils.isEmpty(client.getName())) {
			Response.status(Status.BAD_REQUEST).entity(
			new OA2Response(HttpServletResponse.SC_BAD_REQUEST, 
					"El nombre no puede ser vacio")).build();
		}
		if (Utils.isEmpty(client.getDescription())) {
			Response.status(Status.BAD_REQUEST).entity(
					new OA2Response(HttpServletResponse.SC_BAD_REQUEST, 
							"La descripcion no puede ser vacia")).build();
		}
		if (Utils.isEmpty(client.getUrl())) {
			Response.status(Status.BAD_REQUEST).entity(
					new OA2Response(HttpServletResponse.SC_BAD_REQUEST, 
							 "La URL no puede ser vacia")).build();
		}
		
		Query q = em.createQuery("select count (c) from Client c where c.name like :name");
		q.setParameter("name", client.getName());
		Long result = (Long) q.getSingleResult();
		if(result > 0){
			return Response.status(Status.BAD_REQUEST).entity(
					new OA2Response(HttpServletResponse.SC_BAD_REQUEST, 
							 "Ya existe un cliente con ese nombre.")).build();
		}
		return null;
	}

}
