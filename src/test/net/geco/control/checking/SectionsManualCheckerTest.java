/**
 * Copyright (c) 2014 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.checking;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import net.geco.control.GecoControl;
import net.geco.control.checking.Checker;
import net.geco.control.checking.SectionsManualChecker;
import net.geco.control.checking.SectionsTracer.SectionPunches;
import net.geco.model.Course;
import net.geco.model.RunnerRaceData;
import net.geco.model.Section;
import net.geco.model.SectionTraceData;
import net.geco.model.impl.SectionFactory;

import org.junit.Before;
import org.junit.Test;

import test.net.geco.testfactory.CourseFactory;
import test.net.geco.testfactory.MockControls;
import test.net.geco.testfactory.RunnerFactory;
import test.net.geco.testfactory.TraceFactory;

/**
 * @author Simon Denier
 * @since Sep 17, 2014
 *
 */
public class SectionsManualCheckerTest {

	private SectionFactory factory;
	private SectionsManualChecker subject;
	private Section targetSection;
	private Checker mockChecker;
	private Course xCourse;

	@Before
	public void setup() {
		factory = new SectionFactory();
		mockChecker = mock(Checker.class);
		GecoControl mockGeco = MockControls.mockGecoControlWithFactory(factory);
		when(mockGeco.checker()).thenReturn(mockChecker);
		subject = new SectionsManualChecker(mockGeco);
	}

	@Test
	public void rebuildSectionOrder_returnsSectionTraceWithUpdatedOrder() {
		SectionTraceData reorderedSections = subject.rebuildSectionOrder(createSectionTrace(), targetSection, 3);
		assertThat(reorderedSections.sectionLabelAt(2), equalTo(""));
		assertThat(reorderedSections.sectionLabelAt(3), equalTo("B"));
	}

	@Test
	public void rebuildSectionOrder_canReorderSections() {
		SectionTraceData reorderedSections = subject.rebuildSectionOrder(createSectionTrace(), targetSection, 6);
		assertThat(reorderedSections.sectionLabelAt(5), equalTo("C"));
		assertThat(reorderedSections.sectionLabelAt(2), equalTo(""));
		assertThat(reorderedSections.sectionLabelAt(6), equalTo("B"));
	}
	
	@Test
	public void rebuildSectionPunches_backtracesSectionIndicesFromTrace() {
		SectionTraceData currentSectionTrace = (SectionTraceData) factory.createTraceData();
		currentSectionTrace.setTrace(TraceFactory.createTrace("31", "32", "33", "34", "35", "36", "37", "38", "39"));
		SectionTraceData orderedSections = createSectionTrace();
		List<SectionPunches> sectionPunches = subject.rebuildSectionPunches(currentSectionTrace, orderedSections);
		assertThat(sectionPunches.get(0).toString(), equalTo("[0:1]"));
		assertThat(sectionPunches.get(1).toString(), equalTo("[2:4]"));
		assertThat(sectionPunches.get(2).toString(), equalTo("[5:8]"));
	}

	@Test
	public void rebuildSectionPunches_skipsMPsFromTrace() {
		SectionTraceData currentSectionTrace = (SectionTraceData) factory.createTraceData();
		currentSectionTrace.setTrace(TraceFactory.createTrace("31", "32", "33", "-34", "35", "36", "37", "-38", "39"));
		SectionTraceData orderedSections = createSectionTrace();
		List<SectionPunches> sectionPunches = subject.rebuildSectionPunches(currentSectionTrace, orderedSections);
		assertThat(sectionPunches.get(0).toString(), equalTo("[0:1]"));
		assertThat(sectionPunches.get(1).toString(), equalTo("[2:3]"));
		assertThat(sectionPunches.get(2).toString(), equalTo("[4:6]"));
	}

	@Test
	public void rebuildSectionPunches_takesIntoAccountAddedAndSubstTrace() {
		SectionTraceData currentSectionTrace = (SectionTraceData) factory.createTraceData();
		currentSectionTrace.setTrace(TraceFactory.createTrace("31", "32", "33", "+34", "35", "36", "37", "-38+40", "39"));
		SectionTraceData orderedSections = createSectionTrace();
		List<SectionPunches> sectionPunches = subject.rebuildSectionPunches(currentSectionTrace, orderedSections);
		assertThat(sectionPunches.get(0).toString(), equalTo("[0:1]"));
		assertThat(sectionPunches.get(1).toString(), equalTo("[2:4]"));
		assertThat(sectionPunches.get(2).toString(), equalTo("[5:8]"));
	}

	@Test
	public void refreshTraceWithUpdatedSection() {
		SectionTraceData currentTrace = createSectionTrace();
		currentTrace.setTrace(TraceFactory.createTrace("31", "32", "33", "-34", "35", "36", "37", "-38+40", "39"));
		RunnerRaceData raceData = RunnerFactory.createWithCourse(xCourse);
		raceData.setPunches(TraceFactory.createPunches(31, 32, 33, 35, 36, 37, 40, 39));
		raceData.setTraceData(currentTrace);
		subject.refreshTraceWithUpdatedSection(raceData, targetSection, 4);

		assertThat(raceData.getTraceData().formatTrace(), equalTo("31,32,+33,-33,-34,35,36,37,-38+40,39"));
		verify(mockChecker).setResult(raceData);
	}

	private SectionTraceData createSectionTrace() {
		targetSection = CourseFactory.createSection("B", 2);
		Section sectionA = CourseFactory.createSection("A", 0);
		Section sectionC = CourseFactory.createSection("C", 5);
		xCourse = CourseFactory.createCourse("X", 31, 32, 33, 34, 35, 36, 37, 38, 39);
		xCourse.putSection(sectionA);
		xCourse.putSection(targetSection);
		xCourse.putSection(sectionC);
		xCourse.refreshSectionCodes();
		
		SectionTraceData traceData = (SectionTraceData) factory.createTraceData();
		traceData.putSectionAt(sectionA, 0);
		traceData.putSectionAt(targetSection, 2);
		traceData.putSectionAt(sectionC, 5);
		return traceData;
	}
	
}
