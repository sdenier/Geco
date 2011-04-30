/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.impl;

import valmo.geco.model.AbstractRunner;
import valmo.geco.model.Category;
import valmo.geco.model.Club;


/**
 * @author Simon Denier
 * @since Nov 22, 2008
 *
 */
public abstract class AbstractRunnerImpl implements AbstractRunner {

	private Integer archiveId;

	private String firstname;
	
	private String lastname;
	
	private Club club;
	
	private String ecard;
	
	private Category category;


	@Override
	public Integer getArchiveId() {
		return archiveId;
	}

	@Override
	public void setArchiveId(Integer id) {
		this.archiveId = id;
	}

	public String getName() {
		return firstname + " " + lastname; //$NON-NLS-1$
	}
	
	public String getNameR() {
		return lastname + " " + firstname; //$NON-NLS-1$
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

	public String getEcard() {
		return ecard;
	}

	public void setEcard(String ecard) {
		this.ecard = ecard;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
	
}
