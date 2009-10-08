/**
 * Copyright (c) 2008 Simon Denier
 */
package valmo.geco.control;

import java.util.List;

import valmo.geco.core.Announcer;
import valmo.geco.model.Category;
import valmo.geco.model.Course;
import valmo.geco.model.Factory;
import valmo.geco.model.Result;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Nov 23, 2008
 *
 */
public class ResultBuilder extends Control {
	
	public ResultBuilder(Factory factory, Stage stage, Announcer announcer) {
		super(factory, stage, announcer);
	}
	
	public Result buildResultForCategory(Category cat) {
		Result result = factory().createResult();
		result.setIdentifier(cat.getShortname());
		List<Runner> runners = registry().getRunnersFromCategory(cat);
		if( runners!=null ) {
			sortResult(result, runners);
		}
		return result;
	}
	
	public Result buildResultForCourse(Course course) {
		Result result = factory().createResult();
		result.setIdentifier(course.getName());
		List<Runner> runners = registry().getRunnersFromCourse(course);
		if( runners!=null ) {
			sortResult(result, runners);
		}
		return result;
	}

	protected Result sortResult(Result result, List<Runner> runners) {
		for (Runner runner : runners) {
			RunnerRaceData data = registry().findRunnerData(runner);
			if( runner.isNC() ) {
				result.addNRRunner(data);
			} else {
				switch (data.getResult().getStatus()) {
				case OK: 
					result.addRankedRunner(data);
					break;
				case DNF:
				case DSQ:
				case MP:
					result.addNRRunner(data);
					break;
				case DNS:
				case Unknown:
					result.addOtherRunner(data);
				}
			}
		}
		result.sortRankedRunners();
		return result;
	}


}
