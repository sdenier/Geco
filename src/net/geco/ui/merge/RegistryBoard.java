/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.merge;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.ui.basics.GecoIcon;
import net.geco.ui.basics.SwingUtils;

public class RegistryBoard extends AbstractMergeBoard {

	private JButton mergeRunnerB;
	private JLabel overwriteWarningL;

	private JComboBox searchRegistryCB;
	private DataField categoryF;
	private DataField courseF;
	private DataField raceTimeF;
	private StatusField statusF;

	public RegistryBoard(MergeWizard wizard, JComponent panel, int firstLine) {
		super("Registry", wizard, panel, firstLine);
	}
	
	private Object[] sortedRunners() {
		Runner[] runners = registry().getRunners().toArray(new Runner[0]);
		Arrays.sort(runners, new Comparator<Runner>() {
			public int compare(Runner o1, Runner o2) {
				return o1.getLastname().compareTo(o2.getLastname());
			}
		});
		return runners;
	}

	public void updatePanel() {
		searchRegistryCB.getEditor().getEditorComponent().addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {}
			public void focusGained(FocusEvent e) {
				searchRegistryCB.getEditor().getEditorComponent().removeFocusListener(this);
				searchRegistryCB.setModel(new DefaultComboBoxModel(sortedRunners()));
				searchRegistryCB.setSelectedIndex(-1);
			}
		});
		searchRegistryCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Object selectedItem = searchRegistryCB.getSelectedItem();
				if( selectedItem instanceof Runner ) {
					Runner runner = (Runner) selectedItem;
					categoryF.setText(runner.getCategory().getName());
					courseF.setText(runner.getCourse().getName());
					RunnerRaceData runnerData = registry().findRunnerData(runner);
					raceTimeF.setText(runnerData.getResult().formatRacetime());
					statusF.update(runnerData.getStatus());
					overwriteWarningL.setVisible(runnerData.hasData());
					mergeRunnerB.setEnabled(true);					
				} else {
					categoryF.setText("");
					courseF.setText("");
					raceTimeF.setText("");
					statusF.reset();
					overwriteWarningL.setVisible(false);
					mergeRunnerB.setEnabled(false);
				}
			}
		});
	}
	
	protected Runner getSelectedRunner() {
		Object selectedItem = searchRegistryCB.getSelectedItem();
		if( selectedItem instanceof Runner ) {
			return (Runner) selectedItem;
		} else {
			return null;
		}
	}
	
	protected void initButtons(JComponent panel) {
		mergeRunnerB = new JButton(GecoIcon.createIcon(GecoIcon.MergeRunner));
		mergeRunnerB.setToolTipText("Merge ecard data into selected runner");
		mergeRunnerB.setEnabled(false);
		mergeRunnerB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Runner targetRunner = getSelectedRunner();
				control().mergeRunnerWithData(targetRunner, wizard().getECardData(), wizard().getSourceRunner());
				wizard().closeAfterMerge();
			}
		});
		overwriteWarningL = new JLabel(GecoIcon.createIcon(GecoIcon.Overwrite));
		overwriteWarningL.setToolTipText("Warning! Runner already has ecard data. Merging will overwrite existing data");
		overwriteWarningL.setVisible(false);
		mergeRunnerB.setAlignmentX(MergeWizard.CENTER_ALIGNMENT);
		overwriteWarningL.setAlignmentX(MergeWizard.CENTER_ALIGNMENT);
		Box buttons = Box.createVerticalBox();
		buttons.add(mergeRunnerB);
		buttons.add(overwriteWarningL);
		panel.add(buttons, buttonsCol(3));
	}

	protected void initDataPanel(JComponent panel) {
		initDataLine1(panel);
		initDataLine2(panel);
		initDataLine3(panel);
	}

	private void initDataLine1(JComponent panel) {
		GridBagConstraints c = gridLine();
		setInsets(c, 0, 0);
		c.gridwidth = 4;
		searchRegistryCB = new JComboBox();
		searchRegistryCB.setPreferredSize(new Dimension(100, SwingUtils.SPINNERHEIGHT));
		searchRegistryCB.setEditable(true);
		panel.add(searchRegistryCB, c);
	}

	private void initDataLine2(JComponent panel) {
		GridBagConstraints c = gridLine();
		panel.add(new JLabel("Category"), c);
		categoryF = new DataField();
		panel.add(categoryF, c);
		panel.add(new JLabel("Course"), c);
		courseF = new DataField();
		panel.add(courseF, c);
	}

	private void initDataLine3(JComponent panel) {
		GridBagConstraints c = gridLine();
		panel.add(new JLabel("Status"), c);
		statusF = new StatusField();
		panel.add(statusF, c);
		panel.add(new JLabel("Time"), c);
		raceTimeF = new DataField();
		panel.add(raceTimeF, c);
	}
	
}