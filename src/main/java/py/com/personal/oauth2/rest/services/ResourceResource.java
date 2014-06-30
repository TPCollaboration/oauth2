package py.com.personal.oauth2.rest.services;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
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

import org.jboss.resteasy.spi.BadRequestException;

import py.com.personal.oauth2.data.OAuthRepository;
import py.com.personal.oauth2.model.Action;
import py.com.personal.oauth2.model.Resource;
import py.com.personal.oauth2.rest.client.dao.OA2Response;
import py.com.personal.oauth2.util.Utils;

@Path("/resources")
@Produces("application/json")
@Consumes("application/json")
@Stateless
public class ResourceResource {

	@Inject
	@OAuthRepository
	private EntityManager em;
	
	@Inject
	private Logger logger;

	@POST
	@Path("/")
	public Response create(Resource resource) throws Exception {
		try {
			
			Response validation  = validate(resource);
			if(validation != null) return validation;

			List<Resource> resources = em.createQuery("select r from Resource r where r.url = :url")
			.setParameter("url", resource.getUrl())
			.getResultList();
			
			if(resources.size()>0){
				return Response.status(Status.BAD_REQUEST).
						entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, "Ya existe un recurso con ese URL."))
						.build();
			}
			
			em.persist(resource);
			em.flush();

			return Response.created(new URI("/resources/" + resource.getId()))
					.entity(resource).build();

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
	public Response update(Resource resource) throws Exception {
		try {
			
			Response validation  = validate(resource);
			if(validation != null) return validation;
			
			em.persist(resource);
			em.flush();

			return Response.ok().entity(resource).build();

		} catch (Exception e) {
			logger.severe(e.getLocalizedMessage());
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor."))
					.build();
		}
	}
	
	@DELETE
	@Path("{resourceId:[1-9][0-9]*}")
	public Response remove(@PathParam("resourceId") Long resourceId) throws Exception {
		try {
						
			Resource resource = em.find(Resource.class, resourceId);
			if(resource == null){
				return Response.status(Status.BAD_REQUEST).
						entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, 
								"No existe el recurso con id: " + resourceId.toString()))
						.build();
			}
			em.remove(resource);
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
	@Path("{resourceId:[1-9][0-9]*}/actions")
	public Response getResourceActions(@PathParam("resourceId") Long resourceId){
		try {
			
			List<Action> actions = null;
			Resource resource = em.find(Resource.class, resourceId);
			if(resource == null){
				return Response.status(Status.BAD_REQUEST).
						entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, 
								"No existe el recurso con id: " + resourceId.toString()))
						.build();
			}
			actions = resource.getActions();
			
			return Response.ok().entity(actions).build();
			
		} catch (Exception e) {
			logger.severe(e.getLocalizedMessage());
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor."))
					.build();
		}
	}
	
	/**
	 * Crea una accion sobre un recurso.
	 * 
	 * @param resourceId
	 * @param action
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("{resourceId:[1-9][0-9]*}/actions")
	public Response createAction(@PathParam("resourceId") Long resourceId,	
		Action action) throws Exception {
		try {
			
			Resource resource = em.find(Resource.class, resourceId);
			if(resource == null){
				return Response.status(Status.BAD_REQUEST).
						entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, 
								"No existe el recurso con id: " + resourceId.toString()))
						.build();
			}
			
			Response validation = validateAction(action);
			if(validation != null) return validation;
			
			action.setResource(resource);
			em.persist(action);
			
			em.flush();

			return Response.created(new URI("/resources/" + resourceId + "/actions/" + action.getId()))
					.entity(action)
					.build();

		} catch (Exception e) {
			logger.severe(e.getLocalizedMessage());
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor."))
					.build();
		}
	}
	
	@DELETE
	@Path("{resourceId:[1-9][0-9]*}/actions/{actionId:[1-9][0-9]*}")
	public Response removeAction(@PathParam("resourceId") Long resourceId,
			@PathParam("actionId") Long actionId){
		try {
			
			//TODO: remove cascade
			Resource resource = em.find(Resource.class, resourceId);
			Action action = em.find(Action.class, actionId);
			resource.getActions().remove(action);
			em.merge(resource);
			em.remove(action);
			em.flush();

			return Response.ok().build();

		} catch (Exception e) {
			logger.severe(e.getMessage());
			return Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new OA2Response(
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Internal server Error.")).build();
		}
	}
	
	@Deprecated
	private Resource fetchResource(long resourceId) {
		Resource recurso = em.find(Resource.class, resourceId);
		
		if (recurso == null) {
			throw new BadRequestException("El codigo de recurso no existe");
		}
		return recurso;
	}

	private Response validate(Resource resource) {

		if (Utils.isEmpty(resource.getUrl())) {
			return Response.status(Status.BAD_REQUEST).
					entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, "La URL no puede ser vacia."))
					.build();
		}
		
		if (Utils.isEmpty(resource.getName())) {
			return Response.status(Status.BAD_REQUEST).
					entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, "El nombre no puede ser vacio."))
					.build();
		}
		return null;
	}
	
	private Response validateAction(Action action) {
		
		if (Utils.isEmpty(action.getName())) {
			return Response.status(Status.BAD_REQUEST).
					entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, "El nombre no puede estar vacio."))
					.build();
		}
		if (Utils.isEmpty(action.getUrl())) {
			return Response.status(Status.BAD_REQUEST).
					entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, "La URL no puede ser vacia"))
					.build();
		}
		
		
		return null;
	}
}
