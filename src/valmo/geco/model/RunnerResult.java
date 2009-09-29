/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.model;

import valmo.geco.control.PenaltyChecker.Trace;

/**
 * @author Simon Denier
 * @since Jun 30, 2009
 *
 */
public interface RunnerResult {

	public long getRacetime();

	public void setRacetime(long racetime);

	public Status getStatus();

	public void setStatus(Status status);

	public int getNbMPs();

	public void setNbMPs(int nbMPs);

	public Trace[] getTrace();

	public void setTrace(Trace[] trace);

}