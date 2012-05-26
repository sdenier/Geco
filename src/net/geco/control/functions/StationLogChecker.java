/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.functions;

import java.util.Set;

import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.control.RunnerControl;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Status;

/**
 * @author Simon Denier
 * @since May 24, 2012
 *
 */
public class StationLogChecker extends Control {

	private RunnerControl runnerControl;

	public StationLogChecker(GecoControl gecoControl) {
		super(gecoControl);
		runnerControl = getService(RunnerControl.class);
	}
	
	public void checkECards(Set<String> ecards) {
		geco().announcer().dataInfo("Reading e-cards log from stations");
		int nbRunning = 0;
		for (String ecard : ecards) {
			nbRunning += checkECardStatus(ecard);
		}
		geco().log(String.format("%d runners set to Running status", nbRunning));
	}

	public int checkECardStatus(String ecard) {
		Runner runner = registry().findRunnerByEcard(ecard);
		if( runner!=null ){
			RunnerRaceData runnerData = registry().findRunnerData(runner);
			RunnerResult result = runnerData.getResult();
			if( result.is(Status.NOS) ){
				runnerControl.validateStatus(runnerData, Status.RUN);
				return 1;
			} else
			if( result.is(Status.DNS) ){
				geco().log(String.format("WARNING: %s found in running log, but set as DNS in registry",
										runner.idString()));
			}
		} else {
			geco().announcer().dataInfo(
					String.format("WARNING: ecard %s is unregistered, yet found in running log", ecard));
		}
		return 0;
	}

	public void markNotStartedEntriesAsDNS() {
		geco().announcer().dataInfo("Marking remaining entries as DNS");
		int nbDns = 0;
		for (RunnerRaceData runnerData : registry().getRunnersData()) {
			if( runnerData.getResult().is(Status.NOS) ){
				runnerControl.validateStatus(runnerData, Status.DNS);
				nbDns++;
			}
		}
		geco().log(String.format("%d runners set to DNS status", nbDns));
	}

}
