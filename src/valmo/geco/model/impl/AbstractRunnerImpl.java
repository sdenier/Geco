/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.impl;

import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.IRunner;


/**
 * @author Simon Denier
 * @since Nov 22, 2008
 *
 */
public abstract class AbstractRunnerImpl implements IRunner {
	
	private String firstname;
	
	private String lastname;
	
	private Club club;
	
	private String chipnumber;
	
	private Category category;

	
	public String getName() {
		return firstname + " " + lastname;
	}
	
	public String getNameR() {
		return lastname + " " + firstname;
	}
	
	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public Club getClub() {
		return club;
	}

	public void setClub(Club club) {
		this.club = club;
	}

	public String getChipnumber() {
		return chipnumber;
	}

	public void setChipnumber(String chipnumber) {
		this.chipnumber = chipnumber;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
	
}
