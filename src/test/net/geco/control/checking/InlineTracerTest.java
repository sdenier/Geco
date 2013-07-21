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

	
	int[] amelie = new int[] {
			131,147,144,135,131,158,161,164,131,154,153,131,136,142,131,159,181,104,195,189,188,185,189,183,179,189,199,121,109,189,107,124,102,189,180,175,174,173,172,169,129,200,
	};
	int[] okamelie = new int[] {
			131,147,144,135,131,158,161,164,131,154,153,131,136,142,131,159,181,104,195,189,188,185,189,183,179,189,199,121,109,189,107,124,102,189,180,175,174,173,172,169,129,200,
	};

	@Test
	public void testAmelieRace() {
		tracer.computeTrace(okamelie, punches(amelie));
		assertEquals(0, tracer.getNbMPs());
		assertEquals(
			"131,147,144,135,131,158,161,164,131,154,153,131,136,142,131,159,181,104,195,189,188,185,189,183,179,189,199,121,109,189,107,124,102,189,180,175,174,173,172,169,129,200",
			tracer.getTraceAsString());
	}

	
	int[] misa = new int[] {
			131,158,161,164,131,153,154,153,131,136,142,131,147,144,135,131,159,181,104,195,189,183,179,189,199,121,109,189,107,124,102,189,188,185,189,180,175,174,173,172,169,129,200,
	};
	int[] okmisa = new int[] {
			131,158,161,164,131,154,153,131,136,142,131,147,144,135,131,159,181,104,195,189,183,179,189,199,121,109,189,107,124,102,189,188,185,189,180,175,174,173,172,169,129,200
	};
	
	@Test
	public void testMisaRace() {
		tracer.computeTrace(okmisa, punches(misa));
		assertEquals(0, tracer.getNbMPs());
		assertEquals(
			"131,158,161,164,131,+153,154,153,131,136,142,131,147,144,135,131,159,181,104,195,189,183,179,189,199,121,109,189,107,124,102,189,188,185,189,180,175,174,173,172,169,129,200",
			tracer.getTraceAsString());
	}

	
	int[] lola = new int[] {
			131,154,154,152,131,142,136,142,131,147,144,135,131,158,161,164,131,159,181,104,195,189,199,121,109,189,107,124,102,189,188,185,189,183,179,189,180,175,174,173,172,169,129,200,
	};
	int[] oklola = new int[] {
			131,154,153,131,136,142,131,147,144,135,131,158,161,164,131,159,181,104,195,189,199,121,109,189,107,124,102,189,188,185,189,183,179,189,180,175,174,173,172,169,129,200
	};
	
	@Test
	public void testLolaRace() {
		tracer.computeTrace(oklola, punches(lola));
		assertEquals(1, tracer.getNbMPs());
		assertEquals(
			"131,+154,154,-153+152,131,+142,136,142,131,147,144,135,131,158,161,164,131,159,181,104,195,189,199,121,109,189,107,124,102,189,188,185,189,183,179,189,180,175,174,173,172,169,129,200",
			tracer.getTraceAsString());
	}
	
	
	int[] pauline = new int[] {
			131,136,142,147,144,135,131,158,160,161,164,131,154,153,131,159,181,104,195,189,107,124,102,189,188,185,189,183,179,189,199,121,109,189,180,175,174,173,172,169,129,200,
	};
	int[] okpauline = new int[] {
			131,136,142,131,147,144,135,131,158,160,161,164,131,154,153,131,159,181,104,195,189,107,124,102,189,188,185,189,183,179,189,199,121,109,189,180,175,174,173,172,169,129,200
	};

	@Test
	public void testPaulineRace() {
		tracer.computeTrace(okpauline, punches(pauline));
		assertEquals(1, tracer.getNbMPs());
		assertEquals(
			"131,136,142,-131,147,144,135,131,158,160,161,164,131,154,153,131,159,181,104,195,189,107,124,102,189,188,185,189,183,179,189,199,121,109,189,180,175,174,173,172,169,129,200",
			tracer.getTraceAsString());
	}

}
