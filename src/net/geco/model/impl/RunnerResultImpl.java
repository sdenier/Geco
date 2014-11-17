/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import net.geco.basics.TimeManager;
import net.geco.model.RunnerResult;
import net.geco.model.Status;


public class RunnerResultImpl implements RunnerResult {

	private long raceTime = TimeManager.NO_TIME_l;
	
	private long resultTime = TimeManager.NO_TIME_l;
	
	private Status status = Status.NOS;
	
	private long timePenalty = 0;

	private long manualTimePenalty = 0;

	public RunnerResult clone() {
		try {
			return (RunnerResult) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public long getRaceTime() {
		return raceTime;
	}

	public void setRaceTime(long raceTime) {
		this.raceTime = raceTime;
	}

	public String formatRaceTime() {
		return TimeManager.time(raceTime);
	}
	
	public long getResultTime() {
		return resultTime;
	}

	public void setResultTime(long resultTime) {
		this.resultTime = resultTime;
	}

	public String formatResultTime() {
		return TimeManager.time(resultTime);
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
			formatResultTime() :
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

	public long getManualTimePenalty() {
		return this.manualTimePenalty;
	}

	public void setManualTimePenalty(long time) {
		this.manualTimePenalty = time;
	}

	public String formatManualTimePenalty() {
		return TimeManager.time(manualTimePenalty);
	}

}