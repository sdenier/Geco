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

import net.geco.basics.Html;
import net.geco.model.Messages;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.ui.basics.SwingUtils;


/**
 * @author Simon Denier
 * @since Sep 3, 2010
 *
 */
public class RunnerResultPanel extends ResultPanel {

	private JLabel nameL;
	private JLabel clubL;
	private JLabel courseL;
	private JLabel racetimeL;
	private JLabel mpL;
	private JLabel penaltiesL;
	private JLabel categoryL;

	public RunnerResultPanel() {
		super();
	}

	protected void initRunnerPanel() {
		Font largeFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
		Font boldFont = largeFont.deriveFont(Font.BOLD);
		nameL = new JLabel();
		nameL.setFont(boldFont.deriveFont(20f));
		
		clubL = new JLabel();
		categoryL = new JLabel();
		
		courseL = new JLabel();
		courseL.setFont(largeFont);
		statusL = new JLabel();
		statusL.setFont(boldFont);
		
		racetimeL = new JLabel();
		racetimeL.setFont(boldFont);
		mpL = new JLabel();
		mpL.setFont(boldFont);
		penaltiesL = new JLabel();
		penaltiesL.setFont(largeFont);
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = SwingUtils.gbConstr(0);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(5, 5, 5, 5);
		
		gbc.gridwidth = 2;
		panel.add(nameL, gbc);
		
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		panel.add(clubL, gbc);
		panel.add(categoryL, gbc);
		
		gbc.gridy = 2;
		panel.add(courseL, gbc);
		panel.add(racetimeL, gbc);
		
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(statusL, gbc);
		
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		panel.add(penaltiesL, gbc);
		panel.add(mpL, gbc);
		
		panel.add(Box.createVerticalStrut(20), SwingUtils.gbConstr(5));
		add(panel, BorderLayout.NORTH);
	}
	
	public void updateRunnerData(RunnerRaceData raceData) {
		nameL.setText(raceData.getRunner().getName());
		clubL.setText(raceData.getRunner().getClub().getName());
		categoryL.setText(raceData.getRunner().getCategory().getName());
		courseL.setText(raceData.getCourse().getName());
		
		RunnerResult result = raceData.getResult();
		racetimeL.setText(result.formatResultTime());
		updateStatusLabel(result.getStatus());
		penaltiesL.setText(result.formatTimePenalty());
		updateMps(raceData.getTraceData().getNbMPs());
		
		punchP.refreshPunches(raceData);
	}

	private void updateMps(int mps) {
		String mpText = Integer.toString(mps) + Messages.liveGet("RunnerResultPanel.MPLabel"); //$NON-NLS-1$
		if( mps>0 ) {
			mpText = Html.htmlTag("font", "color=red", mpText); //$NON-NLS-1$ //$NON-NLS-2$
		}
		mpL.setText(mpText);
	}
	
}
