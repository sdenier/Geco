/**
 * Copyright (c) 2009 Simon Denier
 */
package net.geco.model;

import java.util.Date;


/**
 * Factory implenents the Abstract Factory design pattern to hide (some) details about implementation.
 * Needed for future implementation of model persistency.
 * 
 * @author Simon Denier
 * @since Jan 18, 2009
 *
 */
public interface Factory {

	public Category createCategory();

	public Club createClub();

	public Course createCourse();

	public Section createSection();
	
	public Punch createPunch();

	public Runner createRunner();

	public RunnerRaceData createRunnerRaceData();

	public RunnerResult createRunnerResult();
	
	public Trace createTrace(Punch punch);
	
	public Trace createTrace(String code, Date time);

	public Result createResult();

	public Stage createStage();
	
	public HeatSet createHeatSet();
	
	public Heat createHeat();
	
	public ArchiveRunner createArchiveRunner();

}