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

	public long getRunningTime();

	public void setRunningTime(long runningTime);
	
	public String formatRunningTime();

	public long getRacetime();

	public void setRacetime(long racetime);
	
	public String formatRacetime();

	public Status getStatus();

	public void setStatus(Status status);
	
	public String formatStatus();
	
	public boolean is(Status status);
	
	public String shortFormat();

	public void setTimePenalty(long timePenalty);
	
	public long getTimePenalty();

	public String formatTimePenalty();

	public RunnerResult clone();
	
}