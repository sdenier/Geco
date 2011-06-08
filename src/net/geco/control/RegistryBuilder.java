/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.io.File;
import java.io.IOException;

import net.geco.basics.GecoResources;
import net.geco.model.Factory;
import net.geco.model.Registry;
import net.geco.model.iocsv.CardDataIO;
import net.geco.model.iocsv.CategoryIO;
import net.geco.model.iocsv.ClubIO;
import net.geco.model.iocsv.CourseIO;
import net.geco.model.iocsv.CsvReader;
import net.geco.model.iocsv.CsvWriter;
import net.geco.model.iocsv.HeatSetIO;
import net.geco.model.iocsv.ResultDataIO;
import net.geco.model.iocsv.RunnerIO;


/**
 * RegistryBuilder is solely responsible for file-based persistence of Registry in csv format (using the
 * valmo.geco.csv package).
 * 
 * @author Simon Denier
 * @since Jan 20, 2009
 *
 */
public class RegistryBuilder extends BasicControl{

	private CsvReader reader;

	private CsvWriter writer;
	
	public RegistryBuilder(Factory factory) {
		super(factory);
		this.reader = new CsvReader();
		this.writer = new CsvWriter();
	}
	

	public Registry importAllData(Registry registry, String baseDir, long zeroTime, boolean version12) {
		
		try {
			reader.initialize(baseDir, ClubIO.orFilename());
			new ClubIO(factory(), reader, null, registry).importData();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			reader.initialize(baseDir, CourseIO.orFilename());
			new CourseIO(factory(), reader, null, registry).importData();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			reader.initialize(baseDir, CategoryIO.sourceFilename());
			new CategoryIO(factory(), reader, null, registry).importData();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			reader.initialize(baseDir, RunnerIO.sourceFilename());
			new RunnerIO(factory(), reader, null, registry, zeroTime).importData();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			reader.initialize(baseDir, CardDataIO.sourceFilename());
			new CardDataIO(factory(), reader, null, registry, version12).importData(); // MIGR12
		} catch (IOException e) {
			e.printStackTrace();
		}						

		try {
			reader.initialize(baseDir, ResultDataIO.sourceFilename());
			new ResultDataIO(factory(), reader, null, registry, version12).importData();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if( GecoResources.exists(baseDir + GecoResources.sep + HeatSetIO.sourceFilename()) ) {
			try {
				reader.initialize(baseDir, HeatSetIO.sourceFilename());
				new HeatSetIO(factory(), reader, null, registry).importData();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return registry;
	}
	
	
	public void exportAllData(Registry registry, String baseDir, long zeroTime) {
		
		try {
			writer.initialize(baseDir, ClubIO.orFilename());
			new ClubIO(factory(), null, writer, registry).exportData(registry.getClubs());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			writer.initialize(baseDir, CourseIO.orFilename());
			new CourseIO(factory(), null, writer, registry).exportData(registry.getCourses());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			writer.initialize(baseDir, CategoryIO.sourceFilename());
			new CategoryIO(factory(), null, writer, registry).exportData(registry.getCategories());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			writer.initialize(baseDir, RunnerIO.sourceFilename());
			new RunnerIO(factory(), null, writer, registry, zeroTime).exportData(registry.getRunners());
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			writer.initialize(baseDir, CardDataIO.sourceFilename());
			new CardDataIO(factory(), null, writer, registry).exportData(registry.getRunnersData());
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			writer.initialize(baseDir, ResultDataIO.sourceFilename());
			new ResultDataIO(factory(), null, writer, registry).exportData(registry.getRunnersData());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if( registry.getHeatSets().size()>0 ) {
			try {
				writer.initialize(baseDir, HeatSetIO.sourceFilename());
				new HeatSetIO(factory(), null, writer, registry).exportData(registry.getHeatSets());
			} catch (IOException e) {
				e.printStackTrace();
			}			
		} else {
			// the file may still exist if heatsets have just been removed, so we delete it now
			new File(baseDir + File.separator + HeatSetIO.sourceFilename()).delete();
		}
	}
	
}
