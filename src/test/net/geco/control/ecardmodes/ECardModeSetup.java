/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.ecardmodes;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Date;

import net.geco.model.Course;
import net.geco.model.Punch;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.gecosi.SiPunch;
import net.gecosi.dataframe.SiDataFrame;

import org.mockito.Mock;

import test.net.geco.control.MockControlSetup;

/**
 * @author Simon Denier
 * @since Mar 21, 2012
 *
 */
public class ECardModeSetup extends MockControlSetup {

	protected Course testCourse;
	@Mock protected SiDataFrame card;
	protected Runner fullRunner;
	protected RunnerRaceData fullRunnerData;
	protected RunnerRaceData danglingRunnerData;

	public void setUpMockCardData() {
		setUpCardPunches(card);
		testCourse = createCourse();
		fullRunner = createRunner();
		fullRunnerData = createFullRunnerData();
		danglingRunnerData = factory.createRunnerRaceData();
		danglingRunnerData.setResult(factory.createRunnerResult());
	}

	protected void setUpCardPunches(SiDataFrame card) {
		SiPunch[] punchArray = new SiPunch[1];
		punchArray[0] = new SiPunch(31, 2000);
		when(card.getPunches()).thenReturn(punchArray);
		when(card.getCheckTime()).thenReturn(20000l);
		when(card.getStartTime()).thenReturn(25000l);
		when(card.getFinishTime()).thenReturn(30000l);
	}
	
	protected Course createCourse() {
		Course course = factory.createCourse();
		course.setName("dummy course");
		course.setCodes(new int[0]);
		return course;
	}

	protected Runner createRunner() {
		Runner runner = factory.createRunner();
		runner.setStartId(1);
		runner.setEcard("999");
		runner.setCourse(testCourse);
		return runner;
	}

	protected RunnerRaceData createFullRunnerData() {
		RunnerRaceData runnerData = factory.createRunnerRaceData();
		runnerData.setRunner(fullRunner);
		runnerData.setResult(factory.createRunnerResult());
		return runnerData;
	}

	protected void checkCardData(SiDataFrame card, RunnerRaceData data) {
		assertEquals(new Date(card.getCheckTime()), data.getControltime());
		assertEquals(new Date(card.getStartTime()), data.getStarttime());
		assertEquals(new Date(card.getFinishTime()), data.getFinishtime());

		SiPunch[] punchObjects = card.getPunches();
		Punch[] punches = data.getPunches();
		assertEquals(punchObjects.length, punches.length);
		for (int i = 0; i < punches.length; i++) {
			SiPunch punchObject = punchObjects[i];
			assertEquals(punchObject.code(), punches[i].getCode());
			assertEquals(new Date(punchObject.timestamp()), punches[i].getTime());			
		}
	}

}
