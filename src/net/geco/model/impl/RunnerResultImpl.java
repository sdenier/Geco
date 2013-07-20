/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import java.util.ArrayList;

import net.geco.basics.TimeManager;
import net.geco.basics.Util;
import net.geco.model.RunnerResult;
import net.geco.model.Status;
import net.geco.model.Trace;


public class RunnerResultImpl implements RunnerResult {
	
	private long racetime;
	private Status status;
	private long timePenalty;
	private Trace[] trace;

	public RunnerResultImpl() {
		this.racetime = TimeManager.NO_TIME_l;
		this.status = Status.NOS;
		this.trace = new Trace[0];
	}
	
	public RunnerResult clone() {
		try {
			return (RunnerResult) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public long getRacetime() {
		return racetime;
	}
	public void setRacetime(long racetime) {
		this.racetime = racetime;
	}
	public String formatRacetime() {
		return TimeManager.time(racetime);
	}

	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public String formatStatus() {
		return this.status.toString();
	}
	public boolean is(Status status) {
		return this.status.equals(status);
	}
	
	public String shortFormat() {
		return is(Status.OK) ?
			formatRacetime() :
			formatStatus();
	}

	public void setTimePenalty(long timePenalty) {
		this.timePenalty = timePenalty;
	}
	public long getTimePenalty() {
		return this.timePenalty;
	}
	public String formatTimePenalty() {
		return TimeManager.time(timePenalty);
	}

	public Trace[] getTrace() {
		return trace;
	}
	public void setTrace(Trace[] trace) {
		this.trace = trace;
	}
	public String formatTrace() {
		if( trace.length>0 )
			return Util.join(trace, ",", new StringBuilder()); //$NON-NLS-1$
		else
			return ""; //$NON-NLS-1$
	}
	public String formatMpTrace() {
		if( nbMPs==0 ){
			return ""; //$NON-NLS-1$
		}
		StringBuilder mpTrace = new StringBuilder();
		for (Trace t : trace) {
			if( t.isMP() ){
				mpTrace.append(t.getCode()).append(","); //$NON-NLS-1$
			}
		}
		return mpTrace.substring(0, mpTrace.length() - 1); // remove last ","
	}
	
	@Override
	public Trace[] getClearTrace() {
		ArrayList<Trace> clearTrace = new ArrayList<Trace>(trace.length);
		for (Trace t : trace) {
			if( ! t.isAdded() ){
				clearTrace.add(t);
			}
		}
		return clearTrace.toArray(new Trace[0]);
	}
	@Override
	public String formatClearTrace() {
		Trace[] clearTrace = getClearTrace();
		if( clearTrace.length>0 )
			return Util.join(clearTrace, ",", new StringBuilder()); //$NON-NLS-1$
		else
			return ""; //$NON-NLS-1$
	}

	@Override
	public Trace[] retrieveLeg(String legStart, String legEnd) {
		Trace[] clearTrace = getClearTrace();
		for (int i = 0; i < clearTrace.length-1; i++) {
			if( clearTrace[i].getCode().equals(legStart) && clearTrace[i+1].getCode().equals(legEnd) ){
				return new Trace[]{ clearTrace[i], clearTrace[i+1] };
			}
		}
		return null;
	}
	
}