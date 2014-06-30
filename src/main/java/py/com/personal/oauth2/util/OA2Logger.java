package py.com.personal.oauth2.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

@Named
@Deprecated
public class OA2Logger {
	
	
	@Inject
	private Logger logger;
	
	public OA2Logger() {
		// TODO Auto-generated constructor stub
	}
	
	public void logError(HttpServletRequest httpRequest, Level level, String message){
		String msg = "host: " + httpRequest.getRemoteAddr() +
				" - " + message;
		logger.log(level, msg);
	}
	
	public void logError(HttpServletRequest httpRequest, Level level, Throwable t){
		String msg = "host: " + httpRequest.getRemoteAddr() + t.getMessage();
		logger.log(level, msg, t);
	}
}
