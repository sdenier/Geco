/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.results.context;

import net.geco.basics.TimeManager;
import net.geco.model.Messages;
import net.geco.model.RankedRunner;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;

/**
 * @author Simon Denier
 * @since May 6, 2013
 *
 */
public class RunnerContext extends GenericContext {

	public static RunnerContext createRankedRunner(RankedRunner rankedRunner, long bestTime) {
		RunnerRaceData data = rankedRunner.getRunnerData();
		return new RunnerContext()
					.buildBasicContext(data.getRunner())
					.buildResultContext(data)
					.buildRank(	Integer.toString(rankedRunner.getRank()),
								rankedRunner.formatDiffTime(bestTime));
	}

	public static RunnerContext createUnrankedRunner(RunnerRaceData data) {
		return new RunnerContext()
					.buildBasicContext(data.getRunner())
					.buildResultContext(data)
					.buildRank("", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static RunnerContext createNCRunner(RunnerRaceData data) {
		return new RunnerContext()
					.buildBasicContext(data.getRunner())
					.buildResultContext(data)
					.buildRank(Messages.getString("ResultExporter.NC"), ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected RunnerContext buildBasicContext(Runner runner) {
		put("geco_RunnerFirstName", runner.getFirstname()); //$NON-NLS-1$
		put("geco_RunnerLastName", runner.getLastname()); //$NON-NLS-1$
		put("geco_RunnerClubName", runner.getClub().getName()); //$NON-NLS-1$
		put("geco_RunnerCategory", runner.getCategory().getName()); //$NON-NLS-1$
		return this;
	}
	
	protected RunnerContext buildResultContext(RunnerRaceData data) {
		RunnerResult result = data.getResult();
		put("geco_RunnerStatus", result.formatStatus()); //$NON-NLS-1$
		put("geco_RunnerResultTime", result.formatRacetime()); //$NON-NLS-1$
		put("geco_RunnerStatusOrTime", result.shortFormat()); //$NON-NLS-1$
		put("geco_RunnerNbMPs", result.getNbMPs()); //$NON-NLS-1$
		put("geco_RunnerRaceTime", TimeManager.time(data.realRaceTime())); //$NON-NLS-1$
		put("geco_RunnerPace", data.formatPace()); //$NON-NLS-1$
		return this;
	}

	protected RunnerContext buildRank(String rank, String diffTime) {
		put("geco_RunnerRank", rank); //$NON-NLS-1$
		put("geco_RunnerDiffTime", diffTime); //$NON-NLS-1$
		return this;
	}

}
