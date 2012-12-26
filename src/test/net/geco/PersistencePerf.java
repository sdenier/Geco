/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco;

import java.util.Properties;

import net.geco.control.PenaltyChecker;
import net.geco.control.StageBuilder;
import net.geco.model.Stage;
import net.geco.model.impl.POFactory;

/**
 * @author Simon Denier
 * @since Dec 26, 2012
 *
 */
public class PersistencePerf {
	
	private String testDir;
	
	private Stage stage;

	public PersistencePerf(String testDir) {
		this.testDir = testDir;
	}

	public static void main(String[] args) {
		String testDir = args[0];
		new PersistencePerf(testDir).run();
	}

	public void run() {
		System.out.println("*** Performance Tests for Persistence Libraries ***");
		initialLoad();
		assessCsvSave();
		assessCsvLoad();		
	}

	private void initialLoad() {
		long start = System.currentTimeMillis();
		stage = vanillaLoad(testDir);
		long end = System.currentTimeMillis();
		System.out.println("\nInitial Load: " + (end - start) + " ms");
	}

	private Stage vanillaLoad(String testDir) {
		POFactory factory = new POFactory();
		StageBuilder stageBuilder = new StageBuilder(factory);
		return stageBuilder.loadStage(testDir, new PenaltyChecker(factory));
	}

	private void assessCsvSave() {
		System.out.println("\n*** CSV Save/Backup x10 (vanilla) ***");
		long[] times = new long[10];
		long timesSum = 0;
		for (int i = 0; i < times.length; i++) {
			times[i] = vanillaSave(testDir);
			timesSum += times[i];
			System.out.print(times[i] + " ms, ");
		}
		System.out.println("\nMean time: " + (timesSum / 10.0) + " ms");
		
	}
	
	private long vanillaSave(String testDir) {
		long start = System.currentTimeMillis();
		POFactory factory = new POFactory();
		StageBuilder stageBuilder = new StageBuilder(factory);
		stageBuilder.save(stage, new Properties(), "backup.zip");
		long end = System.currentTimeMillis();
		return end - start;
	}
	
	private void assessCsvLoad() {
		System.out.println("\n*** CSV Load x10 (vanilla) ***");
		long[] times = new long[10];
		long timesSum = 0;
		for (int i = 0; i < times.length; i++) {
			times[i] = csvLoad(testDir);
			timesSum += times[i];
			System.out.print(times[i] + " ms, ");
		}
		System.out.println("\nMean time: " + (timesSum / 10.0) + " ms");
		
	}

	private long csvLoad(String testDir) {
		long start = System.currentTimeMillis();
		vanillaLoad(testDir);
		long end = System.currentTimeMillis();
		return end - start;
	}

}
