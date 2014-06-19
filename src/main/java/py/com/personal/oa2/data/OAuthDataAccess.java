package py.com.personal.oa2.data;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import py.com.personal.oa2.config.OAuthProperties;
import py.com.personal.oa2.model.Action;
import py.com.personal.oa2.model.Client;
import py.com.personal.oa2.model.OAuser;
import py.com.personal.oa2.model.Scope;
import py.com.personal.oa2.model.ScopeAction;
import py.com.personal.oa2.model.Session;
import py.com.personal.oa2.model.UserClientAction;
import py.com.personal.oa2.model.UserClientActionId;
import py.com.personal.oa2.model.UserScope;
import py.com.personal.oa2.model.UserScopeId;
import py.com.personal.oa2.rest.client.dao.OA2Response;
import py.com.personal.oa2.util.TokenGenerator;

@Stateless
public class OAuthDataAccess {
	
	@Inject
	@OAuthRepository
	private EntityManager em;
	
	@Inject
	private TokenGenerator tokenGenerator;
	
	@Inject
	private Logger logger;
	
	@Inject
	private OAuthProperties configuration;
	
	@Inject
	private UserTransaction utx;	
	
	public List<OAuser> findUsers(String userName, int first, int limit){
		
		Query q = em.createQuery("select u from OAuser u "
				+ "where u.name like :name");
		q.setParameter("name", "%" + userName + "%");
		q.setFirstResult(first);
		q.setMaxResults(limit);
		
		List<OAuser> result = (List<OAuser>) q.getResultList();
		return result;
	}
	
	public OAuser fetchUserByName(String userName){
		
		Query q = em.createQuery("select u from OAuser u "
				+ "where u.name = :name");
		q.setParameter("name", userName);
		
		List<OAuser> result = (List<OAuser>) q.getResultList();

		if (result.size() == 1){
			return result.get(0);
		}else{
			return null;
		}
	}
	
	public Client fetchClientByClientId(String clientId){
		Query q =  em.createQuery("select c from Client c "
				+ "where c.clientId like :clientId"); 
		q.setParameter("clientId", clientId);
		
		List<Client> result = (List<Client>) q.getResultList();
		
		if (result.size() == 1){
			return result.get(0);
		}else{
			return null;
		}
	}
	
	//TODO:
	public boolean hasClientAccessToUserResources(String clientId, String user){
		//TODO: 
		//verify that the user is not blocked or his permissions are not expired.
		//and verify that the permissions for the client are not expired or blocked.
		
		String sqlString = "SELECT count(distinct(ca.actionId)) from userclientaction ca "
				+ "join action a on ca.actionid=a.id join client as c "
				+ "on ca.clientid=c.id "
				+ "join oauser as u on ca.userid=u.id "
				+ "where c.clientid like :clientId and u.name like :name";
		
		Query q =  em.createNativeQuery(sqlString);
		
		Integer result = (Integer) q.setParameter("clientId", clientId)
				.setParameter("name", user)
				.getSingleResult();
		
		if(Integer.valueOf(result) >= 0){
			return true;
		}		
		return false;
	}
	
	public List<Scope> getUserScopesByClient(String username, String clientId){
		// traer los scopes que estan asociado al usuario
		// y que esten asociados a los recursos que pertenecen 
		// al cleinte (resource server)

		String queryString = "select distinct(s.id), s.active, s.name from scope as s "
				+ "join userscope as us on(s.id=us.scopeid) "
				+ "join oauser as u on(us.userid=u.id) "
				+ "join client as c on(us.clientid=c.id) "
				+ "where u.name= :name "
				+ "and c.clientid= :clientId";
		
		 Query q = em.createNativeQuery(queryString, Scope.class);
		
		 q.setParameter("name", username)
		 .setParameter("clientId", clientId);
		
		List <Scope> scopes	= q.getResultList();
		
		return scopes;
	}
	
	public List<Long> getUserScopeIdsByClient(String username, String clientId){
		// traer los scopes que estan asociado al usuario
		// y que esten asociados a los recursos que pertenecen 
		// al cleinte (resource server)
		
		//TODO
		String queryString = "select s.id from scope as s "
				+ "join userscope as us on(s.id=us.scopeid) "
				+ "join oauser as u on(us.userid=u.id) "
				+ "join client as c on(us.clientid=c.id) "
				+ "where u.name= :name and "
				+ "c.clientid= :clientId";
		
		 Query q = em.createNativeQuery(queryString);
		
		 q.setParameter("name", username)
		 .setParameter("clientId", clientId);
		
		List <Long> scopes = q.getResultList();
		return scopes;
	}
		
