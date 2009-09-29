/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.model;


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

//	public Event createEvent();

	public Punch createPunch();

//	public Race createRace();

	public Runner createRunner();

	public RunnerRaceData createRunnerRaceData();

	public RunnerResult createRunnerResult();

	public Result createResult();

	public Stage createStage();

//	public StartList createStartList();
	
	public HeatSet createHeatSet();
	
	public Heat createHeat();

}