/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import net.geco.basics.TimeManager;
import net.geco.model.RunnerResult;
import net.geco.model.Status;


public class RunnerResultImpl implements RunnerResult {

	private long runningTime = TimeManager.NO_TIME_l;
	
	private long racetime = TimeManager.NO_TIME_l;
	
	private Status status = Status.NOS;
	
	private long timePenalty = 0;

	public RunnerResult clone() {
		try {
			return (RunnerResult) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public long getRunningTime() {
		return runningTime;
	}

	public void setRunningTime(long runningTime) {
		this.runningTime = runningTime;
	}

	public String formatRunningTime() {
		return TimeManager.time(runningTime);
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

}