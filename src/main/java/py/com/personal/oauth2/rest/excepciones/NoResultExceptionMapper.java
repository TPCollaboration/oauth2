package py.com.personal.oauth2.rest.excepciones;

import javax.persistence.NoResultException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import py.com.personal.oauth2.rest.client.dao.OA2Response;

public class NoResultExceptionMapper implements  ExceptionMapper<NoResultException> {

	@Override
	public Response toResponse(NoResultException exception) {
		return Response.status(404).type("application/json")
				.entity(new OA2Response(404, exception.getMessage())).build();
	}
}
