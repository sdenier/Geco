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
		new CsvSavePerf().run();
		new CsvLoadPerf().run();
	}

	public abstract class Perf {
		
		public void run() {
			System.out.println(String.format("\n*** %s x10 ***", title()));
			assessTenRun();
		}

		public void runOnce() {
			System.out.println(String.format("\n*** %s x1 ***", title()));
			System.out.println(timeRun() + " ms");
		}
		
		private void assessTenRun() {
			long[] times = new long[10];
			long timesSum = 0;
			for (int i = 0; i < times.length; i++) {
				times[i] = timeRun();
				timesSum += times[i];
				System.out.print(times[i] + " ms, ");
			}
			System.out.println("\nMean time: " + (timesSum / 10.0) + " ms");
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

}
