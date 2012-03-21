/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.ecardmodes;

import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Mar 21, 2012
 *
 */
public class AutoCheckerHandler extends Control implements ECardHandler {

	public AutoCheckerHandler(GecoControl gecoControl) {
		super(gecoControl);
	}

	@Override
	public String handleECard(RunnerRaceData data) {
		geco().checker().check(data);
		return data.getRunner().getEcard();
	}

}
