/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.functions;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.JComponent;

import net.geco.control.GecoControl;
import net.geco.control.RunnerControl;
import net.geco.model.Messages;
import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public class RecheckFunction extends AbstractRunnerFunction {

	public RecheckFunction(GecoControl gecoControl) {
		super(gecoControl, OperationCategory.BATCH);
	}

	@Override
	public String toString() {
		return Messages.uiGet("RecheckFunction.RecheckTitle"); //$NON-NLS-1$
	}

	@Override
	public String runTooltip() {
		return Messages.uiGet("RecheckFunction.ExecuteTooltip"); //$NON-NLS-1$
	}

	@Override
	protected boolean acceptRunnerData(RunnerRaceData runnerRaceData) {
		return runnerRaceData.statusIsRecheckable();
	}

	@Override
	public void run() {
		RunnerControl runnerControl = getService(RunnerControl.class);
		for (RunnerRaceData runnerData : selectedRunners()) {
			if( runnerData.statusIsRecheckable() ){
				runnerControl.recheckRunner(runnerData);
			}
		}
	}

	@Override
	public JComponent buildInnerUI() {
		JComponent config = super.buildInnerUI();
		Box box = Box.createVerticalBox();
		embedRunButton(box);
		box.setAlignmentY(Component.TOP_ALIGNMENT);
		config.add(box);
		return config;
	}

}
