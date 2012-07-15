/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.geco.basics.Announcer;
import net.geco.basics.TimeManager;
import net.geco.model.Category;
import net.geco.model.Club;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;

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
		runner.setCourse(registry().autoCourse());
		return runner;		
	}

	public Runner buildBasicRunner(String chip) {
		Runner runner = factory().createRunner();
		runner.setStartId(registry().nextStartId());
		runner.setEcard(deriveUniqueEcard(chip));
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
		Runner runner = buildAnonymousRunner("", course); //$NON-NLS-1$
		registerRunner(runner, builder.buildRunnerData());
		return runner;
	}

	public Runner createAnonymousRunner() throws RunnerCreationException {
		return createAnonymousRunner(registry().autoCourse());
	}

	public String newUniqueEcard() {
		return Integer.toString(registry().detectMaxEcardNumber() + 1);
	}
	
	public String deriveUniqueEcard(String ecard) {
		if( ecard.equals("") ){ //$NON-NLS-1$
			return ecard;
		}
		return prvDeriveUniqueEcard(ecard, registry().getEcards());
	}
	
	private String prvDeriveUniqueEcard(String newEcard, Collection<String> ecards) {
		char lastDigit;
		
		if( ! ecards.contains(newEcard) )
			return newEcard;
		else {
			lastDigit = newEcard.charAt(newEcard.length() - 1);
			if ( Character.isDigit(lastDigit) ) {
				newEcard = newEcard + "a"; //$NON-NLS-1$
			}
			else if ( lastDigit == 'z') {
				newEcard = newEcard.substring(0, newEcard.length() - 1) + "aa"; //$NON-NLS-1$
			}
			else {
				newEcard = newEcard.substring(0, newEcard.length() - 1) + ++lastDigit;
			}
			return prvDeriveUniqueEcard(newEcard, ecards); //$NON-NLS-1$
		}
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
	

	public void deleteAllRunners() {
		geco().log("Removing all runners");
		ArrayList<RunnerRaceData> runnerData = new ArrayList<RunnerRaceData>(registry().getRunnersData());
		for (RunnerRaceData runner : runnerData) {
			deleteRunner(runner);
		}
	}
	
	public RunnerRaceData updateRunnerDataFor(Runner runner, RunnerRaceData newData) {
		RunnerRaceData runnerData = registry().findRunnerData(runner);
		Status oldStatus = runnerData.getResult().getStatus();
		runnerData.copyFrom(newData);
		announcer().announceStatusChange(runnerData, oldStatus);
		return runnerData;
	}
	
	
	public boolean verifyStartId(Runner runner, Integer newStart) {
		if( newStart.equals(runner.getStartId()) ){ // no change, proceed
			return true;
		}
		boolean existing = registry().getStartIds().contains(newStart);
		if( existing )
			geco().info(Messages.getString("RunnerControl.StartIdUsedWarning"), true); //$NON-NLS-1$
		return ! existing;
	}
	
	public boolean validateStartId(Runner runner, String newStartString) {
		try {
			Integer newStart = Integer.valueOf(newStartString);
			if( verifyStartId(runner, newStart) ) {
				Integer oldId = runner.getStartId();
				runner.setStartId(newStart);
				registry().updateRunnerStartId(oldId, runner);
				return true;
			}
		} catch (NumberFormatException e) {
			geco().info(Messages.getString("RunnerControl.StartIdFormatWarning"), true); //$NON-NLS-1$
		}
		return false;
	}
	
	public boolean verifyEcard(Runner runner, String newEcardString) {
		String newEcard = newEcardString.trim();
		if( newEcard.equals(runner.getEcard()) ){ // no change, proceed
			return true;
		}
		boolean existing = registry().getEcards().contains(newEcard);
		if( existing )
			geco().info(Messages.getString("RunnerControl.EcardUsedWarning"), true); //$NON-NLS-1$
		return ! existing;
	}

	public boolean validateEcard(Runner runner, String newEcard) {
		if( verifyEcard(runner, newEcard) ) {
			String oldEcard = runner.getEcard();
			runner.setEcard(newEcard.trim());
			registry().updateRunnerEcard(oldEcard, runner);
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
	
	public boolean validateCourse(RunnerRaceData runnerData, String newCourseName) {
		Runner runner = runnerData.getRunner();
		Course oldCourse = runner.getCourse();
		if( !oldCourse.getName().equals(newCourseName) ) {
			updateCourse(runner, oldCourse, registry().findCourse(newCourseName));
			geco().log(Messages.getString("RunnerControl.CourseChangeMessage") //$NON-NLS-1$
						+ runner.idString() + Messages.getString("RunnerControl.FromMessage") //$NON-NLS-1$
						+ oldCourse.getName() + Messages.getString("RunnerControl.ToMessage") //$NON-NLS-1$
						+ newCourseName);
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

	public void updateCourse(Runner runner, Course oldCourse, Course newCourse) {
		runner.setCourse(newCourse);
		registry().updateRunnerCourse(oldCourse, runner);
		announcer().announceCourseChange(runner, oldCourse);
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
		geco().log(Messages.getString("RunnerControl.RecheckAllMessage")); //$NON-NLS-1$
		for (RunnerRaceData data: registry().getRunnersData()) {
			if( data.statusIsRecheckable() ) {
				Status oldStatus = data.getStatus();
				long oldRacetime = data.getResult().getRacetime();
				geco().checker().check(data);
				if( oldStatus!=data.getStatus() ){
					geco().log(Messages.getString("RunnerControl.StatusChangeMessage") //$NON-NLS-1$
								+ data.getRunner().idString() + " " //$NON-NLS-1$
								+ oldStatus + " -> " //$NON-NLS-1$
								+ data.getStatus());
				}
				if( oldRacetime!=data.getResult().getRacetime() ){
					geco().log(Messages.getString("RunnerControl.RacetimeResetMessage") //$NON-NLS-1$
								+ data.getRunner().idString() + " " //$NON-NLS-1$
								+ TimeManager.time(oldRacetime) + " -> " //$NON-NLS-1$
								+ data.getResult().formatRacetime());
				}
			}
		}
		announcer().announceRunnersChange();
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
		try {
			Date oldTime = runner.getRegisteredStarttime();
			Date newTime = (startTime.equals("")) ? //$NON-NLS-1$
				TimeManager.NO_TIME
				:
				TimeManager.userParse(startTime);
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
		if( archiveId.equals("") ){ //$NON-NLS-1$
			runner.setArchiveId(null);
			return true;
		}
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
