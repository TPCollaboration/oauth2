package py.com.personal.oa2.rest.services;

import java.net.URI;
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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.spi.InternalServerErrorException;

import py.com.personal.oa2.data.OAuthRepository;
import py.com.personal.oa2.model.Action;
import py.com.personal.oa2.rest.client.dao.OA2Response;
import py.com.personal.oa2.util.Utils;

@Deprecated
@Path("/actions")
@Produces("application/json")
@Consumes("application/json")
@RequestScoped
public class ActionResource {

	@Inject
	@OAuthRepository
	private EntityManager em;

	@Inject
	private Logger logger;
	
	@POST
	@Path("/")
	public Response create(Action action) throws Exception {
		try {
			Response validation = validate(action);
			
			if(validation != null) return validation;

			em.persist(action);
			em.flush();

			return Response.created(new URI("/actions/" + action.getId()))
					.build();

		} catch (Exception e) {
			logger.severe(e.getMessage());
			return Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new OA2Response(
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Internal server Error.")).build();
		}
	}
	
	@PUT
	@Path("/")
	public Response update(Action action) throws Exception {
		try {
			Response validation = validate(action);
			
			if(validation != null) return validation;

			em.merge(action);
			em.flush();

			return Response.ok().entity(action)	.build();

		} catch (Exception e) {
			logger.severe(e.getMessage());
			return Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new OA2Response(
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Internal server Error.")).build();
		}
	}
	
	@DELETE
	@Path("/")
	public Response remove(Action action) throws Exception {
		try {
			Response validation = validate(action);
			
			if(validation != null) return validation;

			em.remove(action);
			em.flush();

			return Response.ok().entity(action)	.build();

		} catch (Exception e) {
			logger.severe(e.getMessage());
			return Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new OA2Response(
							HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Internal server Error.")).build();
		}
	}
	
	@GET
	@Path("/")
	public Response findActions(@QueryParam("name") String name){
		try{
			List<Action> actions = null;
			
			if(name == null){
				name = "%";
			}
			
			actions = em.createQuery("select a from Action a "
					+ "where a.name like :name")
					.setParameter("name", "%" + name + "%")
					.getResultList();
			
			return Response.ok().entity(actions).build();
		
		} catch (Exception e) {
			throw new InternalServerErrorException(e.getMessage());
		}
	}
	
	private Response validate(Action action) {
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