	public List<String> getUserScopeNamesByClient(String username, String clientId){
		// traer los scopes que estan asociado al usuario
		// y que esten asociados a los recursos que pertenecen 
		// al cleinte (resource server)
		
		String queryString = "select distinct(s.name) from scope as s "
				+ "join userscope as us on(s.id=us.scopeid) "
				+ "join client_scope as cs on(s.id=scopes_id) "
				+ "join oauser as u on(us.userid=u.id) "
				+ "join client as c on(cs.client_id=c.id) "
				+ "where u.name= :name and "
				+ "c.clientid= :clientId";
		
		 Query q = em.createNativeQuery(queryString);
		
		 q.setParameter("name", username)
		 .setParameter("clientId", clientId);
		
		List <String> scopes = q.getResultList();
		return scopes;
	}
	
	public List<Scope> getAllUserScopes(String username){
		 Query q = em.createQuery(
				"select s from UserScope us join us.scope as s "
				+ "where us.oAuser.name like :name");
		
		 q.setParameter("name", username);
		
		List <Scope> scopes	= q.getResultList();
		
		return scopes;
	}
	
	public List<Long> getClientScopesId(String clientId){
		
		 Query q = em.createQuery(
				"select s.id from Client c join "
				+ "c.scopes as s "
				+ "where c.clientId = :clientId");
		
		 q.setParameter("clientId", clientId);
		
		List <Long> scopesId = q.getResultList();
		
		return scopesId;
	}

	public List<String> getClientScopeNames(String clientId){
		Scope scope = null;
		List<String> scopeNames = new ArrayList<String>();
		
		Query q = em.createQuery(
				"select s.name from Client c join "
				+ "c.scopes as s "
				+ "where c.clientId = :clientId");
		
		 q.setParameter("clientId", clientId);
		 
		return q.getResultList();
	}
	
	//validacion basada en el perfil del usuario.
	public boolean canExcecuteAction(String userName, String method, String url){
		//queries if the can execute an action.
		Integer result = null;
		
		String sql = "select count(a.id) from scope as s "
				+ "join userscope as us on(s.id=us.scopeid) "
				+ "join oauser as u on(us.userid=u.id) "
				+ "join scopeaction as sa on(sa.scopeid=us.scopeid) "
				+ "join action as a on (a.id=actionid) "
				+ "where u.name like :username "
				+ "and a.httpmethod like :method "
				+ "and a.url like :url";
		
		Query q = em.createNativeQuery(sql);
		
		q.setParameter("username", userName);
		q.setParameter("method", method);
		q.setParameter("url", url);
		
		result = (Integer) q.getSingleResult();
		if(result.intValue() > 0){
			return true;
		}else{
			return false;
		}
	}

	public List<Session> getActiveSessions(String token){
		Query q = em.createQuery("select s from Session s where "
				+ "s.accessToken = :token "
				+ "and s.active = 1 ");
		
		q.setParameter("token", token);
		
		return q.getResultList();
	}
	
	public Session getActiveSession(String token){
		Session session = null;
		List<Session> sessions = null;
		Query q = em.createQuery("select s from Session s "
				+ "join fetch s.scopes where "
				+ "s.accessToken = :token "
				+ "and s.active = 1 ");
		
		q.setParameter("token", token);		
		sessions = q.getResultList();
		
		if(sessions.size() > 0){
			session = sessions.get(0);
		}else{
			//if the session does not
			//have any scope associated 
			//it returns null due to the "join fetch s.scopes"
			q = em.createQuery("select s from Session s where "
					+ "s.accessToken = :token "
					+ "and s.active = 1 ");
			
			q.setParameter("token", token);			
			sessions = q.getResultList();
			if(sessions.size() > 0){
				session = sessions.get(0);
			}
		}
		return session;
	}
	
	/**
	 *  Returns a list of scopes in a string,
	 *  scopes are separated by space.
	 *  
	 * @param username
	 * @return
	 */
	
