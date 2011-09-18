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

	public long getRacetime();

	public void setRacetime(long racetime);
	
	public String formatRacetime();

	public Status getStatus();

	public void setStatus(Status status);
	
	public String formatStatus();
	
	public boolean is(Status status);
	
	public String shortFormat();

	public int getNbMPs();

	public void setNbMPs(int nbMPs);

	public void setTimePenalty(long timePenalty);
	
	public long getTimePenalty();

	public String formatTimePenalty();

	public Trace[] getTrace();

	public Trace[] getClearTrace();

	public void setTrace(Trace[] trace);
	
	public String formatTrace();
	
	public String formatMpTrace();

	public String formatClearTrace();

	public RunnerResult clone();

	public boolean hasLeg(String legStart, String legEnd);
	
}