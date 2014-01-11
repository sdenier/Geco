/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.config;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.geco.basics.Announcer;
import net.geco.framework.IGecoApp;
import net.geco.model.Category;
import net.geco.model.Messages;
import net.geco.ui.framework.ConfigPanel;

/**
 * @author Simon Denier
 * @since May 25, 2011
 *
 */
public class CategoryConfigPanel extends JPanel implements ConfigPanel {

	@Override
	public String getLabel() {
		return Messages.uiGet("CategoryConfigPanel.Title"); //$NON-NLS-1$
	}

	public CategoryConfigPanel(final IGecoApp geco, final JFrame frame) {
		final ConfigTablePanel<Category> categoryPanel = new ConfigTablePanel<Category>();
		final ConfigTableModel<Category> tableModel = createTableModel(geco);
		tableModel.setData(geco.registry().getSortedCategories());
		final JComboBox coursesCB = new JComboBox();
		final JComboBox coursesetCB = new JComboBox();
		
		geco.announcer().registerStageConfigListener( new Announcer.StageConfigListener() {
			public void coursesChanged() {
				updateComboBoxesforCategories(geco, coursesCB, coursesetCB);
			}
			public void clubsChanged() {}
			public void categoriesChanged() {
				tableModel.setData(geco.registry().getSortedCategories());
			}
			public void coursesetsChanged() {
				updateComboBoxesforCategories(geco, coursesCB, coursesetCB);
			}
		});
	
		ActionListener addAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.stageControl().createCategory();
			}
		};
		ActionListener removeAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Category cat = categoryPanel.getSelectedData();
				if( cat!=null ) {
					try {
						geco.stageControl().removeCategory(cat);	
					} catch (Exception e2) {
						JOptionPane.showMessageDialog(frame,
								Messages.uiGet("CategoryConfigPanel.CategoryNoDeletionWarning") + e2.getMessage(), //$NON-NLS-1$
								Messages.uiGet("ConfigTablePanel.ActionCancelledTitle"), //$NON-NLS-1$
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		};
		ActionListener importAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(geco.getCurrentStagePath());
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle(Messages.uiGet("CategoryConfigPanel.SelectCSVFileTitle")); //$NON-NLS-1$
				int answer = chooser.showDialog(frame, Messages.uiGet("CategoryConfigPanel.ImportLabel")); //$NON-NLS-1$
				if( answer==JFileChooser.APPROVE_OPTION ) {
					try {
						geco.stageControl().importCategoryTemplate(chooser.getSelectedFile().getAbsolutePath());
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(
								frame,
								e1.getLocalizedMessage(),
								Messages.uiGet("CategoryConfigPanel.TemplateErrorTitle"), //$NON-NLS-1$
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
		JButton importB = new JButton(Messages.uiGet("CategoryConfigPanel.TemplateLabel")); //$NON-NLS-1$
		importB.addActionListener(importAction);
		
		categoryPanel.initialize(
				Messages.uiGet("CategoryConfigPanel.Title"), //$NON-NLS-1$
				tableModel,
				new Dimension(350, 450),
				addAction,
				removeAction,
				importB);

		coursesetCB.setEditable(true);
		updateComboBoxesforCategories(geco, coursesCB, coursesetCB);
		categoryPanel.table().getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(coursesCB));
		categoryPanel.table().getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(coursesetCB));
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(categoryPanel);
	}

	public ConfigTableModel<Category> createTableModel(final IGecoApp geco) {
		return new ConfigTableModel<Category>(new String[] {
											Messages.uiGet("CategoryConfigPanel.CategoryShortNameHeader"), //$NON-NLS-1$
											Messages.uiGet("CategoryConfigPanel.CategoryLongNameHeader"), //$NON-NLS-1$
											Messages.uiGet("CategoryConfigPanel.CategoryCourseHeader"), //$NON-NLS-1$
											"Course Set"}) {
			@Override
			public Object getValueIn(Category cat, int columnIndex) {
				switch (columnIndex) {
				case 0: return cat.getShortname();
				case 1: return cat.getLongname();
				case 2: return (cat.getCourse()==null) ? "" : cat.getCourse().getName(); //$NON-NLS-1$
				case 3: return (cat.getCourseSet()==null) ? "" : cat.getCourseSet().getName(); //$NON-NLS-1$
				default: return super.getValueIn(cat, columnIndex);
				}
			}
			@Override
			public void setValueIn(Category cat, Object value, int col) {
				switch (col) {
				case 0: geco.stageControl().updateShortname(cat, (String) value); break;
				case 1: geco.stageControl().updateName(cat, (String) value); break;
				case 2: setCourse(cat, (String) value); break;
				case 3: geco.stageControl().updateCategoryCourseSet(cat, (String) value); break;
				default: break;
				}
			}
			private void setCourse(Category cat, String coursename) {
				if( coursename.equals("") ){ //$NON-NLS-1$
					cat.setCourse(null);
				} else {
					cat.setCourse(geco.registry().findCourse(coursename));
				}
			}
		};
	}

	private void updateComboBoxesforCategories(IGecoApp geco, JComboBox coursesCB, JComboBox coursesetCB) {
		Vector<String> sortedCoursenames = new Vector<String>( geco.registry().getSortedCourseNames() );
		sortedCoursenames.add(0, ""); //$NON-NLS-1$
		coursesCB.setModel(new DefaultComboBoxModel(sortedCoursenames));
		Vector<String> sortedCoursets = new Vector<String>(geco.registry().getSortedCourseSetNames());
		coursesetCB.setModel(new DefaultComboBoxModel(sortedCoursets));
	}

	@Override
	public Component build() {
		return this;
	}
	
}
