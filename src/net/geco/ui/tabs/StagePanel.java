/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.tabs;

import java.awt.BorderLayout;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JList;

import net.geco.framework.IGecoApp;
import net.geco.model.Messages;
import net.geco.model.Stage;
import net.geco.ui.framework.ConfigPanel;
import net.geco.ui.framework.TabPanel;


/**
 * @author Simon Denier
 * @since Feb 8, 2009
 *
 */
public class StagePanel extends TabPanel {

	public StagePanel(IGecoApp geco, JFrame frame) {
		super(geco, frame);
		refresh();
	}
	
	public void refresh() {
		this.removeAll();
		
		setLayout(new BorderLayout());
		add(new JList(), BorderLayout.WEST);
	}

	@Override
	public String getTabTitle() {
		return Messages.uiGet("GecoWindow.Stage");
	}

	public void addConfigPanels(ConfigPanel[] configPanels) {
		// TODO Auto-generated method stub
		
	}	
		
	@Override
	public void changed(Stage previous, Stage next) {
		refresh();
		frame().repaint();
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		super.saving(stage, properties);
	}

}
