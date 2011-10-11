/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.config;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.geco.basics.Announcer;
import net.geco.framework.IGecoApp;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.xml.CourseSaxImporter;
import net.geco.ui.basics.CourseControlDialog;
import net.geco.ui.framework.ConfigPanel;

/**
 * @author Simon Denier
 * @since May 25, 2011
 *
 */
public class CourseConfigPanel extends ConfigTablePanel<Course> implements ConfigPanel {

	@Override
	public String getLabel() {
		return Messages.uiGet("CourseConfigPanel.Title"); //$NON-NLS-1$
	}
	
	public CourseConfigPanel(final IGecoApp geco, final JFrame frame) {
		final ConfigTableModel<Course> tableModel = createTableModel(geco);
		tableModel.setData(geco.registry().getSortedCourses());
		
		geco.announcer().registerStageConfigListener( new Announcer.StageConfigListener() {
			public void coursesChanged() {
				tableModel.setData(new Vector<Course>(geco.registry().getSortedCourses()));
			}
			public void clubsChanged() {}
			public void categoriesChanged() {}
		});
		
		ActionListener addAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.stageControl().createCourse();
			}
		};
		ActionListener removeAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Course course = getSelectedData();
				if( course!=null ) {
					try {
						geco.stageControl().removeCourse(course);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(frame,
							Messages.uiGet("CourseConfigPanel.CourseNoDeletionWarning") + e1.getMessage(), //$NON-NLS-1$
							Messages.uiGet("ConfigTablePanel.ActionCancelledTitle"), //$NON-NLS-1$
							JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		};
		ActionListener editAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Course course = getSelectedData();
				if( course!=null ) {
					boolean change = new CourseControlDialog(frame, course).didChange();
					if( change ){
						geco.announcer().announceCourseChanged(course);
					}
				}
			}
		};
		ActionListener importAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setSelectedFile(new File(geco.getCurrentStagePath()));
				chooser.setDialogTitle(Messages.uiGet("CourseConfigPanel.SelectXMLFileTitle")); //$NON-NLS-1$
				int answer = chooser.showDialog(frame, Messages.uiGet("CourseConfigPanel.CourseImportXMLLabel")); //$NON-NLS-1$
				if( answer==JFileChooser.APPROVE_OPTION ) {
					String file = chooser.getSelectedFile().getAbsolutePath();
					try {
						Vector<Course> courses = CourseSaxImporter.importFromXml(file, geco.stageControl().factory());
						for (Course course : courses) {
							geco.stageControl().addCourse(course);	
						}
					} catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(frame,
								e1.getLocalizedMessage(),
								Messages.uiGet("CourseConfigPanel.XMLLoadError"), //$NON-NLS-1$
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
		ActionListener refreshAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Course course = getSelectedData();
				if( course!=null ) {
					geco.runnerControl().recheckRunnersFromCourse(course);
				}
			}
		};
		
		JButton editB = new JButton(Messages.uiGet("CourseConfigPanel.CourseEditLabel")); //$NON-NLS-1$
		editB.setToolTipText(Messages.uiGet("CourseConfigPanel.CourseEditTooltip")); //$NON-NLS-1$
		editB.addActionListener(editAction);
		
		JButton importB = new JButton(Messages.uiGet("CourseConfigPanel.CourseXMLLabel")); //$NON-NLS-1$
		importB.setToolTipText(Messages.uiGet("CourseConfigPanel.CourseXMLTooltip")); //$NON-NLS-1$
		importB.addActionListener(importAction);

		JButton refreshB = new JButton(Messages.uiGet("CourseConfigPanel.CourseRecheckLabel")); //$NON-NLS-1$
		refreshB.setToolTipText(Messages.uiGet("CourseConfigPanel.CourseRecheckTooltip")); //$NON-NLS-1$
		refreshB.addActionListener(refreshAction);

		initialize(
				Messages.uiGet("CourseConfigPanel.Title"), //$NON-NLS-1$
				tableModel,
				addAction,
				removeAction,
				editB,
				importB,refreshB);
	}

	private ConfigTableModel<Course> createTableModel(final IGecoApp geco) {
		return new ConfigTableModel<Course>(new String[] {
											Messages.uiGet("CourseConfigPanel.CourseNameHeader"), //$NON-NLS-1$
											Messages.uiGet("CourseConfigPanel.CourseNbControlsHeader")}) { //$NON-NLS-1$
			@Override
			public Object getValueIn(Course course, int columnIndex) {
				switch (columnIndex) {
				case 0: return course.getName();
				case 1: return course.nbControls();
				default: return super.getValueIn(course, columnIndex);
				}
			}
			@Override
			public boolean isCellEditable(int row, int col) {
				return col == 0;
			}
			@Override
			public void setValueIn(Course course, Object value, int col) {
				switch (col) {
				case 0: geco.stageControl().updateName(course, (String) value); break;
				default: break;
				}
			}
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				switch (columnIndex) {
				case 1: return Integer.class;
				default: return super.getColumnClass(columnIndex);
				}

			}
		};
	}

	@Override
	public Component build() {
		return this;
	}

}
