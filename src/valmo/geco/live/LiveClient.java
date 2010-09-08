/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import valmo.geco.ui.StartStopButton;

/**
 * @author Simon Denier
 * @since Sep 7, 2010
 *
 */
public class LiveClient {

	private Socket socket;
	private PrintWriter server;
	private Thread thread;
	private StartStopButton serverB;

	public LiveClient(String host, int port, StartStopButton serverB) throws UnknownHostException, IOException {
		this.serverB = serverB;
		socket = new Socket(host, port);
		server = new PrintWriter(socket.getOutputStream(), true);
	}

	public void start() throws IOException {
		thread = new Thread(new Runnable() {
			public synchronized void run() {
				try {
					server.println("Coucou");
					while( !Thread.interrupted() && !server.checkError() ) {
						server.println("Essai");
						wait(1000);
					}
					if( server.checkError() ) {
						JOptionPane.showMessageDialog(null, "Could not send data to Live server. Stopping", "Live connection lost", JOptionPane.WARNING_MESSAGE);
					}
				} catch (InterruptedException e) {}
				close();
				System.out.println("closed");
			}
		});
		thread.start();
	}

	public boolean isActive() {
		return !socket.isClosed();
	}
	
	public void stop() {
		thread.interrupt();
	}

	private void close() {
		if( !socket.isClosed() ) {
			server.println("Bye");
			server.close();
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		serverB.doOffAction();
	}
	
}
