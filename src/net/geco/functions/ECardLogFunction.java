/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.functions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import net.geco.control.GecoControl;
import net.geco.control.ecardmodes.CourseDetector;
import net.geco.control.ecardmodes.ECardMode;
import net.geco.control.ecardmodes.ECardRacingMode;
import net.geco.control.ecardmodes.ECardTrainingMode;
import net.geco.control.functions.ECardLogImporter;
import net.geco.model.Messages;
import net.geco.ui.basics.GecoIcon;


/**
 * @author Simon Denier
 * @since Jun 11, 2012
 *
 */
public class ECardLogFunction extends GecoFunction {

	private JTextField logFileF;
	private JCheckBox autoInsertB;
	private JRadioButton trainingB;

	public ECardLogFunction(GecoControl gecoControl) {
		super(gecoControl, FunctionCategory.BATCH);
	}

	@Override
	public String toString() {
		return Messages.uiGet("ECardLogFunction.ImportEcardTitle"); //$NON-NLS-1$
	}

	@Override
	public String executeTooltip() {
		return Messages.uiGet("ECardLogFunction.ImportEcardTooltip"); //$NON-NLS-1$
	}
	
	@Override
	public void execute() {
		CourseDetector detector = new CourseDetector(geco());
		boolean autoInsert = autoInsertB.isSelected();
		ECardMode processor;
		if( trainingB.isSelected() ){
			processor = new ECardTrainingMode(geco(), detector, false).toggleArchiveLookup(autoInsert);
		} else {
			processor = new ECardRacingMode(geco(), detector, false).toggleArchiveLookup(autoInsert);
		}
		try {
			new ECardLogImporter(geco()).processECardData(processor, new File(logFileF.getText()));
		} catch (IOException e) {
			geco().info(e.getLocalizedMessage(), true);
		}
	}

	@Override
	public JComponent getParametersConfig() {
		logFileF = new JTextField(15);
		logFileF.setMaximumSize(logFileF.getPreferredSize());
		logFileF.setEnabled(false);
		JButton selectB = new JButton(GecoIcon.createIcon(GecoIcon.OpenSmall));
		selectB.setToolTipText(Messages.uiGet("ECardLogFunction.SelectLogFileTooltip")); //$NON-NLS-1$
		selectB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(stage().getBaseDir());
				chooser.setDialogTitle(Messages.uiGet("ECardLogFunction.SelectLogFileTitle")); //$NON-NLS-1$
				int answer = chooser.showDialog(null, Messages.uiGet("ECardLogFunction.SelectLabel")); //$NON-NLS-1$
				if( answer == JFileChooser.APPROVE_OPTION ){
					logFileF.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		Box fileBox = Box.createHorizontalBox();
		fileBox.add(new JLabel(Messages.uiGet("ECardLogFunction.FilePathLabel"))); //$NON-NLS-1$
		fileBox.add(logFileF);
		fileBox.add(selectB);

		trainingB = new JRadioButton(Messages.uiGet("ECardLogFunction.TrainingLabel")); //$NON-NLS-1$
		trainingB.setToolTipText(Messages.uiGet("ECardLogFunction.TrainingTooltip")); //$NON-NLS-1$
		JRadioButton racingB = new JRadioButton(Messages.uiGet("ECardLogFunction.RacingLabel")); //$NON-NLS-1$
		racingB.setToolTipText(Messages.uiGet("ECardLogFunction.RacingTooltip")); //$NON-NLS-1$
		ButtonGroup modeGroup = new ButtonGroup();
		modeGroup.add(racingB);
		modeGroup.add(trainingB);
		trainingB.setSelected(true);
		Box modeBox = Box.createHorizontalBox();
		modeBox.add(new JLabel(Messages.uiGet("ECardLogFunction.ReadingModeLabel"))); //$NON-NLS-1$
		modeBox.add(trainingB);
		modeBox.add(racingB);
		
		autoInsertB = new JCheckBox();
		autoInsertB.setToolTipText(Messages.uiGet("ECardLogFunction.ArchiveLookupTooltip")); //$NON-NLS-1$
		Box archiveBox = Box.createHorizontalBox();
		archiveBox.add(new JLabel(Messages.uiGet("ECardLogFunction.ArchiveLookupLabel"))); //$NON-NLS-1$
		archiveBox.add(autoInsertB);
		
		fileBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		modeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		archiveBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		Box configBox = Box.createVerticalBox();
		configBox.add(fileBox);
		configBox.add(Box.createVerticalStrut(5));
		configBox.add(modeBox);
		configBox.add(Box.createVerticalStrut(5));
		configBox.add(archiveBox);
		return configBox;
	}

}
