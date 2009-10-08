/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.ui;

import javax.swing.JFrame;
import javax.swing.JPanel;

import valmo.geco.core.Geco;
import valmo.geco.model.Registry;

/**
 * GecoPanel is an abstract class for any panel in Geco UI, providing access to the main Geco instance and
 * the main frame. 
 * 
 * @author Simon Denier
 * @since Jan 25, 2009
 *
 */
public abstract class GecoPanel extends JPanel {
	
	private Geco geco;

	private JFrame frame;
	
	
	public GecoPanel(Geco geco, JFrame frame) {
		this.geco = geco;
		this.frame = frame;
	}
	
	public Geco geco() {
		return this.geco;
	}
	
	public JFrame frame() {
		return this.frame;
	}
	
	public Registry registry(){
		return geco().registry();
	}
	
}
