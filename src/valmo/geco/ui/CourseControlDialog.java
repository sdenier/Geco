/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import valmo.geco.core.Util;
import valmo.geco.model.Course;


/**
 * @author Simon Denier
 * @since Mar 5, 2010
 *
 */
public class CourseControlDialog extends JDialog {
	
	private JTextField controlsF;

	public CourseControlDialog(JFrame frame, Course course) {
		super(frame, "Course Editor", true);
		setLocationRelativeTo(frame);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		getContentPane().setLayout(new GridBagLayout());
		getContentPane().add(new JLabel("Control sequence for course " + course.getName() + " (separated by comma):"));
		controlsF = new JTextField(40);
		controlsF.setText(Arrays.toString(course.getCodes()));
		GridBagConstraints c = Util.compConstraint(0, 1);
		c.gridwidth = 2;
		getContentPane().add(controlsF, c);
		JButton saveB = new JButton("Save");
		saveB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO: parse fields and check format, then set new control codes for course
				setVisible(false);
			}
		});
		getContentPane().add(saveB, Util.compConstraint(0, 2));
		JButton cancelB = new JButton("Cancel");
		cancelB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		getContentPane().add(cancelB, Util.compConstraint(1, 2));

		pack();
		setVisible(true);
	}

}
