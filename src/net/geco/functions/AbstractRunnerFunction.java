/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.functions;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import net.geco.control.GecoControl;
import net.geco.model.Messages;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;


/**
 * @author Simon Denier
 * @since Nov 11, 2010
 *
 */
public abstract class AbstractRunnerFunction extends GecoFunction {

	protected JList data;
	protected JRadioButton coursesRB;
	protected JRadioButton categoriesRB;
	protected JRadioButton runnersRB;

	public AbstractRunnerFunction(GecoControl gecoControl) {
		super(gecoControl);
	}

	protected boolean acceptRunnerData(RunnerRaceData runnerRaceData) {
		return true;
	}

	@Override
	public JComponent getParametersConfig() {
		coursesRB = new JRadioButton(Messages.uiGet("AbstractRunnerFunction.CourseLabel")); //$NON-NLS-1$
		categoriesRB = new JRadioButton(Messages.uiGet("AbstractRunnerFunction.CategoryLabel")); //$NON-NLS-1$
		runnersRB = new JRadioButton(Messages.uiGet("AbstractRunnerFunction.RunnerLabel"));		 //$NON-NLS-1$
		coursesRB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				data.setListData(registry().getSortedCourseNames().toArray());
			}
		});
		categoriesRB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				data.setListData(registry().getSortedCategoryNames().toArray());
			}
		});
		runnersRB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Vector<RunnerRaceData> selectedRunners = new Vector<RunnerRaceData>();
				for (RunnerRaceData runnerRaceData : registry().getRunnersData()) {
					if( acceptRunnerData(runnerRaceData) ){
						selectedRunners.add(runnerRaceData);
					}
				}
				data.setListData(selectedRunners);
			}
		});
		
		ButtonGroup dataGroup = new ButtonGroup();
		dataGroup.add(coursesRB);
		dataGroup.add(categoriesRB);
		dataGroup.add(runnersRB);
		
		Box dataTypes = Box.createVerticalBox();
		dataTypes.add(coursesRB);
		dataTypes.add(categoriesRB);
		dataTypes.add(runnersRB);
		dataTypes.setAlignmentY(Component.TOP_ALIGNMENT);

		data = new JList();
		data.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane dataSP = new JScrollPane(data);
		dataSP.setPreferredSize(new Dimension(350, 100));
		dataSP.setMaximumSize(dataSP.getPreferredSize());
		dataSP.setAlignmentY(Component.TOP_ALIGNMENT);
		
		coursesRB.doClick(); // simulate click to update list
		
		Box config = Box.createHorizontalBox();
		config.add(dataTypes);
		config.add(Box.createHorizontalStrut(10));
		config.add(dataSP);
		return config;
	}

	@Override
	public void updateUI() {
		// simulate click to update list
		if( coursesRB.isSelected() ){
			coursesRB.doClick();
		} else
		if( categoriesRB.isSelected() ){
			categoriesRB.doClick();
		} else
		if( runnersRB.isSelected() ){
			runnersRB.doClick();
		}
	}

	protected Collection<RunnerRaceData> selectedRunners() {
		Collection<RunnerRaceData> runners = new Vector<RunnerRaceData>();
		if( coursesRB.isSelected() ){
			for (Object course : data.getSelectedValues()) {
				List<Runner> runnersFromCourse = registry().getRunnersFromCourse((String) course);
				for (Runner runner : runnersFromCourse) {
					runners.add(registry().findRunnerData(runner));
				}
			}
		} else
		if( categoriesRB.isSelected() ){
			for (Object cat : data.getSelectedValues()) {
				List<Runner> runnersFromCategory = registry().getRunnersFromCategory((String) cat);
				for (Runner runner : runnersFromCategory) {
					runners.add(registry().findRunnerData(runner));
				}
			}
		} else
		if( runnersRB.isSelected() ){
			for (Object runner : data.getSelectedValues()) {
				runners.add((RunnerRaceData) runner);
			}
		}
		return runners;
	}
	
}
