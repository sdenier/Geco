/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.valmo.geco.model;

import java.io.IOException;

import valmo.geco.model.Factory;
import valmo.geco.model.Registry;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.impl.POFactory;
import valmo.geco.model.iocsv.CategoryIO;
import valmo.geco.model.iocsv.ClubIO;
import valmo.geco.model.iocsv.CourseIO;
import valmo.geco.model.iocsv.CsvReader;
import valmo.geco.model.iocsv.RaceDataIO;
import valmo.geco.model.iocsv.RunnerIO;

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
		
		try {
			reader.initialize(baseDir, RunnerIO.sourceFilename());
			new RunnerIO(factory, reader, null, registry, 0).importData();

			reader.initialize(baseDir, RaceDataIO.sourceFilename());
			new RaceDataIO(factory, reader, null, registry).importData();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
		try {
			reader.initialize(baseDir, ClubIO.orFilename());
			new ClubIO(factory, reader, null, registry).importData();
			
			reader.initialize(baseDir, CourseIO.orFilename());
			new CourseIO(factory, reader, null, registry).importData();
			
			reader.initialize(baseDir, CategoryIO.sourceFilename());
			new CategoryIO(factory, reader, null, registry).importData();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return registry;
	}
	
}
