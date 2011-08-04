/**
 *  Copyright (c) 2010 Simon Denier
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package net.geco;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import javax.swing.JOptionPane;

import net.geco.model.Messages;

import com.jdotsoft.jarloader.JarClassLoader;

/**
 * @author Simon Denier
 * @since Jun 12, 2010
 *
 */
public class GecoLoader {

	public static void main(String[] args) {
		try {
			PrintStream ps = new PrintStream(new FileOutputStream("gecoerror.log", true)); //$NON-NLS-1$
			System.setErr(ps);
			System.setOut(ps);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		JarClassLoader loader = new JarClassLoader();
		try {
			if( args.length>0 && args[0].startsWith("net.geco") ) { //$NON-NLS-1$
				loader.invokeMain(args[0], Arrays.copyOfRange(args, 1, args.length));
			} else {
				loader.invokeMain("net.geco.Geco", args); //$NON-NLS-1$
			}
		} catch (ClassNotFoundException e) {
			JOptionPane.showMessageDialog(
					null, 
					Messages.getString("GecoLoader.AppNotFoundWarning")  //$NON-NLS-1$
					+ e.getMessage().substring(17), // cut "Failure to load: ".length()
					Messages.getString("GecoLoader.LaunchErrorTitle"),  //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}	
}
