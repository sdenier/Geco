/**
 * Copyright (c) 2008 Simon Denier
 */
package valmo.geco.model.impl;

import java.util.Date;

import valmo.geco.core.TimeManager;
import valmo.geco.model.Course;
import valmo.geco.model.Punch;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.RunnerResult;
import valmo.geco.model.Status;

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
	
	private Punch[] punches;
	
	private RunnerResult result;
	
	private Runner runner;
		

	public RunnerRaceDataImpl() {
		this.starttime = TimeManager.ZEROTIME;
		this.finishtime = TimeManager.ZEROTIME;
		this.erasetime = TimeManager.ZEROTIME;
		this.controltime = TimeManager.ZEROTIME;
		this.punches = new Punch[0];
	}
	
	
	public RunnerRaceData clone() {
		try {
			RunnerRaceData clone = (RunnerRaceData) super.clone();
			clone.setStarttime((Date) getStarttime().clone());
			clone.setFinishtime((Date) getFinishtime().clone());
			clone.setErasetime((Date) getErasetime().clone());
			clone.setControltime((Date) getControltime().clone());
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

	public Punch[] getPunches() {
		return punches;
	}

	public void setPunches(Punch[] punches) {
		this.punches = punches;
	}
	
	public Course getCourse() {
		return runner.getCourse();
	}
	
	public boolean hasResult() {
		return !getResult().getStatus().equals(Status.Unknown);
	}

	public RunnerResult getResult() {
		return result;
	}

	public void setResult(RunnerResult result) {
		this.result = result;
	}

	public boolean isRunning() {
		return getResult().getStatus().equals(Status.Unknown);
	}
	
	public long raceTime() {
		return getFinishtime().getTime() - getStarttime().getTime();
	}
	
	public String punchSummary(int sumLength) {
		StringBuffer buf = new StringBuffer("(");
		int i = 0;
		while( i<sumLength && i<punches.length ) {
			buf.append(punches[i].getCode());
			buf.append(",");
			i++;
		}
		buf.append("...)");
		return buf.toString();
	}
	
}
