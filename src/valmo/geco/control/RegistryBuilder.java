/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.io.File;

import valmo.geco.model.Registry;
import valmo.geco.model.iocsv.CategoryIO;
import valmo.geco.model.iocsv.ClubIO;
import valmo.geco.model.iocsv.CourseIO;
import valmo.geco.model.iocsv.CsvReader;
import valmo.geco.model.iocsv.CsvWriter;
import valmo.geco.model.iocsv.HeatSetIO;
import valmo.geco.model.iocsv.RaceDataIO;
import valmo.geco.model.iocsv.ResultDataIO;
import valmo.geco.model.iocsv.RunnerIO;

/**
 * RegistryBuilder is solely responsible for file-based persistence of Registry in csv format (using the
 * valmo.geco.csv package).
 * 
 * @author Simon Denier
 * @since Jan 20, 2009
 *
 */
public class RegistryBuilder  extends Control{

	private CsvReader reader;

	private CsvWriter writer;
	
	public RegistryBuilder(GecoControl gecoControl) {
		super(gecoControl);
		this.reader = new CsvReader();
		this.writer = new CsvWriter();
	}
	

	public Registry importAllData(Registry registry, String baseDir, boolean importResult) {
		
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
	
	
	public void exportAllData(Registry registry, String baseDir) {
		
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
