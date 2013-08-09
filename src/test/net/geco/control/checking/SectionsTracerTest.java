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
import net.geco.model.Course;
import net.geco.model.Punch;
import net.geco.model.Section.SectionType;
import net.geco.model.SectionTraceData;
import net.geco.model.impl.SectionFactory;

import org.junit.Before;
import org.junit.Test;

import test.net.geco.testfactory.CourseFactory;
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
		subject = new SectionsTracer(new SectionFactory());
	}

	/*
	 * Tests for Trace Compute
	 */

	private Course createThreeSectionsCourse() {
		Course course = CourseFactory.createCourse("Classic", new int[]{31, 32, 33, 34, 35, 36, 37, 38, 39});
		course.putSection(CourseFactory.createSection("A", 0, SectionType.INLINE));
		course.putSection(CourseFactory.createSection("B", 3, SectionType.FREEORDER));
		course.putSection(CourseFactory.createSection("C", 6, SectionType.INLINE));
		course.refreshSectionCodes();
		return course;
	}

	private Course createButterflyCourse() {
		Course course = CourseFactory.createCourse("Butterfly", new int[]{30, 31, 32, 30, 33, 34, 30, 35, 36, 30});
		course.putSection(CourseFactory.createSection("A1", 0, SectionType.FREEORDER));
		course.putSection(CourseFactory.createSection("A2", 3, SectionType.INLINE));
		course.putSection(CourseFactory.createSection("A3", 6, SectionType.INLINE));
		course.refreshSectionCodes();
		return course;
	}

	@Test
	public void computeTrace_nominalCase() {
		Course course = createThreeSectionsCourse();
		Punch[] punches = TraceFactory.createPunches(31, 32, 33, 36, 34, 35, 37, 38, 39);
		SectionTraceData trace = subject.computeTrace(course.getSections(), punches);
		assertThat(trace.formatTrace(), equalTo("31,32,33,36,34,35,37,38,39"));
		assertThat(trace.getNbMPs(), equalTo(0));
		assertThat(trace.sectionLabelAt(0), equalTo("A"));
		assertThat(trace.sectionLabelAt(3), equalTo("B"));
		assertThat(trace.sectionLabelAt(6), equalTo("C"));
	}

	@Test
	public void computeTrace_mps() {
		Course course = createThreeSectionsCourse();
		Punch[] punches = TraceFactory.createPunches(31, 40, 33, 36, 41, 35, 42, 37, 38, 39, 43);
		SectionTraceData trace = subject.computeTrace(course.getSections(), punches);
		assertThat(trace.formatTrace(), equalTo("31,-32+40,33,36,+41,35,+42,-34,37,38,39,+43"));
		assertThat(trace.getNbMPs(), equalTo(2));
		assertThat(trace.sectionLabelAt(0), equalTo("A"));
		assertThat(trace.sectionLabelAt(3), equalTo("B"));
		assertThat(trace.sectionLabelAt(8), equalTo("C"));
	}

	@Test
	public void computeTrace_overlapping() {
		Course course = createThreeSectionsCourse();
		Punch[] punches = TraceFactory.createPunches(31, 32, 36, 33, 34, 36, 37, 35, 38, 39);
		SectionTraceData trace = subject.computeTrace(course.getSections(), punches);
		assertThat(trace.formatTrace(), equalTo("31,32,+36,33,34,36,+37,35,-37,38,39"));
		assertThat(trace.getNbMPs(), equalTo(1));
		assertThat(trace.sectionLabelAt(0), equalTo("A"));
		assertThat(trace.sectionLabelAt(4), equalTo("B"));
		assertThat(trace.sectionLabelAt(8), equalTo("C"));
	}

	@Test
	public void computeTrace_missingSection() {
		Course course = createThreeSectionsCourse();
		Punch[] punches = TraceFactory.createPunches(40, 34, 35, 36, 37, 38, 39);
		SectionTraceData trace = subject.computeTrace(course.getSections(), punches);
		assertThat(trace.formatTrace(), equalTo("-31,-32,-33,+40,34,35,36,37,38,39"));
		assertThat(trace.getNbMPs(), equalTo(3));
		assertThat(trace.sectionLabelAt(0), equalTo("A"));
		assertThat(trace.sectionLabelAt(3), equalTo("B"));
		assertThat(trace.sectionLabelAt(7), equalTo("C"));
	}

	@Test
	public void computeTrace_allMissing() {
		Course course = createThreeSectionsCourse();
		Punch[] punches = TraceFactory.createPunches(40, 41, 42, 43);
		SectionTraceData trace = subject.computeTrace(course.getSections(), punches);
		assertThat(trace.formatTrace(), equalTo("+40,-31+41,-32+42,-33+43,-34,-35,-36,-37,-38,-39"));
		assertThat(trace.getNbMPs(), equalTo(9));
		assertThat(trace.sectionLabelAt(0), equalTo("A"));
		assertThat(trace.sectionLabelAt(4), equalTo("B"));
		assertThat(trace.sectionLabelAt(7), equalTo("C"));
	}

	@Test
	public void computeTrace_butterfly() {
		Course course = createButterflyCourse();
		Punch[] punches = TraceFactory.createPunches(30, 31, 32, 30, 33, 34, 30, 35, 36, 30);
		SectionTraceData trace = subject.computeTrace(course.getSections(), punches);
		assertThat(trace.formatTrace(), equalTo("30,31,32,30,33,34,30,35,36,30"));
		assertThat(trace.getNbMPs(), equalTo(0));
		assertThat(trace.sectionLabelAt(0), equalTo("A1"));
		assertThat(trace.sectionLabelAt(3), equalTo("A2"));
		assertThat(trace.sectionLabelAt(6), equalTo("A3"));
	}

	@Test
	public void computeTrace_butterflyWithMissingCentral() {
		Course course = createButterflyCourse();
		Punch[] punches = TraceFactory.createPunches(30, 32, 31, 33, 34, 30, 35, 36, 30);
		SectionTraceData trace = subject.computeTrace(course.getSections(), punches);
		assertThat(trace.formatTrace(), equalTo("30,32,31,-30,33,34,30,35,36,30"));
		assertThat(trace.getNbMPs(), equalTo(1));
		assertThat(trace.sectionLabelAt(0), equalTo("A1"));
		assertThat(trace.sectionLabelAt(3), equalTo("A2"));
		assertThat(trace.sectionLabelAt(6), equalTo("A3"));
	}

	@Test
	public void computeTrace_butterflyWithMissingLoop() {
		Course course = createButterflyCourse();
		Punch[] punches = TraceFactory.createPunches(30, 33, 34, 30, 35, 36, 30);
		SectionTraceData trace = subject.computeTrace(course.getSections(), punches);
		assertThat(trace.formatTrace(), equalTo("-30,-31,-32,30,33,34,30,35,36,30"));
		assertThat(trace.getNbMPs(), equalTo(3));
		assertThat(trace.sectionLabelAt(0), equalTo("A1"));
		assertThat(trace.sectionLabelAt(3), equalTo("A2"));
		assertThat(trace.sectionLabelAt(6), equalTo("A3"));
	}

	@Test
	public void computeTrace_butterflyWithMissingLoop2() {
		Course course = createButterflyCourse();
		Punch[] punches = TraceFactory.createPunches(30, 31, 32, 30, 35, 36, 30);
		SectionTraceData trace = subject.computeTrace(course.getSections(), punches);
		assertThat(trace.formatTrace(), equalTo("30,31,32,-30,-33,-34,30,35,36,30"));
		assertThat(trace.getNbMPs(), equalTo(3));
		assertThat(trace.sectionLabelAt(0), equalTo("A1"));
		assertThat(trace.sectionLabelAt(3), equalTo("A2"));
		assertThat(trace.sectionLabelAt(6), equalTo("A3"));
	}

	@Test
	public void computeTrace_missingRefinedSection() {
		Course course = CourseFactory.createCourse("Classic", new int[]{31, 32, 33, 34, 35, 36, 37, 38, 39});
		course.putSection(CourseFactory.createSection("A", 0, SectionType.INLINE));
		course.putSection(CourseFactory.createSection("B", 3, SectionType.INLINE));
		course.putSection(CourseFactory.createSection("C", 6, SectionType.INLINE));
		course.refreshSectionCodes();

		Punch[] punches = TraceFactory.createPunches(31, 34, 32, 33, 37, 38, 35, 39);
		SectionTraceData trace = subject.computeTrace(course.getSections(), punches);
		assertThat(trace.formatTrace(), equalTo("31,+34,32,33,-34,-35,-36,37,38,+35,39"));
		assertThat(trace.getNbMPs(), equalTo(3));
		assertThat(trace.sectionLabelAt(0), equalTo("A"));
		assertThat(trace.sectionLabelAt(4), equalTo("B"));
		assertThat(trace.sectionLabelAt(7), equalTo("C"));
	}

	@Test
	public void computeTrace_overlappingAndDisjoined() {
		Course course = CourseFactory.createCourse("Classic", new int[]{31, 32, 33, 34, 35, 36, 37, 38, 39,
																		40, 41, 42, 43, 44, 45, 46, 47, 48, 49});
		course.putSection(CourseFactory.createSection("A", 0, SectionType.INLINE));
		course.putSection(CourseFactory.createSection("B", 9, SectionType.INLINE));
		course.refreshSectionCodes();

		Punch[] punches = TraceFactory.createPunches(100, 31, 40, 32, 41, 33, 34, 35, 42, 36, 37,
													 101, 43, 44, 45, 46, 38, 47, 48, 39);
		SectionTraceData trace = subject.computeTrace(course.getSections(), punches);
		assertThat(trace.formatTrace(), equalTo("+100,31,+40,32,+41,33,34,35,+42,36,37,-38,-39+101,-40,-41,-42,43,44,45,46,+38,47,48,-49+39"));
		assertThat(trace.getNbMPs(), equalTo(6));
		assertThat(trace.sectionLabelAt(0), equalTo("A"));
		assertThat(trace.sectionLabelAt(13), equalTo("B"));
	}

	@Test
	public void computeTrace_overlappingAndDisjoined2() {
		Course course = CourseFactory.createCourse("Classic", new int[]{31, 32, 33, 34, 35, 36, 37, 38, 39,
																		40, 41, 42, 43, 44, 45, 46, 47, 48, 49});
		course.putSection(CourseFactory.createSection("A", 0, SectionType.FREEORDER));
		course.putSection(CourseFactory.createSection("B", 9, SectionType.FREEORDER));
		course.refreshSectionCodes();

		Punch[] punches = TraceFactory.createPunches(100, 31, 40, 32, 41, 33, 34, 35, 42, 36, 37,
													 101, 43, 44, 45, 46, 38, 47, 48, 39);
		SectionTraceData trace = subject.computeTrace(course.getSections(), punches);
		assertThat(trace.formatTrace(), equalTo("+100,31,+40,32,+41,33,34,35,+42,36,37,+101,-38,-39,43,44,45,46,+38,47,48,+39,-40,-41,-42,-49"));
		assertThat(trace.getNbMPs(), equalTo(6));
		assertThat(trace.sectionLabelAt(0), equalTo("A"));
		assertThat(trace.sectionLabelAt(14), equalTo("B"));
	}



	/*
	 * Tests for Section Markers
	 */
	
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
	public void refineSectionMarkers_loopSinglePunchSection() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("30", "31", "32", "+30"),
				TraceFactory.createSectionPunches("30", "+31", "+32", "30"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 2);
		assertSectionBeginEnd(refinedSections.get(1), 3, 3);
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
	public void refineSectionMarkers_missingBeforeSectionsAfterRefinement() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("+51", "+52", "31", "+41", "+53", "+54"),
				TraceFactory.createSectionPunches("+51", "+52", "+31", "41", "+53", "+54"),
				TraceFactory.createSectionPunches("51", "52", "+31", "+41", "53", "54"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), -1, -2);
		assertSectionBeginEnd(refinedSections.get(1), -1, -2);
		assertSectionBeginEnd(refinedSections.get(2), 0, 5);
		assertThat(refinedSections.get(0).isMissing(), is(true));
		assertThat(refinedSections.get(1).isMissing(), is(true));
	}
	
	@Test
	public void refineSectionMarkers_missingAfterSectionsAfterRefinement() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("31", "32", "+41", "+42", "33", "34"),
				TraceFactory.createSectionPunches("+31", "+32", "41", "+42", "+33", "+34"),
				TraceFactory.createSectionPunches("+31", "+32", "+41", "42", "+33", "+34"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 5);
		assertSectionBeginEnd(refinedSections.get(1), -1, -2);
		assertSectionBeginEnd(refinedSections.get(2), -1, -2);
		assertThat(refinedSections.get(1).isMissing(), is(true));
		assertThat(refinedSections.get(2).isMissing(), is(true));
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
				TraceFactory.createSectionPunches("30", "31", "32", "+30", "+35", "+36", "+30"),
				TraceFactory.createSectionPunches("+30", "+31", "+32", "30", "+35", "+36", "+30"),
				TraceFactory.createSectionPunches("+30", "+31", "+32", "30", "35", "36", "30"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 2);
		assertSectionBeginEnd(refinedSections.get(1), -1, -2);
		assertSectionBeginEnd(refinedSections.get(2), 3, 6);
	}
	
	@Test
	public void refineSectionMarkers_ButterflyWithMissingLoop2() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("30", "31", "32", "+30", "+35", "+36", "+30"),
				TraceFactory.createSectionPunches("30", "+31", "+32", "+30", "+35", "+36", "+30"),
				TraceFactory.createSectionPunches("+30", "+31", "+32", "30", "35", "36", "30"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 2);
		assertSectionBeginEnd(refinedSections.get(1), -1, -2);
		assertSectionBeginEnd(refinedSections.get(2), 3, 6);
	}

	@Test
	public void refineSectionMarkers_ButterflyWithRefinedMissingLoop() {
		List<SectionPunches> sections = Arrays.asList(
				TraceFactory.createSectionPunches("100", "31", "32", "+41", "33", "+100", "+51", "+52"),
				TraceFactory.createSectionPunches("+100", "+31", "+32", "41", "+33", "+100", "+51", "+52"),
				TraceFactory.createSectionPunches("100", "+31", "+32", "+41", "+33", "100", "51", "52"));
		List<SectionPunches> refinedSections = subject.refineSectionMarkers(sections);
		assertThat(refinedSections, is(sections));
		assertSectionBeginEnd(refinedSections.get(0), 0, 4);
		assertSectionBeginEnd(refinedSections.get(1), -1, -2);
		assertSectionBeginEnd(refinedSections.get(2), 5, 7);
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
