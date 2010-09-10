/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.core;

import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Aug 21, 2010
 *
 */
public interface GecoRequestHandler {

	public String requestMergeUnknownRunner(RunnerRaceData data, String chip);
	
	public String requestMergeExistingRunner(RunnerRaceData data, Runner target);
	
}
