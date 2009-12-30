/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.control;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import valmo.geco.core.Announcer;
import valmo.geco.core.Geco;
import valmo.geco.core.TimeManager;
import valmo.geco.core.Util;
import valmo.geco.model.Category;
import valmo.geco.model.Course;
import valmo.geco.model.Factory;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Aug 21, 2009
 *
 */
public class RunnerControl extends RunnerBuilder {
	
	private Geco geco;
	private Announcer announcer;
	
	/**
	 * @param factory
	 * @param stage
	 * @param announcer 
	 */
	public RunnerControl(Factory factory, Stage stage, Geco geco, Announcer announcer) {
		super(factory, stage, announcer);
		this.geco = geco;
		this.announcer = announcer;
	}
	
	private Announcer announcer() {
		return this.announcer;
	}

	public Runner buildMockRunner() {
		Runner runner = factory().createRunner();
		runner.setCourse(registry().anyCourse());
		return runner;		
	}
	
	public Runner buildAnonymousRunner(String chip) {
		Runner runner = factory().createRunner();
		runner.setStartnumber(registry().detectMaxStartnumber() + 1);
		runner.setChipnumber(chip);
		runner.setFirstname("John");
		runner.setLastname("Doe");
		runner.setClub(registry().noClub());
		runner.setCategory(registry().noCategory());
		runner.setCourse(registry().anyCourse());
		return runner;
	}
	
	public Runner createAnonymousRunner() {
		Runner runner = buildAnonymousRunner(newUniqueChipnumber());
		registerRunner(runner, buildRunnerData());
		return runner;
	}

	public String newUniqueChipnumber() {
		return Integer.toString(registry().detectMaxChipnumber() + 1);
	}
	
	public String deriveUniqueChipnumber(String chipnumber) {
		String[] chips = registry().collectChipnumbers();
		if( Util.different(chipnumber, -1, chips) ) {
			return chipnumber;
		} else 
			return deriveUniqueChipnumber(chipnumber + "a");
	}
	
	public RunnerRaceData registerRunner(Runner runner, RunnerRaceData runnerData) {
		registry().addRunner(runner);
		announcer().announceRunnerCreation(registerRunnerDataFor(runner, runnerData));
		return runnerData;
	}
	
	public void deleteRunner(RunnerRaceData data) {
		registry().removeRunner(data.getRunner());
		registry().removeRunnerData(data);
		announcer().announceRunnerDeletion(data);
	}
	
	public void updateRunnerDataFor(Runner runner, RunnerRaceData newData) {
		RunnerRaceData runnerData = registry().findRunnerData(runner);
		Status oldStatus = runnerData.getResult().getStatus();
		runnerData.copyFrom(newData);
//		registerRunnerDataFor(runner, runnerData);
		announcer().announceStatusChange(runnerData, oldStatus);
	}
	
	
	public boolean validateStartnumber(Runner runner, int newStart) {
		int oldStart = runner.getStartnumber();
		Integer[] startnums = registry().collectStartnumbers();
		if( Util.different(newStart, Arrays.binarySearch(startnums, oldStart), startnums)) {
			runner.setStartnumber(newStart);
			return true;
		} else {
//			"Start number already used. Reverting to previous start number.",
			return false;
		}		
	}

	public boolean validateChipnumber(Runner runner, String newChip) {
		String oldChip = runner.getChipnumber();
		String[] chips = registry().collectChipnumbers();
		if( Util.different(newChip, Arrays.binarySearch(chips, oldChip), chips)) {
			runner.setChipnumber(newChip);
			registry().updateRunnerChip(oldChip, runner);
			return true;
		} else {
//			"Chip number already in use. Reverting to previous chip number.",
			return false;
		}		
	}
	
	public boolean validateFirstname(Runner runner, String newName) {
		runner.setFirstname(newName);
		return true;
	}

	public boolean validateLastname(Runner runner, String newName) {
		if( newName.length()==0 ) {
//			"Last name can not be empty. Reverting.",
			return false;
		} else {
			runner.setLastname(newName);
			return true;
		}
	}
	
