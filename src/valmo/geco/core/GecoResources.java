/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Simon Denier
 * @since Oct 5, 2010
 *
 */
public class GecoResources {
	
	public final static String sep = "/";
	
	private static boolean webstart = false;

	public static boolean exists(String name) {
		if( webstart ) {
			return GecoResources.class.getResource(name) != null;
		} else {
			return new File(name).exists();
		}
	}
	
	public static InputStream getStreamFor(String name) {
		if( webstart ) {
			return GecoResources.class.getResourceAsStream(name);
		} else {
			try {
				return new FileInputStream(name);
			} catch (FileNotFoundException e) {
				return null;
			}
		}
	}
	
	public static BufferedReader getReaderFor(String name) {
		InputStream stream = getStreamFor(name);
		if( stream!=null ) {
			return new BufferedReader(new InputStreamReader(stream));
		} else {
			return null;
		}
	}
	
	public static void forWebstart() {
		webstart = true;
	}
	
	public static void forDesktop() {
		webstart = false;
	}	

}
