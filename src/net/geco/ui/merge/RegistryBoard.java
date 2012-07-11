/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.merge;

import java.awt.GridBagConstraints;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import net.geco.ui.basics.GecoIcon;

public class RegistryBoard extends AbstractMergeBoard {

	public RegistryBoard(JComponent panel, int firstLine) {
		super(panel, "Registry", firstLine);
	}

	protected void initButtons(JComponent panel) {
		JButton mergeRunnerB = new JButton(GecoIcon.createIcon(GecoIcon.MergeRunner));
		mergeRunnerB.setToolTipText("Merge ecard data into selected runner");
		JLabel overwriteWarningL = new JLabel(GecoIcon.createIcon(GecoIcon.Overwrite));
		overwriteWarningL.setToolTipText("Warning! Runner already has ecard data. Merging will overwrite existing data");
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
		c.anchor = GridBagConstraints.WEST;
		setInsets(c, 0, 0);
		c.gridwidth = 4;
		JComboBox searchRegistryCB = new JComboBox(new String[]{"Runner 1", "Runner 2", "Runner 3"});
		searchRegistryCB.setEditable(true);
		panel.add(searchRegistryCB, c);
	}

	private void initDataLine2(JComponent panel) {
		GridBagConstraints c = gridLine();
		c.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel("Category"), c);
		panel.add(new DataField(), c);
		panel.add(new JLabel("Club"), c);
		panel.add(new DataField(), c);
	}

	private void initDataLine3(JComponent panel) {
		GridBagConstraints c = gridLine();
		c.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel("Status"), c);
		panel.add(new StatusField(), c);
		panel.add(new JLabel("Time"), c);
		panel.add(new DataField(), c);
	}
	
}