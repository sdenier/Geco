/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.functions;

import java.awt.Component;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import net.geco.control.GecoControl;
import net.geco.control.RunnerBuilder;
import net.geco.control.RunnerControl;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Apr 7, 2013
 *
 */
public class ResetECardOperation extends AbstractRunnerOperation {

	private JCheckBox resetAutoCourseC;

	public ResetECardOperation(GecoControl gecoControl) {
		super(gecoControl, OperationCategory.BATCH);
	}

	@Override
	public String toString() {
		return Messages.uiGet("ResetECardFunction.ResetECardTitle"); //$NON-NLS-1$
	}

	@Override
	public String runTooltip() {
		return Messages.uiGet("ResetECardFunction.ResetECardTooltip");  //$NON-NLS-1$
	}

	@Override
	protected boolean acceptRunnerData(RunnerRaceData runnerRaceData) {
		return true;
	}

	@Override
	public void run() {
		RunnerControl runnerControl = getService(RunnerControl.class);
		RunnerBuilder rBuilder = new RunnerBuilder(geco().factory());
		Collection<RunnerRaceData> selectedRunners = selectedRunners();
		boolean resetAutoCourse = resetAutoCourseC.isSelected();
		Course autoCourse = geco().registry().autoCourse();
		geco().log("Resetting " + selectedRunners.size() + " runners data"); //$NON-NLS-1$ //$NON-NLS-2$
		for (RunnerRaceData runnerData : selectedRunners) {
			Runner runner = runnerData.getRunner();
			runnerControl.updateRunnerDataFor(runner, rBuilder.buildRunnerData());
			if( resetAutoCourse ) {
				runnerControl.updateCourse(runner, runner.getCourse(), autoCourse );
			}
		}
		geco().log("Reset done"); //$NON-NLS-1$
	}

	@Override
	public JComponent buildInnerUI() {
		resetAutoCourseC = new JCheckBox(Messages.uiGet("ResetECardFunction.ResetAutoCourseLabel")); //$NON-NLS-1$
		resetAutoCourseC.setToolTipText(Messages.uiGet("ResetECardFunction.ResetAutoCourseTooltip")); //$NON-NLS-1$
		
		Box box = Box.createVerticalBox();
		box.add(resetAutoCourseC);
		embedRunButton(box);
		box.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		box.setAlignmentY(Component.TOP_ALIGNMENT);
		
		JComponent config = super.buildInnerUI();
		config.add(box);
		return config;
	}

	
}
