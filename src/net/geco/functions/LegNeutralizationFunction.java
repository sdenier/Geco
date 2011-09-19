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

	public LegNeutralizationFunction(GecoControl gecoControl) {
		super(gecoControl);
	}

	@Override
	public String toString() {
		return "Leg Neutralization";
	}

	@Override
	public String executeTooltip() {
		return "Neutralize leg for all runners";
	}

	@Override
	public JComponent getParametersConfig() {
		final JFormattedTextField startCodeF = new JFormattedTextField();
		startCodeF.setValue(Integer.valueOf(legStart));
		startCodeF.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				legStart = (Integer) startCodeF.getValue();
			}
		});
		startCodeF.setColumns(6);
		final JFormattedTextField endCodeF = new JFormattedTextField();
		endCodeF.setValue(Integer.valueOf(legEnd));
		endCodeF.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				legEnd = (Integer) endCodeF.getValue();
			}
		});
		endCodeF.setColumns(6);
		JCheckBox simulateCB = new JCheckBox("Simulate");
		simulateCB.setToolTipText("Show what would happen by executing the function but do not apply change");
		simulateCB.setEnabled(false);
		
		JPanel paramP = new JPanel(new GridBagLayout());
		GridBagConstraints c = SwingUtils.gbConstr(0);
		paramP.add(new JLabel("Start code:"), c);
		paramP.add(startCodeF, c);
		c.gridy = 1;
		paramP.add(new JLabel("End code:"), c);
		paramP.add(endCodeF, c);
		c.gridy = 2;
		c.gridwidth = 2;
		paramP.add(simulateCB, c);
		
		JButton detectCoursesB = new JButton("Detect Courses");
		detectCoursesB.setToolTipText("Show which courses contain the leg to neutralize");
		detectCoursesB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				geco().announcer().dataInfo("Courses detected with leg " + legStart + " -> " + legEnd);
				selectCoursesWithNeutralizedLeg();
			}
		});
		JButton resetRaceTimeB = new JButton("Reset race times");
		resetRaceTimeB.setToolTipText("Forget all neutralized legs and reset race times to their original value. Discard manual race times");
		resetRaceTimeB.setEnabled(false);
				
		Box vBoxButtons = Box.createVerticalBox();
		vBoxButtons.add(detectCoursesB);
		vBoxButtons.add(resetRaceTimeB);

		paramP.setMaximumSize(paramP.getPreferredSize());
//		paramP.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		paramP.setAlignmentY(Component.TOP_ALIGNMENT);
		vBoxButtons.setAlignmentY(Component.TOP_ALIGNMENT);
		Box hBox = Box.createHorizontalBox();
		hBox.add(paramP);
		hBox.add(Box.createHorizontalStrut(50));
		hBox.add(vBoxButtons);

		return hBox;
	}

	@Override
	public void updateUI() {}

	@Override
	public void execute() {
		geco().log("Leg neutralization " + legStart + " -> " + legEnd);
		Collection<Course> courses = selectCoursesWithNeutralizedLeg();
		for (Course course : courses) {
			geco().announcer().dataInfo("Course " + course.getName());
			List<Runner> runners = registry().getRunnersFromCourse(course);
			for (Runner runner : runners) {
				neutralizeLeg(registry().findRunnerData(runner));
			}
		}
		geco().log("End leg neutralization " + legStart + " -> " + legEnd);
	}

	public void setNeutralizedLeg(int legStart, int legEnd) {
		this.legStart = legStart;
		this.legEnd = legEnd;
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

	public void neutralizeLeg(RunnerRaceData raceData) {
		Trace[] leg = raceData.retrieveLeg(legStart, legEnd);
		if( leg!=null && ! leg[1].isNeutralized() ){
			long splitTime = TimeManager.computeSplit(leg[0].getTime().getTime(), leg[1].getTime().getTime());
			if( splitTime!=TimeManager.NO_TIME_l ){
				RunnerResult result = raceData.getResult();
				if( result.getRacetime()!=TimeManager.NO_TIME_l ){
					result.setRacetime(result.getRacetime() - splitTime);
					geco().log(raceData.getRunner().idString() + " split: " + TimeManager.time(splitTime) + " - race: " + result.formatRacetime());
				}
				leg[1].setNeutralized(true);				
			}
		}
	}

}
