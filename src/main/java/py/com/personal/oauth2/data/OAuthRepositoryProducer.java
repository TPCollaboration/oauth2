package py.com.personal.oauth2.data;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class OAuthRepositoryProducer {

	@Produces
	@OAuthRepository
	@PersistenceContext(unitName = "oauth2")
	private EntityManager em;
	
//	@Produces
//	public WSDatosUsuario getDatosUsuario() {
//		return new WSDatosUsuario_Service(
//				obtenerUrl("SoapWebServiceProducer.WSDatosUsuario_Service.WsdlLocation"))
//				.getWSDatosUsuarioSoapHttpPort();
//	}
	
	
}
