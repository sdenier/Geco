/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;


/**
 * @author Simon Denier
 * @since Mar 14, 2012
 *
 */
public class ECardTrainingMode extends AbstractECardMode {

	public ECardTrainingMode(GecoControl gecoControl) {
		super(ECardReadingMode.class, gecoControl);
	}

	@Override
	public void handleFinished(RunnerRaceData runnerData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleDuplicate(RunnerRaceData runnerData, String cardId,
			Runner runner) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleUnknown(RunnerRaceData runnerData, String cardId) {
		// TODO Auto-generated method stub
		
	}

}
