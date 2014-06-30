package py.com.personal.oauth2.rest.excepciones;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.BadRequestException;

import py.com.personal.oauth2.rest.client.dao.OA2Response;

@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

	@Override
	public Response toResponse(BadRequestException exception) {
		return Response.status(400).type("application/json")
				.entity(new OA2Response(400, exception.getMessage())).build();
	}

}
