/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.core;

import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Simon Denier
 * @since Oct 22, 2010
 *
 */
public class Messages {

	private static final HashMap<String, ResourceBundle> bundles = new HashMap<String, ResourceBundle>();
	
	public static void put(String bundleKey, String bundleName) {
		if( !bundles.containsKey(bundleKey) ) {
			bundles.put(bundleKey, ResourceBundle.getBundle(bundleName));
		}
	}
	
	public static String get(String bundleKey, String key) {
		try {
			return bundles.get(bundleKey).getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	/*
	 * direct accessors for common bundles
	 */
	public static String uiGet(String key) {
		return get("ui", key);
	}

//	private static final String DEFAULT_KEY = "default"; //$NON-NLS-1$
//	private static final String DEFAULT_BUNDLE = "valmo.geco.messages"; //$NON-NLS-1$
//
//	{
//		put(DEFAULT_KEY, DEFAULT_BUNDLE); //$NON-NLS-1$
//	}
//	
//	public static String get(String key) {
//		return get(DEFAULT_KEY, key);
//	}
	
}
