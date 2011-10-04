/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui;

import net.geco.ui.tabs.RunnersTableAnnouncer;

/**
 * @author Simon Denier
 * @since Sep 25, 2011
 *
 */
public interface UIAnnouncers {

	public void registerAnnouncer(RunnersTableAnnouncer announcer);
	
	public RunnersTableAnnouncer getAnnouncer(Class<RunnersTableAnnouncer> announcerClass);

}
