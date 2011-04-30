/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.basics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.geco.basics.GecoWarning;
import net.geco.model.Messages;


/**
 * @author Simon Denier
 * @since Jul 17, 2010
 *
 */
public class GecoLauncher {
	
	static {
		Messages.put("ui", "valmo.geco.ui.messages"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static final String[] FILES = {
		"Classes.csv", //$NON-NLS-1$
		"Clubs.csv", //$NON-NLS-1$
		"Competitors.csv", //$NON-NLS-1$
		"Courses.csv", //$NON-NLS-1$
		"CardData.csv", //$NON-NLS-1$
		"ResultData.csv", //$NON-NLS-1$
		"geco.prop", //$NON-NLS-1$
		"result.css", //$NON-NLS-1$
		"ticket.css", //$NON-NLS-1$
	};

	private JFileChooser chooser;

	public GecoLauncher(File currentDir) {
		this(currentDir.getAbsolutePath());
	}
	
	public GecoLauncher(String currentDir) {
		chooser = new JFileChooser(currentDir);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle(Messages.uiGet("GecoLauncher.Title")); //$NON-NLS-1$
	}
	
	public String open(JFrame frame) throws Exception {
		int returnValue = chooser.showDialog(frame, Messages.uiGet("GecoLauncher.OpenLabel")); //$NON-NLS-1$
		if( returnValue==JFileChooser.APPROVE_OPTION ) {
			File baseFile = chooser.getSelectedFile();
			String basePath = baseFile.getAbsolutePath();
			if( ! baseFile.exists() || ! directoryHasData(basePath) ) {
				int confirm = JOptionPane.showConfirmDialog(
						chooser, 
						Messages.uiGet("GecoLauncher.CreateLabel"), //$NON-NLS-1$ 
						Messages.uiGet("GecoLauncher.CreateTitle"), //$NON-NLS-1$
						JOptionPane.OK_CANCEL_OPTION, 
						JOptionPane.QUESTION_MESSAGE);
				if( confirm==JOptionPane.OK_OPTION ) {
					baseFile.mkdir();
					createDataFiles(basePath);
					new File(basePath + File.separator + "backups").mkdir(); //$NON-NLS-1$
				} else 
					throw new GecoWarning(Messages.uiGet("GecoLauncher.CancelCreation")); //$NON-NLS-1$
			}
			return basePath;
		} else {
			throw new GecoWarning(Messages.uiGet("GecoLauncher.CancelImport")); //$NON-NLS-1$
		}
	}

	private void createDataFiles(String baseDir) {
		for (String datafile : FILES) {
			createFile(baseDir, datafile);
		}
	}

	private void createFile(String baseDir, String filename) {
		try {
			URL url = getClass().getResource("/resources/templates/" + filename); //$NON-NLS-1$
			ReadableByteChannel inChannel = Channels.newChannel(url.openStream());
			FileChannel outChannel = new FileOutputStream(new File(baseDir + File.separator + filename)).getChannel();
			outChannel.transferFrom(inChannel, 0, url.openConnection().getContentLength());
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e3) {
			e3.printStackTrace();
		}
	}
	
	
	public static boolean directoryHasData(String baseDir) {
		return  new File(baseDir + File.separator + "Competition.csv").exists() //$NON-NLS-1$
				||
				new File(baseDir + File.separator + "geco.prop").exists(); //$NON-NLS-1$
	}
	
}
