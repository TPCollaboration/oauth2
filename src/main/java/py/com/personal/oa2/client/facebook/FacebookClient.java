package py.com.personal.oa2.client.facebook;

import com.restfb.DefaultFacebookClient;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.User;

public class FacebookClient {

	//se tiene que crear una instancia por request
	private com.restfb.FacebookClient facebookClient = null; 
	private User user = null;
	
	public static FacebookClient getInstance(String token){
		return new FacebookClient(token);
	}
	
	private FacebookClient(String token) {
		facebookClient = new DefaultFacebookClient(token);
	}

	//metodo para obtencion de usuario de facebook
	public User getUser() throws FacebookOAuthException{
		if(user == null){
			user = facebookClient.fetchObject("me", User.class);
		}
		return user;
	}
	
	public String getFacebookId(){
		return getUser().getId();
	}
	
	//TODO: todo el codigo de interaccion con facebook
	
	//metodo para ver si el usuario esta vinculado con facebook
	
	//metodo para asociar una cuenta de usuario con una cuenta de facebook
	
	//metodo para desvincular una cuenta de facebook
	
}
