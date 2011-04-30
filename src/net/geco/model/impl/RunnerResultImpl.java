/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import net.geco.basics.TimeManager;
import net.geco.basics.Util;
import net.geco.model.RunnerResult;
import net.geco.model.Status;
import net.geco.model.Trace;


public class RunnerResultImpl implements RunnerResult {
	
	private long racetime;
	private Status status;
	private int nbMPs;
	private long timePenalty;
	private Trace[] trace;

	public RunnerResultImpl() {
		this.racetime = TimeManager.NO_TIME_l;
		this.status = Status.NOS;
		this.nbMPs = 0;
		this.trace = new Trace[0];
	}
	
	public RunnerResult clone() {
		try {
			RunnerResult clone = (RunnerResult) super.clone();
			Trace[] trace = new Trace[getTrace().length];
			for (int i = 0; i < getTrace().length; i++) {
				trace[i] = getTrace()[i].clone();
			}
			clone.setTrace(trace);
			return clone;
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

	public int getNbMPs() {
		return nbMPs;
	}
	public void setNbMPs(int nbMPs) {
		this.nbMPs = nbMPs;
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
			return Util.join(trace, ",", new StringBuffer()); //$NON-NLS-1$
		else
			return ""; //$NON-NLS-1$
	}
	public String formatMpTrace() {
		if( nbMPs==0 ){
			return ""; //$NON-NLS-1$
		}
		StringBuffer mpTrace = new StringBuffer();
		for (Trace t : trace) {
			if( t.isMP() ){
				mpTrace.append(t.getCode()).append(","); //$NON-NLS-1$
			}
		}
		return mpTrace.substring(0, mpTrace.length() - 1); // remove last ","
	}
	
	
}