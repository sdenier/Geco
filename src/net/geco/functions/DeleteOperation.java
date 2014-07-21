/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.functions;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.geco.control.GecoControl;
import net.geco.control.RunnerControl;
import net.geco.control.StageControl;
import net.geco.model.Messages;
import net.geco.ui.basics.SwingUtils;


/**
 * @author Simon Denier
 * @since Jun 11, 2012
 *
 */
public class DeleteOperation extends GecoOperation {

	private JCheckBox deleteRunnerB;
	private JCheckBox deleteCourseB;
	private JCheckBox deleteCategoryB;
	private JCheckBox deleteClubB;
	private JCheckBox deleteBackupB;
	private JSpinner olderBackupS;

	public DeleteOperation(GecoControl gecoControl) {
		super(gecoControl, OperationCategory.BATCH);
	}

	@Override
	public String toString() {
		return Messages.uiGet("DeleteFunction.DeleteTitle"); //$NON-NLS-1$
	}

	@Override
	public String runTooltip() {
		return Messages.uiGet("DeleteFunction.DeleteTooltip"); //$NON-NLS-1$
	}
	
	@Override
	public void run() {
		geco().saveCurrentStage();
		geco().info(Messages.uiGet("DeleteFunction.BackupSavedMessage"), false); //$NON-NLS-1$
		if( deleteRunnerB.isSelected() ){
			getService(RunnerControl.class).deleteAllRunners();
		}
		if( deleteCategoryB.isSelected() ){
			getService(StageControl.class).removeAllCategories();
		}
		if( deleteClubB.isSelected() ){
			getService(StageControl.class).removeAllClubs();
		}
		if( deleteCourseB.isSelected() ){
			getService(StageControl.class).removeAllCourses();
		}
		if( deleteBackupB.isSelected() ){
			SpinnerNumberModel spinnerNumber = (SpinnerNumberModel) olderBackupS.getModel();
			geco().deleteOldBackups(spinnerNumber.getNumber().intValue());
		}
	}

	@Override
	public JComponent buildInnerUI() {
		deleteRunnerB = new JCheckBox(Messages.uiGet("DeleteFunction.RunnersLabel")); //$NON-NLS-1$
		deleteCategoryB = new JCheckBox(Messages.uiGet("DeleteFunction.CategoriesLabel")); //$NON-NLS-1$
		deleteCategoryB.setToolTipText(Messages.uiGet("DeleteFunction.CategoriesTooltip")); //$NON-NLS-1$
		deleteClubB = new JCheckBox(Messages.uiGet("DeleteFunction.ClubsLabel")); //$NON-NLS-1$
		deleteClubB.setToolTipText(Messages.uiGet("DeleteFunction.ClubsTooltip")); //$NON-NLS-1$
		deleteCourseB = new JCheckBox(Messages.uiGet("DeleteFunction.CoursesLabel")); //$NON-NLS-1$
		deleteCourseB.setToolTipText(Messages.uiGet("DeleteFunction.CoursesTooltip")); //$NON-NLS-1$
		Box deleteBox = Box.createHorizontalBox();
		deleteBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
		deleteBox.add(new JLabel(Messages.uiGet("DeleteFunction.DeleteAllLabel"))); //$NON-NLS-1$
		deleteBox.add(deleteRunnerB);
		deleteBox.add(deleteCategoryB);
		deleteBox.add(deleteClubB);
		deleteBox.add(deleteCourseB);
		deleteBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		deleteBackupB = new JCheckBox();
		olderBackupS = new JSpinner(new SpinnerNumberModel(15, 0, null, 1));
		olderBackupS.setPreferredSize(new Dimension(50, SwingUtils.SPINNERHEIGHT));
		olderBackupS.setMaximumSize(olderBackupS.getPreferredSize());
		Box cleanupBox = Box.createHorizontalBox();
		cleanupBox.add(deleteBackupB);
		cleanupBox.add(new JLabel(Messages.uiGet("DeleteFunction.DeleteBackupsLabel1"))); //$NON-NLS-1$
		cleanupBox.add(olderBackupS);
		cleanupBox.add(new JLabel(Messages.uiGet("DeleteFunction.DeleteBackupsLabel2"))); //$NON-NLS-1$
		cleanupBox.setAlignmentX(Component.LEFT_ALIGNMENT);

		Box configBox = Box.createVerticalBox();
		configBox.add(new JLabel(Messages.uiGet("DeleteFunction.BackupLabel"))); //$NON-NLS-1$
		configBox.add(deleteBox);
		configBox.add(cleanupBox);
		embedRunButton(configBox);
		return configBox;
	}

}
