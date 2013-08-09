/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.checking;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static test.net.geco.testfactory.TraceFactory.createPunches;
import static test.net.geco.testfactory.TraceFactory.createSectionPunches;
import net.geco.control.checking.SectionsTracer.SectionPunches;
import net.geco.model.Punch;

import org.junit.Test;

/**
 * @author Simon Denier
 * @since Jul 23, 2013
 *
 */
public class SectionsPunchesTest {

	
	@Test
	public void nominalIndices() {
		SectionPunches punches = createSectionPunches("31", "32", "33");
		assertThat(punches.firstOkPunchIndex(), equalTo(0));
		assertThat(punches.lastOkPunchIndex(), equalTo(2));
	}

	@Test
	public void addedAndSubstPunchesShiftIndices() {
		SectionPunches punches = createSectionPunches("+30", "31", "32", "-30+31", "33", "+35");
		assertThat(punches.firstOkPunchIndex(), equalTo(1));
		assertThat(punches.lastOkPunchIndex(), equalTo(4));
	}

	@Test
	public void mpPunchesDontShiftIndices() {
		SectionPunches punches = createSectionPunches("-30", "31", "+31", "32", "-34", "33", "-35");
		assertThat(punches.firstOkPunchIndex(), equalTo(0));
		assertThat(punches.lastOkPunchIndex(), equalTo(3));
	}

	@Test
	public void oneOkPunch() {
		SectionPunches punches = createSectionPunches("-30", "+31", "+32", "31", "+31", "-32+33");
		assertThat(punches.firstOkPunchIndex(), equalTo(2));
		assertThat(punches.lastOkPunchIndex(), equalTo(2));
	}

	@Test
	public void noOkPunch() {
		SectionPunches punches = createSectionPunches("-30", "+31", "+32", "+31", "-32+33");
		assertThat(punches.firstOkPunchIndex(), equalTo(-1));
		assertThat(punches.lastOkPunchIndex(), equalTo(-2));
	}

	@Test
	public void noPunch() {
		SectionPunches punches = createSectionPunches();
		assertThat(punches.firstOkPunchIndex(), equalTo(-1));
		assertThat(punches.lastOkPunchIndex(), equalTo(-2));
	}
	
	@Test
	public void collectPunches() {
		SectionPunches sp = createSectionPunches("31", "32", "33");
		Punch[] punches = createPunches(31, 32, 33);
		assertEqualPunches(sp.collectPunches(punches), punches);
	}

	@Test
	public void collectPunches_refined() {
		SectionPunches sp = createSectionPunches("+31", "32", "+33", "34");
		Punch[] punches = createPunches(31, 32, 33, 34);
		Punch[] expected = createPunches(32, 33, 34);
		assertEqualPunches(sp.collectPunches(punches), expected);
	}

	@Test
	public void collectPunches_refined2() {
		SectionPunches sp = createSectionPunches("31", "32", "+33", "34", "35");
		sp.foldStartIndex();
		sp.foldEndIndex();
		Punch[] punches = createPunches(31, 32, 33, 34, 35);
		Punch[] expected = createPunches(32, 33, 34);
		assertEqualPunches(sp.collectPunches(punches), expected);
	}
	
	@Test
	public void collectPunches_missing() {
		SectionPunches sp = createSectionPunches("+31", "+32", "+33");
		Punch[] punches = createPunches(31, 32, 33);
		assertEqualPunches(sp.collectPunches(punches), new Punch[0]);
	}
	
	@Test
	public void collectPunches_missing2() {
		SectionPunches sp = createSectionPunches("+31", "32", "+33", "+34");
		Punch[] punches = createPunches(31, 32, 33, 34);
		sp.foldStartIndex();
		assertEqualPunches(sp.collectPunches(punches), new Punch[0]);
	}
	
	private void assertEqualPunches(Punch[] actual, Punch[] expected) {
		assertThat(actual.length, equalTo(expected.length));
		for (int i = 0; i < actual.length; i++) {
			assertThat(actual[i].getCode(), equalTo(expected[i].getCode()));
		}
	}
	
	@Test
	public void isMissing() {
		SectionPunches punches = createSectionPunches("31", "32", "33");
		assertThat(punches.isMissing(), equalTo(false));
		punches = createSectionPunches("-30", "+31", "+32");
		assertThat(punches.isMissing(), equalTo(true));
	}

