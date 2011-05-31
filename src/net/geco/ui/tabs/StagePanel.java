/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.tabs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

	@Override
	public String getTabTitle() {
		return Messages.uiGet("GecoWindow.Stage");
	}

	public void buildConfigPanels(ConfigPanel[] configPanels) {
		for (ConfigPanel configPanel : configPanels) {
			addConfigPanel(configPanel);
		}
		buildGUI();
	}	
		
	public void addConfigPanel(ConfigPanel configPanel) {
		String label = configPanel.getLabel();
		if( ! configs.containsKey(label) ){
			labels.add(label);
			configs.put(label, new JPanel(new FlowLayout(FlowLayout.LEFT)));
			getConfigFor(label).setBorder(BorderFactory.createTitledBorder(label));
		}
		getConfigFor(label).add(configPanel.build());
	}
	
	public JPanel getConfigFor(String label) {
		return configs.get(label);
	}
	
	public void buildGUI() {
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
		JScrollPane jsp = new JScrollPane(labelList);
		jsp.setPreferredSize(new Dimension(150, 200));
		add(jsp, BorderLayout.WEST);

		displayedConfig = new JPanel(); // null object
		add(displayedConfig, BorderLayout.CENTER);
		labelList.setSelectedIndex(0); // first selected by default
	}

	public void showConfigPanelFor(String label){
		remove(displayedConfig);
		displayedConfig = getConfigFor(label);
		add(displayedConfig, BorderLayout.CENTER);
		frame().repaint();
	}

	@Override
	public void changed(Stage previous, Stage next) {
		buildGUI();
		frame().repaint();
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		super.saving(stage, properties);
	}

}
