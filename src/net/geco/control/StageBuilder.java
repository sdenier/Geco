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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.geco.basics.GecoResources;
import net.geco.model.Factory;
import net.geco.model.Registry;
import net.geco.model.Stage;
import net.geco.model.iojson.PersistentStore;


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

	private PersistentStore store;
	
	private final static String[] datafiles = new String[] {
		propName(),
		PersistentStore.STORE_FILE
	};
	
	
	public StageBuilder(Factory factory) {
		super(factory);
		this.registryBuilder = new RegistryBuilder(factory);
		this.store = new PersistentStore();
	}
	
	public Stage loadStage(String baseDir, Checker checker) {
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
			props.load( GecoResources.getSafeReaderFor(propPath(baseDir)) );
		} catch (IOException e) {
			System.out.println(e); // TODO: throw exception and handle in UI
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
											newStage.getZeroHour());
	}
	
	public void save(Stage stage, Properties props, String backupId) {
		saveStageProperties(stage, props);
		registryBuilder.exportAllData(	stage.registry(),
										stage.getBaseDir(),
										stage.getZeroHour());
		store.saveData(stage);
		backupData(stage.getBaseDir(), backupId);
	}
	
	public void saveStageProperties(Stage stage, Properties properties) {
		stage.saveProperties(properties);
		exportProperties(stage.getBaseDir(), properties);
	}

	public static void exportProperties(String baseDir, Properties properties) {
		try {
			BufferedWriter writer = GecoResources.getSafeWriterFor(propPath(baseDir));
			properties.store(writer, "Geco " + new Date(System.currentTimeMillis()).toString()); //$NON-NLS-1$
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void backupData(String basedir, String backupId) {
		try {
			ZipOutputStream zipStream = 
					new ZipOutputStream(new FileOutputStream(backupFile(basedir, backupId)));
			for (String datafile : datafiles) {
				writeZipEntry(zipStream, datafile, basedir);	
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
		inputStream.close();
		zipStream.closeEntry();
	}

	public void deleteOldBackups(Stage stage, Date deadline) {
		final Pattern pattern = Pattern.compile(BACKUP_REGEX);
		File backupDir = new File(backupDir(stage.getBaseDir(), BACKUP_DIR));
		File[] backupFiles = backupDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return pattern.matcher(filename).matches();
			}
		});
		for (File backup : backupFiles) {
			Date lastModified = new Date(backup.lastModified());
			if( lastModified.before(deadline) ){
				backup.delete();
			}
		}
	}

	public static String propName() {
		return "geco.prop"; //$NON-NLS-1$
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

	public static boolean directoryHasData(String baseDir) {
		return propFile(baseDir).exists();
	}
	
	public static final String BACKUP_DIR = "backups"; //$NON-NLS-1$
	public static final String BACKUP_NAME = "backup%s.zip"; //$NON-NLS-1$
	public static final String BACKUP_REGEX = "backup.+zip"; //$NON-NLS-1$

	public static String backupDir(String baseDir, String backupDir) {
		return filepath(baseDir, BACKUP_DIR);
	}
	
	public static String backupFile(String baseDir, String id) {
		return filepath(backupDir(baseDir, BACKUP_DIR), String.format(BACKUP_NAME, id));
	}

}
