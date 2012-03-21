/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Mar 21, 2012
 *
 */
public interface ECardHandler {

	public String handleECard(RunnerRaceData data);
	
}
