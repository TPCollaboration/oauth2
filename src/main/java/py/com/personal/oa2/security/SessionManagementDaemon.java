package py.com.personal.oa2.security;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import py.com.personal.oa2.data.OAuthRepository;
import py.com.personal.oa2.model.Session;
import py.com.personal.oa2.util.Utils;


@Singleton
public class SessionManagementDaemon implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1339514873121807476L;
	
	@Context
	private UriInfo uriInfo;

	@Inject
	@OAuthRepository
	private EntityManager em;
	
	@Inject
	protected UserTransaction utx;

	/*
	 * busca las sesiones expiradas en la base de datos y las desactiva.
	 * a la vez las elimina del cache en memoria.
	 */
	//@Schedule(second="*/3600", minute="*", hour="*", persistent=false)
	//todas las noches a las 00.00 segun http://docs.oracle.com/javaee/6/api/javax/ejb/Schedule.html
	@Schedule
	public void sessionExpirationTask() {
		 
 			Query q = em.createQuery("select s from Session s where "
					+ "s.endTime < :currentTime "
					+ "and s.active = 1");
 			q.setParameter("currentTime", Calendar.getInstance().getTime());
 			
 			List<Session> sessions = q.getResultList();
 			
 			for(Session s : sessions){
 				s.setFinalization(Calendar.getInstance().getTime());
 				s.setActive(false);
 				em.merge(s);
 			}
 			if(sessions.size() > 0){
 				em.flush();
 			}		
	 }
	
}


