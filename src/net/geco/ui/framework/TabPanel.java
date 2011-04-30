/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.framework;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Properties;

import javax.swing.JFrame;

import net.geco.basics.Announcer.StageListener;
import net.geco.framework.IGecoApp;
import net.geco.model.Stage;


/**
 * @author Simon Denier
 * @since Sep 26, 2009
 *
 */
public abstract class TabPanel extends GecoPanel implements StageListener, ComponentListener {

	/**
	 * @param geco
	 * @param frame
	 */
	public TabPanel(IGecoApp geco, JFrame frame) {
		super(geco, frame);
		addComponentListener(this); // panel can react when it gets shown in card layout
		geco().announcer().registerStageListener(this);
	}
	
	public IGecoApp geco() {
		return (IGecoApp) super.geco();
	}


	@Override
	public void changed(Stage previous, Stage current) {
	}

	@Override
	public void closing(Stage stage) {
	}

	@Override
	public void saving(Stage stage, Properties properties) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

}
