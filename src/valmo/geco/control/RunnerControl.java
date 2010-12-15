/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import valmo.geco.core.Announcer;
import valmo.geco.core.TimeManager;
import valmo.geco.core.Util;
import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.Course;
import valmo.geco.model.Messages;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Aug 21, 2009
 *
 */
public class RunnerControl extends Control {
	
	private RunnerBuilder builder;
	
	public RunnerControl(GecoControl gecoControl) {
		super(RunnerControl.class, gecoControl);
		builder = new RunnerBuilder(geco().factory());
	}
	
	private Announcer announcer() {
		return geco().announcer();
	}

	public Runner buildMockRunner() {
		Runner runner = factory().createRunner();
		runner.setCourse(registry().anyCourse());
		return runner;		
	}

	public Runner buildBasicRunner(String chip) {
		Runner runner = factory().createRunner();
		runner.setStartnumber(registry().detectMaxStartnumber() + 1);
		runner.setChipnumber(deriveUniqueChipnumber(chip));
		runner.setFirstname(""); //$NON-NLS-1$
		runner.setLastname("X"); //$NON-NLS-1$
		return runner;
	}
	
	public Runner buildAnonymousRunner(String chip, Course course) throws RunnerCreationException {
		Runner runner = buildBasicRunner(chip);
		setDefaults(course, runner);
		return runner;
	}
	private void setDefaults(Course course, Runner runner) throws RunnerCreationException {
		if( course==null )
			throw new RunnerCreationException(Messages.getString("RunnerControl.NoCourseWarning")); //$NON-NLS-1$
		Club club = registry().noClub();
		if( club==null )
			club = registry().anyClub();
		if( club==null )
			throw new RunnerCreationException(Messages.getString("RunnerControl.NoClubWarning")); //$NON-NLS-1$
		Category category = registry().noCategory();
		if( category==null )
			category = registry().anyCategory();
		if( category==null )
			throw new RunnerCreationException(Messages.getString("RunnerControl.NoCategoryWarning")); //$NON-NLS-1$

		runner.setClub(club);
		runner.setCategory(category);
		runner.setCourse(course);
	}

	public Runner createAnonymousRunner(Course course) throws RunnerCreationException {
		Runner runner = buildAnonymousRunner(newUniqueChipnumber(), course);
		registerRunner(runner, builder.buildRunnerData());
		return runner;
	}

	public Runner createAnonymousRunner() throws RunnerCreationException {
		return createAnonymousRunner(registry().anyCourse());
	}

	public String newUniqueChipnumber() {
		return Integer.toString(registry().detectMaxChipnumber() + 1);
	}
	
	public String deriveUniqueChipnumber(String chipnumber) {
		return prvDeriveUniqueEcard(chipnumber, registry().collectChipnumbers());
	}
	private String prvDeriveUniqueEcard(String newEcard, String[] ecards) {
		if( Util.different(newEcard, -1, ecards) )
			return newEcard;
		else 
			return prvDeriveUniqueEcard(newEcard + "a", ecards); //$NON-NLS-1$		
	}

	public RunnerRaceData registerNewRunner(Runner runner) {
		return registerRunner(runner, builder.buildRunnerData());
	}
	
	public RunnerRaceData registerRunner(Runner runner, RunnerRaceData runnerData) {
		registry().addRunner(runner);
		builder.registerRunnerDataFor(registry(), runner, runnerData);
		announcer().announceRunnerCreation(runnerData);
		return runnerData;
	}
	
	public void deleteRunner(RunnerRaceData data) {
		registry().removeRunner(data.getRunner());
		registry().removeRunnerData(data);
		announcer().announceRunnerDeletion(data);
	}
	
	public RunnerRaceData updateRunnerDataFor(Runner runner, RunnerRaceData newData) {
		RunnerRaceData runnerData = registry().findRunnerData(runner);
		Status oldStatus = runnerData.getResult().getStatus();
		runnerData.copyFrom(newData);
		announcer().announceStatusChange(runnerData, oldStatus);
		return runnerData;
	}
	
	
	public boolean verifyStartnumber(Runner runner, int newStart) {
		int oldStart = runner.getStartnumber();
		Integer[] startnums = registry().collectStartnumbers();
		boolean ok = Util.different(newStart, Arrays.binarySearch(startnums, oldStart), startnums);
		if( !ok )
			geco().info(Messages.getString("RunnerControl.StartnumberUsedWarning"), true); //$NON-NLS-1$
		return ok;
	}
	
