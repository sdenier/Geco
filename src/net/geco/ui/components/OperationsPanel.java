/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.geco.functions.GecoOperation;
import net.geco.functions.GecoOperation.OperationCategory;
import net.geco.model.Messages;
import net.geco.ui.framework.TabbedSubpane;


/**
 * @author Simon Denier
 * @since Nov 11, 2010
 *
 */
public class OperationsPanel extends JPanel implements TabbedSubpane {

	private GecoOperation selectedOperation;
	
	private JComponent operationUI;

	public OperationsPanel(OperationCategory operationCategory, final JFrame frame, JButton clearLogB) {
		setLayout(new BorderLayout());

		final JComboBox gecoOperationsCB = new JComboBox(GecoOperation.getAll(operationCategory));

		final JButton execB = new JButton(Messages.uiGet("FunctionsPanel.ExecuteLabel")); //$NON-NLS-1$
		execB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedOperation.execute();
			}
		});
		
		gecoOperationsCB.setMaximumSize(gecoOperationsCB.getPreferredSize());
		gecoOperationsCB.setAlignmentX(Component.LEFT_ALIGNMENT);
		execB.setAlignmentX(Component.LEFT_ALIGNMENT);
		clearLogB.setAlignmentX(Component.LEFT_ALIGNMENT);
		Box commandBox = Box.createVerticalBox();
		commandBox.setBorder(
			BorderFactory.createTitledBorder(Messages.uiGet("OperationsPanel.OperationTitle"))); //$NON-NLS-1$
		commandBox.add(gecoOperationsCB);
		commandBox.add(Box.createVerticalGlue());
		commandBox.add(execB);
		commandBox.add(clearLogB);
		add(commandBox, BorderLayout.WEST);
		
		operationUI = new JPanel(); // dummy initialization
		add(operationUI, BorderLayout.CENTER);
		gecoOperationsCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				remove(operationUI);
				selectedOperation = (GecoOperation) gecoOperationsCB.getSelectedItem();
				execB.setToolTipText(selectedOperation.executeTooltip());
				operationUI = selectedOperation.buildUI();
				add(operationUI, BorderLayout.CENTER);
				((JComponent) frame.getContentPane()).revalidate();
			}
		});
		gecoOperationsCB.setSelectedIndex(0);
	}

	public void componentShown() {
		selectedOperation.updateUI();
	}
	
}
