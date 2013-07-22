/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.checking;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import net.geco.control.checking.FreeOrderTracer;
import net.geco.control.checking.Tracer;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.TraceData;
import net.geco.model.impl.POFactory;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Simon Denier
 * @since Aug 5, 2011
 *
 */
public class FreeOrderTracerTest {
	
	private Factory factory;
	private Tracer checker;
	private int[] courseCodes;

	@Before
	public void setUp() {
		factory = new POFactory();
		checker = new FreeOrderTracer(factory);
		courseCodes = new int[] { 121, 122, 34, 33, 45};
	}
	
	public Punch[] createPunches(int... punches){
		Punch[] punchez = new Punch[punches.length];
		for (int i = 0; i < punches.length; i++) {
			punchez[i] = punch(punches[i]);
		}
		return punchez;
	}
	
	public Punch punch(Date time, int code) {
		Punch punch = factory.createPunch();
		punch.setTime(time);
		punch.setCode(code);
		return punch;
	}
	
	public Punch punch(int code) {
		return punch(new Date(), code);
	}

	@Test
	public void testNoPunchMP() {
		TraceData data = checker.computeTrace(courseCodes, new Punch[0]);
		assertEquals(5, data.getNbMPs());
		assertEquals("-121,-122,-34,-33,-45", data.formatTrace());
	}
	
	@Test
	public void testSimpleCourseOK() {
		TraceData data = checker.computeTrace(courseCodes, createPunches(121, 122, 34, 33, 45));
		assertEquals(0, data.getNbMPs());
		assertEquals("121,122,34,33,45", data.formatTrace());
	}

	@Test
	public void testSimpleCourseFreeOrderOK() {
		TraceData data = checker.computeTrace(courseCodes, createPunches(45, 122, 34, 33, 121));
		assertEquals(0, data.getNbMPs());
		assertEquals("45,122,34,33,121", data.formatTrace());
	}

	@Test
	public void testSimpleCourseDuplicateOK() {
		TraceData data = checker.computeTrace(courseCodes, createPunches(45, 122, 33, 45, 121, 34, 33));
		assertEquals(0, data.getNbMPs());
		assertEquals("45,122,33,+45,121,34,+33", data.formatTrace());
	}

	@Test
	public void testSimpleCourseMP() {
		TraceData data = checker.computeTrace(courseCodes, createPunches(121, 34, 33, 45));
		assertEquals(1, data.getNbMPs());
		assertEquals("121,34,33,45,-122", data.formatTrace());
	}

	@Test
	public void testSimpleCourseMPs() {
		TraceData data = checker.computeTrace(courseCodes, createPunches(34, 33, 45));
		assertEquals(2, data.getNbMPs());
		assertEquals("34,33,45,-121,-122", data.formatTrace());
	}

	@Test
	public void testSimpleCourseReplacePunch() {
		TraceData data = checker.computeTrace(courseCodes, createPunches(121, 122, 34, 33, 46));
		assertEquals(1, data.getNbMPs());
		assertEquals("121,122,34,33,+46,-45", data.formatTrace());
	}

}
