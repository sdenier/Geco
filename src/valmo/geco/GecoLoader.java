/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import com.jdotsoft.jarloader.JarClassLoader;

/**
 * @author Simon Denier
 * @since Jun 12, 2010
 *
 */
public class GecoLoader {

	public static void main(String[] args) {
		try {
			PrintStream ps = new PrintStream(new FileOutputStream("gecoerror.log", true));
			System.setErr(ps);
			System.setOut(ps);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		JarClassLoader loader = new JarClassLoader();
		try {
			if( args.length>0 && args[0].startsWith("valmo") ) {
				loader.invokeMain(args[0], Arrays.copyOfRange(args, 1, args.length));
			} else {
				loader.invokeMain("valmo.geco.Geco", args);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}	
}
