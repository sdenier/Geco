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
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Apr 7, 2013
 *
 */
public class ResetECardFunction extends AbstractRunnerFunction {

	private JCheckBox resetAutoCourseC;

	public ResetECardFunction(GecoControl gecoControl) {
		super(gecoControl, FunctionCategory.BATCH);
	}

	@Override
	public String toString() {
		return "Reset données de course";
	}

	@Override
	public String executeTooltip() {
		return "Suppression des données puces, remise à zéro des coureurs dans l'état 'Non parti'"; 
	}

	@Override
	protected boolean acceptRunnerData(RunnerRaceData runnerRaceData) {
		return true;
	}

	@Override
	public void execute() {
		RunnerControl runnerControl = getService(RunnerControl.class);
		RunnerBuilder rBuilder = new RunnerBuilder(geco().factory());
		Collection<RunnerRaceData> selectedRunners = selectedRunners();
		boolean resetAutoCourse = resetAutoCourseC.isSelected();
		Course autoCourse = geco().registry().autoCourse();
		geco().log("Resetting " + selectedRunners.size() + " runners data");
		for (RunnerRaceData runnerData : selectedRunners) {
			Runner runner = runnerData.getRunner();
			runnerControl.updateRunnerDataFor(runner, rBuilder.buildRunnerData());
			if( resetAutoCourse ) {
				runnerControl.updateCourse(runner, runner.getCourse(), autoCourse );
			}
		}
		geco().log("Reset done");
	}

	@Override
	public JComponent getParametersConfig() {
		resetAutoCourseC = new JCheckBox("Reset du circuit en [Auto]");
		resetAutoCourseC.setToolTipText("Force le coureur sur le circuit [Auto] si coché");

//		resetAllRunnersC.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		Box box = Box.createVerticalBox();
		box.add(resetAutoCourseC);
		box.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		box.setAlignmentY(Component.TOP_ALIGNMENT);
		
		JComponent config = super.getParametersConfig();
		config.add(box);
		return config;
	}

	
}
