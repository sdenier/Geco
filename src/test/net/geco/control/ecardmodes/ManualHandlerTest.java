/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.ecardmodes;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.geco.basics.MergeRequestHandler;
import net.geco.control.ecardmodes.ManualHandler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Simon Denier
 * @since Mar 23, 2012
 *
 */
public class ManualHandlerTest extends ECardModeSetup {

	@Mock private MergeRequestHandler requestHandler;
	
	@Before
	public void setUp() {
		setUpMockControls();
		setUpMockCardData();
		when(gecoControl.getService(MergeRequestHandler.class)).thenReturn(requestHandler);
	}
	
	@Test
	public void handleDuplicateRequestsMerge() {
		new ManualHandler(gecoControl).handleDuplicate(danglingRunnerData, fullRunner);
		verify(requestHandler).requestMergeExistingRunner(danglingRunnerData, fullRunner);
	}

	@Test
	public void handleUnregisteredRequestsMerge() {
		new ManualHandler(gecoControl).handleUnregistered(danglingRunnerData, "100");
		verify(requestHandler).requestMergeUnknownRunner(danglingRunnerData, "100");
	}
	
}
