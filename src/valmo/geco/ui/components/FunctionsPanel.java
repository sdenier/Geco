/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import valmo.geco.functions.GecoFunction;
import valmo.geco.model.Messages;

/**
 * @author Simon Denier
 * @since Nov 11, 2010
 *
 */
public class FunctionsPanel extends JPanel {

	private GecoFunction gFunction;
	
	private JComponent functionUI;

	public FunctionsPanel(JFrame frame, JButton clearLogB) {
		initFunctionsPanel(frame, clearLogB);
	}

	public JComponent initFunctionsPanel(final JFrame frame, JButton clearLogB) {
		setLayout(new BorderLayout());

		Vector<GecoFunction> functions = GecoFunction.functions();
		final JComboBox gecoFunctionsCB = new JComboBox(functions);

		final JButton execB = new JButton(Messages.uiGet("FunctionsPanel.ExecuteLabel")); //$NON-NLS-1$
		execB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gFunction.execute();
			}
		});
		
		gecoFunctionsCB.setMaximumSize(gecoFunctionsCB.getPreferredSize());
		gecoFunctionsCB.setAlignmentX(Component.LEFT_ALIGNMENT);
		execB.setAlignmentX(Component.LEFT_ALIGNMENT);
		clearLogB.setAlignmentX(Component.LEFT_ALIGNMENT);
		Box commandBox = Box.createVerticalBox();
		commandBox.setBorder(
			BorderFactory.createTitledBorder(Messages.uiGet("FunctionsPanel.FunctionTitle"))); //$NON-NLS-1$
		commandBox.add(gecoFunctionsCB);
		commandBox.add(Box.createVerticalGlue());
		commandBox.add(execB);
		commandBox.add(clearLogB);
		add(commandBox, BorderLayout.WEST);
		
		functionUI = new JPanel(); // dummy initialization
		add(functionUI, BorderLayout.CENTER);
		gecoFunctionsCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				remove(functionUI);
				gFunction = (GecoFunction) gecoFunctionsCB.getSelectedItem();
				execB.setToolTipText(gFunction.executeTooltip());
				functionUI = gFunction.getFunctionUI();
				add(functionUI, BorderLayout.CENTER);
				((JComponent) frame.getContentPane()).revalidate();
			}
		});
		gecoFunctionsCB.setSelectedIndex(0);
		return this;
	}
	public void componentShown() {
		gFunction.updateUI();
	}
	
}
