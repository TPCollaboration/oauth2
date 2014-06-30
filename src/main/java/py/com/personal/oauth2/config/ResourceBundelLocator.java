package py.com.personal.oauth2.config;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ResourceBundelLocator {
	private static final String BUNDLE_NAME = "build"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private ResourceBundelLocator() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
