/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco;


import java.io.IOException;

import javax.imageio.ImageIO;

import valmo.geco.model.Messages;

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
		System.setProperty("apple.laf.useScreenMenuBar", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", //$NON-NLS-1$
							Messages.getString("GecoMacos.GecoTitle")); //$NON-NLS-1$
		try {
			Application.getApplication().setDockIconImage(
				ImageIO.read(Geco.class.getResource("/resources/icons/crystal/cnr128.png"))); //$NON-NLS-1$
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
