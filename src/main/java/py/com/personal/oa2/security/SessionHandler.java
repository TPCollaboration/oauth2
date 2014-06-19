package py.com.personal.oa2.security;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import py.com.personal.oa2.data.OAuthDataAccess;
import py.com.personal.oa2.data.OAuthRepository;
import py.com.personal.oa2.model.Access;
import py.com.personal.oa2.model.Action;
import py.com.personal.oa2.model.Client;
import py.com.personal.oa2.model.ClientUserId;
import py.com.personal.oa2.model.OAuser;
import py.com.personal.oa2.model.Scope;
import py.com.personal.oa2.model.Session;
import py.com.personal.oa2.rest.client.dao.AccessTokenResponse;
import py.com.personal.oa2.rest.client.dao.OA2Response;
import py.com.personal.oa2.util.TokenGenerator;
import py.com.personal.oa2.util.Utils;

@Named("sessionHandler")
@ApplicationScoped
public class SessionHandler {
	
	@Inject
	private TokenGenerator tokenGenerator;
	
	@Inject
	private OAuthDataAccess dataAccess;
	
	private List<Action> publicActions = null;
	
	@Inject
	@OAuthRepository
	private EntityManager em;

	@Inject
	protected UserTransaction utx;

	@Inject
	private Logger logger;
	
	public static final int EXPIRES_IN = 34560; //24 hs
	
	private static final String PATH_PARAM = "*";
	

	@PostConstruct
	public void init() {
		publicActions = getPublicActions();
	}
	
	public AccessTokenResponse createSession(OAuser oAuser, Client client, 
			List<Scope> scopes,
			String state, 
			String remoteAddr) 
			throws NotSupportedException, SystemException, SecurityException, 
			IllegalStateException, RollbackException, HeuristicMixedException, 
			HeuristicRollbackException{

		AccessTokenResponse accessToken = null;
		Session session = new Session();
		session.setUser(oAuser);
		
		if(scopes == null){
			session.setScopes(dataAccess.getUserScopesByClient(oAuser.getName(), client.getClientId()));
		}else{
			session.setScopes(scopes);
		}
		
		session.setAccessToken(tokenGenerator.generateToken(client.getClientId(), 
				oAuser.getName(), remoteAddr));
		accessToken = saveSession(client, remoteAddr, session);		
		accessToken.setScope(dataAccess.createStringScopeList(session.getScopes()));
		accessToken.setExpires_in(EXPIRES_IN);
		accessToken.setState(state);
		saveAccess(client, session.getUser(),remoteAddr);
		
		logger.info("Sesion creada para el usuario: " + oAuser.getName() + 
				" client_id: " + client.getClientId());
		
		return accessToken;
	}
	
	public AccessTokenResponse createSession(Client client, 
			List<Scope> scopes,
			String state, 
			String remoteAddr) 
			throws NotSupportedException, SystemException, SecurityException, 
			IllegalStateException, RollbackException, HeuristicMixedException, 
			HeuristicRollbackException{

		AccessTokenResponse accessToken = null;
		Session session = new Session();
		session.setScopes(scopes);		
		session.setAccessToken(tokenGenerator.generateToken(client.getClientId(), "", remoteAddr));
		accessToken = saveSession(client, remoteAddr, session);
		accessToken.setScope(dataAccess.createStringScopeList(session.getScopes()));
		accessToken.setExpires_in(EXPIRES_IN);
		accessToken.setState(state);
		
		logger.info("Sesion creada para el client_id: " + client.getClientId());
		
		return accessToken;
	}
	
	public AccessTokenResponse createSession(Client client, String state, String remoteAddr) 
			throws NotSupportedException, SystemException, SecurityException, 
			IllegalStateException, RollbackException, HeuristicMixedException, 
			HeuristicRollbackException{
		
		AccessTokenResponse accessToken = null;
		Session session = new Session();
		
		session.setAccessToken(tokenGenerator.generateToken(client.getClientId(), "", remoteAddr));
		session.setScopes(client.getScopes());

		accessToken = saveSession(client, remoteAddr, session);
		accessToken.setExpires_in(EXPIRES_IN);
		accessToken.setState(state);
		accessToken.setScope(dataAccess.createStringScopeList(session.getScopes()));
		
		logger.info("Sesion creada para el client_id: " + client.getClientId());
		
		return accessToken;
	}
	
