/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

import valmo.geco.control.Control;
import valmo.geco.control.GecoControl;
import valmo.geco.ui.StartStopButton;

/**
 * @author Simon Denier
 * @since Sep 6, 2010
 *
 */
public class LiveServer extends Control {

	private ServerSocket serverSocket;
	private Socket clientSocket;
	private Thread serverThread;
	private StartStopButton listenB;

	public LiveServer(GecoControl gecoControl, int port, StartStopButton listenB) throws IOException {
		super(gecoControl);
        serverSocket = new ServerSocket(port);
        this.listenB = listenB;
	}
	
	public LiveServer accept() {
		serverThread = new Thread() {
			public void run() {
				System.out.println("start");
				BufferedReader clientInput;
//				PrintWriter clientOutput;
				try {
					clientSocket = serverSocket.accept();
			        clientInput = new BufferedReader(
							new InputStreamReader(
							clientSocket.getInputStream()));
//			        clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);

			        String inputLine = null;
			        while (!isInterrupted() && (inputLine = clientInput.readLine()) != null) {
			        	processData(inputLine);
			        	if( inputLine.equals("Bye") ) {
			        		JOptionPane.showMessageDialog(null, "Client has terminated the connection", "No live data", JOptionPane.INFORMATION_MESSAGE);
			        		break;
			        	}
			        }
			        if( inputLine==null ) {
			        	JOptionPane.showMessageDialog(null, "No data from client", "Live connection lost", JOptionPane.WARNING_MESSAGE);
			        }
//				} catch (InterruptedException e) {
//					clientOutput.println("Bye");
			        
//			        clientOutput.close();
			        clientInput.close();
			        clientSocket.close();
			        serverSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("XXX");
					e.printStackTrace();
				}
				listenB.doOffAction();
				System.out.println("done");
			}
		};
		
		serverThread.start();
		return this;
	}

	protected String processData(String inputLine) {
		System.out.println(inputLine);
		return null;
	}
	
	public boolean isAlive() {
		return serverThread.isAlive();
	}
	
	public void stop() {
		serverThread.interrupt();
//		try {
//			serverSocket.close();
//		} catch (IOException e) {
//			System.out.println("coucou");
//			e.printStackTrace();
//		}
	}
	
}
