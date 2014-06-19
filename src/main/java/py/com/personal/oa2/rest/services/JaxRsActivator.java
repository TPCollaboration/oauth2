package py.com.personal.oa2.rest.services;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import py.com.personal.oa2.config.OAuthProperties;

@ApplicationPath(OAuthProperties.version)
public class JaxRsActivator extends Application {
	/* class body intentionally left blank */
}
