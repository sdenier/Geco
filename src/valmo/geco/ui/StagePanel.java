/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.ui;

import java.awt.GridLayout;
import java.util.Properties;

import javax.swing.BorderFactory;
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
		this.removeAll(); // not smart?
		JPanel configPanel = Util.embed(initConfigPanel());		
		add(configPanel);
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
