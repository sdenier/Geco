/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.testfactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import net.geco.app.AppBuilder;
import net.geco.basics.Announcer;
import net.geco.control.GecoControl;
import net.geco.control.StageBuilder;
import net.geco.model.Factory;
import net.geco.model.Messages;
import net.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Aug 27, 2011
 *
 */
public class GecoFixtures {
	
	public static GecoControl loadFixtures(String baseDir, AppBuilder builder) {
		Factory factory = builder.getFactory();
		GecoControl gecoControl = MockControls.mockGecoControl();
		Announcer announcer = mock(Announcer.class);
		when(gecoControl.factory()).thenReturn(factory);
		when(gecoControl.announcer()).thenReturn(announcer);
		Messages.put("ui", "net.geco.ui.messages"); //$NON-NLS-1$ //$NON-NLS-2$
		Stage stage = new StageBuilder(builder.getFactory()).loadStage(baseDir, builder.createChecker(gecoControl));
		when(gecoControl.stage()).thenReturn(stage);
		when(gecoControl.registry()).thenReturn(stage.registry());
		return gecoControl;
	}

}
