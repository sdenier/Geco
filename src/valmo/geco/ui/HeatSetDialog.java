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

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import valmo.geco.core.Util;
import valmo.geco.model.HeatSet;
import valmo.geco.model.Pool;
import valmo.geco.model.impl.HeatSetImpl;


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
	private JRadioButton selectCourseB;
	private JRadioButton selectCatB;

	public HeatSet getHeatSet() {
		return this.currentHeatSet;
	}
	
	public boolean cancelled() {
		return this.cancelled;
	}
	
	public HeatSetDialog(JFrame frame) {
		super(frame, "Heat Set Editor", true);
		setLocationRelativeTo(frame);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		getContentPane().setLayout(new GridBagLayout());
		getContentPane().add(new JLabel("Heat Set Name"));
		heatSetF = new JTextField(7);
		getContentPane().add(heatSetF, Util.compConstraint(1, 0));
		GridBagConstraints c = Util.compConstraint(0, 1);
		c.gridwidth = 2;
		getContentPane().add(new JLabel("Heat Names: heat1, heat2, ..."), c);
		heatNamesTA = new JTextArea();
		heatNamesTA.setLineWrap(true);
		heatNamesTA.setPreferredSize(new Dimension(200,40));
		c = Util.compConstraint(0, 2);
		c.gridwidth = 2;
		c.insets = new Insets(0, 0, 10, 0);
		heatNamesTA.setBorder(BorderFactory.createLineBorder(Color.gray));
		getContentPane().add(heatNamesTA, c);

		getContentPane().add(new JLabel("Qualifying Rank"), Util.compConstraint(0, 3));
		qRankF = new JTextField(7);
		getContentPane().add(qRankF, Util.compConstraint(1, 3));
		
		selectCourseB = new JRadioButton("Courses");
		selectCatB = new JRadioButton("Categories");
		ButtonGroup group = new ButtonGroup();
		group.add(selectCourseB);
		group.add(selectCatB);
		getContentPane().add(selectCourseB, Util.compConstraint(0, 4));
		getContentPane().add(selectCatB, Util.compConstraint(1, 4));
		
		JButton saveB = new JButton("Save");
		saveB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkSetFields();
			}
		});
		getContentPane().add(saveB, Util.compConstraint(0, 5));
		JButton cancelB = new JButton("Cancel");
		cancelB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelled = true;
				setVisible(false);
			}
		});
		getContentPane().add(cancelB, Util.compConstraint(1, 5));
		pack();
	}
	
	private void checkSetFields() {
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

	public String getSelectedSettype() {
		if (selectCourseB.isSelected() ) {
			return "course";
		} else {
			return "category";
		}
	}
	
	public void showDialog() { // TODO: factory
		showHeatSet(new HeatSetImpl());
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
		if( heatSet.isCourseType() ) {
			selectCourseB.setSelected(true);
		} else {
			selectCatB.setSelected(true);
		}
	}
	
}
