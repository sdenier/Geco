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
import javax.swing.JOptionPane;
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

	public CourseControlDialog(JFrame frame, final Course course) {
		super(frame, "Course Editor", true);
		setLocationRelativeTo(frame);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		getContentPane().setLayout(new GridBagLayout());
		getContentPane().add(new JLabel("Control sequence for course " + course.getName() + " (separated by comma):"));
		controlsF = new JTextField(40);
		String codes = Arrays.toString(course.getCodes());
		controlsF.setText(codes.substring(1, codes.length() - 1));
		GridBagConstraints c = SwingUtils.compConstraint(0, 1);
		c.gridwidth = 2;
		getContentPane().add(controlsF, c);
		JButton saveB = new JButton("Save");
		saveB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String dataLine = controlsF.getText();
				String[] data = Util.splitAndTrim(dataLine, ",");
				int[] newCodes = new int[data.length];
				try {
					for (int i = 0; i < data.length; i++) {
						newCodes[i] = new Integer(data[i]);
					}
					course.setCodes(newCodes);
					setVisible(false);
				} catch (NumberFormatException e2) {
					JOptionPane.showMessageDialog(
							CourseControlDialog.this,
							e2.getMessage(), 
							"Bad code number", 
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		getContentPane().add(saveB, SwingUtils.compConstraint(0, 2));
		JButton cancelB = new JButton("Cancel");
		cancelB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		getContentPane().add(cancelB, SwingUtils.compConstraint(1, 2));

		pack();
		setVisible(true);
	}

}
