/**
 * Copyright (c) 2009 Simon Denier
 */
package net.geco.model;

import java.util.List;

/**
 * @author Simon Denier
 * @since Jun 30, 2009
 *
 */
public interface Result {

	public String getIdentifier();

	public void setIdentifier(String identifier);

	public boolean isEmpty();

	public List<RunnerRaceData> getRankedRunners();

	public void addRankedRunner(RunnerRaceData runner);

	public void clearRankedRunners();

	public List<RankedRunner> getRanking();

	public List<RunnerRaceData> getNRRunners();

	public void addNRRunner(RunnerRaceData runner);

	public void clearNrRunners();

	public List<RunnerRaceData> getOtherRunners();

	public void addOtherRunner(RunnerRaceData runner);

	public void clearOtherRunners();

	public void sortRankedRunners();
	
	public RunnerRaceData anyRunner();

	public Course anyCourse();
	
	public long bestTime();

}