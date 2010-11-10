/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco;


import java.io.IOException;

import javax.imageio.ImageIO;

import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

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
			Application.getApplication().setDockIconImage(ImageIO.read(Geco.class.getResource("/resources/icons/crystal/cnr128.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setupQuitAction(final Geco geco) {
		Application app = Application.getApplication();
		app.setQuitHandler(new QuitHandler() {
			@Override
			public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
				geco.shutdown();
				response.performQuit();
			}
		});
	}
	
}
