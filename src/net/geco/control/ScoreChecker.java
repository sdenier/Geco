/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.Status;

/**
 * @author Simon Denier
 * @since Aug 5, 2011
 *
 */
public class ScoreChecker extends PunchChecker {

	public ScoreChecker(Factory factory) {
		super(factory);
	}

	@Override
	public Status checkCodes(int[] codes, Punch[] punches) {
		for (int code : codes) {
			if( indexOf(code, punches, 0) == -1 ){
				return Status.MP;
			}
		}
		return Status.OK;
	}

}
