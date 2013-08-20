/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import java.util.Date;

import net.geco.model.ArchiveRunner;
import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.Heat;
import net.geco.model.HeatSet;
import net.geco.model.Punch;
import net.geco.model.Result;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Section;
import net.geco.model.Stage;
import net.geco.model.Trace;
import net.geco.model.TraceData;



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
	
	public Section createSection() {
		return new SectionImpl();
	}
	
	public Punch createPunch() {
		return new PunchImpl();
	}
	
	public Runner createRunner() {
		return new RunnerImpl();
	}
	
	public RunnerRaceData createRunnerRaceData() {
		return new RunnerRaceDataImpl();
	}
	
	public TraceData createTraceData() {
		return new TraceDataImpl();
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

	@Override
	public HeatSet createHeatSet() {
		return new HeatSetImpl();
	}

	@Override
	public Heat createHeat() {
		return new HeatImpl();
	}

	@Override
	public ArchiveRunner createArchiveRunner() {
		return new ArchiveRunnerImpl();
	}
	
}