	public String fetchUsersScopesString(String username) {
		List<Scope> scopeList = getAllUserScopes(username);
		return createStringScopeList(scopeList);
	}

	//retorna la lista de scopes asociados a una sesion.
	public String getTokenScopesAsString(String token){
		Session session = getActiveSession(token);
		return createStringScopeList(session.getScopes());
	}
	
	public List<Scope> getTokenScopes(String token){
		Session session = getActiveSession(token);
		List <Scope> scopes = session.getScopes();
		return scopes;
	}
	
	//recibe una lista de scopes y retorna un string 
	//conforme lo especificado en oauth.
	public String createStringScopeList(List<Scope> scopeList){
		String scopes = "";		
		Scope scope = null;
		Iterator<Scope> iter = scopeList.iterator();
		while(iter.hasNext()){
			scope = iter.next();
			scopes = scopes.concat(scope.getName());
			scopes = scopes.concat(" ");
		}
		if(scopes != null && scopes.length()>0){
			scopes = scopes.substring(0, scopes.lastIndexOf(" "));
			scopes = "["+ scopes + "]";
		}
		return scopes;
	}
	
	public boolean isRedirectURIValid(String redirectUri, String clientId) {
		String qlString = "select count(c) from Client c "
				+ "where c.clientId = :clientId "
				+ "and c.url = :redirectUri";
		Long count = (Long) em.createQuery(qlString)
		.setParameter("clientId", clientId)
		.setParameter("redirectUri", redirectUri)
		.getSingleResult();
		
		if(count > 0){
			return true;
		}
		return false;
	}
	
	@Deprecated
	public List<Long> getActionIdsFromServerResources(String clientId){
		//los recursos que pertenecen a un servidor de recursos
		
		String sql = "select distinct(a.id) from action as a "
				+ "join resource as res on a.resourceid=res.id "
				+ "join client_resource as cr on res.id=cr.resources_id "
				+ "join client as c on c.id=cr.client_id "
				+ "where c.clientid = :clientId";

		List<BigInteger> result = em.createNativeQuery(sql).setParameter("clientId", clientId)
				.getResultList();
		
		List<Long> actionIds = new ArrayList<Long>();
		for(BigInteger id : result){
			actionIds.add(id.longValue());
		}
		
		return actionIds;
	}
	
	public OAuser createUser(OAuser oAuser) throws Exception{
		oAuser = generatePassword(oAuser);
		em.persist(oAuser);
		em.flush();

		return oAuser;
	}
	
	public OAuser createUserWithClientScopes(OAuser oAuser, String clientId) throws Exception{				
		//dirty hack for MiMundo.
		oAuser = generatePassword(oAuser);
		
		em.persist(oAuser);
		
		UserScope userScope = null;
		Client client = fetchClientByClientId(clientId);
		UserClientActionId ucaId = null;
		UserClientAction uca = null;
				
		for (Scope scope : client.getScopes()) {
			userScope = em.find(UserScope.class,
					generateUserScopeId(oAuser, scope, client));
			if (userScope == null) {
				userScope = new UserScope();
				userScope.setId(generateUserScopeId(oAuser, scope, client));
				em.persist(userScope);
				if (oAuser.getUserScopes() == null) {
					oAuser.setUserScopes(new ArrayList<UserScope>());
				}
				oAuser.getUserScopes().add(userScope);
				for (ScopeAction sa : scope.getScopeActions()) {
					ucaId = new UserClientActionId(oAuser.getId(),
							client.getId(), sa.getAction().getId());
					uca = em.find(UserClientAction.class, ucaId);
					if (uca == null) {
						uca = new UserClientAction(ucaId, client, 1);
						uca.setUser(oAuser);
						em.persist(uca);
						if(oAuser.getClientActions() == null){
							oAuser.setClientActions(new ArrayList<UserClientAction>());
						}
					//	oAuser.getClientActions().add(uca);
					}
				}
			}
		}
				
	//	em.merge(oAuser);
		em.flush();
			
		return oAuser;
	}
	
	public OAuser createUser(String username, String email,
				String password) throws Exception{				
				OAuser oAuser = new OAuser();
				oAuser.setName(username);
				oAuser.setMail(email);
				oAuser.setActive(true);
				oAuser.setSecret(password);
				
				oAuser = generatePassword(oAuser);
				em.persist(oAuser);
				em.flush();
				
				return oAuser;
	}
	
