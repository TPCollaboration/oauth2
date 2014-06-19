package py.com.personal.oa2.rest.excepciones;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.NotFoundException;

import py.com.personal.oa2.rest.client.dao.OA2Response;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

	@Override
	public Response toResponse(NotFoundException exception) {
		return Response.status(404).type("application/json")
				.entity(new OA2Response(404, exception.getMessage())).build();
	}

}
