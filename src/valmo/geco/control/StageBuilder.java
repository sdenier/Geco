/**
 * Copyright (c) 2008 Simon Denier
 */
package valmo.geco.control;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import valmo.geco.csv.CategoryIO;
import valmo.geco.csv.ClubIO;
import valmo.geco.csv.CourseIO;
import valmo.geco.csv.CsvReader;
import valmo.geco.csv.RaceDataIO;
import valmo.geco.csv.ResultDataIO;
import valmo.geco.csv.RunnerIO;
import valmo.geco.csv.StageIO;
import valmo.geco.model.Factory;
import valmo.geco.model.Registry;
import valmo.geco.model.Stage;

/**
 * StageBuilder is currently responsible for persistence of Stage and its data. It provides functions to
 * load/save Òr or Geco data (using RegistryBuilder as backend) as well as backup function.
 * 
 * @author Simon Denier
 * @since Nov 23, 2008
 *
 */
public class StageBuilder extends Control {
	/*
- name = name of stage
?- dir = data directory (Òr): should be current directory?
- date = current date of the stage
?- gecodata = true/false, whether to load Status/race time in data files (because they have been manually updated in Geco)
- lastresultfile = path to last exported result file
	 */
	
	private RegistryBuilder registryBuilder;

	private Stage currentStage;
	
	private final static String[] datafiles = new String[] {
		CategoryIO.sourceFilename(),
		ClubIO.orFilename(),
		CourseIO.orFilename(),
		RaceDataIO.sourceFilename(),
		RunnerIO.sourceFilename()
	};
	
	
	/**
	 * @param stage 
	 * 
	 */
	public StageBuilder(Factory factory) {
		super(factory);
		this.registryBuilder = new RegistryBuilder(factory);
	}
	
	public Stage builtStage() {
		return this.currentStage;
	}

	
	public Stage loadStage(String baseDir, PenaltyChecker checker) {
		File propFile = propFile(baseDir);
		if( propFile.exists() ) {
			return importGecoData(baseDir, propFile, checker);
		} else {
			return importOrData(baseDir, checker);	
		}		
	}


	/**
	 * @param propFile
	 * @param checker
	 * @return
	 */
	public Stage importGecoData(String baseDir, File propFile, PenaltyChecker checker) {
		currentStage = factory().createStage();
		currentStage.initialize(baseDir);
		loadStageProperties(currentStage, propFile);
		importDataIntoRegistry(baseDir, true);
		currentStage.registry().checkGecoData(factory(), checker);
		return currentStage;
	}

	public void loadStageProperties(Stage stage, File propFile) {
		try {
			Properties props = new Properties();
			props.load(new BufferedReader(new FileReader(propFile)));
			stage.loadProperties(props);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param string
	 * @param checker
	 * @return
	 */
	public Stage importOrData(String baseDir, PenaltyChecker checker) {
		CsvReader reader = new CsvReader().initialize(baseDir, StageIO.sourceFilename());
		currentStage = new StageIO(factory(), reader).getStage();
		currentStage.initialize(baseDir);
		importDataIntoRegistry(baseDir, false);
		currentStage.registry().checkOrData(factory(), checker);
		return currentStage;
	}

	private void importDataIntoRegistry(String baseDir, boolean importResult) {
		Registry registry = new Registry();
		currentStage.setRegistry(registry);
		this.registryBuilder.setBaseDir(baseDir);
		this.registryBuilder.importAllData(registry, importResult);
	}
	
	public void save(Stage stage, Properties props, String backupname) {
		backupPreviousData(stage.getBaseDir(), backupname);
		saveStageProperties(stage, props);
		registryBuilder.exportAllData(stage.registry());
	}
	
	private void saveStageProperties(Stage stage, Properties properties) {
		stage.saveProperties(properties);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(propFile(stage.getBaseDir())));
			properties.store(writer, "Geco " + new Date(System.currentTimeMillis()).toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void backupPreviousData(String basedir, String backupname) {
		try {
			ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(filepath(basedir, backupname)));
			for (String datafile : datafiles) {
				writeZipEntry(zipStream, datafile, basedir);	
			}
			if( new File(ResultDataIO.sourceFilename()).exists() ) {
				writeZipEntry(zipStream, ResultDataIO.sourceFilename(), basedir);
			}
			zipStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeZipEntry(ZipOutputStream zipStream, String filename, String basedir) throws IOException, FileNotFoundException {
		ZipEntry zipEntry = new ZipEntry(filename);
		zipStream.putNextEntry(zipEntry);
		byte[] buffer = new byte[4096];
		BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filepath(basedir, filename)));
		int len;
		while( (len = inputStream.read(buffer)) != -1 ) {
			zipStream.write(buffer, 0, len);
		}
		zipStream.closeEntry();
	}
	
	public static File propFile(String baseDir) {
		return new File(filepath(baseDir, "geco.prop"));
	}

	public static String filepath(String base, String filename) {
		return base + File.separator + filename;
	}
	
}
