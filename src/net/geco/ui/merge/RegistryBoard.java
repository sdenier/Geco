/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.merge;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import net.geco.model.RunnerRaceData;
import net.geco.ui.basics.GecoIcon;
import net.geco.ui.basics.SwingUtils;

public class RegistryBoard extends AbstractMergeBoard {

	private JComboBox searchRegistryCB;
	private DataField categoryF;
	private DataField courseF;
	private DataField raceTimeF;
	private StatusField statusF;
	private JLabel overwriteWarningL;

	public RegistryBoard(MergeWizard wizard, JComponent panel, int firstLine) {
		super("Registry", wizard, panel, firstLine);
	}

	public void updatePanel() {
		searchRegistryCB.setModel(new DefaultComboBoxModel(registry().getRunnersData().toArray()));
		searchRegistryCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				RunnerRaceData runnerData = (RunnerRaceData) searchRegistryCB.getSelectedItem();
				categoryF.setText(runnerData.getRunner().getCategory().getName());
				courseF.setText(runnerData.getCourse().getName());
				raceTimeF.setText(runnerData.getResult().formatRacetime());
				statusF.update(runnerData.getStatus());
				checkOverwritingWarning(runnerData);
			}

			private void checkOverwritingWarning(RunnerRaceData runnerData) {
				overwriteWarningL.setVisible(runnerData.hasData());
			}
		});
	}
	
	protected void initButtons(JComponent panel) {
		JButton mergeRunnerB = new JButton(GecoIcon.createIcon(GecoIcon.MergeRunner));
		mergeRunnerB.setToolTipText("Merge ecard data into selected runner");
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