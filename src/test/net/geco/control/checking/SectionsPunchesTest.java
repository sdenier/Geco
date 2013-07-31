/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.checking;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import net.geco.control.checking.SectionsTracer.SectionPunches;

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
		SectionPunches punches = TraceFactory.createSectionPunches("31", "32", "33");
		assertThat(punches.firstOkPunchIndex(), equalTo(0));
		assertThat(punches.lastOkPunchIndex(), equalTo(2));
	}

	@Test
	public void addedAndSubstPunchesShiftIndices() {
		SectionPunches punches = TraceFactory.createSectionPunches("+30", "31", "32", "-30+31", "33", "+35");
		assertThat(punches.firstOkPunchIndex(), equalTo(1));
		assertThat(punches.lastOkPunchIndex(), equalTo(4));
	}

	@Test
	public void mpPunchesDontShiftIndices() {
		SectionPunches punches = TraceFactory.createSectionPunches("-30", "31", "+31", "32", "-34", "33", "-35");
		assertThat(punches.firstOkPunchIndex(), equalTo(0));
		assertThat(punches.lastOkPunchIndex(), equalTo(3));
	}

	@Test
	public void oneOkPunch() {
		SectionPunches punches = TraceFactory.createSectionPunches("-30", "+31", "+32", "31", "+31", "-32+33");
		assertThat(punches.firstOkPunchIndex(), equalTo(2));
		assertThat(punches.lastOkPunchIndex(), equalTo(2));
	}

	@Test
	public void noOkPunch() {
		SectionPunches punches = TraceFactory.createSectionPunches("-30", "+31", "+32", "+31", "-32+33");
		assertThat(punches.firstOkPunchIndex(), equalTo(-1));
		assertThat(punches.lastOkPunchIndex(), equalTo(-2));
	}

	@Test
	public void isMissing() {
		SectionPunches punches = TraceFactory.createSectionPunches("31", "32", "33");
		assertThat(punches.isMissing(), equalTo(false));
		punches = TraceFactory.createSectionPunches("-30", "+31", "+32");
		assertThat(punches.isMissing(), equalTo(true));
	}

	@Test
	public void overlaps_false() {
		SectionPunches subject = TraceFactory.createSectionPunches("31", "+34");
		SectionPunches next = TraceFactory.createSectionPunches("+31", "34");
		assertThat(subject.overlaps(next), is(false));
	}

	@Test
	public void overlaps_true() {
		SectionPunches subject = TraceFactory.createSectionPunches("31", "+34", "32");
		SectionPunches next = TraceFactory.createSectionPunches("+31", "34", "+32");
		assertThat(subject.overlaps(next), is(true));
	}

	@Test
	public void overlaps_sharedControl() {
		SectionPunches subject = TraceFactory.createSectionPunches("31", "33", "+34");
		SectionPunches next = TraceFactory.createSectionPunches("+31", "33", "34");
		assertThat(subject.overlaps(next), is(true));
	}
	
	@Test
	public void prevailsOver_nonOverlapping() {
		SectionPunches subject = TraceFactory.createSectionPunches("31", "32", "33", "+34", "+35", "+36");
		SectionPunches next = TraceFactory.createSectionPunches("+31", "+32", "+33", "34", "35", "36");
		assertThat("Non overlapping sections dont prevail over each other", subject.prevailsOver(next), is(false));
	}

	@Test
	public void prevailsOver_subjectPrevailing() {
		SectionPunches subject = TraceFactory.createSectionPunches("+34", "31", "32", "33", "+35", "+36");
		SectionPunches next = TraceFactory.createSectionPunches("34", "+31", "+32", "+33", "35", "36");
		assertThat("Subject prevails over next when counting more punches", subject.prevailsOver(next), is(true));
	}

	@Test
	public void prevailsOver_targetPrevailing() {
		SectionPunches subject = TraceFactory.createSectionPunches("31", "32", "+34", "+35", "33", "+36");
		SectionPunches next = TraceFactory.createSectionPunches("+31", "+32", "34", "35", "+33", "36");
		assertThat("Next section prevails over subject when counting more punches", subject.prevailsOver(next), is(false));
	}
	
	@Test
	public void prevailsOver_equalOverlapping() {
		SectionPunches subject = TraceFactory.createSectionPunches("31", "32", "+34", "33", "+35", "+36");
		SectionPunches next = TraceFactory.createSectionPunches("+31", "+32", "34", "+33", "35", "36");
		assertThat("Subject prevails over next in case of equality", subject.prevailsOver(next), is(true));
	}
	
	@Test
	public void foldStartIndex() {
		SectionPunches subject = TraceFactory.createSectionPunches("31", "32", "+34", "33", "+35", "+36");
		subject.foldStartIndex();
		assertThat(subject.firstOkPunchIndex(), equalTo(1));
		subject.foldStartIndex();
		assertThat(subject.firstOkPunchIndex(), equalTo(3));
	}

	@Test
	public void foldStartIndex_noMoreOkPunch() {
		SectionPunches subject = TraceFactory.createSectionPunches("31", "+34");
		subject.foldStartIndex();
		assertThat(subject.firstOkPunchIndex(), equalTo(-1));
	}

	@Test
	public void foldEndIndex() {
		SectionPunches subject = TraceFactory.createSectionPunches("31", "32", "+34", "33", "+35", "+36");
		subject.foldEndIndex();
		assertThat(subject.lastOkPunchIndex(), equalTo(1));
		subject.foldEndIndex();
		assertThat(subject.lastOkPunchIndex(), equalTo(0));
	}

	@Test
	public void foldEndIndex_noMoreOkPunch() {
		SectionPunches subject = TraceFactory.createSectionPunches("31", "+34");
		subject.foldEndIndex();
		assertThat(subject.lastOkPunchIndex(), equalTo(-2));
	}
	
}
