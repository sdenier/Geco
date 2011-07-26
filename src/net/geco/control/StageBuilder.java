/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.geco.basics.GecoResources;
import net.geco.model.Factory;
import net.geco.model.Registry;
import net.geco.model.Stage;
import net.geco.model.iocsv.CardDataIO;
import net.geco.model.iocsv.CategoryIO;
import net.geco.model.iocsv.ClubIO;
import net.geco.model.iocsv.CourseIO;
import net.geco.model.iocsv.HeatSetIO;
import net.geco.model.iocsv.ResultDataIO;
import net.geco.model.iocsv.RunnerIO;


/**
 * StageBuilder is currently responsible for persistence of Stage and its data. It provides functions to
 * load/save Òr or Geco data (using RegistryBuilder as backend) as well as backup function.
 * 
 * @author Simon Denier
 * @since Nov 23, 2008
 *
 */
public class StageBuilder extends BasicControl {
	
	private RegistryBuilder registryBuilder;

	
	private final static String[] datafiles = new String[] {
		propName(),
		CategoryIO.sourceFilename(),
		ClubIO.orFilename(),
		CourseIO.orFilename(),
		CardDataIO.sourceFilename(),
		RunnerIO.sourceFilename(),
		ResultDataIO.sourceFilename()
	};
	
	
	public StageBuilder(Factory factory) {
		super(factory);
		this.registryBuilder = new RegistryBuilder(factory);
	}
	
	public Stage loadStage(String baseDir, PenaltyChecker checker) {
		Stage newStage = factory().createStage();
		loadStageProperties(newStage, baseDir);
		importDataIntoRegistry(newStage);
		checker.postInitialize(newStage); // post initialization
		new RunnerBuilder(factory()).checkGecoData(newStage, checker);
		return newStage;
	}

	public static Properties loadProperties(String baseDir) {
		Properties props = new Properties();
		try {
			props.load( GecoResources.getReaderFor(propPath(baseDir)) );
		} catch (IOException e) {
			System.out.println(e); // TODO: !!!!!!!!!!!!!!!!!!!!! throw exception and handle in UI
		}
		return props;
	}
	
	public void loadStageProperties(Stage stage, String baseDir) {
		stage.setBaseDir(baseDir);
		stage.loadProperties(loadProperties(baseDir));
	}

	private void importDataIntoRegistry(Stage newStage) {
		Registry registry = new Registry();
		newStage.setRegistry(registry);
		this.registryBuilder.importAllData(	registry,
											newStage.getBaseDir(),
											newStage.getZeroHour(),
											newStage.version12()); // MIGR12
	}
	
	public void save(Stage stage, Properties props, String backupname) {
		saveStageProperties(stage, props);
		registryBuilder.exportAllData(	stage.registry(),
										stage.getBaseDir(),
										stage.getZeroHour());
		backupData(stage.getBaseDir(), backupname);
	}
	
	private void saveStageProperties(Stage stage, Properties properties) {
		stage.saveProperties(properties);
		saveProperties(stage.getBaseDir(), properties);
	}

	public static void saveProperties(String baseDir, Properties properties) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(propFile(baseDir)));
			properties.store(writer, "Geco " + new Date(System.currentTimeMillis()).toString()); //$NON-NLS-1$
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void backupData(String basedir, String backupname) {
		try {
			ZipOutputStream zipStream = 
								new ZipOutputStream(new FileOutputStream(filepath(basedir, backupname)));
			for (String datafile : datafiles) {
				writeZipEntry(zipStream, datafile, basedir);	
			}
			if( fileExists(basedir, HeatSetIO.sourceFilename()) ) {
				writeZipEntry(zipStream, HeatSetIO.sourceFilename(), basedir);
			}
			zipStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeZipEntry(ZipOutputStream zipStream, String filename, String basedir)
					throws IOException, FileNotFoundException {
		ZipEntry zipEntry = new ZipEntry(filename);
		zipStream.putNextEntry(zipEntry);
		byte[] buffer = new byte[4096];
		BufferedInputStream inputStream =
								new BufferedInputStream(new FileInputStream(filepath(basedir, filename)));
		int len;
		while( (len = inputStream.read(buffer)) != -1 ) {
			zipStream.write(buffer, 0, len);
		}
		zipStream.closeEntry();
	}

	public static String propName() {
		return "geco.prop";
	}

	public static String propPath(String baseDir) {
		return filepath(baseDir, propName()); //$NON-NLS-1$
	}
	
	public static File propFile(String baseDir) {
		return new File(propPath(baseDir));
	}

	public static String filepath(String base, String filename) {
		return base + GecoResources.sep + filename;
	}
	
	public static boolean fileExists(String base, String filename) {
		return new File(filepath(base, filename)).exists();
	}
	
}
