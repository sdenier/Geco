/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import valmo.geco.core.Util;
import valmo.geco.model.HeatSet;
import valmo.geco.model.Pool;
import valmo.geco.model.ResultType;


/**
 * @author Simon Denier
 * @since Jan 31, 2009
 *
 */
public class HeatSetDialog extends JDialog {
	
	private HeatSet currentHeatSet;
	private boolean cancelled;
	
	private JTextField heatSetF;
	private JTextField qRankF;
	private JTextArea heatNamesTA;
	private JComboBox setTypeCB;
//	private JRadioButton selectCourseB;
//	private JRadioButton selectCatB;

	public HeatSet getHeatSet() {
		return this.currentHeatSet;
	}
	
	private void cancel() {
		cancelled = true;
		setVisible(false);
	}

	public boolean cancelled() {
		return this.cancelled;
	}
	
	public HeatSetDialog(JFrame frame) {
		super(frame, "Heat Set Editor", true);
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				cancel();
			}
		});
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		getContentPane().setLayout(new GridBagLayout());
		getContentPane().add(new JLabel("Heat Set Name"));
		heatSetF = new JTextField(7);
		getContentPane().add(heatSetF, SwingUtils.compConstraint(1, 0));
		GridBagConstraints c = SwingUtils.compConstraint(0, 1);
		c.gridwidth = 2;
		getContentPane().add(new JLabel("Heat Names: heat1, heat2, ..."), c);
		heatNamesTA = new JTextArea();
		heatNamesTA.setLineWrap(true);
		heatNamesTA.setPreferredSize(new Dimension(200,40));
		c = SwingUtils.compConstraint(0, 2);
		c.gridwidth = 2;
		c.insets = new Insets(0, 0, 10, 0);
		heatNamesTA.setBorder(BorderFactory.createLineBorder(Color.gray));
		getContentPane().add(heatNamesTA, c);

		getContentPane().add(new JLabel("Qualifying Rank"), SwingUtils.compConstraint(0, 3));
		qRankF = new JTextField(7);
		getContentPane().add(qRankF, SwingUtils.compConstraint(1, 3));
		
		setTypeCB = new JComboBox(ResultType.values());
//		selectCourseB = new JRadioButton("Courses");
//		selectCatB = new JRadioButton("Categories");
//		ButtonGroup group = new ButtonGroup();
//		group.add(selectCourseB);
//		group.add(selectCatB);
		c = SwingUtils.compConstraint(0, 4);
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		getContentPane().add(setTypeCB, c);
//		getContentPane().add(selectCourseB, SwingUtils.compConstraint(0, 4));
//		getContentPane().add(selectCatB, SwingUtils.compConstraint(1, 4));
		
		JButton saveB = new JButton("Save");
		saveB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkAndSetFields();
			}
		});
		getContentPane().add(saveB, SwingUtils.compConstraint(0, 5));
		JButton cancelB = new JButton("Cancel");
		cancelB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		getContentPane().add(cancelB, SwingUtils.compConstraint(1, 5));
		pack();
		setLocationRelativeTo(null);
	}
	
	private void checkAndSetFields() {
		boolean ok = true;
		String errorMessage = "";
		
		String newName = "";
		Integer newQRank = 0;
		String[] newHeatNames = new String[0];
		
		try {
			errorMessage = "Qualifying Rank: Bad Number Format";
			newQRank = new Integer(qRankF.getText());
				
			newHeatNames = heatNamesTA.getText().split(",");
			if( !Util.allDifferent(newHeatNames) ) {
				ok = false;
				errorMessage = "Heat names should be all different";
			}
			newName = heatSetF.getText();
			if( newName.isEmpty() || newName.matches("^\\s*$") ) {
				ok = false;
				errorMessage = "HeatSet should have a name";
			}
		} catch (NumberFormatException ex) {
			ok = false;
		}					

		if( ok ) {
			currentHeatSet.setName(newName);
			currentHeatSet.setQualifyingRank(newQRank);
			currentHeatSet.setHeatNames(newHeatNames);
			if( !currentHeatSet.getSetType().equals(getSelectedSettype()) ) {
				currentHeatSet.setSetType(getSelectedSettype());
				currentHeatSet.setSelectedPools(new Pool[0]);
			}
			cancelled = false;
			setVisible(false);
		} else {
			JOptionPane.showMessageDialog(
					HeatSetDialog.this,
					errorMessage, 
					"Invalid Entry", 
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public ResultType getSelectedSettype() {
		return (ResultType) setTypeCB.getSelectedItem();
	}
	
	public void showHeatSet(HeatSet heatSet) {
		this.currentHeatSet = heatSet;
		refreshFields(currentHeatSet);
		setVisible(true);
	}

	private void refreshFields(HeatSet heatSet) {
		heatSetF.setText(heatSet.getName());
		qRankF.setText(heatSet.getQualifyingRank().toString());
		heatNamesTA.setText(Util.join(heatSet.getHeatNames(), ",", new StringBuffer()));
		setTypeCB.setSelectedItem(heatSet.getSetType());
//		if( heatSet.isCourseType() ) {
//			selectCourseB.setSelected(true);
//		} else {
//			selectCatB.setSelected(true);
//		}
	}
	
}
