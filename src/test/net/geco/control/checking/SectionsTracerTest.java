/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.checking;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
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
	public void refineSectionMarkers_nominalJoinedSections() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("31", "32", "33", "+34", "+35", "+36", "+37", "+38", "+39"),
				TraceFactory.createSectionPunches("+31", "+32", "+33", "34", "35", "36", "+37", "+38", "+39"),
				TraceFactory.createSectionPunches("+31", "+32", "+33", "+34", "+35", "+36", "37", "38", "39"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 2);
		assertSectionBeginEnd(refinedSections.get(1), 3, 5);
		assertSectionBeginEnd(refinedSections.get(2), 6, 8);
	}

	@Test
	public void refineSectionMarkers_disjoinedSections() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("31", "32", "33", "+34", "+35", "+36", "+40", "+37", "+38", "+39"),
				TraceFactory.createSectionPunches("+31", "+32", "+33", "34", "35", "36", "+40", "+37", "+38", "+39"),
				TraceFactory.createSectionPunches("+31", "+32", "+33", "+34", "+35", "+36", "+40", "37", "38", "39"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 2);
		assertSectionBeginEnd(refinedSections.get(1), 3, 6);
		assertSectionBeginEnd(refinedSections.get(2), 7, 9);
	}
	
	@Test
	public void refineSectionMarkers_expandStartEndSections() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("+30", "31", "32", "33", "+34", "+35", "+36", "+37", "+38", "+39", "+41"),
				TraceFactory.createSectionPunches("+30", "+31", "+32", "+33", "34", "35", "36", "+37", "+38", "+39", "+41"),
				TraceFactory.createSectionPunches("+30", "+31", "+32", "+33", "+34", "+35", "+36", "37", "38", "39", "+41"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 3);
		assertSectionBeginEnd(refinedSections.get(1), 4, 6);
		assertSectionBeginEnd(refinedSections.get(2), 7, 10);
	}

	@Test
	public void refineSectionMarkers_overlappingSections() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("31", "+34", "32", "33", "+35", "+36"),
				TraceFactory.createSectionPunches("+31", "34", "+32", "+33", "35", "36"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 3);
		assertSectionBeginEnd(refinedSections.get(1), 4, 5);
	}

	@Test
	public void refineSectionMarkers_overlappingSections2() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("31", "32", "+34", "+35", "33", "+36"),
				TraceFactory.createSectionPunches("+31", "+32", "34", "35", "+33", "36"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 1);
		assertSectionBeginEnd(refinedSections.get(1), 2, 5);
	}
	
	@Test
	public void refineSectionMarkers_equallyOverlappingSections() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("31", "32", "+34", "33", "+35", "+36"),
				TraceFactory.createSectionPunches("+31", "+32", "34", "+33", "35", "36"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 3);
		assertSectionBeginEnd(refinedSections.get(1), 4, 5);
	}

	@Test
	public void refineSectionMarkers_OverlappingWithOutliersSections() {
		List<SectionPunches> sections = Arrays.asList(
			TraceFactory.createSectionPunches("31", "+40", "32", "33", "34", "+36", "+37", "+38", "+39", "35", "+41"),
			TraceFactory.createSectionPunches("+31", "40", "+32", "+33", "+34", "36", "37", "38", "39", "+35", "41"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 4);
		assertSectionBeginEnd(refinedSections.get(1), 5, 10);
	}

	@Test
	public void refineSectionMarkers_OverlappingWithMultipleOutliersSections() {
		List<SectionPunches> sections = Arrays.asList(
			TraceFactory.createSectionPunches("31", "+40", "32", "+41", "33", "34", "35", "+42", "36", "37",
												"+43", "+44", "+45", "+46", "38", "+47", "+48", "39"),
			TraceFactory.createSectionPunches("+31", "40", "+32", "41", "+33", "+34", "+35", "42", "+36", "+37",
												"43", "44", "45", "46", "+38", "47", "48", "+39"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 9);
		assertSectionBeginEnd(refinedSections.get(1), 10, 17);
	}

	@Test
	public void refineSectionMarkers_Butterfly() {
		List<SectionPunches> sections = Arrays.asList(
			TraceFactory.createSectionPunches("30", "31", "32", "+30", "+33", "+34", "+30", "+35", "+36", "30"),
			TraceFactory.createSectionPunches("+30", "+31", "+32", "30", "33", "34", "+30", "+35", "+36", "30"),
			TraceFactory.createSectionPunches("+30", "+31", "+32", "+30", "+33", "+34", "30", "35", "36", "30"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 2);
		assertSectionBeginEnd(refinedSections.get(1), 3, 5);
		assertSectionBeginEnd(refinedSections.get(2), 6, 9);
	}

	@Test
	public void refineSectionMarkers_ButterflyWithMissingCentral() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("30", "31", "32", "+33", "+34", "+30", "+35", "+36", "30"),
				TraceFactory.createSectionPunches("+30", "+31", "+32", "33", "34", "+30", "+35", "+36", "30"),
				TraceFactory.createSectionPunches("+30", "+31", "+32", "+33", "+34", "30", "35", "36", "30"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 2);
		assertSectionBeginEnd(refinedSections.get(1), 3, 4);
		assertSectionBeginEnd(refinedSections.get(2), 5, 8);
	}

	@Test
	public void refineSectionMarkers_ButterflyWithOverlapping() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("30", "31", "32", "+30", "+33", "+30", "+34", "+35", "+36", "30"),
				TraceFactory.createSectionPunches("+30", "+31", "+32", "30", "33", "+30", "34", "+35", "+36", "30"),
				TraceFactory.createSectionPunches("+30", "+31", "+32", "+30", "+33", "30", "+34", "35", "36", "30"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 2);
		assertSectionBeginEnd(refinedSections.get(1), 3, 6);
		assertSectionBeginEnd(refinedSections.get(2), 7, 9);
	}
	
	@Test
	public void refineSectionMarkers_missingSectionAfterRefinement() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("31", "+34", "32", "33", "+37", "+38", "+35", "+39"),
				TraceFactory.createSectionPunches("+31", "34", "+32", "+33", "+37", "+38", "35", "+39"),
				TraceFactory.createSectionPunches("+31", "+34", "+32", "+33", "37", "38", "+35", "39"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 3);
		assertSectionBeginEnd(refinedSections.get(1), 6, 1);
		assertSectionBeginEnd(refinedSections.get(2), 4, 7);
		assertThat(refinedSections.get(1).isMissing(), is(true));
	}
	
	@Test
	public void refineSectionMarkers_missingSection() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("31", "32", "+33", "+34"),
				TraceFactory.createSectionPunches("+31", "+32", "+33", "+34"),
				TraceFactory.createSectionPunches("+31", "+32", "33", "34"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 1);
		assertSectionBeginEnd(refinedSections.get(1), -1, -2);
		assertSectionBeginEnd(refinedSections.get(2), 2, 3);
	}

	@Test
	public void refineSectionMarkers_missingAndOverlappingSection() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("31", "+33", "32", "+34"),
				TraceFactory.createSectionPunches("+31", "+33", "+32", "+34"),
				TraceFactory.createSectionPunches("+31", "33", "+32", "34"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 2);
		assertSectionBeginEnd(refinedSections.get(1), -1, -2);
		assertSectionBeginEnd(refinedSections.get(2), 3, 3);
	}
	
	@Test
	public void refineSectionMarkers_successiveMissingSections() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("31", "32", "+33", "+34"),
				TraceFactory.createSectionPunches("+31", "+32", "+33", "+34"),
				TraceFactory.createSectionPunches("+31", "+32", "+33", "+34"),
				TraceFactory.createSectionPunches("+31", "+32", "33", "34"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 1);
		assertSectionBeginEnd(refinedSections.get(1), -1, -2);
		assertSectionBeginEnd(refinedSections.get(2), -1, -2);
		assertSectionBeginEnd(refinedSections.get(3), 2, 3);
	}
	
	@Test
	public void refineSectionMarkers_startEndMissingSections() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("+31", "+32", "+33"),
				TraceFactory.createSectionPunches("31", "32", "33"),
				TraceFactory.createSectionPunches("+31", "+32", "+33"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), -1, -2);
		assertSectionBeginEnd(refinedSections.get(1), 0, 2);
		assertSectionBeginEnd(refinedSections.get(2), -1, -2);
	}

	@Test
	public void refineSectionMarkers_allMissing() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("+31", "+32", "+33"),
				TraceFactory.createSectionPunches("+31", "+32", "+33"),
				TraceFactory.createSectionPunches("+31", "+32", "+33"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 2);
		assertSectionBeginEnd(refinedSections.get(1), -1, -2);
		assertSectionBeginEnd(refinedSections.get(2), -1, -2);
	}

	@Test
	public void refineSectionMarkers_OverlappingAndDisjoinedSections() {
		List<SectionPunches> sections = Arrays.asList(
			TraceFactory.createSectionPunches("+100", "31", "+40", "32", "+41", "33", "34", "35", "+42", "36", "37",
												"+101", "+43", "+44", "+45", "+46", "38", "+47", "+48", "39"),
			TraceFactory.createSectionPunches("+100", "+31", "40", "+32", "41", "+33", "+34", "+35", "42", "+36", "+37",
												"+101", "43", "44", "45", "46", "+38", "47", "48", "+39"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 11);
		assertSectionBeginEnd(refinedSections.get(1), 12, 19);
	}

	@Test
	public void refineSectionMarkers_ButterflyWithMissingLoop() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("30", "31", "32", "+30", "+35", "+36", "30"),
				TraceFactory.createSectionPunches("+30", "+31", "+32", "30", "+35", "+36", "30"),
				TraceFactory.createSectionPunches("+30", "+31", "+32", "30", "35", "36", "30"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 2);
		assertSectionBeginEnd(refinedSections.get(1), 3, 3);
		assertSectionBeginEnd(refinedSections.get(2), 4, 6);
	}
	
	@Test
	public void refineSectionMarkers_ButterflyWithMissingLoop2() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("30", "31", "32", "+30", "+35", "+36", "30"),
				TraceFactory.createSectionPunches("30", "+31", "+32", "+30", "+35", "+36", "30"),
				TraceFactory.createSectionPunches("+30", "+31", "+32", "30", "35", "36", "30"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 2);
		assertSectionBeginEnd(refinedSections.get(1), -1, -2);
		assertSectionBeginEnd(refinedSections.get(2), 3, 6);
	}
	
	@Test
	public void refineSectionMarkers_noSection() {
		List<SectionPunches> sections = Collections.emptyList();
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
	}
	
	public void assertSectionBeginEnd(SectionPunches section, int expectedFirst, int expectedLast) {
		assertThat(section.firstOkPunchIndex(), equalTo(expectedFirst));
		assertThat(section.lastOkPunchIndex(), equalTo(expectedLast));
	}
	
}
