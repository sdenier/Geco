/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import valmo.geco.core.Geco;
import valmo.geco.core.TimeManager;
import valmo.geco.core.Util;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Status;


/**
 * @author Simon Denier
 * @since Jan 31, 2009
 *
 */
public class MergeRunnerDialog extends JDialog {
	// TODO: disable buttons in window bar or change default actions?
	
	private Geco geco;
	private RunnerRaceData runnerData;

	private JLabel mergeInfoL;
	private JLabel dataInfoL;
	private JComboBox runnersCB;
//	private JButton insertB;
	private JButton mergeB;
	private JButton createB;
	private JButton discardB;
	private JComboBox courseCB;
	
	private JLabel chipL;
	private JLabel punchesL;
	private JLabel timeL;
	private JLabel statusL;
	private Runner runner;
	private String chipNumber;

	
	public MergeRunnerDialog(Geco geco, JFrame frame, String title) {
		super(frame, title, true);
		this.geco = geco;
		setLocationRelativeTo(frame);
		setResizable(false);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		chipL = new JLabel();
		punchesL = new JLabel();
		timeL = new JLabel();
		statusL = new JLabel();
		courseCB = new JComboBox(courseItems(geco));
		createB = new JButton("Create"); // focus on created John Doe
//		createB.setToolTipText("Create runner with card data");
		
//		dataInfoL = new JLabel("YYY");
		mergeInfoL = new JLabel("XXX");		
		runnersCB = new JComboBox(geco.registry().getRunners().toArray());
		mergeB = new JButton("Merge"); // merge or override existing data
		
		discardB = new JButton("Discard"); // delete John Doe
//		discardB.setToolTipText("Discard card data");

		initCardPanel();
		initMergePanel();
		initDiscardPanel();
		
		initListeners();
	}

	private Vector<String> courseItems(Geco geco) {
		Vector<String> items = new Vector<String>();
		items.add("[None]");
		items.addAll(geco.registry().getSortedCoursenames());
		return items;
	}

	private void initCardPanel() {
		JPanel cardPanel = new JPanel(new GridBagLayout());
		Insets insets = new Insets(0, 10, 0, 0);
		cardPanel.add(new JLabel("Chip number:"), Util.gbConstr(0));
		GridBagConstraints c = Util.gbConstr(0);
		c.insets = insets;
		cardPanel.add(chipL, c);
		cardPanel.add(new JLabel("Punches:"), Util.gbConstr(1));
		c = Util.gbConstr(1);
		c.gridwidth = 2;
		c.insets = insets;
		cardPanel.add(punchesL, c);
		cardPanel.add(new JLabel("Race time:"), Util.gbConstr(2));
		c = Util.gbConstr(2);
		c.insets = insets;
		cardPanel.add(timeL, c);
		cardPanel.add(new JLabel("Status:"), Util.gbConstr(3));
		c = Util.gbConstr(3);
		c.insets = new Insets(0, 10, 0, 10);
		cardPanel.add(statusL, c);
		c = Util.gbConstr(3);
		c.anchor = GridBagConstraints.CENTER;
		cardPanel.add(courseCB, c);
		cardPanel.add(createB, Util.gbConstr(4));
		c = Util.gbConstr(4);
		c.gridwidth = 2;
		c.insets = insets;
		cardPanel.add(new JLabel("Create new runner with above card data"), c);
		
		JPanel embed = Util.embed(cardPanel);
		embed.setBorder(BorderFactory.createTitledBorder("Card data"));
		getContentPane().add(embed);
	}
	
	private void showCardData(String chip, RunnerRaceData runnerData) {
		chipL.setText(Util.italicize(chip));
		punchesL.setText(Util.italicize(runnerData.getPunches().length
										+ " starting with " + runnerData.punchSummary(5)));
		timeL.setText(Util.italicize(TimeManager.time(runnerData.raceTime())));
		updateStatusLabel();
	}

	private void updateStatusLabel() {
		statusL.setText(Util.italicize(runnerData.getResult().getStatus().toString()));
	}

	private void initMergePanel() {
		JPanel mergePanel = new JPanel(new BorderLayout());
		mergePanel.setBorder(BorderFactory.createTitledBorder("Merge"));
		mergePanel.add(runnersCB, BorderLayout.CENTER);
		JPanel south = new JPanel();
		south.add(mergeB);
		south.add(mergeInfoL);
		mergePanel.add(south, BorderLayout.SOUTH);
		getContentPane().add(mergePanel);
	}
	
