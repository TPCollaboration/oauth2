package py.com.personal.oauth2.security;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import py.com.personal.oauth2.config.OAuthProperties;
import py.com.personal.oauth2.rest.client.dao.OA2Response;

/**
 * Servlet Filter implementation class SecurityFilter
 */
@WebFilter("/*")
public class SecurityFilter implements Filter {
	
	@Inject
	private SessionHandler sessionHandler;
	
	ObjectMapper mapper = null;
	
	/**
	 * Default constructor.
	 */
	public SecurityFilter() {
		mapper = new ObjectMapper();
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		mapper = null;
		sessionHandler = null; 
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest)) {
			throw new ServletException("Can only process HttpServletRequest");
		}
		if (!(response instanceof HttpServletResponse)) {
			throw new ServletException("Can only process HttpServletResponse");
		}
		
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		String path = httpRequest.getRequestURI()
				.substring(httpRequest.getContextPath().length());

		if(path.contains(OAuthProperties.version)){
			path = path.replaceFirst("/"+OAuthProperties.version, "");
		}
		
		String token =  httpRequest.getHeader("authorization");
		String method = httpRequest.getMethod();
		String client_id = httpRequest.getHeader("client_id");
		
		if(path.equals("/")
				|| path.equals("/auth/authorize") 
				|| path.equals("/auth/token")
				|| path.equals("/auth/logout")
				|| path.startsWith("/auth/webmail")
				|| path.equals("/login.html")
				|| path.equals("/js/login.js")
				|| path.startsWith("/lib/")
				|| path.startsWith("/images/")
				|| path.startsWith("/css/")
				|| path.startsWith("/fonts/")
				|| path.equals("/oauth2_servicios.pdf")
				|| path.equals("/configurations/info")){

			chain.doFilter(request, response);
			
		}else if (client_id != null){
			validateSession(client_id, token, path, method, httpRequest, httpResponse, chain);
		}else{
			 //400 Bad Request
			responseWriter(new OA2Response(400, 
					"Bad Request: url, mehtod, client_id or token invalid or null."), 
					HttpServletResponse.SC_BAD_REQUEST, httpResponse, httpRequest, chain);
		}
	}
	
	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}
	
	private void validateSession(String client_id, String token, String path, String method,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain){
		try {
			//evaluate oauth2 client token validity 
			OA2Response result = sessionHandler.validateSession(client_id, "", token, path, method);
			
			if(result.getCode() == 200){
				chain.doFilter(httpRequest, httpResponse);
			}else{
				responseWriter(result, 
						HttpServletResponse.SC_FORBIDDEN,
						httpResponse, httpRequest, chain);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
			responseWriter(new OA2Response(500, "Internal Server Error."), 
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					httpResponse, httpRequest, chain);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			responseWriter(new OA2Response(500, "Internal Server Error."), 
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					httpResponse, httpRequest, chain);
		} catch (IOException e) {
			e.printStackTrace();
			responseWriter(new OA2Response(500, "Internal Server Error."), 
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					httpResponse, httpRequest, chain);
		} catch (ServletException e) {
			e.printStackTrace();
			responseWriter(new OA2Response(500, "Internal Server Error."), 
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					httpResponse, httpRequest, chain);
		}
	}
	
	private void responseWriter(Object content, int status, 
			HttpServletResponse response, 
			HttpServletRequest request, FilterChain chain){
		
		try {
			response.addHeader("Content-type", "application/json");
			String json = mapper.writeValueAsString(content);
			ServletOutputStream out = response.getOutputStream();
			response.setContentLength(json.length());
			response.setStatus(status);
			out.print(json);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
