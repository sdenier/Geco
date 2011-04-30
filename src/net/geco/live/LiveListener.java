/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.live;

import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Sep 10, 2010
 *
 */
public interface LiveListener {
	
	public void dataReceived(RunnerRaceData data);

	public void newDataIncoming();

}
