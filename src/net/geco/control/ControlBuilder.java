/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import net.geco.model.Factory;

/**
 * @author Simon Denier
 * @since Nov 17, 2011
 *
 */
public abstract class ControlBuilder {

	protected Factory factory;

	public Factory getFactory() {
		if( factory==null ){
			factory = createFactory();
		}
		return factory;
	}

	protected abstract Factory createFactory();
	
	public abstract StageBuilder createStageBuilder();
	
	public abstract Checker createChecker(GecoControl gecoControl);
	
	public abstract void buildControls(GecoControl gecoControl);

}
