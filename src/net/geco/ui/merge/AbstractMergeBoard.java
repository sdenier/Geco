/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.merge;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import net.geco.control.MergeControl;
import net.geco.model.Registry;
import net.geco.model.RunnerRaceData;
import net.geco.ui.basics.SwingUtils;

public abstract class AbstractMergeBoard {

	protected int nextLine;

	private MergeWizard mergeWizard;

	private MergeControl mergeControl;
	
	public AbstractMergeBoard(String title, MergeWizard wizard, JComponent panel, int firstLineY) {
		this.mergeWizard = wizard;
		this.mergeControl = wizard.mergeControl();
		this.nextLine = firstLineY;
		initTitle(panel, title);
		initButtons(panel);
		initDataPanel(panel);
	}

	protected MergeWizard wizard() {
		return mergeWizard;
	}
	
	protected MergeControl control() {
		return mergeControl;
	}
	
	protected Registry registry() {
		return mergeWizard.registry();
	}

	protected RunnerRaceData ecardData() {
		return mergeWizard.getECardData();
	}

	protected void initTitle(JComponent panel, String title) {
		Box titleBox = Box.createHorizontalBox();
		titleBox.add(new JLabel(title));
		titleBox.add(Box.createHorizontalStrut(INSET));
		titleBox.add(new JSeparator());
		GridBagConstraints c = gridLine();
		c.insets = new Insets(INSET, 2 * INSET, INSET, 2 * INSET);
		c.gridwidth = 6;
		panel.add(titleBox, c);
	}

	protected abstract void initButtons(JComponent panel);

	protected abstract void initDataPanel(JComponent panel);

	protected int nextLine() {
		return nextLine++;
	}

	protected GridBagConstraints gridLine() {
		GridBagConstraints c = SwingUtils.compConstraint(GridBagConstraints.RELATIVE,
														 nextLine(),
														 GridBagConstraints.HORIZONTAL,
														 GridBagConstraints.CENTER);
		resetInsets(c);
		return c;
	}

	protected GridBagConstraints buttonsCol(int colHeight) {
		GridBagConstraints c = SwingUtils.gbConstr(nextLine);
		setInsets(c, 15, 15);
		c.anchor = GridBagConstraints.NORTH;
		c.gridheight = colHeight;
		return c;
	}

	protected void resetInsets(GridBagConstraints c) {
		setInsets(c, 0, INSET);
	}

	protected void setInsets(GridBagConstraints c, int left, int right) {
		c.insets = new Insets(TOP, left, 0, right);
	}

	protected static final int TOP = 3;

	protected static final int INSET = 5;
	
}