	private void saveAccess(Client client, OAuser user, String remoteAddr)
			throws NotSupportedException, SystemException, SecurityException, 
			IllegalStateException, RollbackException, HeuristicMixedException, 
			HeuristicRollbackException{
		
		Access access = null;
		ClientUserId clientUserId = null;
		clientUserId = new ClientUserId(client.getId(), user.getId());
		access = em.find(Access.class,clientUserId);
		
		if(access == null){
			access = new Access();
			access.setId(clientUserId);
		}
		access.setLastLogin(Calendar.getInstance().getTime());
		access.setLastIp(remoteAddr);

		utx.begin();
		if(access.getCount() == null || access.getCount().equals(Long.valueOf(0))){
			access.setCount(Long.valueOf(1));
			em.persist(access);
		}else {
			access.setCount(access.getCount() + Long.valueOf(1));
			em.merge(access);
		}
		em.flush();
		utx.commit();
	}
	
	public OA2Response deactivateSession(String token) throws NotSupportedException, 
		SystemException, SecurityException, IllegalStateException, 
		RollbackException, HeuristicMixedException, HeuristicRollbackException{
		Session session = dataAccess.getActiveSession(token);
		if(session.getFinalization() == null){//terminate session if not already terminated.
			session.setFinalization(Calendar.getInstance().getTime());
			session.setActive(false);
			
			utx.begin();
			em.merge(session);
			em.flush();
			utx.commit();
			
			logger.info("Sesion terminada para el token: " + token);
			
			return new OA2Response(HttpServletResponse.SC_OK,"OK");
		}
		return new OA2Response(HttpServletResponse.SC_BAD_REQUEST,"No se encontraron sessiones activas.");
	}
	
	private AccessTokenResponse saveSession(Client client, String remoteAddr, Session session) 
			throws NotSupportedException, SystemException, SecurityException, IllegalStateException, 
			RollbackException, HeuristicMixedException, HeuristicRollbackException{
		
		AccessTokenResponse accessToken = new AccessTokenResponse();
		accessToken.setAccess_token(session.getAccessToken());
		
		utx.begin();
		
		session.setAccessToken(accessToken.getAccess_token());
		session.setStartTime(Calendar.getInstance().getTime());
		session.setEndTime(Utils.addSeccondsToCurrentDate(EXPIRES_IN));
		session.setClient(client);
		session.setRemoteAddr(remoteAddr);
		session.setActive(true);
		em.merge(session);
		
		em.flush();
		utx.commit();
		
		return accessToken;
	}
	
	@SuppressWarnings("unchecked")
	public OA2Response validateSession(String client_id, String user, String token, String url,String method) {
		
		List<Session> sessions = null;
		
		if(method == null || method.equals("") || url == null || url.equals("") ){
			return new OA2Response(HttpServletResponse.SC_BAD_REQUEST, "Metodo HTTP o URL nulo.");
		}
		
		if(client_id == null){
			return new OA2Response(HttpServletResponse.SC_BAD_REQUEST, "client_id no puede ser nulo.");
		}
		
		if(this.isPublicAction(method, url)){
			sessions = dataAccess.getActiveSessions(token);
			if(sessions.size() > 0){
				return updateSessionTime(sessions.get(0));
			}
			return new OA2Response(HttpServletResponse.SC_OK,"OK");
		}
		
		//if this token belongs to an user we verify it's actions 
		if (user != null && !user.equals("")){
			if(!dataAccess.canExcecuteAction(user, method, url)){
				return new OA2Response(HttpServletResponse.SC_FORBIDDEN, 
						"El usuario no puede ejectuar esta accion.");
			}
		}
		
		sessions = dataAccess.getActiveSessions(token);
		
		//verify that the requested Action(url+httpmethod) corresponds to one of the scopes associated to the session
		if(sessions.size() == 1) { //one session, match values.
			Session s = sessions.get(0);
			
			String qlString = "select distinct(a.id), a.active, a.httpmethod, a.name, a.url, a.resourceid, "
					+ "a.publicAction from scope s "
					+ "join session_scope as ss on s.id=ss.scopes_id "
					+ "join scopeaction sa on (sa.scopeid=s.id) "
					+ "join action as a on(sa.actionid=a.id) "
					+ "where session_accesstoken = :token "
					+ "and a.httpmethod= :method";
			
			List<Action> actions = (List<Action>) em.createNativeQuery(qlString, Action.class)
					.setParameter("method", method)
					.setParameter("token", token)
					.getResultList();
			
			//params token, url, httpmethod
			if(actions.size() < 1 ){
				return new OA2Response(HttpServletResponse.SC_FORBIDDEN, "El token no puede ejectuar esta accion.");
			}
			boolean matchUrl = false;
			for(Action action : actions){
				if (match(url, action.getUrl())){
					matchUrl = true;
					break;
				}
			}
			if (matchUrl) {
				if (tokenGenerator.matchToken(
						client_id + user + s.getRemoteAddr(), token)) {
					// session_end_time > now = true
					if (s.getEndTime().after(Calendar.getInstance().getTime())) {
						// persist new end time
							return updateSessionTime(s);
						
					} else {
						logger.info("Token expirado: " + token);
						return new OA2Response(HttpServletResponse.SC_UNAUTHORIZED, "expired");
					}
				} else {
					return new OA2Response(HttpServletResponse.SC_FORBIDDEN, "Token invalido.");
				}
			} else {
				return new OA2Response(HttpServletResponse.SC_FORBIDDEN,"Accion no permitida para este token.");
			}
		}else if(sessions.size() < 1){//no session			
			return new OA2Response(HttpServletResponse.SC_UNAUTHORIZED, "No existe una session.");
		}else{ //more than one session
			//if there is more than one session invalidate all.
			for(Session s : sessions){
				s.setActive(false);
				s.setEndTime(Calendar.getInstance().getTime());
				s.setFinalization(s.getEndTime());
			}
			return new OA2Response(HttpServletResponse.SC_BAD_REQUEST, "Session invalida.");
		}
	}
	
