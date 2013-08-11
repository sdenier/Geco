/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.app;

import javax.swing.JFrame;

import net.geco.basics.GecoConfig;
import net.geco.control.ControlBuilder;
import net.geco.framework.IGecoApp;
import net.geco.ui.framework.ConfigPanel;
import net.geco.ui.framework.TabPanel;
import net.geco.ui.framework.UIAnnouncers;

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
public abstract class AppBuilder extends ControlBuilder {

	public abstract String getAppName();

	public abstract GecoConfig getConfig();
	
	public abstract TabPanel[] buildUITabs(IGecoApp geco, JFrame frame, UIAnnouncers announcers);

	public abstract ConfigPanel[] buildConfigPanels(IGecoApp eq, JFrame window);

}