	public boolean validateStartnumber(Runner runner, String newStartString) {
		try {
			int newStart = new Integer(newStartString);
			if( verifyStartnumber(runner, newStart) ) {
				runner.setStartnumber(newStart);
				return true;
			}
		} catch (NumberFormatException e) {
			geco().info(Messages.getString("RunnerControl.StartnumberFormatWarning"), true); //$NON-NLS-1$
		}
		return false;
	}
	
	public boolean verifyChipnumber(Runner runner, String newChipString) {
		String newChip = newChipString.trim();
		if( newChip.isEmpty() ) {
			geco().info(Messages.getString("RunnerControl.EcardEmptyWarning"), true); //$NON-NLS-1$
			return false;
		}
		String oldChip = runner.getChipnumber();
		String[] chips = registry().collectChipnumbers();
		boolean ok = Util.different(newChip, Arrays.binarySearch(chips, oldChip), chips);
		if( !ok )
			geco().info(Messages.getString("RunnerControl.EcardUsedWarning"), true); //$NON-NLS-1$
		return ok;
	}

	public boolean validateChipnumber(Runner runner, String newChip) {
		if( verifyChipnumber(runner, newChip) ) {
			String oldChip = runner.getChipnumber();
			runner.setChipnumber(newChip.trim());
			registry().updateRunnerChip(oldChip, runner);
			return true;
		} else {
			return false;
		}		
	}
	
	public boolean validateFirstname(Runner runner, String newName) {
		runner.setFirstname(newName.trim());
		return true;
	}

	public boolean verifyLastname(String newName) {
		boolean ok = ! newName.trim().isEmpty();
		if( !ok ) {
			geco().info(Messages.getString("RunnerControl.LastnameEmptyWarning"), true); //$NON-NLS-1$
		}
		return ok;
	}

	public boolean validateLastname(Runner runner, String newName) {
		if( verifyLastname(newName) ) {
			runner.setLastname(newName.trim());
			return true;
		} else {
			return false;
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
			geco().log(Messages.getString("RunnerControl.CategoryChangeMessage1") //$NON-NLS-1$
						+ runner.idString() + Messages.getString("RunnerControl.FromMessage") //$NON-NLS-1$
						+ oldCat.getShortname() + Messages.getString("RunnerControl.ToMessage") //$NON-NLS-1$
						+ newCat);
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
			announcer().announceCourseChange(runner, oldCourse);
			geco().log(Messages.getString("RunnerControl.CourseChangeMessage") //$NON-NLS-1$
						+ runner.idString() + Messages.getString("RunnerControl.FromMessage") //$NON-NLS-1$
						+ oldCourse.getName() + Messages.getString("RunnerControl.ToMessage") //$NON-NLS-1$
						+ newCourse);
			// Proceed by checking the new status
			if( runnerData.statusIsRecheckable() ) {
				Status oldStatus = runnerData.getResult().getStatus();
				geco().checker().check(runnerData);
				announcer().announceStatusChange(runnerData, oldStatus);
			}
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
			if( oldTime!=newTime.getTime() ) {
				runnerData.getResult().setRacetime(newTime.getTime());
				geco().log(Messages.getString("RunnerControl.RacetimeChangeMessage") //$NON-NLS-1$
						+ runnerData.getRunner().idString() + Messages.getString("RunnerControl.FromMessage") //$NON-NLS-1$
						+ TimeManager.fullTime(oldTime) + Messages.getString("RunnerControl.ToMessage") //$NON-NLS-1$
						+ TimeManager.fullTime(newTime));
			}
			return true;
		} catch (ParseException e1) {
			geco().info(Messages.getString("RunnerControl.RacetimeFormatMessage"), true); //$NON-NLS-1$
			return false;
		}
	}
	
	public boolean resetRaceTime(RunnerRaceData runnerData) {
		long oldTime = runnerData.getResult().getRacetime();
		geco().checker().resetRaceTime(runnerData);
		long newTime = runnerData.getResult().getRacetime();
		if( oldTime!=newTime ) {
			geco().log(Messages.getString("RunnerControl.RacetimeResetMessage") //$NON-NLS-1$
						+ runnerData.getRunner().idString() + Messages.getString("RunnerControl.FromMessage") //$NON-NLS-1$
						+ TimeManager.fullTime(oldTime) + Messages.getString("RunnerControl.ToMessage") //$NON-NLS-1$
						+ TimeManager.fullTime(newTime));
		}
		return true;
	}
	
