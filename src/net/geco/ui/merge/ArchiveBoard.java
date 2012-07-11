/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.merge;

import java.awt.GridBagConstraints;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import net.geco.ui.basics.GecoIcon;

public class ArchiveBoard extends AbstractMergeBoard {

	public ArchiveBoard(JComponent panel, int firstLine) {
		super(panel, "Archive", firstLine);
	}

	protected void initButtons(JComponent panel) {
		JButton insertArchiveB = new JButton(GecoIcon.createIcon(GecoIcon.ArchiveAdd));
		insertArchiveB.setToolTipText("Insert runner from archive with ecard data");
		panel.add(insertArchiveB, buttonsCol(2));
	}

	protected void initDataPanel(JComponent panel) {
		initDataLine1(panel);
		initDataLine2(panel);
	}

	private void initDataLine1(JComponent panel) {
		GridBagConstraints c = gridLine();
		c.gridwidth = 4;
		JComboBox searchArchiveCB = new JComboBox(new String[]{"", "Runner 1", "Runner 2", "Runner 3"});
		searchArchiveCB.setEditable(true);
		panel.add(searchArchiveCB, c);
		c.gridwidth = 1;
		JButton lookupArchiveB = new JButton(GecoIcon.createIcon(GecoIcon.ArchiveSearch));
		lookupArchiveB.setToolTipText("Lookup ecard in archive");
		panel.add(lookupArchiveB, c);
	}

	private void initDataLine2(JComponent panel) {
		GridBagConstraints c = gridLine();
		panel.add(new JLabel("Category"), c);
		panel.add(new DataField(), c);
		panel.add(new JLabel("Club"), c);
		panel.add(new DataField(), c);
	}

}