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

	private StageControl stageControl;
	
	private RunnerControl runnerControl;

	private ArchiveManager archiveManager;

	private RunnerBuilder builder;

	public RegisterRunnerHandler(GecoControl gecoControl) {
		super(gecoControl);
		stageControl = getService(StageControl.class);
		runnerControl = getService(RunnerControl.class);
		archiveManager = getService(ArchiveManager.class);
		builder = new RunnerBuilder(factory());
	}

	@Override
	public String handleFinish(RunnerRaceData data) {
		if( data.getResult().is(Status.NOS) ) {
			data.getResult().setStatus(Status.RUN);
			geco().announcer().announceStatusChange(data, Status.NOS);
		}
		return null;}

	@Override
	public String handleDuplicate(RunnerRaceData data, Runner runner) {return null;}

	@Override
	public String handleUnregistered(RunnerRaceData nullRunner, String cardId) {
		Course autoCourse = stageControl.getAutoCourse();
		Runner runner = archiveManager.findAndCreateRunner(cardId, autoCourse);
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
