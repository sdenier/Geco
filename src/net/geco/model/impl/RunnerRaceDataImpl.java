/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import java.util.Date;
import java.util.TimeZone;

import net.geco.basics.TimeManager;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.Punch;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Status;
import net.geco.model.Trace;


/**
 * @author Simon Denier
 * @since Nov 23, 2008
 *
 */
public class RunnerRaceDataImpl implements RunnerRaceData {

	private Date starttime;
	
	private Date finishtime;
	
	private Date erasetime;
	
	private Date controltime;

	private Date readtime;
	
	private Punch[] punches;
	
	private RunnerResult result;
	
	private Runner runner;
		

	public RunnerRaceDataImpl() {
		this.starttime = TimeManager.NO_TIME;
		this.finishtime = TimeManager.NO_TIME;
		this.erasetime = TimeManager.NO_TIME;
		this.controltime = TimeManager.NO_TIME;
		this.readtime = TimeManager.NO_TIME;
		this.punches = new Punch[0];
	}
	
	
	public RunnerRaceData clone() {
		try {
			RunnerRaceData clone = (RunnerRaceData) super.clone();
			clone.setStarttime((Date) getStarttime().clone());
			clone.setFinishtime((Date) getFinishtime().clone());
			clone.setErasetime((Date) getErasetime().clone());
			clone.setControltime((Date) getControltime().clone());
			clone.setReadtime((Date) getReadtime().clone());
			Punch[] punches = new Punch[getPunches().length];
			for (int i = 0; i < getPunches().length; i++) {
				punches[i] = getPunches()[i].clone();
			}
			clone.setPunches(punches);
			clone.setResult(getResult().clone());
			// dont clone runner, keep the reference
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void copyFrom(RunnerRaceData data) {
		
		setStarttime(data.getStarttime());
		setFinishtime(data.getFinishtime());
		setErasetime(data.getErasetime());
		setControltime(data.getControltime());
		setReadtime(data.getReadtime());
		setPunches(data.getPunches());
		setResult(data.getResult());
	}

	public Runner getRunner() {
		return runner;
	}

	public void setRunner(Runner runner) {
		this.runner = runner;
	}

	public Date getStarttime() {
		return starttime;
	}

	public void setStarttime(Date starttime) {
		this.starttime = starttime;
	}
	
	public Date getOfficialStarttime() {
		return ( starttime.equals(TimeManager.NO_TIME) ) ?
				getRunner().getRegisteredStarttime() : // return NO_TIME if no registered start time
				starttime;
	}
	
	public boolean useRegisteredStarttime() {
		return this.starttime.equals(TimeManager.NO_TIME)
			&& ! getRunner().getRegisteredStarttime().equals(TimeManager.NO_TIME);
	}

	public Date getFinishtime() {
		return finishtime;
	}

	public void setFinishtime(Date finishtime) {
		this.finishtime = finishtime;
	}

	public Date getErasetime() {
		return erasetime;
	}

	public void setErasetime(Date erasetime) {
		this.erasetime = erasetime;
	}

	public Date getControltime() {
		return controltime;
	}

	public void setControltime(Date controltime) {
		this.controltime = controltime;
	}
	
	@Override
	public Date getReadtime() {
		return this.readtime;
	}

	@Override
	public void setReadtime(Date readtime) {
		this.readtime = readtime;
	}

	@Override
	public Date stampReadtime() {
		// Use TimeZone to set the time with the right offset
		long stamp = System.currentTimeMillis();
		setReadtime(new Date(stamp + TimeZone.getDefault().getOffset(stamp)));
		return getReadtime();
	}

	public Punch[] getPunches() {
		return punches;
	}

	public void setPunches(Punch[] punches) {
		this.punches = punches;
	}
	
	public Course getCourse() {
		return runner.getCourse();
	}
	
	public boolean hasData() {
		return getResult().getStatus().hasData();
	}
	
	public boolean hasResult() {
		return getResult().getStatus().isResolved() && getResult().getStatus().isTraceable();
	}
	
	public boolean hasTrace() {
		return getResult().getStatus().isTraceable();
	}
	
	public boolean statusIsRecheckable() {
		return getResult().getStatus().isRecheckable();
	}
	
	public Status getStatus() {
		return getResult().getStatus();
	}

	public RunnerResult getResult() {
		return result;
	}

	public void setResult(RunnerResult result) {
		this.result = result;
	}

	public long realRaceTime() {
		Date finish = getFinishtime();
		if( finish.equals(TimeManager.NO_TIME) ) {
			return TimeManager.NO_TIME_l;
		}
		Date start = getOfficialStarttime();
		if( start.equals(TimeManager.NO_TIME) ) {
			return TimeManager.NO_TIME_l;
		}
		return finish.getTime() - start.getTime();
	}
	
	public long officialRaceTime() {
		long realRaceTime = realRaceTime();
		if( realRaceTime==TimeManager.NO_TIME_l ){
			return TimeManager.NO_TIME_l;
		} else {
			return realRaceTime + getResult().getTimePenalty();
		}
	}
	
	public String punchSummary(int sumLength) {
		StringBuffer buf = new StringBuffer("("); //$NON-NLS-1$
		int i = 0;
		while( i<sumLength && i<punches.length ) {
			buf.append(punches[i].getCode());
			buf.append(","); //$NON-NLS-1$
			i++;
		}
		buf.append("...)"); //$NON-NLS-1$
		return buf.toString();
	}
	
	public String infoString() {
		StringBuffer buffer = new StringBuffer(getRunner().idString());
		buffer.append(", " + getCourse().getName() + " " + getResult().formatStatus()); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(Messages.getString("RunnerRaceDataImpl.InTimeLabel") + getResult().formatRacetime()); //$NON-NLS-1$
		return buffer.toString();
	}
	
	public String toString() {
		return getRunner().toString();
	}

	@Override
	public Trace[] retrieveLeg(int legStart, int legEnd) {
		return result.retrieveLeg(Integer.toString(legStart), Integer.toString(legEnd));
	}

}
