/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model;

/**
 * @author Simon Denier
 * @since Jul 20, 2013
 *
 */
public interface TraceData extends Cloneable {

	public long getRunningTime();

	public void setRunningTime(long runningTime);
	
	public String formatRunningTime();

	public int getNbMPs();

	public void setNbMPs(int nbMPs);

	public Trace[] getTrace();

	public Trace[] getClearTrace();

	public Trace[] getPunchTrace();

	public void setTrace(Trace[] trace);
	
	public String formatTrace();
	
	public String formatMpTrace();

	public String formatClearTrace();

	public String formatPunchTrace();

	public Trace[] retrieveLeg(String legStart, String legEnd);
	
	public TraceData clone();

}
