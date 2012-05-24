/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.functions;

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
public class StationLogFunction extends Control {

	private RunnerControl runnerControl;

	public StationLogFunction(GecoControl gecoControl) {
		super(gecoControl);
		runnerControl = getService(RunnerControl.class);
	}

	public void checkECardStatus(String ecard) {
		Runner runner = registry().findRunnerByEcard(ecard);
		if( runner!=null ){
			RunnerRaceData runnerData = registry().findRunnerData(runner);
			RunnerResult result = runnerData.getResult();
			if( result.is(Status.NOS) ){
				runnerControl.validateStatus(runnerData, Status.RUN);
			} else
			if( result.is(Status.DNS) ){
				geco().log(String.format("WARNING: %s found in running log, but set as %s in registry",
										runner.idString(), Status.DNS.toString()));
			}
		} else {
			geco().info(String.format("WARNING: ecard %s is unregistered, yet found in running log", ecard),
						false);
		}
	}

}
