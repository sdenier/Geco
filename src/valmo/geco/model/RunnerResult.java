/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.model;


/**
 * @author Simon Denier
 * @since Jun 30, 2009
 *
 */
public interface RunnerResult extends Cloneable {

	public long getRacetime();

	public void setRacetime(long racetime);

	public Status getStatus();

	public void setStatus(Status status);

	public int getNbMPs();

	public void setNbMPs(int nbMPs);

	public Trace[] getTrace();

	public void setTrace(Trace[] trace);

	public RunnerResult clone();

	
}