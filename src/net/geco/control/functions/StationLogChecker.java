/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.functions;

import java.util.Set;

import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.control.RunnerControl;
import net.geco.control.ecardmodes.RegisterRunnerHandler;
import net.geco.model.Messages;
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

	private RegisterRunnerHandler registerHandler;

	private boolean simulationMode;

	public StationLogChecker(GecoControl gecoControl, boolean simulation) {
		super(gecoControl);
		runnerControl = getService(RunnerControl.class);
		simulationMode = simulation;
	}
	
	public void checkECards(Set<String> ecards, boolean autoInsert) {
		geco().announcer().dataInfo(Messages.getString("StationLogChecker.ReadingEcardLogMessage")); //$NON-NLS-1$
		int nbRunning = 0;
		for (String ecard : ecards) {
			nbRunning += checkECardStatus(ecard, autoInsert);
		}
		geco().log(String.format(Messages.getString("StationLogChecker.RunningStatusSetMessage"), nbRunning)); //$NON-NLS-1$
	}

	public int checkECardStatus(String ecard, boolean autoInsert) {
		Runner runner = registry().findRunnerByEcard(ecard);
		if( runner!=null ){
			RunnerRaceData runnerData = registry().findRunnerData(runner);
			RunnerResult result = runnerData.getResult();
			if( result.is(Status.NOS) ){
				if( simulationMode ){
					geco().announcer().dataInfo(String.format(Messages.getString("StationLogChecker.WouldSetRunningStatusMessage"), //$NON-NLS-1$
																runnerData.getRunner().idString()));
				} else {
					runnerControl.validateStatus(runnerData, Status.RUN);
				}
				return 1;
			} else
			if( result.is(Status.DNS) ){
				geco().log(String.format(Messages.getString("StationLogChecker.FoundDnsStatusInLogWarning"), //$NON-NLS-1$
										runner.idString()));
			}
		} else {
			if( autoInsert ) {
				if( simulationMode ) {
					geco().announcer().dataInfo(
						String.format(Messages.getString("StationLogChecker.WouldInsertEcardMessage"), ecard)); //$NON-NLS-1$
				} else {
					registerHandler().handleUnregistered(null, ecard);
				}
			} else {
				geco().announcer().dataInfo(
					String.format(Messages.getString("StationLogChecker.UnregisteredEcardLogWarning"), ecard)); //$NON-NLS-1$
			}
		}
		return 0;
	}
	
	private RegisterRunnerHandler registerHandler() {
		if( registerHandler == null ) {
			registerHandler = new RegisterRunnerHandler(geco());
		}
		return registerHandler;
	}

	public void markNotStartedEntriesAsDNS(Set<String> ecards) {
		geco().announcer().dataInfo(Messages.getString("StationLogChecker.MarkDnsMessage")); //$NON-NLS-1$
		int nbDns = 0;
		for (RunnerRaceData runnerData : registry().getRunnersData()) {
			if( runnerData.getResult().is(Status.NOS) ){
				if( simulationMode ) {
					Runner runner = runnerData.getRunner();
					if( ! ecards.contains(runner.getEcard()) ) {
						geco().announcer().dataInfo(
								String.format(Messages.getString("StationLogChecker.WouldMarkDnsMessage"), runner.idString())); //$NON-NLS-1$
						nbDns++;
					} // else would have been set to Running
				} else {
					runnerControl.validateStatus(runnerData, Status.DNS);
					nbDns++;
				}
			}
		}
		geco().log(String.format(Messages.getString("StationLogChecker.DnsStatusSetMessage"), nbDns)); //$NON-NLS-1$
	}

}
