/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Status;
import valmo.geco.ui.PunchPanel;

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
		Color bg = Color.white;
		switch (status) {
		case OK: bg = new Color(0.6f, 1, 0.6f); break;
		case MP: bg = new Color(0.75f, 0.5f, 0.75f); break;
		case Unknown: bg = new Color(1, 1, 0.5f); break;
		default: break;
		}
		statusL.setOpaque(true);
		statusL.setBackground(bg);
		statusL.setHorizontalAlignment(SwingConstants.CENTER);
		statusL.setText(status.toString());
	}

	protected void initPunchPanel() {
		punchP = new PunchPanel();
		add(punchP, BorderLayout.CENTER);
	}

}