	private void initDiscardPanel() {
		JPanel discardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		discardPanel.setBorder(BorderFactory.createTitledBorder("Discard"));
		discardPanel.add(discardB);
		discardPanel.add(new JLabel("Do nothing and discard card data"));
		getContentPane().add(discardPanel);
	}
	
//	public void showMergeInfo() {
//		mergeInfoL.setText("Merge into runner data:");
//		StringBuffer buf = new StringBuffer("With: ");
//		buf.append(runnerData.toString());
//		dataInfoL.setText(buf.toString());
//		mergeB.setText("Merge");
//		mergeB.setToolTipText("Merge card data into runner");
//		repaint();
//	}
	
	public void showMergeDialogFor(RunnerRaceData data, String chip) {
		this.runnerData = data;
		this.chipNumber = chip;
		if( data.getRunner()==null ) {
			this.runner = geco.runnerControl().buildMockRunner();
			this.runnerData.setRunner(this.runner);
		} else {
			this.runner = data.getRunner();
			courseCB.setSelectedItem(data.getCourse().getName());
		}
		runnersCB.setSelectedIndex(-1);
		showCardData(chip, data);
		pack();
		setVisible(true);
	}
	
//	public void showOverrideInfo() {
//		mergeInfoL.setText("Override runner data from:");
//		StringBuffer buf = new StringBuffer("With: ");
//		buf.append(runnerData.toString());
//		dataInfoL.setText(buf.toString());
//		mergeB.setText("Override");
//		mergeB.setToolTipText("Override results of runner");
//		repaint();
//	}
	
	public void showOverrideDialogFor(RunnerRaceData data, Runner target) {
		this.runnerData = data;
		if( data.getRunner()==null ) { // mock runner for good play 
			this.runner = geco.runnerControl().buildMockRunner();
			this.runnerData.setRunner(this.runner);
		} else {
			this.runner = data.getRunner();
		}
		runnersCB.setSelectedItem(target);
//		showOverrideInfo();
		pack();
		setVisible(true);
	}
	
	public Runner getTargetRunner() {
		return (Runner) runnersCB.getSelectedItem();
	}
	
	public boolean hasStatus(Runner runner) {
		return geco.registry().findRunnerData(runner).hasResult();
	}
	

	public void initListeners() {
		courseCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedCoursename = getSelectedCoursenmae();
				if( selectedCoursename.equals("[None]")) {
					runner.setCourse(geco.registry().anyCourse());
					runnerData.getResult().setStatus(Status.Unknown);
				} else {
					runner.setCourse(geco.registry().findCourse(selectedCoursename));
					geco.checker().check(runnerData);
				}
				updateStatusLabel();
			}
		});
		
		createB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				 * Create a anon runner and register it
				 * link with data
				 * (choose course + check data?)
				 */

				// Create from scratch a brand new runner
				runner = geco.runnerControl().buildAnonymousRunner(chipNumber);
				// do not run checker as it should have been run
				String selectedCoursename = getSelectedCoursenmae();
				if( selectedCoursename.equals("[None]")) {
					runner.setCourse(geco.registry().anyCourse());
				} else {
					runner.setCourse(geco.registry().findCourse(selectedCoursename));
				}
				geco.runnerControl().registerRunner(runner, runnerData);
				setVisible(false);
			}
		});
		runnersCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// update selected course???
				// check racedata against course
				if( getTargetRunner()==null ) {
					mergeB.setEnabled(false);
					return;
				}
				mergeB.setEnabled(true);
				if( hasStatus(getTargetRunner()) ) {
//					showOverrideInfo();
				} else {
//					showMergeInfo();
				}
			}
		});
		mergeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				 * set course?
				 * set data for target runner
				 * check against chosen course (default target)
				 * 
				 */
				// might have to delete runnerdata.getRunner if exists
				setVisible(false);
			}
		});
		discardB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// do nothing, just close the dialog to lose the ref
				setVisible(false);
			}
		});
	}
	

	private String getSelectedCoursenmae() {
		return (String) courseCB.getSelectedItem();
	}

	
}
