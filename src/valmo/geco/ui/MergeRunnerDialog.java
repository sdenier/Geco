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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

import valmo.geco.Geco;
import valmo.geco.control.RunnerControl;
import valmo.geco.core.Html;
import valmo.geco.core.TimeManager;
import valmo.geco.model.Course;
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
	/**
	 * Chipnumber identifying the runner which actually got changed, or null if data was discarded
	 */
	private String returnChip;
	
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
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close();
			}
		});

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

	private void close() {
		returnChip = null;
		// do nothing, just close the dialog to lose the ref
		setVisible(false);
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
		cardPanel.add(new JLabel("Chip number:"), SwingUtils.gbConstr(0));
		GridBagConstraints c = SwingUtils.gbConstr(0);
		c.insets = insets;
		cardPanel.add(chipL, c);
		cardPanel.add(new JLabel("Punches:"), SwingUtils.gbConstr(1));
		c = SwingUtils.gbConstr(1);
		c.gridwidth = 2;
		c.insets = insets;
		cardPanel.add(punchesL, c);
		cardPanel.add(new JLabel("Race time:"), SwingUtils.gbConstr(2));
		c = SwingUtils.gbConstr(2);
		c.insets = insets;
		cardPanel.add(timeL, c);
		cardPanel.add(new JLabel("Status:"), SwingUtils.gbConstr(3));
		c = SwingUtils.gbConstr(3);
		c.insets = new Insets(0, 10, 0, 10);
		cardPanel.add(statusL, c);
		c = SwingUtils.gbConstr(3);
		c.anchor = GridBagConstraints.CENTER;
		cardPanel.add(courseCB, c);
		
		cardPanel.add(createB, SwingUtils.gbConstr(5));
		c = SwingUtils.gbConstr(5);
		c.gridwidth = 2;
		c.insets = insets;
		cardPanel.add(new JLabel("Create new runner with above card data"), c);
		cardPanel.add(closeB, SwingUtils.gbConstr(6));
		c = SwingUtils.gbConstr(6);
		c.gridwidth = 2;
		c.insets = insets;
		cardPanel.add(new JLabel("Do nothing (discard card data if new)"), c);
		
		JPanel embed = SwingUtils.embed(cardPanel);
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
		chipL.setText(Html.htmlTag("i", chip));
		punchesL.setText(Html.htmlTag("i", runnerData.getPunches().length
										+ " starting with " + runnerData.punchSummary(5)));
		timeL.setText(Html.htmlTag("i", TimeManager.time(runnerData.realRaceTime())));
		updateStatusLabel();
	}

	private void updateStatusLabel() {
		statusL.setText(Html.htmlTag("i", runnerData.getResult().getStatus().toString()));
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
		} else {
			courseCB.setSelectedItem("[Unknown]");
		}

		showCardData(chip, data);
	}

	
	public String showMergeDialogFor(RunnerRaceData data, String chip) {
		showDialogFor(data, chip);
		runnersCB.setSelectedIndex(-1);
		showMergeInfo();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		return returnChip;
	}
	
	public String showOverwriteDialogFor(RunnerRaceData data, Runner target) {
		showDialogFor(data, target.getChipnumber());
		courseCB.setSelectedItem(target.getCourse().getName());
		runnersCB.setSelectedItem(target);
		showOverwriteInfo();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		return returnChip;
	}

	
	public void showMergeInfo() {
		mergeInfoL.setText("Merge card data into above runner");
		mergeB.setText("Merge");
		repaint();
	}

	public void showOverwriteInfo() {
//		mergeInfoL.setText("Override runner result with card data.");
		RunnerResult result = registry().findRunnerData(getTargetRunner()).getResult();
		mergeInfoL.setText("Overwrite " + printResult(result) + " with " + printResult(runnerData.getResult()));
		mergeB.setText("Overwrite");
		repaint();
	}
	
	private String printResult(RunnerResult result) {
		return result.getStatus() + " in " + result.formatRacetime();
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
						geco.checker().normalTrace(runnerData);
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
				String selectedCoursename = getSelectedCoursename();
				Course course = selectedCoursename.equals("[Unknown]") ?
					registry().anyCourse() :
					registry().findCourse(selectedCoursename);
				// Create from scratch a brand new runner
				Runner newRunner = runnerControl().buildAnonymousRunner(uniqueChipnumber, course);
				// do not run checker as it should have been run
				runnerControl().registerRunner(newRunner, runnerData);
				geco.log("Creation " + runnerData.infoString());
				returnChip = uniqueChipnumber;
				setVisible(false);
			}
		});
		closeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
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
					showOverwriteInfo();
				} else {
					showMergeInfo();
				}
			}
		});
		mergeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RunnerRaceData updatedData = runnerControl().updateRunnerDataFor(getTargetRunner(), runnerData);
				runnerControl().validateCourse(updatedData, getSelectedCoursename());
				if( existingRunner != null ) {// offer to delete previous runner if applicable
					int confirm = JOptionPane.showConfirmDialog(MergeRunnerDialog.this,
																"Confirm deletion of " + existingRunner.idString(),
																"Delete original runner",
																JOptionPane.YES_NO_OPTION);
					if( confirm == JOptionPane.YES_OPTION ) {
						runnerControl().deleteRunner(getRunnerData(existingRunner));
					}
				}
				geco.log("Merge " + getRunnerData(getTargetRunner()).infoString());
				returnChip = getTargetRunner().getChipnumber();
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
