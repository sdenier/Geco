/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import net.geco.model.TraceData;

import org.junit.Test;

import test.net.geco.testfactory.TraceFactory;

/**
 * @author Simon Denier
 * @since Jul 23, 2013
 *
 */
public class TraceDataTest {

	@Test
	public void getPunchTrace_nominal() {
		TraceData trace = TraceFactory.createTraceData("31", "+32", "-33+34", "35", "+36"); 
		assertThat(trace.formatPunchTrace(), equalTo("31,+32,-33+34,35,+36"));
	}

	@Test
	public void getPunchTrace_mps() {
		TraceData trace = TraceFactory.createTraceData("-100", "31", "+32", "-37", "-33+34", "35", "+36", "-38");
		assertThat(trace.formatPunchTrace(), equalTo("31,+32,-33+34,35,+36"));
	}
	
}
