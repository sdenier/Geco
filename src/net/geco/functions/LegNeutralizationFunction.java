/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.functions;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.geco.basics.TimeManager;
import net.geco.control.GecoControl;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Trace;
import net.geco.ui.basics.SwingUtils;

/**
 * @author Simon Denier
 * @since Sep 18, 2011
 *
 */
public class LegNeutralizationFunction extends GecoFunction {

	private int legStart;
	private int legEnd;
	private JCheckBox simulateCB;

	public LegNeutralizationFunction(GecoControl gecoControl) {
		super(gecoControl, FunctionCategory.REFEREE);
	}

	@Override
	public String toString() {
		return Messages.uiGet("LegNeutralizationFunction.Title"); //$NON-NLS-1$
	}

	@Override
	public String executeTooltip() {
		return Messages.uiGet("LegNeutralizationFunction.ExecuteTooltip"); //$NON-NLS-1$
	}

	@Override
	public JComponent getParametersConfig() {
		final JFormattedTextField startCodeF = new JFormattedTextField();
		startCodeF.setValue(Integer.valueOf(legStart));
		startCodeF.addPropertyChangeListener("value", new PropertyChangeListener() { //$NON-NLS-1$
			public void propertyChange(PropertyChangeEvent evt) {
				legStart = (Integer) startCodeF.getValue();
			}
		});
		startCodeF.setColumns(6);
		final JFormattedTextField endCodeF = new JFormattedTextField();
		endCodeF.setValue(Integer.valueOf(legEnd));
		endCodeF.addPropertyChangeListener("value", new PropertyChangeListener() { //$NON-NLS-1$
			public void propertyChange(PropertyChangeEvent evt) {
				legEnd = (Integer) endCodeF.getValue();
			}
		});
		endCodeF.setColumns(6);
		simulateCB = new JCheckBox(Messages.uiGet("LegNeutralizationFunction.SimulateLabel")); //$NON-NLS-1$
		simulateCB.setToolTipText(Messages.uiGet("LegNeutralizationFunction.SimulateTooltip")); //$NON-NLS-1$
		
		JPanel paramP = new JPanel(new GridBagLayout());
		GridBagConstraints c = SwingUtils.gbConstr(0);
		paramP.add(new JLabel(Messages.uiGet("LegNeutralizationFunction.StartcodeLabel")), c); //$NON-NLS-1$
		paramP.add(startCodeF, c);
		c.gridy = 1;
		paramP.add(new JLabel(Messages.uiGet("LegNeutralizationFunction.EndcodeLabel")), c); //$NON-NLS-1$
		paramP.add(endCodeF, c);
		c.gridy = 2;
		c.gridwidth = 2;
		paramP.add(simulateCB, c);
		
		JButton detectCoursesB = new JButton(Messages.uiGet("LegNeutralizationFunction.DetectCoursesLabel")); //$NON-NLS-1$
		detectCoursesB.setToolTipText(Messages.uiGet("LegNeutralizationFunction.DetectCoursesTooltip")); //$NON-NLS-1$
		detectCoursesB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				geco().announcer().dataInfo(Messages.uiGet("LegNeutralizationFunction.DetectCoursesMessage") + legStart + " -> " + legEnd); //$NON-NLS-1$ //$NON-NLS-2$
				selectCoursesWithNeutralizedLeg();
			}
		});
		JButton resetRaceTimeB = new JButton(Messages.uiGet("LegNeutralizationFunction.ResetTimesLabel")); //$NON-NLS-1$
		resetRaceTimeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetAllOfficialTimes();
			}
		});
		resetRaceTimeB.setToolTipText(Messages.uiGet("LegNeutralizationFunction.ResetTimesTooltip")); //$NON-NLS-1$
		
		Box vBoxButtons = Box.createVerticalBox();
		vBoxButtons.add(detectCoursesB);
		vBoxButtons.add(resetRaceTimeB);

		paramP.setMaximumSize(paramP.getPreferredSize());
		paramP.setAlignmentY(Component.TOP_ALIGNMENT);
		vBoxButtons.setAlignmentY(Component.TOP_ALIGNMENT);
		Box hBox = Box.createHorizontalBox();
		hBox.add(paramP);
		hBox.add(Box.createHorizontalStrut(50));
		hBox.add(vBoxButtons);

		return hBox;
	}

	public void setNeutralizedLeg(int legStart, int legEnd) {
		this.legStart = legStart;
		this.legEnd = legEnd;
	}

	@Override
	public void execute() {
		boolean simulate = simulateCB.isSelected();
		String startMessage = Messages.uiGet("LegNeutralizationFunction.StartingLegNeutralizationMessage") + legStart + " -> " + legEnd; //$NON-NLS-1$ //$NON-NLS-2$
		if( simulate ){
			geco().announcer().dataInfo("SIMULATION - " + startMessage); //$NON-NLS-1$
		} else {
			geco().log(startMessage);
		}
		Collection<Course> courses = selectCoursesWithNeutralizedLeg();
		for (Course course : courses) {
			geco().announcer().dataInfo(Messages.uiGet("LegNeutralizationFunction.CourseMessage") + course.getName()); //$NON-NLS-1$
			List<Runner> runners = registry().getRunnersFromCourse(course);
			for (Runner runner : runners) {
				neutralizeLeg(registry().findRunnerData(runner), simulate);
			}
		}
		String endMessage = Messages.uiGet("LegNeutralizationFunction.EndingLegNeutralizationMessage") + legStart + " -> " + legEnd; //$NON-NLS-1$ //$NON-NLS-2$
		if( simulate ){
			geco().announcer().dataInfo("SIMULATION - " + endMessage); //$NON-NLS-1$
		} else {
			geco().log(endMessage);
		}
	}

	public Collection<Course> selectCoursesWithNeutralizedLeg() {
		ArrayList<Course> courses = new ArrayList<Course>();
		for (Course c : registry().getCourses()) {
			if( c.hasLeg(legStart, legEnd) ){
				courses.add(c);
				geco().announcer().dataInfo(c.getName());
			}
		}
		return courses;
	}

	public Collection<Runner> selectRunnersWithLegToNeutralize(Course course) {
		return registry().getRunnersFromCourse(course);
	}

	public void neutralizeLeg(RunnerRaceData raceData, boolean simulate) {
		Trace[] leg = raceData.retrieveLeg(legStart, legEnd);
		if( leg!=null && ! leg[1].isNeutralized() ){
			long splitTime = TimeManager.computeSplit(leg[0].getTime().getTime(), leg[1].getTime().getTime());
			if( splitTime!=TimeManager.NO_TIME_l ){
				if( simulate ){
					geco().announcer().dataInfo(raceData.getRunner().idString() + " - split " + TimeManager.time(splitTime)); //$NON-NLS-1$
				} else {
					RunnerResult result = raceData.getResult();
					if( result.getResultTime()!=TimeManager.NO_TIME_l ){
						result.setResultTime(result.getResultTime() - splitTime);
						geco().log(raceData.getRunner().idString() + " - split " + TimeManager.time(splitTime) + " - race " + result.formatResultTime()); //$NON-NLS-1$ //$NON-NLS-2$
					}
					leg[1].setNeutralized(true);					
				}
			}
		}
	}

	public void resetAllOfficialTimes() {
		geco().log(Messages.uiGet("LegNeutralizationFunction.ResetTimesMessage")); //$NON-NLS-1$
		for (RunnerRaceData raceData : registry().getRunnersData()) {
			geco().checker().resetRaceTime(raceData);
			for (Trace t : raceData.getTraceData().getTrace()) {
				t.setNeutralized(false);
			}
		}
	}

}
