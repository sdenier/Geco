/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.live;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.geco.model.Course;
import net.geco.model.RunnerRaceData;
import net.geco.ui.basics.SwingUtils;


/**
 * @author Simon Denier
 * @since Sep 3, 2010
 *
 */
public class LeisureResultPanel extends ResultPanel {

	private JLabel nameL;
	private JLabel courseL;
	private JLabel resultL;

	public LeisureResultPanel() {
		super();
	}

	protected void initRunnerPanel() {
		Font largeFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
		Font boldFont = largeFont.deriveFont(Font.BOLD);
		nameL = new JLabel();
		nameL.setFont(boldFont);
		
		courseL = new JLabel();
		courseL.setFont(largeFont);
		statusL = new JLabel();
		statusL.setFont(boldFont);
		
		resultL = new JLabel();
		resultL.setFont(boldFont.deriveFont(20f));
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = SwingUtils.gbConstr(0);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(5, 5, 5, 5);		
		panel.add(nameL, gbc);
		
		gbc.gridy = 1;
		panel.add(courseL, gbc);
		
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(statusL, gbc);
		
		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(resultL, gbc);
		
		panel.add(Box.createVerticalStrut(20), SwingUtils.gbConstr(5));
		add(panel, BorderLayout.NORTH);
	}
	
	public void updateRunnerData(RunnerRaceData raceData) {
		nameL.setText(raceData.getRunner().getName());
		Course course = raceData.getCourse();
		courseL.setText(course.getName());
		updateStatusLabel(raceData.getResult().getStatus());
		updateMps(raceData.getTraceData().getNbMPs(), course.nbControls());
		punchP.refreshPunches(raceData);
	}

	private void updateMps(int mps, int courseSize) {
		resultL.setText(Integer.toString(courseSize - mps) + " / " + Integer.toString(courseSize)); //$NON-NLS-1$
	}

	
}
