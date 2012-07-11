/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.merge;

import net.geco.model.Status;

public class StatusField extends DataField {
	public void update(Status status) {
		setText(status.toString());
		setBackground(status.color());			
	}
}