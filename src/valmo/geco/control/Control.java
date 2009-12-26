/**
 * Copyright (c) 2008 Simon Denier
 */
package valmo.geco.control;

import java.util.Properties;

import valmo.geco.core.Announcer;
import valmo.geco.model.Factory;
import valmo.geco.model.Registry;
import valmo.geco.model.Stage;

/**
 * Control is an abstract class for any class acting on the stage data. In particular, one control should
 * be responsible for one single model class. Control provides access to the stage (and the registry through
 * it) and to the factory responsible for creating data. A control should not store any data about the
 * stage, except some configuration parameters. They can use Stage properties for storing persistent parameters. 
 * 
 * @author Simon Denier
 * @since Nov 23, 2008
 */
public abstract class Control implements Announcer.StageListener {
	
	private Factory factory;

	private Stage stage;
	

	public Control(Factory factory) {
		setFactory(factory);
	}
	
	public Control(Factory factory, Stage stage, Announcer announcer) {
		this(factory);
		setStage(stage);
		announcer.registerStageListener(this);
	}
	
	public Factory factory() {
		return this.factory;
	}

	public void setFactory(Factory factory) {
		this.factory = factory;
	}
	
	public Stage stage() {
		return this.stage;
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public Registry registry() {
		return stage().registry();
	}


	@Override
	public void saving(Stage stage, Properties properties) {
	}

	@Override
	public void changed(Stage previous, Stage next) {
		this.stage = next;
	}
	
	@Override
	public void closing(Stage stage) {
	}
	
}
