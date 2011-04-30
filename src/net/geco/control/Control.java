/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import net.geco.basics.GService;
import net.geco.model.Factory;
import net.geco.model.Registry;
import net.geco.model.Stage;

/**
 * Control is an abstract class for any class acting on the stage data. In particular, one control should
 * be responsible for one single model class. Control provides access to the stage (and the registry through
 * it) and to the factory responsible for creating data. A control should not store any data about the
 * stage, except some configuration parameters. They can use Stage properties for storing persistent parameters. 
 * 
 * @author Simon Denier
 * @since Nov 23, 2008
 */
public abstract class Control implements GService {
		
	private GecoControl gecoControl;

	public Control(GecoControl gecoControl) {
		this.gecoControl = gecoControl;
	}
	
	protected Control(Class<? extends Control> clazz, GecoControl gecoControl) {
		this(gecoControl);
		gecoControl.registerService(clazz, this);
	}

	
	protected GecoControl geco() {
		return this.gecoControl;
	}
	
	public Factory factory() {
		return gecoControl.factory();
	}

	public Stage stage() {
		return gecoControl.stage();
	}
	
	public Registry registry() {
		return stage().registry();
	}
	
	public <T extends GService> T getService(Class<T> clazz) {
		return gecoControl.getService(clazz);
	}

}
