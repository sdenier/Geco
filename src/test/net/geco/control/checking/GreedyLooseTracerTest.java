/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.checking;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static test.net.geco.testfactory.TraceFactory.createPunches;
import net.geco.control.checking.GreedyLooseTracer;
import net.geco.model.TraceData;
import net.geco.model.impl.POFactory;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Simon Denier
 * @since Aug 8, 2013
 *
 */
public class GreedyLooseTracerTest {

	private GreedyLooseTracer subject;
	private int[] testCodes;

	@Before
	public void setUp() {
		subject = new GreedyLooseTracer(new POFactory());
		testCodes = new int[] {31,  32, 33, 34, 35};
	}
	
	@Test
	public void markAllMatchingPunches() {
		TraceData trace = subject.computeTrace(testCodes, createPunches(31, 32, 33, 40, 34, 35, 41));
		assertThat(trace.formatTrace(), equalTo("31,32,33,+40,34,35,+41"));
	}
	
	@Test
	public void markDuplicatedPunches() {
		TraceData trace = subject.computeTrace(testCodes, createPunches(31, 32, 33, 33, 34, 35, 31));
		assertThat(trace.formatTrace(), equalTo("31,32,33,33,34,35,31"));
	}
	
	@Test
	public void doesNotLookAtPunchOrder() {
		TraceData trace = subject.computeTrace(testCodes, createPunches(33, 32, 31, 33, 34, 35, 35));
		assertThat(trace.formatTrace(), equalTo("33,32,31,33,34,35,35"));
	}
	
	@Test
	public void doesNotMarkMissingPunches() {
		TraceData trace = subject.computeTrace(testCodes, createPunches(40, 33, 31, 40, 33, 34, 35, 35));
		assertThat(trace.formatTrace(), equalTo("+40,33,31,+40,33,34,35,35"));
		assertThat(trace.getNbMPs(), equalTo(0));
	}
	
}
