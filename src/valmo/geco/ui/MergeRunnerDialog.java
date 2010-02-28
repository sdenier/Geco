/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import valmo.geco.control.RunnerControl;
import valmo.geco.core.Geco;
import valmo.geco.core.TimeManager;
import valmo.geco.core.Util;
import valmo.geco.model.Registry;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.RunnerResult;
import valmo.geco.model.Status;


/**
 * @author Simon Denier
 * @since Jan 31, 2009
 *
 */
public class MergeRunnerDialog extends JDialog {
	
	private Geco geco;
	private RunnerRaceData runnerData;
	private Runner existingRunner;
	private Runner mockRunner;
	private String chipNumber;
	
	private JLabel chipL;
	private JLabel punchesL;
	private JLabel timeL;
	private JLabel statusL;

	private JComboBox courseCB;
	private JButton createB;
	private JButton closeB;
	private JComboBox runnersCB;
	private JButton mergeB;
	private JLabel mergeInfoL;
	
	private boolean updateStatus;

	
	public MergeRunnerDialog(Geco geco, JFrame frame, String title) {
		super(frame, title, true);
		this.geco = geco;
		setLocationRelativeTo(frame);
		setResizable(false);
//		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		chipL = new JLabel();
		punchesL = new JLabel();
		timeL = new JLabel();
		statusL = new JLabel();
		courseCB = new JComboBox(courseItems(geco));
		createB = new JButton("Create");
		closeB = new JButton("Close");
		
		runnersCB = new JComboBox(runnerItems(geco));
		mergeB = new JButton("Merge");
		mergeInfoL = new JLabel();

		initCardPanel();
		initMergePanel();
		initListeners();
	}

	private Vector<String> courseItems(Geco geco) {
		Vector<String> items = new Vector<String>();
		items.add("[Unknown]");
		items.addAll(geco.registry().getSortedCoursenames());
		return items;
	}

	private Object[] runnerItems(Geco geco) {
		Runner[] runners = geco.registry().getRunners().toArray(new Runner[0]);
		Arrays.sort(runners, new Comparator<Runner>() {
			@Override
			public int compare(Runner o1, Runner o2) {
				return o1.getLastname().compareTo(o2.getLastname());
			}
		});
		return runners;
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
		
		cardPanel.add(createB, Util.gbConstr(5));
		c = Util.gbConstr(5);
		c.gridwidth = 2;
		c.insets = insets;
		cardPanel.add(new JLabel("Create new runner with above card data."), c);
		cardPanel.add(closeB, Util.gbConstr(6));
		c = Util.gbConstr(6);
		c.gridwidth = 2;
		c.insets = insets;
		cardPanel.add(new JLabel("Do nothing (discard card data if new)."), c);
		
		JPanel embed = Util.embed(cardPanel);
		embed.setBorder(BorderFactory.createTitledBorder("Card data"));
		getContentPane().add(embed);
	}

