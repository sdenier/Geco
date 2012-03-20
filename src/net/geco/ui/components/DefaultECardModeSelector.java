/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import net.geco.framework.IGeco;

/**
 * @author Simon Denier
 * @since Mar 20, 2012
 *
 */
public class DefaultECardModeSelector extends JComboBox implements ECardModeSelector {

	private ECardMode currentMode = ECardMode.OffMode;

	private IGeco geco;

	private ECardModeRenderer modeRenderer;

	private boolean recovery = false;
	
	public DefaultECardModeSelector(IGeco geco) {
		super(ECardMode.values());
		this.geco = geco;
		modeRenderer = new ECardModeRenderer();
		setRenderer(modeRenderer);
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectMode((ECardMode) getSelectedItem());
			}
		});
	}

	private void selectMode(ECardMode selectedMode) {
		if( currentMode != selectedMode ){
			currentMode = selectedMode;
			if( !recovery ){
				if( currentMode.isReadMode() ){
					beforeStartingReadMode();
				}
				if( currentMode.isActiveMode() ){
					modeStarted();
				}
				dummyActivation(); // TODO: remove
				currentMode.execute(geco.siHandler());
			}
		}
	}

	private void dummyActivation() {
		new Thread() {
			public void run() {
				try {
					sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				modeActivated();
			}
		}.start();
	}

	public void beforeStartingReadMode() {
		// can be overridden to perform external UI actions
	}
	
	public void modeStarted() {
		modeRenderer.setTemporaryLabel("Starting...");
	}
	
	public void modeActivated() {
		modeRenderer.setTemporaryLabel(null);
		repaint();
	}
	
	public void recoverOffMode() {
		this.recovery = true;
		setSelectedItem(ECardMode.OffMode);
		modeActivated();
		this.recovery = false;
	}
	
}
