/**
 * Copyright (c) 2014 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.checking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.geco.model.Trace;

/**
 * @author Simon Denier
 * @since Jul 30, 2014
 *
 */
public class ExtraneousPunchTracer {

	public static List<Trace> compute(int[] codes, List<Trace> trace) {
		HashSet<String> codez = new HashSet<String>();
		for (int code : codes) {
			codez.add(Integer.toString(code));
		}
		ArrayList<Trace> extraneousPunches = new ArrayList<Trace>();
		for (Trace t : trace) {
			if( (t.isAdded() || t.isSubst()) && ! codez.contains(t.getAddedCode()) ) {
				extraneousPunches.add(t);
			}
		}
		return extraneousPunches;
	}

}
