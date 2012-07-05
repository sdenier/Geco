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
import net.geco.ui.basics.SwingUtils;


/**
 * @author Simon Denier
 * @since Jun 11, 2012
 *
 */
public class DeleteFunction extends GecoFunction {

	private JCheckBox deleteRunnerB;
	private JCheckBox deleteCourseB;
	private JCheckBox deleteCategoryB;
	private JCheckBox deleteClubB;
	private JCheckBox deleteBackupB;
	private JSpinner olderBackupS;

	public DeleteFunction(GecoControl gecoControl) {
		super(gecoControl, FunctionCategory.BATCH);
	}

	@Override
	public String toString() {
		return "Delete Data";
	}

	@Override
	public String executeTooltip() {
		return "Delete all selected data to start over";
	}
	
	@Override
	public void execute() {
		geco().saveCurrentStage();
		geco().info("Backup saved", false);
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
	public JComponent getParametersConfig() {
		deleteRunnerB = new JCheckBox("Runners");
		deleteCategoryB = new JCheckBox("Categories");
		deleteCategoryB.setToolTipText("Can only delete categories without runners");
		deleteClubB = new JCheckBox("Clubs");
		deleteClubB.setToolTipText("Can only delete clubs without runners");
		deleteCourseB = new JCheckBox("Courses");
		deleteCourseB.setToolTipText("Can only delete courses without runners or linked categories");
		Box deleteBox = Box.createHorizontalBox();
		deleteBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
		deleteBox.add(new JLabel("Delete all "));
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
		cleanupBox.add(new JLabel("Also delete backups older than "));
		cleanupBox.add(olderBackupS);
		cleanupBox.add(new JLabel(" days"));
		cleanupBox.setAlignmentX(Component.LEFT_ALIGNMENT);

		Box configBox = Box.createVerticalBox();
		configBox.add(new JLabel("Geco will perform a backup before deleting any data"));
		configBox.add(deleteBox);
		configBox.add(cleanupBox);
		return configBox;
	}

}
