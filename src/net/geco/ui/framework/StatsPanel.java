/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.framework;

import javax.swing.JFrame;

import net.geco.basics.Announcer;
import net.geco.control.RegistryStats;
import net.geco.framework.IGecoApp;
import net.geco.model.Stage;


/**
 * @author Simon Denier
 * @since Sep 13, 2009
 *
 */
public abstract class StatsPanel extends TabPanel implements Announcer.StageConfigListener {

	private Thread updateThread;
	
	public StatsPanel(IGecoApp geco, JFrame frame) {
		super(geco, frame);
		geco().announcer().registerStageConfigListener(this);
	}
	
	@Override
	public String getTabTitle() {
		return "Stats";
	}

	protected Thread startAutoUpdate() {
		updateThread = new Thread(new Runnable() {
			@Override
			public synchronized void run() {
				while( true ){
					updateTable();
					try {
						wait(5000);
					} catch (InterruptedException e) {
						return;
					}					
				}
			}});
		updateThread.start();
		return updateThread;
	}
	
	protected abstract void updateTable();


	protected RegistryStats stats() {
		return geco().registryStats();
	}
	
	@Override
	public void closing(Stage stage) {
		updateThread.interrupt();
		try {
			updateThread.join();
		} catch (InterruptedException e) {
			geco().logger().debug(e);
		}
	}

	
	@Override
	public void categoriesChanged() {}

	@Override
	public void clubsChanged() {}

	@Override
	public void coursesChanged() {
		changed(null, null);
	}

	
}
