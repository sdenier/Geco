/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.config;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

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

	public SectionControlDialog(JFrame frame, final Course selectedCourse, final Section targetSection) {
		super(frame, "Create or Edit a Section...", true); //$NON-NLS-1$
		setResizable(false);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		
		final JTextField sectionNameF = new JTextField(10);
		sectionNameF.setText(targetSection.getName());
		final JComboBox sectionTypeCB = new JComboBox(Section.SectionType.values());
		sectionTypeCB.setSelectedItem(targetSection.getType());
		
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
					selectedCourse.putSection(targetSection);
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
		
		getContentPane().setLayout(new GridBagLayout());
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//		getContentPane().add(new JLabel(Messages.uiGet("CourseControlDialog.HelpLabel1") //$NON-NLS-1$
//		+ course.getName()
//		+ Messages.uiGet("CourseControlDialog.HelpLabel2"))); //$NON-NLS-1$
		GridBagConstraints c = SwingUtils.gbConstr();
		getContentPane().add(new JLabel("Section Name:"), c);
		getContentPane().add(sectionNameF, c);
		c.gridy = 1;
		getContentPane().add(new JLabel("Section Type:"), c);
		getContentPane().add(sectionTypeCB, c);
		c.gridy = 2;
		getContentPane().add(saveB, c);
		getContentPane().add(deleteB, c);
		getContentPane().add(cancelB, c);

		c.gridy = 3;
		getContentPane().add(new JLabel(selectedCourse.getName()), c);
		getContentPane().add(new JLabel("" + targetSection.getStartIndex() + " / " + selectedCourse.getCodes()[targetSection.getStartIndex()]), c);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