	private OA2Response updateSessionTime(Session s){
		try {
			
			s.setEndTime(Utils.addSeccondsToCurrentDate(EXPIRES_IN));
			utx.begin();
			em.merge(s);
			em.flush();
			utx.commit();
			
		}catch (RollbackException e) {
			e.printStackTrace();
			return new OA2Response(500, "Internal Server Error.");
		} catch (HeuristicMixedException e) {
			e.printStackTrace();
			return new OA2Response(500, "Internal Server Error.");
		} catch (HeuristicRollbackException e) {
			e.printStackTrace();
			return new OA2Response(500, "Internal Server Error.");
		} catch (SystemException e) {
			e.printStackTrace();
			return new OA2Response(500, "Internal Server Error.");
		} catch (NotSupportedException e) {
			e.printStackTrace();
			return new OA2Response(500, "Internal Server Error.");
		}

		return new OA2Response(HttpServletResponse.SC_OK,"OK");
	}
	
	
	/*
	 *  Se llama a este metodo al construirse este Objeto CDI.
	 *  Este metodo retorna la lista de acciones que son publicas y
	 *  no pasan por el filtro de seguridad.
	 *  
	 */
	private List<Action> getPublicActions(){

		String qlString = "select a from Action a where "
				+ "a.publicAction = :public ";

			List<Action> actions = (List<Action>) em.createQuery(qlString)
				.setParameter("public", 1)
				.getResultList();
	
			return actions;
	}	
	
	
	private boolean isPublicAction(String method, String url) {
		/*
		 * itera por las acciones publicas y compara los url
		 * si encuentra un match compara el metodo
		 * si encuentra un match retorna si es publica o no la accion.
		 */
		if(publicActions == null){
			publicActions = getPublicActions();
		}
		for(Action a : publicActions){
			if(match(url, a.getUrl())){
				if(a.getHttpMethod().equalsIgnoreCase(method)){
						if(a.isPublic() == 1){
							return true;
						}
						return false;
				}
			}
		}
		
		return false;
	}
	private boolean match(String url, String actionUrl) {
		String[] urlTokens = url.split("/");
		String[] accionTokens = actionUrl.split("/");
		if (urlTokens.length != accionTokens.length) {
			return false;
		}
		for (int i = 1; i < urlTokens.length - 1; i++) {
			String urlToken = urlTokens[i];
			String accionToken = accionTokens[i];
			if (!accionToken.equals(PATH_PARAM)) {
				if (!accionToken.equals(urlToken)) {
					return false;
				}
			}
		}
		String urlToken = urlTokens[urlTokens.length - 1];
		String accionToken = accionTokens[accionTokens.length - 1];
		String[] urlQueryParam = urlToken.split(Pattern.quote("?"));
		String[] accionQueryParam = accionToken.split(Pattern.quote("?"));
		if (!accionQueryParam[0].equals(PATH_PARAM)) {
			if (!accionQueryParam[0].equals(urlQueryParam[0])) {
				return false;
			}
		}
		return true;
	}
}
