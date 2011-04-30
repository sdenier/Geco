/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.framework;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.geco.framework.IGeco;
import net.geco.model.Registry;


/**
 * GecoPanel is an abstract class for any panel in Geco UI, providing access to the main Geco instance and
 * the main frame. 
 * 
 * @author Simon Denier
 * @since Jan 25, 2009
 *
 */
public abstract class GecoPanel extends JPanel {
	
	private IGeco geco;

	private JFrame frame;
	
	
	public GecoPanel(IGeco geco, JFrame frame) {
		this.geco = geco;
		this.frame = frame;
	}
	
	public IGeco geco() {
		return this.geco;
	}
	
	public JFrame frame() {
		return this.frame;
	}
	
	public Registry registry(){
		return geco().registry();
	}
	
}
