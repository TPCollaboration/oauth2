package py.com.personal.oa2.rest.services;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import py.com.personal.oa2.config.OAuthProperties;
import py.com.personal.oa2.data.OAuthRepository;
import py.com.personal.oa2.model.Configuration;
import py.com.personal.oa2.rest.client.dao.OA2Response;

@Path("/configurations")
@Produces("application/json")
@Consumes("application/json")
@Stateless
public class ConfigurationResource {

	@Inject
	@OAuthRepository
	private EntityManager em;
	
	@Inject
	private OAuthProperties properties;

	@POST
	@Path("/")
	public Response create(Configuration configuration){
		try {

			Response validation = validateConfiguration(configuration);
			if(validation != null) return validation;
			
			Configuration c = em.find(Configuration.class, configuration.getName());
			if(c == null){
				em.persist(configuration);
				em.flush();
				return Response.created(new URI("/configurations/" + 
						configuration.getName())).build();
			}else{
				return Response.status(Status.BAD_REQUEST).
						entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, 
								"La configuracion ya existe.")).build();
			}
			
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).
					entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, "El scope ya existe."))
					.build();
		}
	}
	
	@PUT
	@Path("/")
	public Response update(Configuration configuration) {
		try {
			
			Response validation = validateConfiguration(configuration);
			if(validation != null) return validation;
				
			em.merge(configuration);
			em.flush();
			
			return Response.ok(new URI("/configurations/" + configuration.getName())).build();
	
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
							"Error interno del servidor.")).build();
		}
	}

	@DELETE
	@Path("/{name}")
	public Response delete(@PathParam("name") String name) {
		try {
			Configuration c = em.find(Configuration.class, name);
			if(c == null){
				return Response.status(Status.BAD_REQUEST).
						entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, 
								"Valores invalidos.")).build();
			}else{
				em.remove(c);
				return Response.ok().build();
			}
			
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
							"Error interno del servidor.")).build();
		}
	}
	
	@GET
	@Path("/reload")
	public Response reload(){
		try {
			properties.loadProperties();
			return Response.ok().build();
			
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).
					entity(new OA2Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
							"Error interno del servidor.")).build();
		}
	}
	
	private Response validateConfiguration(Configuration configuration){
		
		if(configuration.getName() != null && configuration.getValue() != null
				&& configuration.getName().length() > 0 && configuration.getValue().length() > 0){
			
		}else{
			return Response.status(Status.BAD_REQUEST).
					entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, 
							"Valores invalidos.")).build();
		}
		
			
		Pattern regex = Pattern.compile("[\\s$&+,:;=?@#|]");
		Matcher matcher = regex.matcher(configuration.getName());
		
		if (matcher.find()){
			return Response.status(Status.BAD_REQUEST).
					entity(new OA2Response(HttpServletResponse.SC_BAD_REQUEST, 
							"Valores invalidos.")).build();
		}
		
		return null;
	}

}
