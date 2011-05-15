/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control;

import java.io.FileNotFoundException;
import java.util.Properties;

import junit.framework.Assert;
import net.geco.basics.TimeManager;
import net.geco.control.StageBuilder;
import net.geco.model.Stage;
import net.geco.model.impl.POFactory;

import org.junit.Before;
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
		Assert.assertEquals(name, stage.getName());
		Assert.assertEquals(baseDir, stage.getBaseDir());
		Assert.assertEquals(zeroHour, TimeManager.time(stage.getZeroHour()));
		Assert.assertEquals(nbBackups, stage.getNbAutoBackups());
		Assert.assertEquals(saveDelay, stage.getAutosaveDelay());
		
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
	 * 
	 * @throws FileNotFoundException
	 */
	@Test
	public void testLoadStageProperties() throws FileNotFoundException {
		StageBuilder stageBuilder = new StageBuilder(factory);
		stageBuilder.loadStageProperties(stage, "testData/valid/");
		assertStageData(stage, "Valid Stage", "testData/valid/", "10:00:00", 10, 1);
	}
	
}
