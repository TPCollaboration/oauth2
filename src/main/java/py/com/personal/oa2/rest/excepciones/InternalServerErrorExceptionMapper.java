package py.com.personal.oa2.rest.excepciones;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.InternalServerErrorException;

import py.com.personal.oa2.rest.client.dao.OA2Response;

@Provider
public class InternalServerErrorExceptionMapper implements
		ExceptionMapper<InternalServerErrorException> {

	@Override
	public Response toResponse(InternalServerErrorException exception) {
		return Response.status(500).type("application/json")
				.entity(new OA2Response(500, exception.getMessage())).build();
	}

}
