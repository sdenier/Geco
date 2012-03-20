/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.components;


/**
 * @author Simon Denier
 * @since Mar 20, 2012
 *
 */
public interface ECardModeSelector {

	public boolean requestFocusInWindow();
	
	public void beforeStartingReadMode();
	
	public void modeStarted();
	
	public void modeActivated();

	public void recoverOffMode();
	
}