	public OA2Response addScopesToUser(OAuser oAuser, String clientId,
			List<Scope> scopes) throws Exception {
		UserScope userScope = null;
		Client client = fetchClientByClientId(clientId);
		UserClientActionId ucaId = null;
		UserClientAction uca = null;
				
		for (Scope scope : scopes) {
			userScope = em.find(UserScope.class,
					generateUserScopeId(oAuser, scope, client));
			if (userScope == null) {
				userScope = new UserScope();
				userScope.setId(generateUserScopeId(oAuser, scope, client));
				em.persist(userScope);
				if (oAuser.getUserScopes() == null) {
					oAuser.setUserScopes(new ArrayList<UserScope>());
				}
				oAuser.getUserScopes().add(userScope);
				for (ScopeAction sa : scope.getScopeActions()) {

					ucaId = new UserClientActionId(oAuser.getId(),
							client.getId(), sa.getAction().getId());
					uca = em.find(UserClientAction.class, ucaId);
					if (uca == null) {
						uca = new UserClientAction(ucaId, client, 1);
						em.persist(uca);
						oAuser.getClientActions().add(uca);
					}
				}
			}
		}
		em.merge(oAuser);
		em.flush();
		return new OA2Response(HttpServletResponse.SC_OK, "OK");
	}
	
	/**
	 * 
	 * @param oAuser
	 * @param clientId: el id del cliente al cual estan relacionado los scopes.
	 * @param scopeNames: Nombres de los scopes a ser asociados.
	 * @return
	 * @throws Exception
	 */
	public OA2Response addScopeNamesToUser(OAuser oAuser, String clientId, List<String> scopeNames)
	throws Exception{
		Scope scope  = null;
		UserScope userScope = null;
		//b0rked
		Client client = fetchClientByClientId(clientId);
		
		UserClientActionId ucaId = null;
		UserClientAction uca = null;
		
		for(String scopeName : scopeNames){
			scope = getScopeByName(scopeName);
			if(scope != null){
				userScope = em.find(UserScope.class, generateUserScopeId(oAuser, scope, client));
				if(userScope == null){
					userScope = new UserScope();
					userScope.setId(generateUserScopeId(oAuser, scope, client));
					em.persist(userScope);
					if(oAuser.getUserScopes() == null){
						oAuser.setUserScopes(new ArrayList<UserScope>());
					}
					oAuser.getUserScopes().add(userScope);
					for(ScopeAction sa : scope.getScopeActions()){
						ucaId = new UserClientActionId(oAuser.getId(),client.getId(), sa.getAction().getId());
						uca = em.find(UserClientAction.class, ucaId);
						if(uca == null){
							uca = new UserClientAction(ucaId, client, 1);
							em.persist(uca);
							oAuser.getClientActions().add(uca);
						}
					}
				}
				em.merge(oAuser);
				em.flush();
			}else{
				logger.warning("No se encontro el scope " + scopeName + " en OAuth.");
			}
		}

		return  new OA2Response(HttpServletResponse.SC_OK, "OK");
	}
	
	public OA2Response updateUserScopes(OAuser user, String clientId, List<String> newScopeList) throws Exception {
		//obtenemos todos los scopes de este usuario con este cliente
		//si el usuario ya tiene el nuevo scope: nada
		//si el usuario no tiene => new UserScope
		//todo los scopes que no estan en la nueva lista se eliminan
		
		Scope scope  = null;
		UserScope userScope = null;
		List<Scope> scopeFinalList = new ArrayList<Scope>();
		List<String> currentUserScopeNames = new ArrayList<String>();
		
		Client client = fetchClientByClientId(clientId);
		
		for(UserScope us : user.getUserScopes()){
			if(us.getClient().getClientId().equals(clientId)){
				currentUserScopeNames.add(us.getScope().getName());
			}
		}
		
		for (int index = 0; index < newScopeList.size(); index++){
				String newScopeName =  newScopeList.get(index);
				scope = getScopeByName(newScopeName);
				if(scope == null){
					continue;
				}
				scopeFinalList.add(scope);
				if(!containsString(newScopeName, currentUserScopeNames)){
					//el usuario no tiene el nuevo scope se crea
					userScope = new UserScope();
					userScope.setId(generateUserScopeId(user, scope, client));
					userScope.setActive(true);
					em.persist(userScope);
				}
			}
				
		userScope = null;
		//el usuario tiene y no esta en la nueva lista: se elimina
		for(String currentScopeName : currentUserScopeNames){
			if(!containsString(currentScopeName, newScopeList)){
				scope = getScopeByName(currentScopeName);
				userScope = em.find(UserScope.class, generateUserScopeId(user, scope, client));
				if(userScope != null){
					em.remove(userScope);
					user.getUserScopes().remove(userScope);
				}
			}
		}
		em.merge(user);
		em.flush();
		
		updateUserClientAction(user, client, scopeFinalList);
		return new OA2Response(HttpServletResponse.SC_OK, "OK");
	}
	
