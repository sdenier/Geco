/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import valmo.geco.core.GService;
import valmo.geco.model.Factory;


/**
 * @author Simon Denier
 * @since Aug 22, 2010
 *
 */
public abstract class BasicControl implements GService {

	private Factory factory;
	
	public BasicControl(Factory factory) {
		this.factory = factory;
	}
	
	public Factory factory() {
		return this.factory;
	}
	
}
