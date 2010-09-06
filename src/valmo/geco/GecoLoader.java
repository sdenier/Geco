/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import com.jdotsoft.jarloader.JarClassLoader;

/**
 * @author Simon Denier
 * @since Jun 12, 2010
 *
 */
public class GecoLoader {

	public static void main(String[] args) {
		try {
			PrintStream ps = new PrintStream("gecoerror.log");
			System.setErr(ps);
			System.setOut(ps);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		JarClassLoader loader = new JarClassLoader();
		try {
			loader.invokeMain("valmo.geco.Geco", new String[0]);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}	
}
