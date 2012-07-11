/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.merge;

import javax.swing.JTextField;

public class DataField extends JTextField {
	public DataField() {
		super(5);
		setEditable(false);
		setHorizontalAlignment(CENTER);
	}
}