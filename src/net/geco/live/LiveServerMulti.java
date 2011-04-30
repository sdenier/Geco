/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.live;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Vector;

import net.geco.control.GecoControl;
import net.geco.model.RunnerRaceData;


/**
 * @author Simon Denier
 * @since Sep 6, 2010
 *
 */
public class LiveServerMulti {

	private GecoControl gecoControl;
	private ServerSocket serverSocket;
	private Thread serverThread;

	private Vector<LiveServer> liveThreads;
	private Vector<LiveListener> listeners;

	public LiveServerMulti(GecoControl gecoControl, int port) throws IOException {
		this.gecoControl = gecoControl;
        serverSocket = new ServerSocket(port);
        liveThreads = new Vector<LiveServer>();
        listeners = new Vector<LiveListener>();
	}
	
	public LiveServerMulti accept() {
		serverThread = new Thread() {
			public void run() {
				System.out.println("listening"); //$NON-NLS-1$
				try {
					while( !isInterrupted() ) {
						liveThreads.add(new LiveServer(	gecoControl,
														serverSocket.accept(),
														LiveServerMulti.this).start());
					}
				} catch (IOException e) {}
				System.out.println("not listening"); //$NON-NLS-1$
			}
		};
		serverThread.start();
		return this;
	}
	
	public void terminated(LiveServer live) {
		liveThreads.remove(live);
	}
	
	public void stop() {
		serverThread.interrupt();
        try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		stopAll();
	}

	private void stopAll() {
		@SuppressWarnings("unchecked")
		Vector<LiveServer> copy = (Vector<LiveServer>) liveThreads.clone(); // avoid concurrent modif
		for (LiveServer live : copy) {
			live.stop();
		}
	}

	public void registerListener(LiveListener listener) {
		listeners.add(listener);
	}
	
	public void announceData(RunnerRaceData raceData) {
		for (LiveListener listener : listeners) {
			listener.dataReceived(raceData);
		}
	}

	public void announceNewData() {
		for (LiveListener listener : listeners) {
			listener.newDataIncoming();
		}		
	}
	
}
