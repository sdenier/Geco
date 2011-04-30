/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.live;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.geco.model.RunnerRaceData;
import net.geco.model.Status;
import net.geco.ui.basics.PunchPanel;


/**
 * @author Simon Denier
 * @since Sep 26, 2010
 *
 */
public abstract class ResultPanel extends JPanel {

	protected PunchPanel punchP;
	protected JLabel statusL;

	public ResultPanel() {
		setLayout(new BorderLayout());
		initRunnerPanel();
		initPunchPanel();
	}

	protected abstract void initRunnerPanel();

	public abstract void updateRunnerData(RunnerRaceData runnerData);

	protected void updateStatusLabel(Status status) {
		statusL.setOpaque(true);
		statusL.setBackground( status.color() );
		statusL.setHorizontalAlignment(SwingConstants.CENTER);
		statusL.setText(status.toString());
	}

	protected void initPunchPanel() {
		punchP = new PunchPanel();
		add(punchP, BorderLayout.CENTER);
	}

}