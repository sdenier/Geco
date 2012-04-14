/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import net.geco.basics.TimeManager;
import net.geco.control.PenaltyChecker;
import net.geco.control.StageBuilder;
import net.geco.model.Course;
import net.geco.model.Registry;
import net.geco.model.Stage;
import net.geco.model.impl.POFactory;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Fonctionnalité: chargement d'une étape dans Geco.
 * En tant qu'utilisateur
 * Je veux charger les données d'une étape
 * Pour les exploiter dans Geco 
 * 
 * @author Simon Denier
 * @since May 15, 2011
 *
 */
public class StageBuilderTest {

	private POFactory factory;
	private Stage stage;

	@Before
	public void setUp() {
		factory = new POFactory();
		stage = factory.createStage();
	}
	
	public void assertStageData(Stage stage, String name, String baseDir, String zeroHour, int nbBackups, int saveDelay) {
		assertEquals(name, stage.getName());
		assertEquals(baseDir, stage.getBaseDir());
		assertEquals(zeroHour, TimeManager.time(stage.getZeroHour()));
		assertEquals(nbBackups, stage.getNbAutoBackups());
		assertEquals(saveDelay, stage.getAutosaveDelay());
		
	}

	@Test
	public void testDefaultStageProperties() {
		stage.loadProperties(new Properties());
		assertStageData(stage, "Stage Name", null, "9:00:00", 9, 2);
	}

	
	/**
	 * Scénario: chargement des données générales.
	 * Lorsque je charge le fichier valide "geco.prop"
	 * Et que je regarde les propriétés de l'étape
	 * Alors je dois voir le nom "Valid Stage"
	 * Et je dois voir l'horaire zéro 10:00
	 */
	@Test
	public void testLoadStageProperties() {
		StageBuilder stageBuilder = new StageBuilder(factory);
		stageBuilder.loadStageProperties(stage, "testData/valid/");
		assertStageData(stage, "Valid Stage", "testData/valid/", "10:00:00", 10, 1);
	}
	
	@Test
	public void testMigration12() {
		StageBuilder stageBuilder = new StageBuilder(factory);
		stageBuilder.loadStageProperties(stage, "testData/valid/");
		assertTrue(stage.version12());
		
		stageBuilder = new StageBuilder(factory);
		stageBuilder.loadStageProperties(stage, "testData/belfield/");
		assertFalse(stage.version12());
	}
	
	@Test
	public void testLoadStage() {
		StageBuilder stageBuilder = new StageBuilder(factory);
		stage = stageBuilder.loadStage("testData/belfield", new PenaltyChecker(factory));
		assertStageData(stage, "Belfield", "testData/belfield", "9:00:00", 9, 2);
		Registry registry = stage.registry();
		assertEquals(32, registry.getCategories().size());
		assertEquals(13, registry.getClubs().size());
		assertEquals(3, registry.getCourses().size());
		assertEquals(60, registry.getRunners().size());
	}

	@Test
	public void ensureAutoCourse() {
		StageBuilder stageBuilder = new StageBuilder(factory);
		stage = stageBuilder.loadStage("testData/belfield", new PenaltyChecker(factory));
		
		Course autoCourse = stage.registry().autoCourse();
		assertEquals("[Auto]", autoCourse.getName());
	}
	
	@Test @Ignore
	public void testLoadStageWithMissingData() {
		StageBuilder stageBuilder = new StageBuilder(factory);
		stage = stageBuilder.loadStage("testData/damaged", new PenaltyChecker(factory));
		Registry registry = stage.registry();
		assertEquals(3, registry.getCategories().size());
		assertEquals(4, registry.getClubs().size());
		assertEquals(2, registry.getCourses().size());
		assertEquals(9, registry.getRunners().size());		
	}

	
}
