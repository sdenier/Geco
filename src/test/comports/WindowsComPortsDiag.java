/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.comports;

import gnu.io.CommPortIdentifier;
import gnu.io.RXTXVersion;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;

import valmo.geco.core.WindowsRegistryQuery;

/**
 * @author Simon Denier
 * @since 3 janv. 2011
 *
 */
public class WindowsComPortsDiag {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BufferedWriter buffer;
		try {
			buffer = new BufferedWriter(new FileWriter("comports.txt"));
			listWindowsPortsInRegistry(buffer);
			buffer.flush();
			listRxtxComPorts(buffer);
			buffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @throws IOException 
	 * 
	 */
	private static void listWindowsPortsInRegistry(BufferedWriter buffer) throws IOException {
		String[] entries =
			WindowsRegistryQuery.listRegistryEntries("HKLM\\System\\CurrentControlSet\\Enum").split("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		boolean found = false;
		buffer.write("****** Windows COM ports in registry ******");
		buffer.newLine();
		for (String string : entries) {
			if( string.contains("COM") && string.contains("FriendlyName") ){
				found = true;
				buffer.write(string.trim());
				buffer.newLine();
			}
		}
		if( !found ){
			buffer.newLine();
			buffer.write("****** No friendly names for COM ports ******");
			buffer.newLine();
			for (String string : entries) {
				if( string.contains("COM") || string.contains("FriendlyName") ){
					buffer.write(string.trim());
					buffer.newLine();
				}
			}			
		}
	}

	/**
	 * @param buffer
	 * @throws IOException
	 */
	private static void listRxtxComPorts(BufferedWriter buffer)
			throws IOException {
		buffer.newLine();
		buffer.write("****** RXTX COM PORTS " + RXTXVersion.getVersion() + " ******");
		buffer.newLine();
		@SuppressWarnings("rawtypes")
		Enumeration ports = CommPortIdentifier.getPortIdentifiers();
		while( ports.hasMoreElements() ) {
			CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
			buffer.write(port.getName() + " - "
					+ (port.getPortType()==CommPortIdentifier.PORT_SERIAL ? "Serial" : "Other") );
			buffer.newLine();
		}
	}

}
