/**
 * Copyright (c) 2009 Simon Denier
 */
package test.valmo.geco.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import valmo.geco.model.Registry;
import valmo.geco.model.Runner;

/**
 * @author Simon Denier
 * @since Jan 20, 2009
 *
 */
public class OrRunnerTest {
	
	private Registry registryB;
	
	private Registry registryM;
	
	@Before
	public void setUp() {
		OrFixture fixture = new OrFixture();
		registryB = fixture.importBelfieldData(true);
		registryM = fixture.importMullaghmeenData(true);
	}
	
	@Test
	public void testRunnerBelfieldBase() {
		assertEquals(60, registryB.getRunners().size());
		assertEquals(10, registryB.getRunnersFromCategory("M21").size());
		assertEquals(9, registryB.getRunnersFromCourse("Short Course").size());
	}

	@Test
	public void testRunnerMullaghmeenBase() {
		assertEquals(181, registryM.getRunners().size());
		assertEquals(8, registryM.getRunnersFromCategory("W40").size());
		assertEquals(7, registryM.getRunnersFromCourse("Red").size());
	}

	@Test
	public void testRuthLynamB() {
//		24	10886	Ruth Lynam	CNOC	Long Course	false	W50	36000000	-2	2	true		0
		Runner runner = registryB.findRunnerByChip("10886");
		assertEquals(24, runner.getStartnumber());
		assertEquals("Ruth", runner.getFirstname());
		assertEquals("Lynam", runner.getLastname());
		assertEquals(registryB.findClub("CNOC"), runner.getClub());
		assertEquals(registryB.findCategory("W50"), runner.getCategory());
		assertEquals(registryB.findCourse("Long Course"), runner.getCourse());
		assertTrue(runner.isNC());
	}

	@Test
	public void testErikOronnolsB() {
//		34	27326	Erik Oronnols	Linkopings	Long Course	false	M21	42027000	43736000	2	false		0
		Runner runner = registryB.findRunnerByChip("27326");
		assertEquals(34, runner.getStartnumber());
		assertEquals("Erik", runner.getFirstname());
		assertEquals("Oronnols", runner.getLastname());
		assertEquals(registryB.findClub("Linkopings"), runner.getClub());
		assertEquals(registryB.findCategory("M21"), runner.getCategory());
		assertEquals(registryB.findCourse("Long Course"), runner.getCourse());
		assertFalse(runner.isNC());
	}

	@Test
	public void testSusieNaughtonB() {
//		16	11261	Susie Amelie Naughton	3ROC	Short Course	false	W12	40636000	42908000	2	false		0
		Runner runner = registryB.findRunnerByChip("11261");
		assertEquals(16, runner.getStartnumber());
		assertEquals("Susie Amelie", runner.getFirstname());
		assertEquals("Naughton", runner.getLastname());
		assertEquals(registryB.findClub("3ROC"), runner.getClub());
		assertEquals(registryB.findCategory("W12"), runner.getCategory());
		assertEquals(registryB.findCourse("Short Course"), runner.getCourse());
		assertFalse(runner.isNC());
	}

	@Test
	public void testBillyFyffeM() {
//		168	203186	Billy Fyffe	FERMO	Brown	false	M50	46438000	54054000	2	false		0
		Runner runner = registryM.findRunnerByChip("203186");
		assertEquals(168, runner.getStartnumber());
		assertEquals("Billy", runner.getFirstname());
		assertEquals("Fyffe", runner.getLastname());
		assertEquals(registryM.findClub("FERMO"), runner.getClub());
		assertEquals(registryM.findCategory("M50"), runner.getCategory());
		assertEquals(registryM.findCourse("Brown"), runner.getCourse());
		assertFalse(runner.isNC());
	}

	@Test
	public void testOBoyleM() {
//		162	51009	Caoimhe O'Boyle	CNOC	Orange	false	 	47396000	48530000	2	false		0
		Runner runner = registryM.findRunnerByChip("51009");
		assertEquals(162, runner.getStartnumber());
		assertEquals("Caoimhe", runner.getFirstname());
		assertEquals("O'Boyle", runner.getLastname());
		assertEquals(registryM.findClub("CNOC"), runner.getClub());
		assertEquals("", runner.getCategory().getShortname());
		assertEquals(registryM.findCourse("Orange"), runner.getCourse());
		assertFalse(runner.isNC());
	}

	@Test
	public void testAidanBlagdenM() {
//		43	11445	Aidan Blagden	 	Orange	true	M14	41219000	42673000	2	false		0
		Runner runner = registryM.findRunnerByChip("11445");
		assertEquals(43, runner.getStartnumber());
		assertEquals("Aidan", runner.getFirstname());
		assertEquals("Blagden", runner.getLastname());
		assertEquals(registryM.findClub("[None]"), runner.getClub());
		assertEquals(registryM.findCategory("M14"), runner.getCategory());
		assertEquals(registryM.findCourse("Orange"), runner.getCourse());
		assertFalse(runner.isNC());
	}

}
