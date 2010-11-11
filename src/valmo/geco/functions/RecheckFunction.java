/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.functions;

import java.util.Collection;

import valmo.geco.control.GecoControl;
import valmo.geco.control.RunnerControl;
import valmo.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public class RecheckFunction extends AbstractRunnerFunction {

	public RecheckFunction(GecoControl gecoControl) {
		super(gecoControl);
	}

	@Override
	public String toString() {
		return "Recheck runners";
	}

	@Override
	public String executeTooltip() {
		return "Recheck status and time for OK|MP runners in selection";
	}

	@Override
	protected boolean acceptRunnerData(RunnerRaceData runnerRaceData) {
		return runnerRaceData.statusIsRecheckable();
	}

	@Override
	public void execute() {
		RunnerControl runnerControl = getService(RunnerControl.class);
		for (RunnerRaceData runnerData : selectedRunners()) {
			if( runnerData.statusIsRecheckable() ){
				runnerControl.recheckRunner(runnerData);
			}
		}
	}


}
