package py.com.personal.oauth2.rest.excepciones;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.MethodNotAllowedException;

import py.com.personal.oauth2.rest.client.dao.OA2Response;

@Provider
public class MethodNotAllowedExceptionMapper implements ExceptionMapper<MethodNotAllowedException> {

	@Override
	public Response toResponse(MethodNotAllowedException exception) {
		return Response.status(405).type("application/json")
				.entity(new OA2Response(405, exception.getMessage())).build();
	}

}
