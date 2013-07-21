/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import java.util.Date;

import net.geco.basics.TimeManager;
import net.geco.model.Course;
import net.geco.model.ECardData;
import net.geco.model.Messages;
import net.geco.model.Punch;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Status;
import net.geco.model.Trace;
import net.geco.model.TraceData;


/**
 * @author Simon Denier
 * @since Nov 23, 2008
 *
 */
public class RunnerRaceDataImpl implements RunnerRaceData {

	private ECardData ecardData;

	private TraceData traceData;
	
	private RunnerResult result;
	
	private Runner runner;
		

	public RunnerRaceDataImpl() {
		this.ecardData = new ECardDataImpl();
	}
	
	public RunnerRaceData clone() {
		try {
			RunnerRaceData clone = (RunnerRaceData) super.clone();
			clone.setEcardData(ecardData.clone());
			clone.setTraceData(traceData.clone());
			clone.setResult(result.clone());
			// do not clone runner, keep the reference
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void copyFrom(RunnerRaceData data) {
		setEcardData(data.getEcardData());
		setResult(data.getResult());
	}

	public Runner getRunner() {
		return runner;
	}

	public void setRunner(Runner runner) {
		this.runner = runner;
	}

	public ECardData getEcardData() {
		return ecardData;
	}

	public void setEcardData(ECardData ecardData) {
		this.ecardData = ecardData;
	}

	public Date getStarttime() {
		return ecardData.getStartTime();
	}

	public void setStarttime(Date starttime) {
		ecardData.setStartTime(starttime);
	}
	
	public Date getOfficialStarttime() {
		return ( getStarttime().equals(TimeManager.NO_TIME) ) ?
				getRunner().getRegisteredStarttime() : // return NO_TIME if no registered start time
				getStarttime();
	}
	
	public Date getFinishtime() {
		return ecardData.getFinishTime();
	}

	public void setFinishtime(Date finishtime) {
		ecardData.setFinishTime(finishtime);
	}

	public Date getErasetime() {
		return ecardData.getClearTime();
	}

	public void setErasetime(Date erasetime) {
		ecardData.setClearTime(erasetime);
	}

	public Date getControltime() {
		return ecardData.getCheckTime();
	}

	public void setControltime(Date controltime) {
		ecardData.setCheckTime(controltime);
	}
	
	public Date getReadtime() {
		return ecardData.getReadTime();
	}

	public void setReadtime(Date readtime) {
		ecardData.setReadTime(readtime);
	}

	public Date stampReadtime() {
		return ecardData.stampReadTime();
	}

	public Punch[] getPunches() {
		return ecardData.getPunches();
	}

	public void setPunches(Punch[] punches) {
		ecardData.setPunches(punches);
	}
	
	public Course getCourse() {
		return runner.getCourse();
	}
	
	public TraceData getTraceData() {
		return traceData;
	}

	public void setTraceData(TraceData trace) {
		traceData = trace;
	}

	public Status getStatus() {
		return result.getStatus();
	}

	public boolean hasData() {
		return getStatus().hasData();
	}
	
	public boolean hasResult() {
		return getStatus().isResolved() && getStatus().isTraceable();
	}
	
	public boolean hasTrace() {
		return getStatus().isTraceable();
	}
	
	public boolean statusIsRecheckable() {
		return getStatus().isRecheckable();
	}
	
	@Override
	public String getIofStatus() {
		if( runner.isNC() ){
			return "NotCompeting"; //$NON-NLS-1$
		} else {
			return getStatus().iofFormat();
		}
	}

	public RunnerResult getResult() {
		return result;
	}

	public void setResult(RunnerResult result) {
		this.result = result;
	}

	public long getRacetime() {
		return result.getRacetime();
	}

	public long computeRunningTime() {
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
	
	public String infoString() {
		StringBuilder buffer = new StringBuilder(getRunner().idString());
		buffer.append(", " + getCourse().getName() + " " + getResult().formatStatus()); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(Messages.getString("RunnerRaceDataImpl.InTimeLabel") + getResult().formatRacetime()); //$NON-NLS-1$
		return buffer.toString();
	}
	
	public String toString() {
		return getRunner().toString();
	}

	public Trace[] retrieveLeg(int legStart, int legEnd) {
		return traceData.retrieveLeg(Integer.toString(legStart), Integer.toString(legEnd));
	}

	public float getMillisecondPace() {
		float km = getCourse().getLength() / 1000.0f;
		return getRacetime() / km;
	}

	public String formatPace() {
		if( getCourse().hasDistance() ) {
			return TimeManager.time(Math.round(getMillisecondPace()));
		} else {
			return TimeManager.NO_TIME_STRING;
		}
	}

}
