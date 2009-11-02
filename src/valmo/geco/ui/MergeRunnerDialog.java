/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import valmo.geco.core.Geco;
import valmo.geco.core.Util;


/**
 * @author Simon Denier
 * @since Jan 31, 2009
 *
 */
public class MergeRunnerDialog extends JDialog {
	
	private JLabel dataInfoL;
	private JComboBox runnersCB;
	private JButton mergeB;
//	private JButton createB;
	private JButton cancelB;
	private JLabel actionInfoL;
	
	public MergeRunnerDialog(Geco geco, JFrame frame) {
		super(frame, "Merge runner into...", true);
		setLocationRelativeTo(frame);
		
		getContentPane().setLayout(new GridBagLayout());
		dataInfoL = new JLabel("Merge following runner data:");
		GridBagConstraints c = Util.compConstraint(0, 0);
		c.gridwidth = 4;
		getContentPane().add(dataInfoL, c);
		runnersCB = new JComboBox(geco.registry().getRunners().toArray());
		c = Util.compConstraint(0, 1);
		c.gridwidth = 4;
		getContentPane().add(runnersCB, c);
		
		getContentPane().add(Box.createGlue(), Util.compConstraint(0, 2));
		mergeB = new JButton("Merge");
		getContentPane().add(mergeB, Util.compConstraint(1, 2));
//		createB = new JButton("Create"); // or delete John Doe?
//		getContentPane().add(createB, Util.compConstraint(2, 2));
		cancelB = new JButton("Cancel");
		cancelB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				cancelled = true;
				setVisible(false);
			}
		});
		getContentPane().add(cancelB, Util.compConstraint(2, 2));
		getContentPane().add(Box.createGlue(), Util.compConstraint(0, 2));

		c = Util.compConstraint(0, 3);
		c.gridwidth = 4;
		actionInfoL = new JLabel("Merge will override punches from ... with ...\nCreate will record punches in a new runner.");
		getContentPane().add(actionInfoL, c);
		
		pack();
		setVisible(true);
	}
	
}
