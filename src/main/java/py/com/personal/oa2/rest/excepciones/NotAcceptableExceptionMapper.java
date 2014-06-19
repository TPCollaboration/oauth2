package py.com.personal.oa2.rest.excepciones;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.NotAcceptableException;

import py.com.personal.oa2.rest.client.dao.OA2Response;

@Provider
public class NotAcceptableExceptionMapper implements ExceptionMapper<NotAcceptableException> {

	@Override
	public Response toResponse(NotAcceptableException exception) {

		return Response.status(406).type("application/json")
				.entity(new OA2Response(406, exception.getMessage())).build();
	}

}
