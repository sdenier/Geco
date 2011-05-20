/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.app;

import javax.swing.JFrame;

import net.geco.control.GecoControl;
import net.geco.control.StageBuilder;
import net.geco.framework.IGecoApp;
import net.geco.model.Factory;
import net.geco.ui.framework.TabPanel;

/**
 * I define the API to customize a Geco application for a given type of orienteering race.
 * 
 * Role: AppBuilder
 * Responsibilities:
 * - I declare factory methods to create different custom parts of the application: controls, UI tabs
 * - My subclasses override my factories to customize the parts
 * Collaborations:
 * - I interact with Geco to get a GecoControl and return controls
 * - I interact with GecoWindow to return UI tabs
 * 
 * @author Simon Denier
 * @since May 1, 2011
 *
 */
public abstract class AppBuilder {

	protected Factory factory;

	public AppBuilder() {
	}
	
	public Factory getFactory() {
		if( factory==null ){
			factory = createFactory();
		}
		return factory;
	}
	
	protected abstract Factory createFactory();
	
	public abstract StageBuilder createStageBuilder();
	
	public abstract <T> T createChecker(GecoControl gecoControl);
	
	public abstract void buildControls(GecoControl gecoControl);
	
	public abstract TabPanel[] buildUITabs(IGecoApp geco, JFrame frame);

}
