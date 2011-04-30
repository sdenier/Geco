/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model;

/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public interface AbstractRunner {

	public Integer getArchiveId();
	
	public void setArchiveId(Integer id);

	public String getName();

	public String getNameR();

	public String getFirstname();

	public void setFirstname(String firstname);

	public String getLastname();

	public void setLastname(String lastname);

	public Club getClub();

	public void setClub(Club club);

	public String getEcard();

	public void setEcard(String ecard);

	public Category getCategory();

	public void setCategory(Category category);

	public String toString();

	public String idString();

}
