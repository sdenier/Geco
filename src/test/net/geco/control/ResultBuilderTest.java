/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.geco.control.GecoControl;
import net.geco.control.ResultBuilder;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.Status;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author Simon Denier
 * @since May 29, 2010
 *
 */
public class ResultBuilderTest {

	private static ResultBuilder belfieldResultBuilder;
	private static Stage belfieldStage;
	private static Stage mullaghmeenStage;
	private static ResultBuilder mullaghmeenResultBuilder;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		GecoControl belfieldGeco = new GecoControl("testData/belfield");
		belfieldStage = belfieldGeco.stage();
		belfieldResultBuilder = new ResultBuilder(belfieldGeco);
		
		GecoControl mullaghmeenGeco = new GecoControl("testData/mullaghmeen");
		mullaghmeenStage = mullaghmeenGeco.stage();
		mullaghmeenResultBuilder = new ResultBuilder(mullaghmeenGeco);

	}
	
	@Test
	public void testBlueCourseBasics() {
		Result blueResults = mullaghmeenResultBuilder.buildResultForCourse(mullaghmeenStage.registry().findCourse("Blue"));
		assertEquals("Blue", blueResults.getIdentifier());
		assertFalse(blueResults.isEmpty());
		
		assertFalse(blueResults.getRankedRunners().isEmpty());
		blueResults.clearRankedRunners();
		assertTrue(blueResults.getRankedRunners().isEmpty());

		assertFalse(blueResults.getNRRunners().isEmpty());
		blueResults.clearNrRunners();
		assertTrue(blueResults.getNRRunners().isEmpty());

//		assertFalse(blueResults.getOtherRunners().isEmpty());
//		blueResults.clearOtherRunners();
		assertTrue(blueResults.getOtherRunners().isEmpty());
		
		assertTrue(blueResults.isEmpty());		
	}
	
	@Test
	public void testBlueCourseRanking() {
		Result blueResults = mullaghmeenResultBuilder.buildResultForCourse(mullaghmeenStage.registry().findCourse("Blue"));
		
		List<RankedRunner> ranking = blueResults.getRanking();
		assertEquals(32, ranking.size());
		assertEquals(1, ranking.get(0).getRank());
		assertEquals("Jack Millar", ranking.get(0).getRunnerData().getRunner().getName());
		
		// runner with same rank
		assertEquals(12, ranking.get(11).getRank());
		assertEquals("Bill Hopkins", ranking.get(12).getRunnerData().getRunner().getName());
		assertEquals(12, ranking.get(12).getRank());
		assertEquals("Richard Williamson", ranking.get(11).getRunnerData().getRunner().getName());
		// next
		assertEquals(14, ranking.get(13).getRank());
		assertEquals("Joe Lalor", ranking.get(13).getRunnerData().getRunner().getName());
		
		assertEquals(32, ranking.get(31).getRank());
		assertEquals("Des Doyle", ranking.get(31).getRunnerData().getRunner().getName());
		
		for (int i = 0; i < ranking.size()-1; i++) {
			assertTrue(ranking.get(i).getRunnerData().getResult().getRacetime() <= ranking.get(i+1).getRunnerData().getResult().getRacetime());
		}
	}

	@Test
	public void testBlueCourseLists() {
		Result blueResults = mullaghmeenResultBuilder.buildResultForCourse(mullaghmeenStage.registry().findCourse("Blue"));

		assertEquals(0, blueResults.getOtherRunners().size());
		
		List<RunnerRaceData> nrRunners = blueResults.getNRRunners();
		assertEquals(3, nrRunners.size());
		for (RunnerRaceData runnerRaceData : nrRunners) {
			Assert.assertNotSame(Status.OK, runnerRaceData.getResult().getStatus());
		}

		String[] nr = new String[] {
				"50924", // "Alan Murphy"
				"11993", // "John Martin July Anne Ennis"
				"261733" // "Alan Gartside"
		};
		for (String nrChip : nr) {
			assertTrue(nrRunners.contains(mullaghmeenStage.registry().findRunnerData(nrChip)));
		}
		
	}
	
	@Test
	public void testOrangeCourse() {
		Result orangeResults = mullaghmeenResultBuilder.buildResultForCourse(mullaghmeenStage.registry().findCourse("Orange"));
		assertEquals("Orange", orangeResults.getIdentifier());
		assertFalse(orangeResults.isEmpty());
		
		assertEquals(23, orangeResults.getRanking().size());
		assertEquals(1, orangeResults.getNRRunners().size());
		assertEquals(1, orangeResults.getOtherRunners().size());
		RunnerRaceData runnerRaceData = orangeResults.getOtherRunners().get(0);
		assertEquals("Jackie McCavana", runnerRaceData.getRunner().getName());
		
	}

	
	@Test
	public void testLongCourse() {
		Result lcResults = belfieldResultBuilder.buildResultForCourse(belfieldStage.registry().findCourse("Long Course"));
		assertEquals("Long Course", lcResults.getIdentifier());
		assertFalse(lcResults.isEmpty());
		
		assertEquals(38, lcResults.getRanking().size());
		assertEquals("Gerard Butler", lcResults.getRanking().get(0).getRunnerData().getRunner().getName());
		assertEquals(13, lcResults.getNRRunners().size());
		RunnerRaceData ncRunner = belfieldStage.registry().findRunnerData("10886"); // "Ruth Lynam, N/C"
		assertTrue(ncRunner.getRunner().isNC());
		assertTrue(lcResults.getNRRunners().contains(ncRunner));
	}

	
}
