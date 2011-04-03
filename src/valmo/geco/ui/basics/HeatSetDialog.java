/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui.basics;

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

import valmo.geco.basics.Util;
import valmo.geco.model.HeatSet;
import valmo.geco.model.Messages;
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
		super(frame, Messages.uiGet("HeatSetDialog.EditorTitle"), true); //$NON-NLS-1$
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				cancel();
			}
		});
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		getContentPane().setLayout(new GridBagLayout());
		getContentPane().add(new JLabel(Messages.uiGet("HeatSetDialog.HeatSetNameLabel"))); //$NON-NLS-1$
		heatSetF = new JTextField(7);
		getContentPane().add(heatSetF, SwingUtils.compConstraint(1, 0));
		GridBagConstraints c = SwingUtils.compConstraint(0, 1);
		c.gridwidth = 2;
		getContentPane().add(new JLabel(Messages.uiGet("HeatSetDialog.HeatNamesLabel")), c); //$NON-NLS-1$
		heatNamesTA = new JTextArea();
		heatNamesTA.setLineWrap(true);
		heatNamesTA.setPreferredSize(new Dimension(200,40));
		c = SwingUtils.compConstraint(0, 2);
		c.gridwidth = 2;
		c.insets = new Insets(0, 0, 10, 0);
		heatNamesTA.setBorder(BorderFactory.createLineBorder(Color.gray));
		getContentPane().add(heatNamesTA, c);

		getContentPane().add(new JLabel(Messages.uiGet("HeatSetDialog.QualifyingRankLabel")), //$NON-NLS-1$
							SwingUtils.compConstraint(0, 3));
		qRankF = new JTextField(7);
		getContentPane().add(qRankF, SwingUtils.compConstraint(1, 3));
		
		setTypeCB = new JComboBox(ResultType.values());
		c = SwingUtils.compConstraint(0, 4);
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		getContentPane().add(setTypeCB, c);
		
		JButton saveB = new JButton(Messages.uiGet("HeatSetDialog.SaveLabel")); //$NON-NLS-1$
		saveB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkAndSetFields();
			}
		});
		getContentPane().add(saveB, SwingUtils.compConstraint(0, 5));
		JButton cancelB = new JButton(Messages.uiGet("HeatSetDialog.CancelLabel")); //$NON-NLS-1$
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
		String errorMessage = ""; //$NON-NLS-1$
		
		String newName = ""; //$NON-NLS-1$
		Integer newQRank = 0;
		String[] newHeatNames = new String[0];
		
		try {
			errorMessage = Messages.uiGet("HeatSetDialog.QualifyingRankWarning"); //$NON-NLS-1$
			newQRank = new Integer(qRankF.getText());
				
			newHeatNames = heatNamesTA.getText().split(","); //$NON-NLS-1$
			if( !Util.allDifferent(newHeatNames) ) {
				ok = false;
				errorMessage = Messages.uiGet("HeatSetDialog.HeatNamesWarning"); //$NON-NLS-1$
			}
			newName = heatSetF.getText();
			if( newName.isEmpty() || newName.matches("^\\s*$") ) { //$NON-NLS-1$
				ok = false;
				errorMessage = Messages.uiGet("HeatSetDialog.HeatSetNameWarning"); //$NON-NLS-1$
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
					Messages.uiGet("HeatSetDialog.WarningTitle"),  //$NON-NLS-1$
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
		heatNamesTA.setText(Util.join(heatSet.getHeatNames(), ",", new StringBuffer())); //$NON-NLS-1$
		setTypeCB.setSelectedItem(heatSet.getSetType());
	}
	
}
