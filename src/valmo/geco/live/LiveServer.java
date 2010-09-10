/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JOptionPane;

import valmo.geco.control.Control;
import valmo.geco.control.GecoControl;
import valmo.geco.control.RunnerControl;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.RunnerResult;
import valmo.geco.model.Status;
import valmo.geco.model.Trace;

/**
 * @author Simon Denier
 * @since Sep 6, 2010
 *
 */
public class LiveServer extends Control {

	private Socket clientSocket;
	private Thread liveThread;
	private LiveServerMulti serverMulti;
	private RunnerControl runnerControl;

	public LiveServer(GecoControl gecoControl, Socket clientSocket, LiveServerMulti serverMulti) throws IOException {
		super(gecoControl);
		runnerControl = new RunnerControl(gecoControl);
		this.clientSocket = clientSocket;
		this.serverMulti = serverMulti;
	}
	
	public LiveServer start() {
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
		serverMulti.terminated(this);
	}

	public void stop() {
		liveThread.interrupt();
		close();
	}

	private String processData(String input) {
		if( input.equals("Hi") ||
			input.equals("Idle") ||
			input.equals("Bye") )
			return input;
		return processData(input.split(","));
	}
	
	private String processData(String[] data) {
		// chip, start, last, first, cat, club, course, status, racetime, mps, penalties, trace...
		Runner runner = registry().findRunnerByChip(data[0]);
		if( runner==null || Integer.parseInt(data[1])!=runner.getStartnumber() ) {
			System.err.println("creation " + data[0]);
			runner = runnerControl.createAnonymousRunner(registry().findCourse(data[6]));
			runnerControl.validateChipnumber(runner, data[0]);
			runnerControl.validateStartnumber(runner, data[1]);
			runnerControl.validateLastname(runner, data[2]);
			runnerControl.validateFirstname(runner, data[3]);
			runnerControl.validateCategory(runner, data[4]);
			runnerControl.validateClub(runner, data[5]);
			serverMulti.announceNewData();
		} else { // check that course is consistent
			if( !runner.getCourse().getName().equals(data[6]) ) {
				System.err.println("inconsistent course " + data[6]);
				runnerControl.validateCourse(registry().findRunnerData(runner), data[6]);
			}
		}
		
		RunnerResult result = factory().createRunnerResult();
		result.setStatus(Status.valueOf(data[7]));
		result.setRacetime(Long.parseLong(data[8]));
		result.setNbMPs(Integer.parseInt(data[9]));
		result.setTimePenalty(Long.parseLong(data[10]));
		result.setTrace(createTraceFrom(Arrays.copyOfRange(data, 11, data.length)));
		
		RunnerRaceData runnerData = registry().findRunnerData(runner);
		runnerData.setResult(result);
		serverMulti.announceData(runnerData);
		return "ok";
	}

	/**
	 * @param copyOfRange
	 * @return
	 */
	private Trace[] createTraceFrom(String[] trace) {
		Trace[] trace2 = new Trace[trace.length];
		for (int i = 0; i < trace.length; i++) {
			trace2[i] = factory().createTrace(trace[i], new Date(0));
		}
		return trace2;
	}
	
}