	/*
	 * ve si una cadena esta contenida en una lista de cadenas.
	 */
	private boolean containsString(String value, List<String> strings){
		for(String str : strings){
			if(str.equalsIgnoreCase(value)){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 
	 * @param oAuser
	 * @param client
	 * @param scope lista final de scopes del usuario.
	 * @throws SystemException 
	 * @throws NotSupportedException 
	 * @throws HeuristicRollbackException 
	 * @throws HeuristicMixedException 
	 * @throws RollbackException 
	 * @throws IllegalStateException 
	 * @throws SecurityException 
	 */
	private void updateUserClientAction(OAuser oAuser, Client client, List<Scope> scopeFinalList) throws NotSupportedException, SystemException, SecurityException, 
		IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException{
	//B0rk3d
		List<Action> actionFinalList = new ArrayList<Action>();
		UserClientActionId ucaId = null;
		UserClientAction uca = null;
		List<Action> actions = new ArrayList<Action>();
		
		List<UserClientAction> userClientActionFinalLIst = new ArrayList<UserClientAction>();
		
		//lista de las acciones que se agrega al usuario
		//en base a la lista final de scopes.
		for(Scope scope : scopeFinalList){	
			actions = em.createNativeQuery("SELECT distinct(a.id), a.active, a.httpmethod,"
					+ " a.name, a.publicAction, a. url, a.resourceid from "
					+ "action a join scopeaction sa on a.id=sa.actionid "
					+ "join scope s on sa.scopeid=s.id "
					+ "where s.id = :scopeId", Action.class)
					.setParameter("scopeId", scope.getId())
					.getResultList();
			
			actionFinalList.addAll(actions);
		}
		
		//si el usuario tiene y no esta en la lista se ELIMINA de la lista final
		//si el usuario no tiene y esta en la lista se CREA y se agrega a la lista final.
		//si el usuario tiene y esta en la lista, se agrega a la lista final.
		for(Action action : actionFinalList){
			
			ucaId = new UserClientActionId();
			ucaId.setActionId(action.getId());
			ucaId.setUserId(oAuser.getId());
			ucaId.setClientId(client.getId());
			
			uca = em.find(UserClientAction.class, ucaId);
			if(uca == null){
				uca = new UserClientAction(ucaId, client, 1);
				em.persist(uca);
			}else{
				em.merge(uca);
			}
			userClientActionFinalLIst.add(uca);
		}
		
		
		for (UserClientAction uca1 : oAuser.getClientActions()) {
			
			if(!userClientActionFinalLIst.contains(uca1)){
				em.remove(uca1);
			}
		}
		
		oAuser.setClientActions(userClientActionFinalLIst);
		em.merge(oAuser);		
		em.flush();
	}
	
	@Deprecated
	private void updateUserClientAction(OAuser oAuser, Client client, Scope scope){
		//crea los userclientactions necesarios para que el cliente pueda llamar
		// a las acciones asociadas al usuario 
		//crear client actions del con la lista de scopes que relacione al usuario 
		// y al cliente. y que no existan ya 
		UserClientActionId ucaId = null;
		UserClientAction uca = null;
		List<BigInteger> savedActionIds = new ArrayList<BigInteger>();
		
		List<BigInteger> actionIds = em.createNativeQuery("select distinct(a.id) from action a "
				+ "join scopeaction as sa "
				+ "on a.id=sa.actionid where sa.scopeid= :scopeId "
				+ "and a.id not in(select uca.actionid from userclientaction uca"
				+ " where userid= :userId and clientid= :clientId)")
				.setParameter("scopeId", scope.getId())
				.setParameter("userId", oAuser.getId())
				.setParameter("clientId", client.getId())
				.getResultList();

		if(actionIds.size() == 0){ 
			//si no se encuentran agregamos todas las acciones del scope			
			actionIds= em.createNativeQuery("select distinct(a.id) from action a "
					+ "join scopeaction as sa "
					+ "on a.id=sa.actionid where sa.scopeid= :scopeId")
					.setParameter("scopeId", scope.getId())
					.getResultList();
		}
		
		for(BigInteger actionId : actionIds){
			ucaId = new UserClientActionId();
			ucaId.setActionId(actionId.longValue());
			ucaId.setUserId(oAuser.getId());
			ucaId.setClientId(client.getId());
			if(!savedActionIds.contains(actionId)){
				uca  = new UserClientAction(ucaId, client, 1);
				em.merge(uca);
				savedActionIds.add(actionId);
			}else{
				uca = em.find(UserClientAction.class, ucaId);
				em.remove(uca);
			}
		}
	
	}
	
	public OA2Response createUserClientActions(String username, String clientId,
			List<Long> actionIds){
		
			OAuser user = fetchUserByName(username);				
			if(user == null){	
				return new OA2Response(HttpServletResponse.SC_NOT_FOUND, 
								"No se encontro el usuario "+user+".");
			}
		
			Client client = fetchClientByClientId(clientId);
			if(client == null){
				return new OA2Response(HttpServletResponse.SC_NOT_FOUND, 
								"No se encontro el cliente." );
			}
			
			for (Long actionId : actionIds){
				UserClientActionId id = new UserClientActionId();
				id.setActionId(actionId);
				id.setUserId(user.getId());
				
				UserClientAction clientAction = new UserClientAction();
				clientAction.setId(id);
				clientAction.setClient(client);
				clientAction.setActive(true);
				em.persist(clientAction);
			}

			em.flush();
			return new OA2Response(HttpServletResponse.SC_CREATED, "Created");
		}
		
		@Deprecated
		//UserClientAction es una entidad que relaciona a otras tres:
		//Usuario: El propietario del recurso
		//Client: algun cliente que ejecute peticiones en nombre del usuario. Por ej: MiMundoWeb.
		//Action: una accion que se puede hacer sobre un Recurso.
		//esta entidad registra un permiso al cliente sobre acciones de un Usuario.
		private void createMiMundoUserClientAction(String clientId,String resourceServerId, String username){
			//TODO: esta porcion de codigo es especifica de MiMundo Personal
			// deberia manejarse de otra manera en el futuro.
			// Se crean permisos para todos los recursos de MiMundoAPI
			if(clientId.equals(configuration.get(configuration.MiMundoAPI)) 
					|| clientId.equals(configuration.get(configuration.MiMundoWEB))){ 
				//mi mundo api se le agrega todos los permisos del api
				//id MiMundo Web: TbEXNw==
				//id MiMundo Api: 3kVfBA==
//				createMiMundoUserClientAction((String) configuration.get(configuration.MiMundoWEB),
//						(String) configuration.get(configuration.MiMundoAPI), 
//						username);
				
				createUserClientActions(username, 
						clientId, getActionIdsFromServerResources(resourceServerId));
			}
			//====================== END HARDCODE=====================
			
			
		}
		
	public OAuser generatePassword(OAuser oAuser) throws Exception {
		oAuser.setSalt(tokenGenerator.generateRandomSalt(8));
		oAuser.setSecret(tokenGenerator.generatePassword(oAuser.getSalt()+oAuser.getSecret()));		
		return oAuser;
	}
		
	public Scope getScopeByName(String scopeId) {
			
			List<Scope> scopes = em.createQuery("select s from Scope s "
					+ "where s.name like :name")
					.setParameter("name", scopeId).getResultList();
			
			if (scopes.size() > 0) {
				return scopes.get(0);
			}
			return null;
		}


	public UserScopeId generateUserScopeId(OAuser user, Scope scope, Client client) {
		if(user != null && scope != null && client != null){
			UserScopeId scopeId = new UserScopeId();
			scopeId.setUserId(user.getId());
			scopeId.setScopeId(scope.getId());
			scopeId.setClientId(client.getId());
			return scopeId;
		}
		return null;
	}
}
