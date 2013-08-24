/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.config;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.geco.basics.Html;
import net.geco.framework.IGecoApp;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.Section;
import net.geco.model.Section.SectionType;
import net.geco.ui.basics.SwingUtils;

/**
 * @author Simon Denier
 * @since Jul 15, 2013
 *
 */
public class SectionControlDialog extends JDialog {

	public SectionControlDialog(final IGecoApp geco, JFrame frame, final Course selectedCourse, final Section targetSection) {
		super(frame, "Create or Edit a Section...", true); //$NON-NLS-1$
		setResizable(false);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		
		final JTextField sectionNameF = new JTextField(12);
		sectionNameF.setText(targetSection.getName());
		sectionNameF.requestFocusInWindow();
		final JComboBox sectionTypeCB = new JComboBox(Section.SectionType.values());
		sectionTypeCB.setSelectedItem(targetSection.getType());
		final JCheckBox neutralizedCB = new JCheckBox("", targetSection.neutralized());
		final JTextField penaltyF = new JTextField(12);
		
		JButton saveB = new JButton(Messages.uiGet("CourseControlDialog.SaveLabel")); //$NON-NLS-1$
		saveB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sectionName = sectionNameF.getText();
				if( sectionName.isEmpty() ){
					JOptionPane.showMessageDialog(
							SectionControlDialog.this,
							"Section name can not be empty", 
							"Warning",
							JOptionPane.ERROR_MESSAGE);
				} else {
					targetSection.setName(sectionName);
					targetSection.setType((SectionType) sectionTypeCB.getSelectedItem());
					targetSection.setNeutralized(neutralizedCB.isSelected());
					selectedCourse.putSection(targetSection);
					selectedCourse.refreshSectionCodes();
					geco.stageControl().validateControlsPenalty(targetSection.getCodes(), penaltyF.getText());
					setVisible(false);
				}
			}
		});
		
		JButton deleteB = new JButton("Delete");
		deleteB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedCourse.removeSection(targetSection);
				setVisible(false);
			}
		});
		
		JButton cancelB = new JButton(Messages.uiGet("CourseControlDialog.CancelLabel")); //$NON-NLS-1$
		cancelB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		
		JPanel formPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = SwingUtils.gbConstr();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 0, 0, 0);
		formPanel.add(new JLabel("Control:"), c);
		formPanel.add(new JLabel(Html.htmlTag("i",
				Integer.toString(selectedCourse.getCodes()[targetSection.getStartIndex()]))), c);
		c.gridy = 1;
		formPanel.add(new JLabel("Section Name:"), c);
		formPanel.add(sectionNameF, c);
		c.gridy = 2;
		formPanel.add(new JLabel("Section Type:"), c);
		formPanel.add(sectionTypeCB, c);
		c.gridy = 3;
		formPanel.add(new JLabel("Neutralized:"), c);
		formPanel.add(neutralizedCB, c);
		c.gridy = 4;
		formPanel.add(new JLabel("Reset Penalties:"), c);
		formPanel.add(penaltyF, c);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		buttons.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		buttons.add(saveB, c);
		buttons.add(deleteB, c);
		buttons.add(cancelB, c);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(formPanel, BorderLayout.CENTER);
		getContentPane().add(buttons, BorderLayout.SOUTH);
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