	public boolean validateClub(Runner runner, String newClub) {
		runner.setClub( registry().findClub(newClub) );
		return true;
	}
	
	public boolean validateCategory(Runner runner, String newCat) {
		Category oldCat = runner.getCategory();
		if( !oldCat.getShortname().equals(newCat) ) {
			runner.setCategory(registry().findCategory(newCat));
			registry().updateRunnerCategory(oldCat, runner);
			geco.logger().log("Category change for " + runner.idString() + " from " + oldCat.getShortname() + " to " + newCat);
			return true;
		}
		return false;
	}
	
	public boolean validateCourse(RunnerRaceData runnerData, String newCourse) {
		Runner runner = runnerData.getRunner();
		Course oldCourse = runner.getCourse();
		if( !oldCourse.getName().equals(newCourse) ) {
			runner.setCourse(registry().findCourse(newCourse));
			registry().updateRunnerCourse(oldCourse, runner);
			Status oldStatus = runnerData.getResult().getStatus();
			geco.checker().check(runnerData); // use and share an action with refresh button
			geco.logger().log("Course change for " + runner.idString() + " from " + oldCourse.getName() + " to " + newCourse);
			announcer().announceCourseChange(runner, oldCourse);
			announcer().announceStatusChange(runnerData, oldStatus);
			return true;
		}
		return false;
	}

	public boolean validateNCStatus(Runner runner, boolean nc) {
		runner.setNC(nc);
		return true;
	}
	
	public boolean validateRaceTime(RunnerRaceData runnerData, String raceTime) {
		try {
			long oldTime = runnerData.getResult().getRacetime();
			Date newTime = TimeManager.userParse(raceTime);
			runnerData.getResult().setRacetime(newTime.getTime());
			geco.logger().log("Race time change for " + runnerData.getRunner().idString() + " from " + TimeManager.fullTime(oldTime) + " to " + TimeManager.fullTime(newTime));
			return true;
		} catch (ParseException e1) {
			return false;
		}
	}
	
	public boolean resetRaceTime(RunnerRaceData runnerData) {
		long oldTime = runnerData.getResult().getRacetime();
		geco.checker().resetRaceTime(runnerData);
		long newTime = runnerData.getResult().getRacetime();
		if( oldTime!=newTime ) {
			geco.logger().log("Race time reset for " + runnerData.getRunner().idString() + " from " + TimeManager.fullTime(oldTime) + " to " + TimeManager.fullTime(newTime));
		}
		return true;
	}
	
	public boolean validateStatus(RunnerRaceData runnerData, Status newStatus) {
		Status oldStatus = runnerData.getResult().getStatus();
		if( !newStatus.equals(oldStatus) ) {
			runnerData.getResult().setStatus(newStatus);
			geco.logger().log("Status change for " + runnerData.getRunner().idString() + " from " + oldStatus + " to " + newStatus);
			announcer().announceStatusChange(runnerData, oldStatus);
			return true;
		}
		return false;
	}
	
	public boolean resetStatus(RunnerRaceData runnerData) {
		Status oldStatus = runnerData.getResult().getStatus();
		geco.checker().check(runnerData);
		Status newStatus = runnerData.getResult().getStatus();
		if( !oldStatus.equals(newStatus) ) {
			geco.logger().log("Status reset for " + runnerData.getRunner().idString() + " from " + oldStatus + " to " + newStatus);
			announcer().announceStatusChange(runnerData, oldStatus);
			return true;
		}
		return false;
	}
	
	public void recheckAllRunners() {
		for (RunnerRaceData data: geco.registry().getRunnersData()) {
			if( data.getResult().getStatus().equals(Status.OK) 
					|| data.getResult().getStatus().equals(Status.MP) ) {
				geco.checker().check(data);
			}
		}
		announcer().announceRunnersChange();
		geco.logger().log("Recheck all OK|MP data");
	}


}
