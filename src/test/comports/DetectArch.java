/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.comports;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


/**
 * @author Simon Denier
 * @since 3 janv. 2011
 *
 */
public class DetectArch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BufferedWriter buffer;
		try {
			buffer = new BufferedWriter(new FileWriter("arch.txt"));

			String[] props = new String[] { "os.name", "os.version", "os.arch", "sun.arch.data.model" };
			for (String prop : props) {
				buffer.write(prop + " = " + System.getProperty(prop));
				buffer.newLine();
			}
			String[] envs = new String[] { "ProgramFiles(x86)", "PROCESSOR_ARCHITECTURE", "PROCESSOR_ARCHITEW6432" };
			for (String env : envs) {
				buffer.write(env + " = " +System.getenv(env));
				buffer.newLine();
			}
			
			buffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
