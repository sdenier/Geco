/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.impl;

import valmo.geco.model.Club;


/**
 * @author Simon Denier
 * @since Jan 4, 2009
 *
 */
public class ClubImpl implements Club {
	
	private String name;
	
	private String shortname;
	

	public String getName() {
		return name;
	}

	public String getShortname() {
		return shortname;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

}
