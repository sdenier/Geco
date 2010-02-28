/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.impl;

import java.util.Date;

import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.Course;
import valmo.geco.model.Factory;
import valmo.geco.model.Heat;
import valmo.geco.model.HeatSet;
import valmo.geco.model.Punch;
import valmo.geco.model.Result;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.RunnerResult;
import valmo.geco.model.Stage;
import valmo.geco.model.Trace;


/**
 * Implementation of Factory using Plain Objects.
 * 
 * @author Simon Denier
 * @since Nov 23, 2008
 *
 */
public class POFactory implements Factory {
	
	public Category createCategory() {
		return new CategoryImpl();
	}
	
	public Club createClub() {
		return new ClubImpl();
	}
	
	public Course createCourse() {
		return new CourseImpl();
	}
	
//	public Event createEvent() {
//		return new EventImpl();
//	}
	
	public Punch createPunch() {
		return new PunchImpl();
	}
	
//	public Race createRace() {
//		return new Race();
//	}
	
	public Runner createRunner() {
		return new RunnerImpl();
	}
	
	public RunnerRaceData createRunnerRaceData() {
		return new RunnerRaceDataImpl();
	}
	
	public RunnerResult createRunnerResult() {
		return new RunnerResultImpl();
	}
	
	public Trace createTrace(Punch punch) {
		return new TraceImpl(punch);
	}

	@Override
	public Trace createTrace(String code, Date time) {
		return new TraceImpl(code, time);
	}

	public Result createResult() {
		return new ResultImpl();
	}
	
	public Stage createStage() {
		return new StageImpl();
	}
	
//	public StartList createStartList() {
//		return new StartList();
//	}

	@Override
	public HeatSet createHeatSet() {
		return new HeatSetImpl();
	}

	@Override
	public Heat createHeat() {
		return new HeatImpl();
	}
	
}
