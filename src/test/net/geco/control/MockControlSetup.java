/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control;

import static org.mockito.Mockito.when;
import net.geco.control.GecoControl;
import net.geco.model.Factory;
import net.geco.model.Registry;
import net.geco.model.Stage;
import net.geco.model.impl.POFactory;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Simon Denier
 * @since Mar 21, 2012
 *
 */
public class MockControlSetup {

	protected Factory factory = new POFactory();

	@Mock protected Stage stage;
	@Mock protected Registry registry;
	@Mock protected GecoControl gecoControl;
	
	public void setUpMockControls() {
		MockitoAnnotations.initMocks(this);
		when(stage.registry()).thenReturn(registry);
		when(gecoControl.stage()).thenReturn(stage);
		when(gecoControl.factory()).thenReturn(factory);
	}

	
}