	private void initMergePanel() {
		JPanel mergePanel = new JPanel(new BorderLayout());
		mergePanel.setBorder(BorderFactory.createTitledBorder("Merge"));
		mergePanel.add(runnersCB, BorderLayout.CENTER);
		JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
		south.add(mergeB);
		south.add(mergeInfoL);
		mergePanel.add(south, BorderLayout.SOUTH);
		getContentPane().add(mergePanel);
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
	
	private void showDialogFor(RunnerRaceData data, String chip) {
		this.runnerData = data;
		this.chipNumber = chip;
		this.existingRunner = data.getRunner();
		
		// Only compute new status if initially one of the following
		Status status = data.getResult().getStatus();
		updateStatus = status == Status.OK || status == Status.MP || status == Status.Unknown;
		
		this.mockRunner = runnerControl().buildMockRunner();
		this.runnerData.setRunner(this.mockRunner);
		if( this.existingRunner != null ) {
			// initialize mock object with minimal props for the checker
			mockRunner.setCourse(this.existingRunner.getCourse());
			courseCB.setSelectedItem(data.getCourse().getName());
		}

		showCardData(chip, data);
	}

	
	public void showMergeDialogFor(RunnerRaceData data, String chip) {
		showDialogFor(data, chip);
		runnersCB.setSelectedIndex(-1);
		showMergeInfo();
		pack();
		setVisible(true);
	}
	
	public void showOverrideDialogFor(RunnerRaceData data, Runner target) {
		showDialogFor(data, target.getChipnumber());
		courseCB.setSelectedItem(target.getCourse().getName());
		runnersCB.setSelectedItem(target);
		showOverrideInfo();
		pack();
		setVisible(true);
	}

	
	public void showMergeInfo() {
		mergeInfoL.setText("Merge card data into above runner.");
		mergeB.setText("Merge");
		repaint();
	}

	public void showOverrideInfo() {
//		mergeInfoL.setText("Override runner result with card data.");
		RunnerResult result = registry().findRunnerData(getTargetRunner()).getResult();
		mergeInfoL.setText("Override " + printResult(result) + " with " + printResult(runnerData.getResult()));
		mergeB.setText("Override");
		repaint();
	}
	
	private String printResult(RunnerResult result) {
		return result.getStatus() + " in " + TimeManager.time(result.getRacetime());
	}

	
	public void initListeners() {
		courseCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( updateStatus ) {
					String selectedCoursename = getSelectedCoursename();
					if( selectedCoursename.equals("[Unknown]")) {
						mockRunner.setCourse(registry().anyCourse());
						runnerData.getResult().setStatus(Status.Unknown);
					} else {
						mockRunner.setCourse(registry().findCourse(selectedCoursename));
						geco.checker().check(runnerData);
					}
				}
				updateStatusLabel();
			}
		});
		
		createB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// always assert we use a unique chipnumber (in particular if creating a new runner
				// when one exists with the same chip)
				String uniqueChipnumber = runnerControl().deriveUniqueChipnumber(chipNumber);
				// Create from scratch a brand new runner
				Runner newRunner = runnerControl().buildAnonymousRunner(uniqueChipnumber);
				// do not run checker as it should have been run
				String selectedCoursename = getSelectedCoursename();
				if( selectedCoursename.equals("[Unknown]")) {
					newRunner.setCourse(registry().anyCourse());
				} else {
					newRunner.setCourse(registry().findCourse(selectedCoursename));
				}
				runnerControl().registerRunner(newRunner, runnerData);
				setVisible(false);
			}
		});
		closeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// do nothing, just close the dialog to lose the ref
				setVisible(false);
			}
		});
		runnersCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( getTargetRunner()==null ) {
					mergeB.setEnabled(false);
					return;
				}
				courseCB.setSelectedItem(getTargetRunner().getCourse().getName());
				mergeB.setEnabled(true);
				if( hasStatus(getTargetRunner()) ) {
					showOverrideInfo();
				} else {
					showMergeInfo();
				}
			}
		});
		mergeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// for simplicity, do not update course here
				// can still be done outside
				runnerControl().updateRunnerDataFor(getTargetRunner(), runnerData);
				if( existingRunner != null ) {// offer to delete previous runner if applicable
					int confirm = JOptionPane.showConfirmDialog(MergeRunnerDialog.this,
																"Confirm deletion of " + existingRunner.idString(),
																"Delete original runner",
																JOptionPane.YES_NO_OPTION);
					if( confirm == JOptionPane.YES_OPTION ) {
						runnerControl().deleteRunner(getRunnerData(existingRunner));
					}
				}
				setVisible(false);
			}
		});

	}

	private Registry registry() {
		return geco.registry();
	}

	private RunnerControl runnerControl() {
		return geco.runnerControl();
	}
	
	private Runner getTargetRunner() {
		return (Runner) runnersCB.getSelectedItem();
	}

	private RunnerRaceData getRunnerData(Runner runner) {
		return registry().findRunnerData(runner);
	}
	
	private boolean hasStatus(Runner runner) {
		return getRunnerData(runner).hasResult();
	}

	private String getSelectedCoursename() {
		return (String) courseCB.getSelectedItem();
	}

	
}
