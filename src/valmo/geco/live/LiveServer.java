/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.swing.JOptionPane;

import valmo.geco.control.Control;
import valmo.geco.control.GecoControl;

/**
 * @author Simon Denier
 * @since Sep 6, 2010
 *
 */
public class LiveServer extends Control {

	private Socket clientSocket;
	private Thread liveThread;

	public LiveServer(GecoControl gecoControl, Socket clientSocket) throws IOException {
		super(gecoControl);
		this.clientSocket = clientSocket;
	}
	
	public LiveServer start(final LiveServerMulti serverMulti) {
		liveThread = new Thread() {
			public void run() {
				System.out.println("start");
				try {
					BufferedReader clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			        String inputLine = null;
			        
			        while (!isInterrupted() && (inputLine = clientInput.readLine()) != null) {
			        	processData(inputLine);
			        	if( inputLine.equals("Bye") ) {
			        		JOptionPane.showMessageDialog(null, "Client has terminated the connection", "No live connection", JOptionPane.INFORMATION_MESSAGE);
			        		break;
			        	}
			        }
			        if( inputLine==null ) {
			        	JOptionPane.showMessageDialog(null, "Connection with client lost", "No live connection", JOptionPane.WARNING_MESSAGE);
			        }
			        close();
				} catch (IOException e) {
					System.out.println("Live thread stopped");
				}
				serverMulti.terminated(LiveServer.this);
				System.out.println("done");
			}
		};
		
		liveThread.start();
		return this;
	}
	
	private void close() {
        try {
        	System.out.print("closing");
			clientSocket.close();
			System.out.println(" - closed");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected String processData(String inputLine) {
		System.out.println(inputLine);
		return null;
	}
	
	public void stop() {
		liveThread.interrupt();
		close();
	}
	
}
