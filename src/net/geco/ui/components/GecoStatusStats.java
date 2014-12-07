/**
 * Copyright (c) 2014 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.components;

import java.awt.Component;
import java.util.Map;
import java.util.Properties;

import javax.swing.JLabel;

import net.geco.basics.Announcer;
import net.geco.control.RegistryStats.StatItem;
import net.geco.framework.IGeco;
import net.geco.model.Messages;
import net.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Nov 20, 2014
 * 
 */
public class GecoStatusStats implements Announcer.StageListener {

	private JLabel view;

	private Thread updateThread;

	private IGeco geco;

	public GecoStatusStats(IGeco geco) {
		this.geco = geco;
		this.view = new JLabel();
		geco.announcer().registerStageListener(this);
		startAutoUpdate();
	}

	protected Thread startAutoUpdate() {
		updateThread = new Thread(new Runnable() {
			@Override
			public synchronized void run() {
				while (true) {
					updateView();
					try {
						wait(5000);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		});
		updateThread.start();
		return updateThread;
	}

	public Component getView() {
		return view;
	}
	
	public void updateView() {
		Map<StatItem, Integer> statuses = geco.registryStats().getTotalCourse();
		view.setText(String.format(Messages.uiGet("GecoStatusStats.StatusFormatString"), //$NON-NLS-1$
				statuses.get(StatItem.Finished), statuses.get(StatItem.Unresolved), statuses.get(StatItem.NOS), statuses.get(StatItem.RUN)));
	}

	@Override
	public void closing(Stage stage) {
		updateThread.interrupt();
		try {
			updateThread.join();
		} catch (InterruptedException e) {
			geco.logger().debug(e);
		}
	}

	@Override
	public void changed(Stage previous, Stage current) {}

	@Override
	public void saving(Stage stage, Properties properties) {}

}
