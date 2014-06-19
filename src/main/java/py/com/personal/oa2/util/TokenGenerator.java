package py.com.personal.oa2.util;

import javax.inject.Named;

import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64;
import org.jasypt.digest.StandardStringDigester;
import org.jasypt.salt.RandomSaltGenerator;
import org.jasypt.util.password.StrongPasswordEncryptor;

@Named("tokenGenerator")
public class TokenGenerator {
	
	private int SALT_SIZE_BYTES = 36;
	
	private StandardStringDigester digester;
	
	private StrongPasswordEncryptor encryptor; 
	
	private RandomSaltGenerator random;
	
	private Base64 base64;
	
	public TokenGenerator(){
		encryptor = new StrongPasswordEncryptor();
		random = new RandomSaltGenerator();
		base64 = new Base64();
		digester = new StandardStringDigester();	
		digester.setAlgorithm("SHA1");
		digester.setIterations(10000);
		digester.setSaltSizeBytes(SALT_SIZE_BYTES);
		digester.initialize();
	}
	
	public String generateToken(String clientId, String user, String remoteAddr){
		return digester.digest(clientId+user+remoteAddr);
	}
	
	public boolean matchToken(String clientId, String user, String remoteAddr, String digest){
		String msg = clientId+user+remoteAddr;
		return digester.matches(msg, digest);
	}
	
	public boolean matchToken(String message, String digest){
		return digester.matches(message, digest);
	}

	public String generatePassword(String string) throws Exception {
		return encryptor.encryptPassword(string);
	}
	
	public boolean checkPassword(String plainPassword, String encryptedPassword){
		return encryptor.checkPassword(plainPassword, encryptedPassword);
	}
	
	public String generateRandomSalt(int size){
		byte[] bytes = random.generateSalt(size);
		return new String(base64.encode(bytes));
	}
	
}
