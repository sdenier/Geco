/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;

import javax.swing.JPanel;

/**
 * @author Simon Denier
 * @since Aug 19, 2010
 *
 */
public class SwingUtils {

	public static JPanel embed(Component comp) {
		JPanel pan = new JPanel();
		pan.add(comp);
		return pan;
	}

	public static GridBagConstraints gbConstr() {
		return compConstraint(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE);
	}

	public static GridBagConstraints gbConstr(int gridy) {
		return compConstraint(GridBagConstraints.RELATIVE, gridy);
	}
	
	public static GridBagConstraints compConstraint(int gridx, int gridy) {
		return compConstraint(gridx, gridy, GridBagConstraints.LINE_START);
	}

	public static GridBagConstraints compConstraint(int gridx, int gridy, int anchor) {
		return compConstraint(gridx, gridy, GridBagConstraints.NONE, anchor);
	}
	
	public static GridBagConstraints compConstraint(int gridx, int gridy, int fill, int anchor) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = gridx;
		constraints.gridy = gridy;
		constraints.fill = fill;
		constraints.anchor = anchor;
		return constraints;
	}
	
}
