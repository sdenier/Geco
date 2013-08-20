/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.model.iojson;

import java.util.Properties;

import net.geco.control.StageBuilder;
import net.geco.control.checking.PenaltyChecker;
import net.geco.model.Stage;
import net.geco.model.impl.POFactory;
import net.geco.model.iojson.PersistentStore;

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

	public PersistencePerf(String testDir, Stage stage) {
		this(testDir);
		this.stage = stage;
	}
	
	public static void main(String[] args) {
		System.out.println("*** Performance Tests for Persistence Libraries ***");
		String testDir = args[0];
		Stage stage = new PersistencePerf(testDir).initialLoad();
		new PersistencePerf(testDir, stage).run();
	}
	
	private Stage initialLoad() {
		long start = System.currentTimeMillis();
		Stage stage = vanillaLoad(testDir);
		long end = System.currentTimeMillis();
		System.out.println("\nInitial Load: " + (end - start) + " ms");
		return stage;
	}

	private Stage vanillaLoad(String testDir) {
		POFactory factory = new POFactory();
		StageBuilder stageBuilder = new StageBuilder(factory);
		return stageBuilder.loadStage(testDir, new PenaltyChecker(factory));
	}
	
	public void run() {
		// Comparison not valid anymore
//		new CsvSavePerf().run();
//		new CsvLoadPerf().run();
		new JsonSavePerf().run();
		new JsonLoadPerf().run();
	}

	public abstract class Perf {
		
		private static final int X = 10;

		public void run() {
			System.out.println(String.format("\n*** %s x%s ***", title(), X));
			assessXRun();
		}

		public void runOnce() {
			System.out.println(String.format("\n*** %s x1 ***", title()));
			System.out.println(timeRun() + " ms");
		}
		
		private void assessXRun() {
			long dummyRun = timeRun();
			System.out.println("First run: " + dummyRun + " ms");
			
			long[] times = new long[X];
			long timesSum = 0;
			for (int i = 0; i < times.length; i++) {
				times[i] = timeRun();
				timesSum += times[i];
				System.out.print(times[i] + " ms, ");
			}
			System.out.println("\nMean time (without first): " + (timesSum / (float) X) + " ms");
		}
		
		private long timeRun() {
			long start = System.currentTimeMillis();
			doRun();
			long end = System.currentTimeMillis();
			return end - start;
		}

		protected abstract String title();
		
		protected abstract void doRun();
		
	}

	public class CsvSavePerf extends Perf {
		
		protected String title() {
			return "CSV Save (vanilla)";
		}

		protected void doRun() {
			POFactory factory = new POFactory();
			StageBuilder stageBuilder = new StageBuilder(factory);
			stageBuilder.save(stage, new Properties(), "backup.zip");
		}
		
	}

	public class CsvLoadPerf extends Perf {
		
		protected String title() {
			return "CSV Load (vanilla)";
		}

		protected void doRun() {
			vanillaLoad(testDir);
		}
		
	}

	public class JsonSavePerf extends Perf {
		
		protected String title() {
			return "Json Save";
		}

		protected void doRun() {
			new PersistentStore().saveData(stage);
		}
		
	}
	
	public class JsonLoadPerf extends Perf {

		protected String title() {
			return "Json Load";
		}

		protected void doRun() {
			POFactory factory = new POFactory();
			Stage stage = factory.createStage();
			stage.setBaseDir(testDir);
			new PersistentStore().loadData(stage, factory);
		}

	}
	
}
