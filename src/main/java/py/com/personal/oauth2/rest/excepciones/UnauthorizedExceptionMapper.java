package py.com.personal.oauth2.rest.excepciones;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.UnauthorizedException;

import py.com.personal.oauth2.rest.client.dao.OA2Response;

@Provider
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {

	@Override
	public Response toResponse(UnauthorizedException exception) {

		return Response.status(401).type("application/json")
				.entity(new OA2Response(401, exception.getMessage())).build();
	}

}
