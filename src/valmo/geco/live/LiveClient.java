/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

import javax.swing.JOptionPane;

import valmo.geco.Geco;
import valmo.geco.core.Announcer;
import valmo.geco.ui.StartStopButton;

/**
 * @author Simon Denier
 * @since Sep 7, 2010
 *
 */
public class LiveClient implements Announcer.CardListener {

	private Geco geco;
	private StartStopButton serverB;

	private Thread thread;
	private Socket socket;
	private PrintWriter server;
	
	private Vector<String> messages;

	public LiveClient(Geco geco, StartStopButton serverB) {
		this.geco = geco;
		this.serverB = serverB;
	}
	
	public void setupNetworkParameters(String host, int port) throws UnknownHostException, IOException {
		socket = new Socket(host, port);
		server = new PrintWriter(socket.getOutputStream(), true);
	}

	public void start() throws IOException {
		messages = new Vector<String>();
		thread = new Thread(new Runnable() {
			public synchronized void run() {
				try {
					sendLoop();
				} catch (InterruptedException e) {}
				close();
			}
		});
		thread.start();
		geco.announcer().registerCardListener(this);
	}

	private synchronized void sendLoop() throws InterruptedException {
		server.println("Hi");
		while( !Thread.interrupted() && !server.checkError() ) {
			while( !messages.isEmpty() ) {
				server.println(messages.remove(0));
			}
			wait(15000);
			server.println("Idle"); // testing if socket is still active
		}
		if( server.checkError() ) {
			JOptionPane.showMessageDialog(null, "Could not send data to Live server. Stopping", "Live connection lost", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	private synchronized void addMessage(String message) {
		messages.add(message);
		notify();
	}

	private void close() {
		geco.announcer().unregisterCardListener(this);
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
	
	public boolean isActive() {
		return !socket.isClosed();
	}

	public void stop() {
		thread.interrupt();
	}

	@Override
	public void cardRead(String chip) {
		addMessage(geco.registry().findRunnerData(chip).infoString());
		addMessage(geco.registry().findRunnerData(chip).getResult().formatTrace());
	}

	@Override
	public void unknownCardRead(String chip) {
		addMessage(chip);
	}

	@Override
	public void cardReadAgain(String chip) {
		addMessage("Again " + chip);
	}
	
}
