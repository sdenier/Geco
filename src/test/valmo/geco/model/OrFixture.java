/**
 * Copyright (c) 2009 Simon Denier
 */
package test.valmo.geco.model;

import valmo.geco.csv.CategoryIO;
import valmo.geco.csv.ClubIO;
import valmo.geco.csv.CourseIO;
import valmo.geco.csv.CsvReader;
import valmo.geco.csv.RaceDataIO;
import valmo.geco.csv.RunnerIO;
import valmo.geco.model.Factory;
import valmo.geco.model.Registry;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.impl.POFactory;

/**
 * @author Simon Denier
 * @since Jan 20, 2009
 *
 */
public class OrFixture {
	
	private Factory factory;
	
	private Registry registry;

	private CsvReader reader;

	private String baseDir;
	
	/**
	 * 
	 */
	public OrFixture() {
		this.factory = new POFactory();
		this.reader = new CsvReader();
	}
	
	public Registry importBelfieldData(boolean allData) {
		this.registry = new Registry();
		baseDir = "testData/belfield";
		return importData(allData);
	}

	public Registry importMullaghmeenData(boolean allData) {
		this.registry = new Registry();
		baseDir = "testData/mullaghmeen";
		return importData(allData);
	}

	private Registry importData(boolean allData) {
		if( allData ) {
			return importAllData();	
		} else {
			return importConfigData();
		}
		
	}
	
	/**
	 * @param registry 
	 * @return 
	 * 
	 */
	public Registry importAllData() {
		importConfigData();
		
		reader.initialize(baseDir, RunnerIO.sourceFilename());
		new RunnerIO(factory, reader, null, registry).importData();

		reader.initialize(baseDir, RaceDataIO.sourceFilename());
		new RaceDataIO(factory, reader, null, registry).importData();
		
		checkDNSRunner();
		
		return registry;
	}

	/**
	 * 
	 */
	private void checkDNSRunner() {
		for (Runner runner : registry.getRunners()) {
			if( registry.findRunnerData(runner) == null ) {
				RunnerRaceData data = factory.createRunnerRaceData();
				data.setRunner(runner);
				data.setResult(factory.createRunnerResult());
				registry.addRunnerData(data);
			}
				
		}
	}

	public Registry importConfigData() {
		reader.initialize(baseDir, ClubIO.orFilename());
		new ClubIO(factory, reader, null, registry).importData();
		
		reader.initialize(baseDir, CourseIO.orFilename());
		new CourseIO(factory, reader, null, registry).importData();
		
		reader.initialize(baseDir, CategoryIO.sourceFilename());
		new CategoryIO(factory, reader, null, registry).importData();
		
		return registry;
	}
	
}
