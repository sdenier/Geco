/**
 * Copyright (c) 2009 Simon Denier
 */
package net.geco.model;


/**
 * @author Simon Denier
 * @since Jun 30, 2009
 *
 */
public interface RunnerResult extends Cloneable {

	public RunnerResult clone();

	public long getRaceTime();

	public void setRaceTime(long raceTime);
	
	public String formatRaceTime();

	public long getResultTime();

	public void setResultTime(long resultTime);
	
	public String formatResultTime();

	public Status getStatus();

	public void setStatus(Status status);
	
	public String formatStatus();
	
	public boolean is(Status status);
	
	public String shortFormat();

	public void setTimePenalty(long timePenalty);
	
	public long getTimePenalty();

	public String formatTimePenalty();

	public long getManualTimePenalty();

	public void setManualTimePenalty(long time);

	public String formatManualTimePenalty();
	
}