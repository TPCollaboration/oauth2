package py.com.personal.oauth2.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

@Qualifier
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER,	ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OAuthRepository {
	/* class body intentionally left blank */
}
