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

	public boolean isEmpty();

	public boolean sameCourse();
	
	public Course anyCourse();

	public long bestTime();

	public List<RankedRunner> getRanking();

	public List<RunnerRaceData> getRankedRunners();

	public List<RunnerRaceData> getUnrankedRunners();

	public List<RunnerRaceData> getUnresolvedRunners();

	public int nbFinishedRunners();

	public int nbPresentRunners();

	/*
	 * Building API
	 */
	public void setIdentifier(String identifier);

	public void addRankedRunner(RunnerRaceData runner);

	public void addUnrankedRunner(RunnerRaceData runner);

	public void addUnresolvedRunner(RunnerRaceData runner);

	public void sortRankedRunners();

}