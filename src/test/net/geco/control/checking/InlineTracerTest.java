/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.checking;


import static org.junit.Assert.assertEquals;

import java.util.Date;

import net.geco.control.checking.InlineTracer;
import net.geco.control.checking.Tracer;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.TraceData;
import net.geco.model.impl.POFactory;

import org.junit.Before;
import org.junit.Test;



/**
 * @author Simon Denier
 * @since Jan 2, 2009
 *
 */
public class InlineTracerTest {

	private Tracer tracer;
	private Factory factory;

	@Before
	public void setUp() {
		factory = new POFactory();
		tracer = new InlineTracer(factory);
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
	
	public Punch[] punches(int[] codes) {
		Punch[] punches = new Punch[codes.length];
		for (int i = 0; i < codes.length; i++) {
			punches[i] = punch(codes[i]);
		}
		return punches;
	}

	
	int[] trace1 = new int[] {
			131,147,144,135,131,158,161,164,131,154,153,131,136,142,131,159,181,104,195,189,188,185,189,183,179,189,199,121,109,189,107,124,102,189,180,175,174,173,172,169,129,200,
	};
	int[] course1 = new int[] {
			131,147,144,135,131,158,161,164,131,154,153,131,136,142,131,159,181,104,195,189,188,185,189,183,179,189,199,121,109,189,107,124,102,189,180,175,174,173,172,169,129,200,
	};

	@Test
	public void testAmelieRace() {
		TraceData trace = tracer.computeTrace(course1, punches(trace1));
		assertEquals(0, trace.getNbMPs());
		assertEquals(
			"131,147,144,135,131,158,161,164,131,154,153,131,136,142,131,159,181,104,195,189,188,185,189,183,179,189,199,121,109,189,107,124,102,189,180,175,174,173,172,169,129,200",
			trace.formatTrace());
	}

	
	int[] trace2 = new int[] {
			131,158,161,164,131,153,154,153,131,136,142,131,147,144,135,131,159,181,104,195,189,183,179,189,199,121,109,189,107,124,102,189,188,185,189,180,175,174,173,172,169,129,200,
	};
	int[] course2 = new int[] {
			131,158,161,164,131,154,153,131,136,142,131,147,144,135,131,159,181,104,195,189,183,179,189,199,121,109,189,107,124,102,189,188,185,189,180,175,174,173,172,169,129,200
	};
	
	@Test
	public void testMisaRace() {
		TraceData trace = tracer.computeTrace(course2, punches(trace2));
		assertEquals(0, trace.getNbMPs());
		assertEquals(
			"131,158,161,164,131,+153,154,153,131,136,142,131,147,144,135,131,159,181,104,195,189,183,179,189,199,121,109,189,107,124,102,189,188,185,189,180,175,174,173,172,169,129,200",
			trace.formatTrace());
	}

	
	int[] trace3 = new int[] {
			131,154,154,152,131,142,136,142,131,147,144,135,131,158,161,164,131,159,181,104,195,189,199,121,109,189,107,124,102,189,188,185,189,183,179,189,180,175,174,173,172,169,129,200,
	};
	int[] course3 = new int[] {
			131,154,153,131,136,142,131,147,144,135,131,158,161,164,131,159,181,104,195,189,199,121,109,189,107,124,102,189,188,185,189,183,179,189,180,175,174,173,172,169,129,200
	};
	
	@Test
	public void testLolaRace() {
		TraceData trace = tracer.computeTrace(course3, punches(trace3));
		assertEquals(1, trace.getNbMPs());
		assertEquals(
			"131,+154,154,-153+152,131,+142,136,142,131,147,144,135,131,158,161,164,131,159,181,104,195,189,199,121,109,189,107,124,102,189,188,185,189,183,179,189,180,175,174,173,172,169,129,200",
			trace.formatTrace());
	}
	
	
	int[] trace4 = new int[] {
			131,136,142,147,144,135,131,158,160,161,164,131,154,153,131,159,181,104,195,189,107,124,102,189,188,185,189,183,179,189,199,121,109,189,180,175,174,173,172,169,129,200,
	};
	int[] course4 = new int[] {
			131,136,142,131,147,144,135,131,158,160,161,164,131,154,153,131,159,181,104,195,189,107,124,102,189,188,185,189,183,179,189,199,121,109,189,180,175,174,173,172,169,129,200
	};

	@Test
	public void testPaulineRace() {
		TraceData trace = tracer.computeTrace(course4, punches(trace4));
		assertEquals(1, trace.getNbMPs());
		assertEquals(
			"131,136,142,-131,147,144,135,131,158,160,161,164,131,154,153,131,159,181,104,195,189,107,124,102,189,188,185,189,183,179,189,199,121,109,189,180,175,174,173,172,169,129,200",
			trace.formatTrace());
	}

	int[] course5 = new int[] {31,32,33,34,35};
	int[] trace_addedPunches = new int[] {31,32,32,33,35,34,35};
	int[] trace_extraneousPunches = new int[] {31,32,43,34,45,35,34};

	@Test
	public void trace_hasAddedPunches_withoutExtraneousPenalties() {
		TraceData trace = tracer.computeTrace(course5, punches(trace_addedPunches));
		assertEquals(0, trace.getNbExtraneous());
		assertEquals("31,+32,32,33,+35,34,35", trace.formatTrace());
	}

	@Test
	public void trace_hasAddedPunches_withExtraneousPenalties() {
		TraceData trace = tracer.computeTrace(course5, punches(trace_extraneousPunches));
		assertEquals(2, trace.getNbExtraneous());
		assertEquals("31,32,-33+43,34,+45,35,+34", trace.formatTrace());
	}

}
