/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.tabs;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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

	private Vector<String> labels;
	private Map<String,JPanel> configs;
	private JPanel displayedConfig;
	
	public StagePanel(IGecoApp geco, JFrame frame) {
		super(geco, frame);
		labels = new Vector<String>();
		configs = new HashMap<String, JPanel>();
	}
	
	public void refresh() {
		this.removeAll();
		
		setLayout(new BorderLayout());
		final JList labelList = new JList(labels);
		labelList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if( !e.getValueIsAdjusting() ){
					String selectedLabel = (String) labelList.getSelectedValue();
					showConfigPanelFor(selectedLabel);
				}
			}
		});
		add(labelList, BorderLayout.WEST);
		displayedConfig = new JPanel(); // null object
		add(displayedConfig, BorderLayout.CENTER);
		labelList.setSelectedIndex(0); // first selected by default
	}

	@Override
	public String getTabTitle() {
		return Messages.uiGet("GecoWindow.Stage");
	}
	
	public void showConfigPanelFor(String label){
		remove(displayedConfig);
		displayedConfig = getConfigFor(label);
		add(displayedConfig, BorderLayout.CENTER);
		frame().repaint();
	}

	public void addConfigPanels(ConfigPanel[] configPanels) {
		for (ConfigPanel configPanel : configPanels) {
			addConfigPanel(configPanel);
		}
	}	
		
	public void addConfigPanel(ConfigPanel configPanel) {
		String label = configPanel.getLabel();
		if( ! configs.containsKey(label) ){
			labels.add(label);
			configs.put(label, new JPanel());
		}
		getConfigFor(label).add(configPanel.get());
		refresh();
	}

	public JPanel getConfigFor(String label) {
		return configs.get(label);
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
