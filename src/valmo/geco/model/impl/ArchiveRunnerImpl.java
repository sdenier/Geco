/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.impl;

import valmo.geco.model.ArchiveRunner;

/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public class ArchiveRunnerImpl extends AbstractRunnerImpl implements ArchiveRunner {
	
	private String birthYear;
	
	private String sex;
	

	@Override
	public String getBirthYear() {
		return birthYear;
	}

	@Override
	public void setBirthYear(String year) {
		this.birthYear = year;
	}

	@Override
	public String getSex() {
		return sex;
	}

	@Override
	public void setSex(String sex) {
		this.sex = sex;
	}

	@Override
	public String toString() {
		return idString();
	}
	
	@Override
	public String idString() {
		return getNameR() + ", " + getArchiveId() + ", " + getChipnumber();  
	}


}
