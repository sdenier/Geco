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
		Punch[] data = new Punch[0];
		checker.computeTrace(courseCodes, data);
		assertEquals(5, checker.getNbMPs());
		assertEquals("-121,-122,-34,-33,-45", checker.getTraceAsString());
	}
	
	@Test
	public void testSimpleCourseOK() {
		Punch[] data = createPunches(121, 122, 34, 33, 45);
		checker.computeTrace(courseCodes, data);
		assertEquals(0, checker.getNbMPs());
		assertEquals("121,122,34,33,45", checker.getTraceAsString());
	}

	@Test
	public void testSimpleCourseFreeOrderOK() {
		Punch[] data = createPunches(45, 122, 34, 33, 121);
		checker.computeTrace(courseCodes, data);
		assertEquals(0, checker.getNbMPs());
		assertEquals("45,122,34,33,121", checker.getTraceAsString());
	}

	@Test
	public void testSimpleCourseDuplicateOK() {
		Punch[] data = createPunches(45, 122, 33, 45, 121, 34, 33);
		checker.computeTrace(courseCodes, data);
		assertEquals(0, checker.getNbMPs());
		assertEquals("45,122,33,+45,121,34,+33", checker.getTraceAsString());
	}

	@Test
	public void testSimpleCourseMP() {
		Punch[] data = createPunches(121, 34, 33, 45);
		checker.computeTrace(courseCodes, data);
		assertEquals(1, checker.getNbMPs());
		assertEquals("121,34,33,45,-122", checker.getTraceAsString());
	}

	@Test
	public void testSimpleCourseMPs() {
		Punch[] data = createPunches(34, 33, 45);
		checker.computeTrace(courseCodes, data);
		assertEquals(2, checker.getNbMPs());
		assertEquals("34,33,45,-121,-122", checker.getTraceAsString());
	}

	@Test
	public void testSimpleCourseReplacePunch() {
		Punch[] data = createPunches(121, 122, 34, 33, 46);
		checker.computeTrace(courseCodes, data);
		assertEquals(1, checker.getNbMPs());
		assertEquals("121,122,34,33,+46,-45", checker.getTraceAsString());
	}

}
