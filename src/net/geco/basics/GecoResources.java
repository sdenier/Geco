/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.basics;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.ibm.icu.text.CharsetDetector;

/**
 * @author Simon Denier
 * @since Oct 5, 2010
 *
 */
public class GecoResources {
	
	public final static String sep = "/"; //$NON-NLS-1$
	
	public static Charset CHARSET;
	
	static {
		try {
			CHARSET = Charset.forName("UTF-8");	 //$NON-NLS-1$
		} catch (Exception e) {
			CHARSET = Charset.defaultCharset();
		}
	}
	
	private static boolean webstart = false;

	public static boolean exists(String name) {
		if( webstart ) {
			return GecoResources.class.getResource(name) != null;
		} else {
			return new File(name).exists();
		}
	}
	
	public static InputStream getStreamFor(String name) throws FileNotFoundException {
		if( webstart ) {
			return GecoResources.class.getResourceAsStream(name);
		} else {
			return new FileInputStream(name);
		}
	}
	
	public static BufferedReader getReaderFor(String name) throws FileNotFoundException {
		InputStream stream = getStreamFor(name);
		if( stream!=null ) {
			return new BufferedReader(new InputStreamReader(stream));
		} else {
			return null;
		}
	}

	public static BufferedReader getSafeReaderFor(String name) throws FileNotFoundException {
		InputStream stream = getStreamFor(name);
		if( stream!=null ) {		
			Charset charset;
			try {
				CharsetDetector detector = new CharsetDetector();
				detector.setText(new BufferedInputStream(stream));
				charset = Charset.forName( detector.detect().getName() );
			} catch (Exception e) {
				charset = Charset.defaultCharset();
			}
			return new BufferedReader(new InputStreamReader(getStreamFor(name), charset));
		} else {
			return null;
		}
	}
	
	public static BufferedWriter getSafeWriterFor(String name) throws FileNotFoundException {
		return getSafeWriterFor(name, CHARSET);
	}

	public static BufferedWriter getSafeWriterFor(String name, Charset charset) throws FileNotFoundException {
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(name), charset);
		return new BufferedWriter(writer);
	}
	
	public static void forWebstart() {
		webstart = true;
	}
	
	public static void forDesktop() {
		webstart = false;
	}

	public static boolean platformIsMacOs() {
		return System.getProperty("os.name").startsWith("Mac"); //$NON-NLS-1$ //$NON-NLS-2$
	}	
	public static boolean platformIsWindows() {
		return System.getProperty("os.name").startsWith("Windows"); //$NON-NLS-1$ //$NON-NLS-2$
	}	
	public static boolean platformIsLinux() {
		return System.getProperty("os.name").startsWith("Linux"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static File getGecoSupportDirectory() {
		String homeDir = System.getProperty("user.home"); //$NON-NLS-1$
		File gecoSupport = new File(homeDir + "/.geco2"); //$NON-NLS-1$
		if( platformIsMacOs() ){
			gecoSupport = new File(homeDir + "/Library/Geco2"); //$NON-NLS-1$
		}
		if( platformIsWindows() ){
			gecoSupport = new File(System.getenv("APPDATA") + "/Geco2"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if( ! gecoSupport.exists() ){
			gecoSupport.mkdir();
		}
		return gecoSupport;
	}

}
