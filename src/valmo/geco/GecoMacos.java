/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco;


import java.io.IOException;

import javax.imageio.ImageIO;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/**
 * @author Simon Denier
 * @since Jul 6, 2010
 *
 */
public class GecoMacos {

	public static void earlySetup() {
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Geco for Orientshow");
		try {
			Application.getApplication().setDockIconImage(ImageIO.read(Geco.class.getResource("/resources/icons/crystal/cnr.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setupQuitAction(final Geco geco) {
		Application app = Application.getApplication();
		app.addApplicationListener(new ApplicationAdapter() {
			@Override
			public void handleQuit(ApplicationEvent arg0) {
				geco.exit();
			}
		});			
	}
	
}
