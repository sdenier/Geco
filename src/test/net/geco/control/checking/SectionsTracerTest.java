/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.checking;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import net.geco.control.checking.SectionsTracer;
import net.geco.control.checking.SectionsTracer.SectionPunches;
import net.geco.model.impl.POFactory;

import org.junit.Before;
import org.junit.Test;

import test.net.geco.testfactory.TraceFactory;

/**
 * @author Simon Denier
 * @since Jul 28, 2013
 *
 */
public class SectionsTracerTest {

	private SectionsTracer subject;

	@Before
	public void setup() {
		subject = new SectionsTracer(new POFactory());
	}
	
	@Test
	public void refineSectionMarkers_nominalCase() {
		List<SectionPunches> okSections = Arrays.asList(
				new SectionPunches(TraceFactory.createTraceData("31", "32", "33", "+34", "+35", "+36", "+37", "+38", "+39")),
				new SectionPunches(TraceFactory.createTraceData("+31", "+32", "+33", "34", "35", "36", "+37", "+38", "+39")),
				new SectionPunches(TraceFactory.createTraceData("+31", "+32", "+33", "+34", "+35", "+36", "37", "38", "39")));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(okSections);
		assertThat(refinedSections, is(okSections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 2);
		assertSectionBeginEnd(refinedSections.get(1), 3, 5);
		assertSectionBeginEnd(refinedSections.get(2), 6, 8);
	}

	@Test
	public void refineSectionMarkers_extendedCase() {
		List<SectionPunches> okSections = Arrays.asList(
				new SectionPunches(TraceFactory.createTraceData("31", "32", "33", "+34", "+35", "+36", "+40", "+37", "+38", "+39")),
				new SectionPunches(TraceFactory.createTraceData("+31", "+32", "+33", "34", "35", "36", "+40", "+37", "+38", "+39")),
				new SectionPunches(TraceFactory.createTraceData("+31", "+32", "+33", "+34", "+35", "+36", "+40", "37", "38", "39")));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(okSections);
		assertThat(refinedSections, is(okSections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 2);
		assertSectionBeginEnd(refinedSections.get(1), 3, 6);
		assertSectionBeginEnd(refinedSections.get(2), 7, 9);
	}
	
	@Test
	public void refineSectionMarkers_beginEndCase() {
		List<SectionPunches> okSections = Arrays.asList(
				new SectionPunches(TraceFactory.createTraceData("+30", "31", "32", "33", "+34", "+35", "+36", "+37", "+38", "+39", "+41")),
				new SectionPunches(TraceFactory.createTraceData("+30", "+31", "+32", "+33", "34", "35", "36", "+37", "+38", "+39", "+41")),
				new SectionPunches(TraceFactory.createTraceData("+30", "+31", "+32", "+33", "+34", "+35", "+36", "37", "38", "39", "+41")));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(okSections);
		assertThat(refinedSections, is(okSections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 3);
		assertSectionBeginEnd(refinedSections.get(1), 4, 6);
		assertSectionBeginEnd(refinedSections.get(2), 7, 10);
	}
	
	public void assertSectionBeginEnd(SectionPunches section, int expectedFirst, int expectedLast) {
		assertThat(section.firstOkPunchIndex(), equalTo(expectedFirst));
		assertThat(section.lastOkPunchIndex(), equalTo(expectedLast));
	}
	
}
