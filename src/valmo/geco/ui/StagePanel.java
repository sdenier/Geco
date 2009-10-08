/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.ui;

import java.awt.GridLayout;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import valmo.geco.core.Announcer;
import valmo.geco.core.Geco;
import valmo.geco.core.Util;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Feb 8, 2009
 *
 */
public class StagePanel extends TabPanel {
	
	/*
	 * Stage information/configuration and race statistics
	 * 
	 * TODO:
	 * - make it interactive! currently changing the name field has no effect
	 * - make it dynamic and react to change in registry (at least RunnerListener)
	 * take care of the fact we dont really need to compute new stats each time there is a change.
	 * We only need them when the panel becomes isVisible(). 
	 * 
	 * Current implementation is not valid: should directly change the field data.
	 * It's more problematic with respect to stats per course. perhaps we should use a table for those.
	 * 
	 */

	/**
	 * @param geco
	 * @param frame
	 */
	public StagePanel(Geco geco, JFrame frame, Announcer announcer) {
		super(geco, frame, announcer);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		refresh();
	}
	
	public void refresh() {
		this.removeAll();
		JPanel configPanel = Util.embed(initConfigPanel());		
		JPanel statsPanel = Util.embed(initStatsPanel());
		add(configPanel);
		add(statsPanel);		
	}

	private JPanel initConfigPanel() {
		JPanel panel = new JPanel(new GridLayout(0,2));
		panel.setBorder(BorderFactory.createTitledBorder("Stage Configuration"));
		panel.add(new JLabel("Stage name:"));
		panel.add(new JTextField(geco().stage().getName()));
		panel.add(new JLabel("Previous stage:"));
		panel.add(new JLabel(geco().getPreviousStageDir()));
		panel.add(new JLabel("Next stage:"));
		panel.add(new JLabel(geco().getNextStageDir()));
		return panel;
	}

	/**
	 * @return
	 */
	private JPanel initStatsPanel() {
		Map<String, Integer> statusCounts = registry().statusCounts();
		JPanel panel = new JPanel(new GridLayout(0,3));
		panel.setBorder(BorderFactory.createTitledBorder("Stage Statistics"));
		panel.add(new JLabel("Number of registered runners:"));
		panel.add(new JLabel(statusCounts.get("total").toString()));
		panel.add(Box.createHorizontalGlue());
		panel.add(new JLabel("Number of actual runners:"));
		panel.add(new JLabel(statusCounts.get("actual").toString()));
		panel.add(Box.createHorizontalGlue());
		panel.add(new JLabel("Number of finished runners:"));
		panel.add(new JLabel(statusCounts.get("finished").toString()));
		panel.add(Box.createHorizontalGlue());
		panel.add(new JLabel("Status OK"));
		panel.add(new JLabel(statusCounts.get("OK").toString()));
		panel.add(Box.createHorizontalGlue());
		panel.add(new JLabel("Status MP"));
		panel.add(new JLabel(statusCounts.get("MP").toString()));
		panel.add(Box.createHorizontalGlue());
		panel.add(new JLabel("Status DNF"));
		panel.add(new JLabel(statusCounts.get("DNF").toString()));
		panel.add(Box.createHorizontalGlue());
		panel.add(new JLabel("Status Disq"));
		panel.add(new JLabel(statusCounts.get("DSQ").toString()));
		panel.add(Box.createHorizontalGlue());
		panel.add(new JLabel("Status Unknown"));
		panel.add(new JLabel(statusCounts.get("Unknown").toString()));
		panel.add(Box.createHorizontalGlue());
		panel.add(new JLabel("Status DNS"));
		panel.add(new JLabel(statusCounts.get("DNS").toString()));
		panel.add(Box.createHorizontalGlue());
		panel.add(Box.createHorizontalGlue());
		panel.add(Box.createHorizontalGlue());
		panel.add(Box.createHorizontalGlue());
		initCourseStatsPanel(panel);
//		panel.add(Util.embed(new JButton("Refresh")));
		return panel;
	}

	/**
	 * @param panel
	 */
	private void initCourseStatsPanel(JPanel panel) {
		Map<String, Integer> coursesCounts = registry().coursesCounts();
		for (String c : registry().getCoursenames()) {
			panel.add(new JLabel(c));
			panel.add(new JLabel(coursesCounts.get(c).toString()));
			panel.add(new JLabel(coursesCounts.get(c+"running").toString()));
		}
	}

	@Override
	public void changed(Stage previous, Stage next) {
		refresh();
		frame().repaint();
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		// TODO save stage properties
		super.saving(stage, properties);
	}
	

}
