/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.basics;

import java.util.Comparator;

/**
 * @author Simon Denier
 * @since Oct 4, 2011
 *
 */
public class EcardComparator implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		Integer n1 = null;
		try {
			n1 = Integer.valueOf(o1);
			Integer n2 = Integer.valueOf(o2);
			return n1.compareTo(n2);
		} catch (NumberFormatException e) {
			if( n1==null ){ // n1 is XXXXaa ecard
				return 1;
			} else {		// n2 is XXXXaa ecard
				return -1;
			}
		}
	}
	
}
