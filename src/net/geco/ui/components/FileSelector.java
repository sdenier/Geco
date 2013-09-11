/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;

import net.geco.framework.IGecoApp;
import net.geco.ui.basics.GecoIcon;

/**
 * @author Simon Denier
 * @since May 8, 2013
 *
 */
public abstract class FileSelector {
	
	private JTextField filenameDisplayF;

	private JButton selectFileB;

	public FileSelector(final IGecoApp geco, final JFrame frame, final String fileChooserTitle, GecoIcon openFileChooserIcon) {
		filenameDisplayF = new JTextField();
		filenameDisplayF.setEditable(false);
		filenameDisplayF.setText(filenameValue());

		selectFileB = new JButton(GecoIcon.createIcon(openFileChooserIcon));
		selectFileB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser(openPath(geco));
				fileChooser.setDialogTitle(fileChooserTitle);
				int answer = fileChooser.showOpenDialog(frame);
				if( answer==JFileChooser.APPROVE_OPTION ) {
					fileChosen(fileChooser.getSelectedFile());
					filenameDisplayF.setText(filenameValue());
				}
			}
		});
	}
	
	public JTextField getFilenameField() {
		return filenameDisplayF;
	}

	public JButton getSelectFileButton() {
		return selectFileB;
	}

	public String filenameValue() {
		return currentFile() != null ? currentFile().getName() : "";
	}
	
	public abstract File currentFile();
	
	public abstract void fileChosen(File selectedFile);

	private String openPath(final IGecoApp geco) {
		if( currentFile() != null && currentFile().exists() ) {
			return currentFile().getAbsolutePath();
		} else {
			return geco.getCurrentStagePath();
		}
	}
}
