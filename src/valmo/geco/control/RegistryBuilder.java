/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.control;

import java.io.File;

import valmo.geco.csv.CategoryIO;
import valmo.geco.csv.ClubIO;
import valmo.geco.csv.CourseIO;
import valmo.geco.csv.CsvReader;
import valmo.geco.csv.CsvWriter;
import valmo.geco.csv.HeatSetIO;
import valmo.geco.csv.RaceDataIO;
import valmo.geco.csv.ResultDataIO;
import valmo.geco.csv.RunnerIO;
import valmo.geco.model.Factory;
import valmo.geco.model.Registry;

/**
 * RegistryBuilder is solely responsible for file-based persistence of Registry in csv format (using the
 * valmo.geco.csv package).
 * 
 * @author Simon Denier
 * @since Jan 20, 2009
 *
 */
public class RegistryBuilder  extends Control{

	private String baseDir;
	
	private CsvReader reader;

	private CsvWriter writer;
	
	
	/**
	 * @param factory 
	 * @param stage 
	 * 
	 */
	public RegistryBuilder(Factory factory) {
		this(factory, "");
	}
	
	/**
	 * 
	 */
	public RegistryBuilder(Factory factory, String baseDir) {
		super(factory);
		this.baseDir = baseDir;
		this.reader = new CsvReader();
		this.writer = new CsvWriter();
	}
	
	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}


	/**
	 * @param registry 
	 * @return 
	 * 
	 */
	public Registry importAllData(Registry registry, boolean importResult) {
//		setRegistry(registry);
		
		reader.initialize(baseDir, ClubIO.orFilename());
		new ClubIO(factory(), reader, null, registry).importData();
		
		reader.initialize(baseDir, CourseIO.orFilename());
		new CourseIO(factory(), reader, null, registry).importData();
		
		reader.initialize(baseDir, CategoryIO.sourceFilename());
		new CategoryIO(factory(), reader, null, registry).importData();
		
		reader.initialize(baseDir, RunnerIO.sourceFilename());
		new RunnerIO(factory(), reader, null, registry).importData();

		reader.initialize(baseDir, RaceDataIO.sourceFilename());
		new RaceDataIO(factory(), reader, null, registry).importData();

		if( importResult ) {
			reader.initialize(baseDir, ResultDataIO.sourceFilename());
			new ResultDataIO(factory(), reader, null, registry).importData();
		}
		
		if( new File(baseDir + File.separator + HeatSetIO.sourceFilename()).exists() ) {
			reader.initialize(baseDir, HeatSetIO.sourceFilename());
			new HeatSetIO(factory(), reader, null, registry).importData();
		}
		
		return registry;
	}
	
	public void exportAllData(Registry registry) {
//		setRegistry(registry);
		
		writer.initialize(baseDir, ClubIO.orFilename());
		new ClubIO(factory(), null, writer, registry).exportData(registry.getClubs());
		
		writer.initialize(baseDir, CourseIO.orFilename());
		new CourseIO(factory(), null, writer, registry).exportData(registry.getCourses());
		
		writer.initialize(baseDir, CategoryIO.sourceFilename());
		new CategoryIO(factory(), null, writer, registry).exportData(registry.getCategories());
		
		writer.initialize(baseDir, RunnerIO.sourceFilename());
		new RunnerIO(factory(), null, writer, registry).exportData(registry.getRunners());

		writer.initialize(baseDir, RaceDataIO.sourceFilename());
		new RaceDataIO(factory(), null, writer, registry).exportData(registry.getRunnersData());

		writer.initialize(baseDir, ResultDataIO.sourceFilename());
		new ResultDataIO(factory(), null, writer, registry).exportData(registry.getRunnersData());
		
		if( registry.getHeatSets().size()>0 ) {
			writer.initialize(baseDir, HeatSetIO.sourceFilename());
			new HeatSetIO(factory(), null, writer, registry).exportData(registry.getHeatSets());			
		}
	}
	
}
