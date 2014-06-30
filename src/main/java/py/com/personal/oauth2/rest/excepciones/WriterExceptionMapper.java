package py.com.personal.oauth2.rest.excepciones;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.WriterException;

import py.com.personal.oauth2.rest.client.dao.OA2Response;

@Provider
public class WriterExceptionMapper implements ExceptionMapper<WriterException> {

	@Override
	public Response toResponse(WriterException exception) {

		return Response.status(500).type("application/json")
				.entity(new OA2Response(500, exception.getMessage())).build();
	}

}
