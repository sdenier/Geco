/**
 * Copyright (c) 2009 Simon Denier
 */
package test.valmo.geco.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import valmo.geco.core.TimeManager;
import valmo.geco.model.Punch;
import valmo.geco.model.Registry;
import valmo.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Jan 22, 2009
 *
 */
public class OrRaceDataTest {
	
	private Registry registryB;
	
	private Registry registryM;
	
	@Before
	public void setUp() {
		OrFixture fixture = new OrFixture();
		registryB = fixture.importBelfieldData(true);
		registryM = fixture.importMullaghmeenData(true);
	}

	@Test
	public void testData363786B() {
//		363786;0:00:00;11:16:07;11:16:12;11:31:41;187;11:17:10;186;11:17:59;177;11:18:46;174;11:20:04;175;11:22:25;176;11:23:47;182;11:25:27;163;11:27:19;184;11:27:56;185;11:28:12;161;11:30:33;162;11:30:54;190;11:31:27;
		RunnerRaceData data = registryB.findRunnerData("363786");
		assertEquals("Jonathan", data.getRunner().getFirstname());
		assertEquals("Quinn", data.getRunner().getLastname());
		assertEquals("0:00", TimeManager.time(data.getErasetime()));
		assertEquals("11:16:07", TimeManager.time(data.getControltime()));
		assertEquals("11:16:12", TimeManager.time(data.getStarttime()));
		assertEquals("11:31:41", TimeManager.time(data.getFinishtime()));
		Punch[] punches = data.getPunches();
		assertEquals(13, punches.length);
		assertArrayEquals(data.getRunner().getCourse().getCodes(),
							collectPunchCodes(punches));
		testPunch(187, "11:17:10", punches[0]);
		testPunch(174, "11:20:04", punches[3]);
		testPunch(190, "11:31:27", punches[12]);
		long raceTime = data.getFinishtime().getTime() - data.getStarttime().getTime();
		assertEquals("15:29", TimeManager.time(new Date(raceTime)));
	}

	private int[] collectPunchCodes(Punch[] punches) {
		int[] codes = new int[punches.length];
		for (int i = 0; i < punches.length; i++) {
			codes[i] = punches[i].getCode();
		}
		return codes;
	}
	
	private void testPunch(int code, String time, Punch punch) {
		assertEquals(code, punch.getCode());
		assertEquals(time, TimeManager.time(punch.getTime()));
	}

}
