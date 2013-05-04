/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.results;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import net.geco.app.FreeOrderAppBuilder;
import net.geco.basics.TimeManager;
import net.geco.control.GecoControl;
import net.geco.control.results.ResultBuilder;
import net.geco.control.results.ResultBuilder.SplitTime;
import net.geco.model.Registry;
import net.geco.model.Result;
import net.geco.model.RunnerRaceData;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import test.net.geco.GecoFixtures;


/**
 * @author Simon Denier
 * @since Aug 28, 2011
 *
 */
public class SplitBuilderTest {

	private static GecoControl gecoControl;

	private static Registry registry;

	private Map<RunnerRaceData, SplitTime[]> allSplits;

	@BeforeClass
	public static void setUpBeforeClass() {
		gecoControl = GecoFixtures.loadFixtures("testData/freeorder", new FreeOrderAppBuilder());
		registry = gecoControl.registry();
	}

	@Before
	public void setUp() {
		ResultBuilder resultBuilder = new ResultBuilder(gecoControl);
		Result resultB = resultBuilder.buildResultForCourse(registry.findCourse("Course B"));
		SplitTime[] bestSplits = null;
		allSplits = resultBuilder.buildAllNormalSplits(resultB, bestSplits);
	}
	
	@Test
	public void testComputeSplits() {
		assertEquals(4, registry.getRunnersFromCourse("Course B").size());
		assertEquals(4, allSplits.size());
	}
	
	@Test
	public void testOkSplits() {
		SplitTime[] splitTimes = allSplits.get( registry.findRunnerData(Integer.valueOf(68)) );
		assertEquals(7, splitTimes.length);
		assertSplit(splitTimes[0], "1", "162", "9:14", "9:14");
		assertSplit(splitTimes[2], "3", "165", "27:55", "13:10");
		assertSplit(splitTimes[6], "F", null, "40:10", "1:18");
	}
	
	@Test
	public void testMpSplits() {
		SplitTime[] splitTimes = allSplits.get( registry.findRunnerData(Integer.valueOf(65)) );
		assertEquals(7, splitTimes.length);
		assertSplit(splitTimes[1], "2", "167", "15:31", "5:30");
		assertSplit(splitTimes[5], "6", "-166", TimeManager.NO_TIME_STRING, TimeManager.NO_TIME_STRING);
		assertSplit(splitTimes[6], "F", null, "44:38", "1:07");
	}
	
	@Test
	public void testAddedSplits() {
		SplitTime[] splitTimes = allSplits.get( registry.findRunnerData(Integer.valueOf(64)) );
		assertEquals(8, splitTimes.length);
		assertSplit(splitTimes[2], "3", "166", "19:13", "8:24");
		assertSplit(splitTimes[6], "F", null, "39:39", "0:49");
		assertSplit(splitTimes[7], "", "+163", "0:50", TimeManager.NO_TIME_STRING);	
	}
	
	@Test
	public void testSubSplits() {
		SplitTime[] splitTimes = allSplits.get( registry.findRunnerData(Integer.valueOf(70)) );
		assertEquals(9, splitTimes.length);
		assertSplit(splitTimes[3], "4", "162", "27:19", "8:34");
		assertSplit(splitTimes[4], "5", "-166", TimeManager.NO_TIME_STRING, TimeManager.NO_TIME_STRING);
		assertSplit(splitTimes[5], "6", "-168", TimeManager.NO_TIME_STRING, TimeManager.NO_TIME_STRING);
		assertSplit(splitTimes[6], "F", null, "53:11", "25:52");
		assertSplit(splitTimes[7], "", "+184", "43:47", TimeManager.NO_TIME_STRING);
		assertSplit(splitTimes[8], "", "+167", "51:15", TimeManager.NO_TIME_STRING);
	}

	public void assertSplit(SplitTime splitTime, String seq, String trace, String time, String split) {
		assertEquals(seq, splitTime.seq);
		if( trace!=null ){
			assertEquals(trace, splitTime.trace.getCode());
		}
		assertEquals(time, TimeManager.time(splitTime.time));
		assertEquals(split, TimeManager.time(splitTime.split));
	}
	
}