	public boolean validateStatus(RunnerRaceData runnerData, Status newStatus) {
		Status oldStatus = runnerData.getResult().getStatus();
		if( !newStatus.equals(oldStatus) ) {
			runnerData.getResult().setStatus(newStatus);
			geco().log(Messages.getString("RunnerControl.StatusChangeMessage") //$NON-NLS-1$
						+ runnerData.getRunner().idString() + Messages.getString("RunnerControl.FromMessage") //$NON-NLS-1$
						+ oldStatus + Messages.getString("RunnerControl.ToMessage") //$NON-NLS-1$
						+ newStatus);
			announcer().announceStatusChange(runnerData, oldStatus);
			return true;
		}
		return false;
	}
	
	public boolean recheckRunner(RunnerRaceData runnerData) {
		Status oldStatus = runnerData.getResult().getStatus();
		geco().checker().check(runnerData);
		Status newStatus = runnerData.getResult().getStatus();
		if( !oldStatus.equals(newStatus) ) {
			geco().log(Messages.getString("RunnerControl.RecheckMessage") //$NON-NLS-1$
						+ runnerData.getRunner().idString() + Messages.getString("RunnerControl.FromMessage") //$NON-NLS-1$
						+ oldStatus + Messages.getString("RunnerControl.ToMessage") //$NON-NLS-1$
						+ newStatus);
			announcer().announceStatusChange(runnerData, oldStatus);
//			return true;
		}
		return true;
	}
	
	public void recheckOkMpRunners() {
		for (RunnerRaceData data: registry().getRunnersData()) {
			if( data.statusIsRecheckable() ) {
				geco().checker().check(data);
			}
		}
		announcer().announceRunnersChange();
		geco().log(Messages.getString("RunnerControl.RecheckAllMessage")); //$NON-NLS-1$
	}

	public void recheckRunnersFromCourse(Course course) {
		List<Runner> runners = registry().getRunnersFromCourse(course);
		geco().log(Messages.getString("RunnerControl.RecheckCourseMessage") + course.getName()); //$NON-NLS-1$
		for (Runner runner : runners) {
			RunnerRaceData runnerData = registry().findRunnerData(runner);
			if( runnerData.statusIsRecheckable() ) {
				recheckRunner(runnerData);
			}
		}
	}
	
	public boolean validateRegisteredStartTime(Runner runner, String startTime) {
		if( startTime.equals("") ) //$NON-NLS-1$
			return false;
		try {
			Date oldTime = runner.getRegisteredStarttime();
			Date newTime = TimeManager.userParse(startTime);
			if( ! oldTime.equals(newTime) ) {
				runner.setRegisteredStarttime(newTime);
				geco().log(Messages.getString("RunnerControl.RegisteredStartimeChangeMessage") //$NON-NLS-1$
						+ runner.idString() + Messages.getString("RunnerControl.FromMessage") //$NON-NLS-1$
						+ TimeManager.fullTime(oldTime) + Messages.getString("RunnerControl.ToMessage") //$NON-NLS-1$
						+ TimeManager.fullTime(newTime));
			}
			return true;
		} catch (ParseException e1) {
			geco().info(Messages.getString("RunnerControl.RegisteredStartimeWarning"), true); //$NON-NLS-1$
			return false;
		}
	}

	public void updateRegisteredStarttimes(long zeroTime, long oldTime) {
		for (Runner runner : registry().getRunners()) {
			Date relativeTime = TimeManager.relativeTime(runner.getRegisteredStarttime(), oldTime);
			runner.setRegisteredStarttime(TimeManager.absoluteTime(relativeTime, zeroTime));
		}
	}

	public boolean validateArchiveId(Runner runner, String archiveId) {
		if( archiveId.equals("") ) //$NON-NLS-1$
			return false;
		try {
			// TODO: check unicity of archive id
			runner.setArchiveId(Integer.parseInt(archiveId));
			return true;
		} catch (NumberFormatException e) {
			geco().info(Messages.getString("RunnerControl.ArchiveIdFormatWarning"), true); //$NON-NLS-1$
			return false;
		}
	}

}
