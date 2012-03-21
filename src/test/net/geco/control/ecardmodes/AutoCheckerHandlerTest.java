/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.ecardmodes;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.geco.control.Checker;
import net.geco.control.ecardmodes.AutoCheckerHandler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Simon Denier
 * @since Mar 21, 2012
 *
 */
public class AutoCheckerHandlerTest extends ECardModeSetup {
	
	@Mock private Checker checker;

	@Before
	public void setUp() {
		setUpMockControls();
		setUpMockCardData();
		when(gecoControl.checker()).thenReturn(checker);
	}
	
	@Test
	public void handleEcard() {
		new AutoCheckerHandler(gecoControl).handleECard(fullRunnerData);
		verify(checker).check(fullRunnerData);
	}

	@Test
	public void handleEcardReturnsId() {
		String returnedEcard = new AutoCheckerHandler(gecoControl).handleECard(fullRunnerData);
		assertEquals(fullRunnerData.getRunner().getEcard(), returnedEcard);
	}

}
