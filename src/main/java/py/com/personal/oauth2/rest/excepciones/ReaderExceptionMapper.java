package py.com.personal.oauth2.rest.excepciones;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.ReaderException;

import py.com.personal.oauth2.rest.client.dao.OA2Response;

@Provider
public class ReaderExceptionMapper implements ExceptionMapper<ReaderException> {

	@Override
	public Response toResponse(ReaderException exception) {
		return Response.status(500).type("application/json")
				.entity((new OA2Response(500, exception.getMessage()))).build();
	}

}
