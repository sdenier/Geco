/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.merge;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import net.geco.model.Archive;
import net.geco.model.ArchiveRunner;
import net.geco.model.Category;
import net.geco.model.Course;
import net.geco.ui.basics.FilterComboBox;
import net.geco.ui.basics.FilterComboBox.LazyLoader;
import net.geco.ui.basics.GecoIcon;
import net.geco.ui.basics.SwingUtils;

public class ArchiveBoard extends AbstractMergeBoard {

	private JButton insertArchiveB;

	private FilterComboBox searchArchiveCB;
	private JButton lookupArchiveB;
	private DataField categoryF;
	private DataField courseF;

	public ArchiveBoard(MergeWizard wizard, JComponent panel, int firstLine) {
		super("Archive", wizard, panel, firstLine);
	}

	public void updatePanel() {
		searchArchiveCB.lazyLoadItems(new LazyLoader() {
			public Object[] loadItems() {
				Archive archive = null;
				try {
					archive = control().archive();
				} catch (IOException e) {
					wizard().catchException(e);
					try {
						archive = control().archive();
					} catch (IOException e1) {
						wizard().catchException(e);
					}
				}
				lookupArchiveB.setEnabled(true);
				return archive.runners().toArray();
			}
		});
		searchArchiveCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Object selectedItem = searchArchiveCB.getSelectedItem();
				if( selectedItem instanceof ArchiveRunner ) {
					ArchiveRunner runner = (ArchiveRunner) selectedItem;
					Category category = runner.getCategory();
					categoryF.setText(category.getName());
					courseF.setText(getDefaultCourseForArchiveCategory(category).getName());
					insertArchiveB.setEnabled(true);					
				} else {
					categoryF.setText("");
					courseF.setText("");
					insertArchiveB.setEnabled(false);
				}
			}

			private Course getDefaultCourseForArchiveCategory(Category category) {
				Category regCategory = registry().findCategory(category.getName());
				Course course = regCategory == null ? registry().autoCourse() : registry().getDefaultCourseOrAutoFor(regCategory);
				return course;
			}
		});
	}

	protected void initButtons(JComponent panel) {
		insertArchiveB = new JButton(GecoIcon.createIcon(GecoIcon.ArchiveAdd));
		insertArchiveB.setToolTipText("Insert runner from archive with ecard data");
		insertArchiveB.setEnabled(false);
		panel.add(insertArchiveB, buttonsCol(2));
	}

	protected void initDataPanel(JComponent panel) {
		initDataLine1(panel);
		initDataLine2(panel);
	}

	private void initDataLine1(JComponent panel) {
		GridBagConstraints c = gridLine();
		c.gridwidth = 4;
		searchArchiveCB = new FilterComboBox();
		searchArchiveCB.setPreferredSize(new Dimension(100, SwingUtils.SPINNERHEIGHT));
		panel.add(searchArchiveCB, c);
		c.gridwidth = 1;
		lookupArchiveB = new JButton(GecoIcon.createIcon(GecoIcon.ArchiveSearch));
		lookupArchiveB.setToolTipText("Lookup ecard in archive");
		lookupArchiveB.setEnabled(false);
		lookupArchiveB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ArchiveRunner runner = control().archive().findRunner(ecardData().getRunner().getEcard());
					searchArchiveCB.setSelectedItem(runner);							
				} catch (IOException ex) {
					wizard().catchException(ex);
				}
			}
		});
		panel.add(lookupArchiveB, c);
	}

	private void initDataLine2(JComponent panel) {
		GridBagConstraints c = gridLine();
		panel.add(new JLabel("Category"), c);
		categoryF = new DataField();
		panel.add(categoryF, c);
		panel.add(new JLabel("Course"), c);
		courseF = new DataField();
		panel.add(courseF, c);
	}

}