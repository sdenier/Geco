/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.testfactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import net.geco.basics.Announcer;
import net.geco.control.GecoControl;
import net.geco.model.Factory;
import net.geco.model.Registry;
import net.geco.model.Stage;
import net.geco.model.impl.POFactory;

/**
 * @author Simon Denier
 * @since May 26, 2012
 *
 */
public class MockControls {

	public static GecoControl mockGecoControl() {
		return mock(GecoControl.class);
	}

	public static GecoControl mockGecoControlWith(Factory factory, Stage stage, Announcer announcer) {
		GecoControl mockGecoControl = mockGecoControl();
		when(mockGecoControl.factory()).thenReturn(factory);
		when(mockGecoControl.stage()).thenReturn(stage);
		when(mockGecoControl.announcer()).thenReturn(announcer);
		return mockGecoControl;		
	}

	public static GecoControl mockGecoControlWithFactory(Factory factory) {
		return mockGecoControlWith(factory, mock(Stage.class), mock(Announcer.class));
	}

	public static GecoControl mockGecoControlWithRegistry(Registry registry) {
		Stage mockStage = mock(Stage.class);
		when(mockStage.registry()).thenReturn(registry);
		return mockGecoControlWith(new POFactory(), mockStage, mock(Announcer.class));
	}

}
