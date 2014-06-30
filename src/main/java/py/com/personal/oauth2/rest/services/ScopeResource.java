package py.com.personal.oauth2.rest.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import py.com.personal.oauth2.data.OAuthDataAccess;
import py.com.personal.oauth2.data.OAuthRepository;
import py.com.personal.oauth2.model.Action;
import py.com.personal.oauth2.model.Scope;
import py.com.personal.oauth2.model.ScopeAction;
import py.com.personal.oauth2.rest.client.dao.OA2Response;
import py.com.personal.oauth2.util.Utils;

@Path("/scopes")
@Produces("application/json")
@Consumes("application/json")
@RequestScoped
public class ScopeResource {

	@Inject
	private OAuthDataAccess dataAccess;
	
	@Inject
	@OAuthRepository
	private EntityManager em;

	@Inject
	private Logger logger;
	
	@POST
	@Path("/")
	public Response create(Scope scope) {
		try {
			Response validation = validate(scope);
			if(validation != null) {
				return validation;
			}
			
			Scope s = dataAccess.getScopeByName(scope.getName());
			if(s != null){
				return Response.status(Status.BAD_REQUEST).
						entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, "El scope ya existe."))
						.build();
			}
			
			em.persist(scope);
			em.flush();

			return Response.created(new URI("/scopes/" + scope.getId())).build();

		} catch (Exception e) {
			logger.severe(e.getLocalizedMessage());
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor."))
					.build();
		}
	}
	
	@PUT
	@Path("/")
	public Response update(Scope scope) {
		try {
			Response validation = validate(scope);
			if(validation != null) {
				return validation;
			}
			
			em.merge(scope);
			em.flush();

			return Response.created(new URI("/scopes/" + scope.getId())).build();

		} catch (Exception e) {
			logger.severe(e.getLocalizedMessage());
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST).
					entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, "El scope ya existe."))
					.build();
		}
	}
	
	@DELETE
	@Path("/{name}")
	public Response remove(@PathParam("name") String name) {
		try {
			//TODO: check that this scope is not related to users or clients
			//if so warn
			Scope s = dataAccess.getScopeByName(name);
			if(s != null){
				return Response.status(Status.BAD_REQUEST).
						entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, "El scope ya existe."))
						.build();
			}
			
			em.remove(s);
			em.flush();

			return Response.ok().build();

		} catch (Exception e) {
			logger.severe(e.getLocalizedMessage());
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor."))
					.build();
		}
	}
	
	@GET
	@Path("/{name}")
	public Response getScopes(@PathParam("name") String name){		
		try {

			Scope s = dataAccess.getScopeByName(name);
			
			if(s == null){
				return Response.status(Status.NOT_FOUND).
						entity(new OA2Response(HttpServletResponse.SC_NOT_FOUND, "El scope no existe."))
						.build();
			}
			
			return Response.ok().entity(s).build();
			
		} catch (Exception e) {
			logger.severe(e.getLocalizedMessage());
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor."))
					.build();
		}
	}
	
	@POST
	@Path("/{scopeName}/actions")
	public Response addActions(@PathParam("scopeName") String scopeName,
			List<ScopeAction> scopeActions){
		
		//validar que el id de las acciones no sea nulo.
		Scope scope = dataAccess.getScopeByName(scopeName);
		if(scope == null){
			return Response.status(Status.BAD_REQUEST).
					entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, "No se encontro el scope."))
					.build();
		}
		
		Action action = null;
		
		for(ScopeAction sa : scopeActions){
			action = em.find(Action.class, sa.getId().getActionId());
			sa.setAction(action);
			sa.setScope(scope);
			em.persist(sa);
		}
		em.flush();
		return Response.ok().build();
	}
	
	@PUT
	@Path("/{scopeName}/actions")
	public Response updateScopeActions(@PathParam("scopeName") String scopeName, List<Action> actions) {
		
		ScopeAction sa = null;
		//obtener todas las acciones de este scope
		//eliminar todas esas acciones
		//poner las nuevas acciones
		Scope scope = dataAccess.getScopeByName(scopeName);
		if(scope == null){
			return Response.status(Status.BAD_REQUEST).
					entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, "No se encontro el scope."))
					.build();
		}

		scope.setScopeActions(new ArrayList<ScopeAction>());
		for(Action action : actions){
			//em.find(Action.class, action.getId());	
			em.merge(action);
			sa = new ScopeAction();
			sa.setScope(scope);
			sa.setAction(action);
			em.persist(sa);
			
			scope.getScopeActions().add(sa);
		}
		em.merge(scope);
		em.flush();
		return Response.ok().build();
	}

	private Response validate(Scope scope) {
		if (Utils.isEmpty(scope.getName())) {
			return Response.status(Status.BAD_REQUEST).
					entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, "El nombre no puede estar vacio."))
					.build();
		}
		return null;
	}

}
