/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * @author Simon Denier
 * @since Jul 17, 2010
 *
 */
public class GecoLauncher {
	
	public static final String[] FILES = {
		"Classes.csv",
		"Clubs.csv",
		"Competitors.csv",
		"Courses.csv",
		"ResultData.csv",
		"results.csv",
		"geco.prop"
	};

	private JFileChooser chooser;

	public GecoLauncher(File currentDir) {
		this(currentDir.getAbsolutePath());
	}
	
	public GecoLauncher(String currentDir) {
		chooser = new JFileChooser(currentDir);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select data folder (or create a new one)");
	}
	
	public String open(JFrame frame) throws Exception {
		int returnValue = chooser.showDialog(frame, "Open");
		if( returnValue==JFileChooser.APPROVE_OPTION ) {
			File baseFile = chooser.getSelectedFile();
			String basePath = baseFile.getAbsolutePath();
			if( ! baseFile.exists() || ! directoryHasData(basePath) ) {
				int confirm = JOptionPane.showConfirmDialog(chooser, "Create a new stage?", "New stage", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				if( confirm==JOptionPane.OK_OPTION ) {
					baseFile.mkdir();
					createDataFiles(basePath);
					new File(basePath + File.separator + "backups").mkdir();
				} else 
					throw new Exception("Cancel creation");
			}
			return basePath;
		} else {
			throw new Exception("Cancel import");
		}
	}

	private void createDataFiles(String baseDir) {
		for (String datafile : FILES) {
			createFile(baseDir, datafile);
		}
	}

	private void createFile(String baseDir, String filename) {
		try {
			URI uri = getClass().getResource("/resources/templates/" + filename).toURI();
			FileChannel inChannel = new FileInputStream(new File(uri)).getChannel();
			FileChannel outChannel = new FileOutputStream(new File(baseDir + File.separator + filename)).getChannel();
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e3) {
			e3.printStackTrace();
		}
	}
	
	
	public static boolean directoryHasData(String baseDir) {
		return  new File(baseDir + File.separator + "Competition.csv").exists()
				||
				new File(baseDir + File.separator + "geco.prop").exists();
	}
	
}
