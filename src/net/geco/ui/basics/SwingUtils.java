/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.basics;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * @author Simon Denier
 * @since Aug 19, 2010
 *
 */
public class SwingUtils {
	
	public static int SPINNERHEIGHT = 25;
	
	public static void setLookAndFeel() {
		if( UIManager.getLookAndFeel().getID().equals("Aqua") ) { //$NON-NLS-1$
			SPINNERHEIGHT = 20;
		} else { // try to use Nimbus unless on Mac Os
			try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"); //$NON-NLS-1$
			} catch (Exception e) {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				} catch (InstantiationException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public static JPanel embed(Component comp) {
		JPanel pan = new JPanel();
		pan.add(comp);
		return pan;
	}
	
	public static JPanel makeButtonBar(int align, Component... comps) {
		JPanel pan = new JPanel(new FlowLayout(align));
		for (Component component : comps) {
			pan.add(component);
		}
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