	@Test
	public void overlaps_true() {
		SectionPunches subject = createSectionPunches("31", "+34", "32");
		SectionPunches next = createSectionPunches("+31", "34", "+32");
		assertThat(subject.overlaps(next), is(true));
	}

	@Test
	public void overlaps_false() {
		SectionPunches subject = createSectionPunches("31", "+34");
		SectionPunches next = createSectionPunches("+31", "34");
		assertThat(subject.overlaps(next), is(false));
	}
	
	@Test
	public void overlaps_trueWithSharedControl() {
		SectionPunches subject = createSectionPunches("31", "33", "+34");
		SectionPunches next = createSectionPunches("+31", "33", "34");
		assertThat(subject.overlaps(next), is(true));
	}
	
	@Test
	public void overlaps_falseWithMissingSection() {
		SectionPunches subject = createSectionPunches("31", "+34");
		SectionPunches next = createSectionPunches("+31", "+34");
		assertThat(subject.overlaps(next), is(false));

		subject = createSectionPunches("+31", "+34");
		next = createSectionPunches("+31", "34");
		assertThat(subject.overlaps(next), is(false));

		subject = createSectionPunches("+31", "+34");
		next = createSectionPunches("+31", "+34");
		assertThat(subject.overlaps(next), is(false));
	}
	
	@Test
	public void prevailsOver_subjectPrevailing() {
		SectionPunches subject = createSectionPunches("+34", "31", "32", "33", "+35", "+36");
		SectionPunches next = createSectionPunches("34", "+31", "+32", "+33", "35", "36");
		assertThat("Subject prevails over next when counting more punches", subject.prevailsOver(next), is(true));
	}

	@Test
	public void prevailsOver_targetPrevailing() {
		SectionPunches subject = createSectionPunches("31", "32", "+34", "+35", "33", "+36");
		SectionPunches next = createSectionPunches("+31", "+32", "34", "35", "+33", "36");
		assertThat("Next section prevails over subject when counting more punches",
					subject.prevailsOver(next), is(false));
	}
	
	@Test
	public void prevailsOver_equalOverlapping() {
		SectionPunches subject = createSectionPunches("31", "32", "+34", "33", "+35", "+36");
		SectionPunches next = createSectionPunches("+31", "+32", "34", "+33", "35", "36");
		assertThat("Subject prevails over next in case of equality", subject.prevailsOver(next), is(true));
	}

	@Test
	public void prevailsOver_sameControlOverlapping() {
		SectionPunches subject = createSectionPunches("31", "32", "40", "+33", "+34");
		SectionPunches next = createSectionPunches("+31", "+32", "40", "33", "34");
		assertThat("Next prevails over subject when conflict on the common joint punch",
					subject.prevailsOver(next), is(false));
	}
	
	@Test
	public void foldStartIndex() {
		SectionPunches subject = createSectionPunches("31", "32", "+34", "33", "+35", "+36");
		subject.foldStartIndex();
		assertThat(subject.firstOkPunchIndex(), equalTo(1));
		subject.foldStartIndex();
		assertThat(subject.firstOkPunchIndex(), equalTo(3));
	}

	@Test
	public void foldStartIndex_noMoreOkPunch() {
		SectionPunches subject = createSectionPunches("31", "+34");
		subject.foldStartIndex();
		assertThat(subject.firstOkPunchIndex(), equalTo(-1));
		assertThat(subject.isMissing(), is(true));
	}

	@Test
	public void foldEndIndex() {
		SectionPunches subject = createSectionPunches("31", "32", "+34", "33", "+35", "+36");
		subject.foldEndIndex();
		assertThat(subject.lastOkPunchIndex(), equalTo(1));
		subject.foldEndIndex();
		assertThat(subject.lastOkPunchIndex(), equalTo(0));
	}

	@Test
	public void foldEndIndex_noMoreOkPunch() {
		SectionPunches subject = createSectionPunches("31", "+34");
		subject.foldEndIndex();
		assertThat(subject.lastOkPunchIndex(), equalTo(-2));
		assertThat(subject.isMissing(), is(true));
	}
	
}
