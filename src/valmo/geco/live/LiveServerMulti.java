/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Vector;

import valmo.geco.control.GecoControl;

/**
 * @author Simon Denier
 * @since Sep 6, 2010
 *
 */
public class LiveServerMulti {

	private ServerSocket serverSocket;
	private Thread serverThread;

	private Vector<LiveServer> liveThreads;

	public LiveServerMulti(GecoControl gecoControl, int port) throws IOException {
        serverSocket = new ServerSocket(port);
        liveThreads = new Vector<LiveServer>();
	}
	
	public LiveServerMulti accept() {
		serverThread = new Thread() {
			public void run() {
				System.out.println("listening");
				try {
					while( !isInterrupted() ) {
						liveThreads.add(new LiveServer(null, serverSocket.accept()).start(LiveServerMulti.this));
					}
				} catch (IOException e) {}
				System.out.println("not listening");
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
		for (LiveServer live : liveThreads) {
			live.stop();
		}
	}
	
}
