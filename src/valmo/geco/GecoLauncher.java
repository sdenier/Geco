/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco;

import com.jdotsoft.jarloader.JarClassLoader;

/**
 * @author Simon Denier
 * @since Jun 12, 2010
 *
 */
public class GecoLauncher {

	public static void main(String[] args) {
		JarClassLoader loader = new JarClassLoader();
		try {
			loader.invokeMain("valmo.geco.core.Geco", new String[0]);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}	
}
