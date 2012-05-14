/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.ecardmodes;

import static junit.framework.Assert.assertEquals;
import net.geco.control.ecardmodes.AnonCreationHandler;
import net.geco.model.Status;

import org.junit.Test;

/**
 * @author Simon Denier
 * @since Mar 21, 2012
 *
 */
public class UnknownCreationHandlerTest extends AnonCreationHandlerTest {
	
	@Override
	protected AnonCreationHandler subject() {
		return new AnonCreationHandler.UnknownCreationHandler(gecoControl, detector);
	}	
	
	public void handleUnregisteredDontChangeStatus() {
		// void opposite test
	}

	@Test
	public void handleUnregisteredSetsCustomStatus() {
		subject().handleUnregistered(fullRunnerData, "500");
		assertEquals(Status.UNK, fullRunnerData.getStatus());
	}
	
}
