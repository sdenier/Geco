/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.impl;

import valmo.geco.model.RunnerResult;
import valmo.geco.model.Status;
import valmo.geco.model.Trace;


public class RunnerResultImpl implements RunnerResult {
	
	private long racetime;
	private Status status;
	private int nbMPs;
	private Trace[] trace;

	public RunnerResultImpl() {
		this.racetime = 0;
		this.status = Status.Unknown;
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

	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}

	public int getNbMPs() {
		return nbMPs;
	}
	public void setNbMPs(int nbMPs) {
		this.nbMPs = nbMPs;
	}

	public Trace[] getTrace() {
		return trace;
	}
	public void setTrace(Trace[] trace) {
		this.trace = trace;
	}
	
}