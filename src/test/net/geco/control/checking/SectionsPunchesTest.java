/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.checking;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import net.geco.control.checking.SectionsTracer;
import net.geco.control.checking.SectionsTracer.SectionPunches;
import net.geco.model.TraceData;

import org.junit.Test;

import test.net.geco.testfactory.TraceFactory;

/**
 * @author Simon Denier
 * @since Jul 23, 2013
 *
 */
public class SectionsPunchesTest {

	
	@Test
	public void nominalIndices() {
		TraceData traceData = TraceFactory.createTraceData("31", "32", "33");
		SectionPunches punches = new SectionsTracer.SectionPunches(traceData);
		assertThat(punches.firstOkPunchIndex(), equalTo(0));
		assertThat(punches.lastOkPunchIndex(), equalTo(2));
	}

	@Test
	public void addedAndSubstPunchesShiftIndices() {
		TraceData traceData = TraceFactory.createTraceData("+30", "31", "32", "-30+31", "33", "+35");
		SectionPunches punches = new SectionsTracer.SectionPunches(traceData);
		assertThat(punches.firstOkPunchIndex(), equalTo(1));
		assertThat(punches.lastOkPunchIndex(), equalTo(4));
	}

	@Test
	public void mpPunchesDontShiftIndices() {
		TraceData traceData = TraceFactory.createTraceData("-30", "31", "+31", "32", "-34", "33", "-35");
		SectionPunches punches = new SectionsTracer.SectionPunches(traceData);
		assertThat(punches.firstOkPunchIndex(), equalTo(0));
		assertThat(punches.lastOkPunchIndex(), equalTo(3));
	}

	@Test
	public void oneOkPunch() {
		TraceData traceData = TraceFactory.createTraceData("-30", "+31", "+32", "31", "+31", "-32+33");
		SectionPunches punches = new SectionsTracer.SectionPunches(traceData);
		assertThat(punches.firstOkPunchIndex(), equalTo(2));
		assertThat(punches.lastOkPunchIndex(), equalTo(2));
	}

	@Test
	public void noOkPunch() {
		TraceData traceData = TraceFactory.createTraceData("-30", "+31", "+32", "+31", "-32+33");
		SectionPunches punches = new SectionsTracer.SectionPunches(traceData);
		assertThat(punches.firstOkPunchIndex(), equalTo(-1));
		assertThat(punches.lastOkPunchIndex(), equalTo(-1));
	}

}
