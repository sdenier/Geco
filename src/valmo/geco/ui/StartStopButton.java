/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

/**
 * @author Simon Denier
 * @since Sep 7, 2010
 *
 */
public abstract class StartStopButton extends JButton {

	public StartStopButton() {
		initialize();
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( isSelected() ) {
					doOffAction();
				} else {
					doOnAction();
				}
			}
		});
	}

	protected void initialize() { }

	public void doOnAction() {
		setSelected(true);
		actionOn();
	}

	public abstract void actionOn();

	public void doOffAction() {
		setSelected(false);
		actionOff();
	}

	public abstract void actionOff();

}
