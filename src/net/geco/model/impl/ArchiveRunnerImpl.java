/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.impl;

import net.geco.model.ArchiveRunner;

/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public class ArchiveRunnerImpl extends AbstractRunnerImpl implements ArchiveRunner {
	
	private String sex;
	

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
		return getNameR() + ", " + getArchiveId() + ", " + getEcard();   //$NON-NLS-1$ //$NON-NLS-2$
	}


}
