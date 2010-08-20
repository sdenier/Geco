/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.util.Properties;

import javax.swing.JFrame;

import valmo.geco.Geco;
import valmo.geco.core.Announcer.StageListener;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Sep 26, 2009
 *
 */
public abstract class TabPanel extends GecoPanel implements StageListener {

	/**
	 * @param geco
	 * @param frame
	 */
	public TabPanel(Geco geco, JFrame frame) {
		super(geco, frame);
		geco().announcer().registerStageListener(this);
	}

	@Override
	public void changed(Stage previous, Stage current) {
	}

	@Override
	public void closing(Stage stage) {
	}

	@Override
	public void saving(Stage stage, Properties properties) {
	}

}
