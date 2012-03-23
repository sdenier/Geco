/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.basics;

import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Aug 21, 2010
 *
 */
public interface GecoRequestHandler extends GService {

	public String requestMergeUnknownRunner(RunnerRaceData data, String chip);
	
	public String requestMergeExistingRunner(RunnerRaceData data, Runner target);
	
}
