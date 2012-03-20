/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.components;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ECardModeRenderer extends JLabel implements ListCellRenderer {

	private String temporaryLabel;

	public ECardModeRenderer() {
		setPreferredSize(new Dimension(100, 32));
		setOpaque(true);
		setHorizontalAlignment(LEFT);
		setVerticalAlignment(CENTER);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		ECardMode mode = (ECardMode) value;
		setIcon(mode.getIcon());
		if( temporaryLabel==null ){
			setText(mode.getTitle());
		} else {
			setText(temporaryLabel);
		}
		setFont(list.getFont());
		if( isSelected ){
			setForeground(list.getSelectionForeground());
			setBackground(list.getSelectionBackground());
		} else {
			setForeground(list.getForeground());
			setBackground(list.getBackground());
		}
		return this;
	}

	public void setTemporaryLabel(String label) {
		this.temporaryLabel = label;
	}
}