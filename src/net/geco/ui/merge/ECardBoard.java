/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.merge;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import net.geco.basics.TimeManager;
import net.geco.ui.basics.GecoIcon;
import net.geco.ui.basics.SwingUtils;

public class ECardBoard extends AbstractMergeBoard {

	private JComboBox coursesCB;
	private DataField ecardF;
	private DataField startTimeF;
	private DataField finishTimeF;
	private DataField raceTimeF;
	private StatusField statusF;
	
	private boolean recheckCourseOnChange = true;

	public ECardBoard(MergeWizard wizard, JComponent panel, int firstLine) {
		super("ECard Data", wizard, panel, firstLine);
	}
	
	public void updatePanel() {
		ecardF.setText(ecardData().getRunner().getEcard());
		startTimeF.setText(TimeManager.fullTime(ecardData().getStarttime()));
		finishTimeF.setText(TimeManager.fullTime(ecardData().getFinishtime()));
		updateResults();
		initCoursesComboBox();
	}

	protected void initCoursesComboBox() {
		coursesCB.setModel(new DefaultComboBoxModel(registry().getSortedCourseNames().toArray()));
		updateCourseSelection();
		coursesCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if( recheckCourseOnChange ) {
					String selectedCoursename = (String) coursesCB.getSelectedItem();
					control().checkTentativeCourse(ecardData(), selectedCoursename);					
				}
				wizard().updateResults();
			}
		});
	}

	protected void updateCourseSelection() {
		coursesCB.setSelectedItem(ecardData().getCourse().getName());
	}

	public void updateResults() {
		raceTimeF.setText(ecardData().getResult().formatRacetime());
		statusF.update(ecardData().getStatus());
	}
	
	protected void initButtons(JComponent panel) {
		JButton createAnonB = new JButton(GecoIcon.createIcon(GecoIcon.CreateAnon));
		createAnonB.setToolTipText("Create anonymous runner with ecard data");
		createAnonB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				control().registerAnonymousRunner(ecardData());
				wizard().closeAfterCreate();
			}
		});
		JButton cancelB = new JButton(GecoIcon.createIcon(GecoIcon.Cancel));
		cancelB.setToolTipText("Close wizard and cancel the merge");
		cancelB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				wizard().closeAndReturn(null);
			}
		});
		Box buttons = Box.createVerticalBox();
		buttons.add(createAnonB);
		buttons.add(cancelB);
		panel.add(buttons, buttonsCol(3));
	}

	protected void initDataPanel(JComponent panel) {
		initDataLine1(panel);
		initDataLine2(panel);
		initDataLine3(panel);
	}

	private void initDataLine1(JComponent panel) {
		GridBagConstraints c = gridLine();
		panel.add(new JLabel("ECard"), c);
		ecardF = new DataField();
		panel.add(ecardF, c);
		c.gridwidth = 2;
		setInsets(c, 0, 0);
		coursesCB = new JComboBox();
		coursesCB.setPreferredSize(new Dimension(50, SwingUtils.SPINNERHEIGHT + 2));
		panel.add(coursesCB, c);
		c.gridwidth = 1;
		resetInsets(c);
		JButton detectCourseB = new JButton(GecoIcon.createIcon(GecoIcon.DetectCourse));
		detectCourseB.setToolTipText("Detect course with best match");
		detectCourseB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				control().detectCourse(ecardData());
				recheckCourseOnChange = false; // temporarily disabled coursesCB recheck
				updateCourseSelection();
				recheckCourseOnChange = true;
			}
		});
		panel.add(detectCourseB, c);
	}

	private void initDataLine2(JComponent panel) {
		GridBagConstraints c = gridLine();
		panel.add(new JLabel("Start"), c);
		startTimeF = new DataField();
		panel.add(startTimeF, c);
		setInsets(c, INSET, INSET);
		panel.add(new JLabel("Finish"), c);
		resetInsets(c);
		finishTimeF = new DataField();
		panel.add(finishTimeF, c);
	}

	private void initDataLine3(JComponent panel) {
		GridBagConstraints c = gridLine();
		panel.add(new JLabel("Status"), c);
		statusF = new StatusField();
		panel.add(statusF, c);
		setInsets(c, INSET, INSET);
		panel.add(new JLabel("Time"), c);
		resetInsets(c);
		raceTimeF = new DataField();
		panel.add(raceTimeF, c);
	}
	
}