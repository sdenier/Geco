/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import net.geco.control.ArchiveManager;
import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.control.RunnerBuilder;
import net.geco.control.RunnerControl;
import net.geco.control.RunnerCreationException;
import net.geco.control.StageControl;
import net.geco.model.Course;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;

/**
 * @author Simon Denier
 * @since Mar 24, 2012
 *
 */
public class RegisterRunnerHandler extends Control implements ECardHandler {

	private RunnerControl runnerControl;

	private ArchiveManager archive;

	private RunnerBuilder builder;

	private Course autoCourse;

	public RegisterRunnerHandler(GecoControl gecoControl) {
		super(gecoControl);
		runnerControl = getService(RunnerControl.class);
		archive = getService(ArchiveManager.class);
		builder = new RunnerBuilder(factory());
		autoCourse = ensureAutoCourseInRegistry();
	}

	public Course ensureAutoCourseInRegistry() {
		Course autoCourse = registry().findCourse("[Auto]");
		if( autoCourse==null ){
			getService(StageControl.class).createCourse("[Auto]");
		}
		return autoCourse;
	}

	@Override
	public String handleFinish(RunnerRaceData data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String handleDuplicate(RunnerRaceData data, Runner runner) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String handleUnregistered(RunnerRaceData d, String cardId) {
		Runner runner = archive.findAndCreateRunner(cardId, autoCourse);
		RunnerRaceData runnerData = builder.buildRunnerData();
		runnerData.getResult().setStatus(Status.RUN);
		if( runner == null ) {
			try {
				runner = runnerControl.buildAnonymousRunner(cardId, autoCourse);
			} catch (RunnerCreationException e) {
				geco().log(e.getLocalizedMessage());
				return null;
			}
		}
		runnerControl.registerRunner(runner, runnerData);
		geco().log("Register " + runner.idString()); //$NON-NLS-1$
		
		return cardId;
	}

	@Override
	public boolean foundInArchive() {
		return false;
	}

}
