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
import valmo.geco.control.RunnerCreationException;
import valmo.geco.core.Html;
import valmo.geco.core.TimeManager;
import valmo.geco.model.Course;
import valmo.geco.model.Messages;
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
	private String ecard;
	/**
	 * E-card identifying the runner which actually got changed, or null if data was discarded
	 */
	private String returnCard;
	private Status defaultCreationStatus;
	
	private JLabel ecardL;
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
		
		ecardL = new JLabel();
		punchesL = new JLabel();
		timeL = new JLabel();
		statusL = new JLabel();
		courseCB = new JComboBox(courseItems(geco));
		createB = new JButton(Messages.uiGet("MergeRunnerDialog.CreateLabel")); //$NON-NLS-1$
		closeB = new JButton(Messages.uiGet("MergeRunnerDialog.CloseLabel")); //$NON-NLS-1$
		
		runnersCB = new JComboBox(runnerItems(geco));
		mergeB = new JButton(Messages.uiGet("MergeRunnerDialog.MergeLabel")); //$NON-NLS-1$
		mergeInfoL = new JLabel();

		initCardPanel();
		initMergePanel();
		initListeners();
	}

	private void close() {
		returnCard = null;
		// do nothing, just close the dialog to lose the ref
		setVisible(false);
	}

	private Vector<String> courseItems(Geco geco) {
		Vector<String> items = new Vector<String>();
		items.add(Messages.uiGet("MergeRunnerDialog.UnknownCourseItem")); //$NON-NLS-1$
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
		cardPanel.add(new JLabel(Messages.uiGet("MergeRunnerDialog.EcardLabel")), SwingUtils.gbConstr(0)); //$NON-NLS-1$
		GridBagConstraints c = SwingUtils.gbConstr(0);
		c.insets = insets;
		cardPanel.add(ecardL, c);
		cardPanel.add(new JLabel(Messages.uiGet("MergeRunnerDialog.PunchLabel")), SwingUtils.gbConstr(1)); //$NON-NLS-1$
		c = SwingUtils.gbConstr(1);
		c.gridwidth = 2;
		c.insets = insets;
		cardPanel.add(punchesL, c);
		cardPanel.add(new JLabel(Messages.uiGet("MergeRunnerDialog.RacetimeLabel")), SwingUtils.gbConstr(2)); //$NON-NLS-1$
		c = SwingUtils.gbConstr(2);
		c.insets = insets;
		cardPanel.add(timeL, c);
		cardPanel.add(new JLabel(Messages.uiGet("MergeRunnerDialog.StatusLabel")), SwingUtils.gbConstr(3)); //$NON-NLS-1$
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
		cardPanel.add(new JLabel(Messages.uiGet("MergeRunnerDialog.CreateHelp")), c); //$NON-NLS-1$
		cardPanel.add(closeB, SwingUtils.gbConstr(6));
		c = SwingUtils.gbConstr(6);
		c.gridwidth = 2;
		c.insets = insets;
		cardPanel.add(new JLabel(Messages.uiGet("MergeRunnerDialog.CloseHelp")), c); //$NON-NLS-1$
		
		JPanel embed = SwingUtils.embed(cardPanel);
		embed.setBorder(BorderFactory.createTitledBorder(Messages.uiGet("MergeRunnerDialog.CardDataTitle"))); //$NON-NLS-1$
		getContentPane().add(embed);
	}

	private void initMergePanel() {
		JPanel mergePanel = new JPanel(new BorderLayout());
		mergePanel.setBorder(BorderFactory.createTitledBorder(Messages.uiGet("MergeRunnerDialog.MergeTitle"))); //$NON-NLS-1$
		mergePanel.add(runnersCB, BorderLayout.CENTER);
		JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
		south.add(mergeB);
		south.add(mergeInfoL);
		mergePanel.add(south, BorderLayout.SOUTH);
		getContentPane().add(mergePanel);
	}

	
	private void showCardData(String ecard, RunnerRaceData runnerData) {
		ecardL.setText(Html.htmlTag("i", ecard)); //$NON-NLS-1$
		punchesL.setText(Html.htmlTag("i", runnerData.getPunches().length //$NON-NLS-1$
										+ Messages.uiGet("MergeRunnerDialog.StartingPunchesLabel") + runnerData.punchSummary(5))); //$NON-NLS-1$
		timeL.setText(Html.htmlTag("i", TimeManager.time(runnerData.realRaceTime()))); //$NON-NLS-1$
		updateStatusLabel();
	}

	private void updateStatusLabel() {
		statusL.setText(Html.htmlTag("i", runnerData.getResult().getStatus().toString())); //$NON-NLS-1$
	}
	
	private void showDialogFor(RunnerRaceData data, String ecard, Status defaultStatus) {
		this.runnerData = data;
		this.ecard = ecard;
		this.existingRunner = data.getRunner();
		this.defaultCreationStatus = defaultStatus;
		
		// Only compute new status if initially one of the following
		Status status = data.getResult().getStatus();
		updateStatus = status.isRecheckable() || status.isUnresolved();  // TODO: we should not recheck when manual mod
		
		this.mockRunner = runnerControl().buildMockRunner();
		this.runnerData.setRunner(this.mockRunner);
		if( this.existingRunner != null ) {
			// initialize mock object with minimal props for the checker
			mockRunner.setCourse(this.existingRunner.getCourse());
			courseCB.setSelectedItem(data.getCourse().getName());
		} else {
			courseCB.setSelectedItem(Messages.uiGet("MergeRunnerDialog.UnknownCourseItem")); //$NON-NLS-1$
		}

		showCardData(ecard, data);
	}

	
	public String showMergeDialogFor(RunnerRaceData data, String ecard, Status defaultStatus) {
		showDialogFor(data, ecard, defaultStatus);
		runnersCB.setSelectedIndex(-1);
		showMergeInfo();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		return returnCard;
	}
	
	public String showOverwriteDialogFor(RunnerRaceData data, Runner target) {
		showDialogFor(data, target.getChipnumber(), Status.DUP);
		courseCB.setSelectedItem(target.getCourse().getName());
		runnersCB.setSelectedItem(target);
		showOverwriteInfo();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		return returnCard;
	}

	
	public void showMergeInfo() {
		mergeInfoL.setText(Messages.uiGet("MergeRunnerDialog.MergeHelp")); //$NON-NLS-1$
		mergeB.setText(Messages.uiGet("MergeRunnerDialog.MergeLabel")); //$NON-NLS-1$
		repaint();
	}

	public void showOverwriteInfo() {
//		mergeInfoL.setText("Override runner result with card data.");
		RunnerResult result = registry().findRunnerData(getTargetRunner()).getResult();
		mergeInfoL.setText(
				Messages.uiGet("MergeRunnerDialog.OverwriteHelp1") //$NON-NLS-1$
				+ printResult(result)
				+ Messages.uiGet("MergeRunnerDialog.OverwriteHelp2") //$NON-NLS-1$
				+ printResult(runnerData.getResult()));
		mergeB.setText(Messages.uiGet("MergeRunnerDialog.OverwriteLabel")); //$NON-NLS-1$
		repaint();
	}
	
	private String printResult(RunnerResult result) {
		return result.getStatus() + Messages.uiGet("MergeRunnerDialog.InLabel") + result.formatRacetime(); //$NON-NLS-1$
	}

	
	public void initListeners() {
		courseCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( updateStatus ) {
					String selectedCoursename = getSelectedCoursename();
					if( selectedCoursename.equals(Messages.uiGet("MergeRunnerDialog.UnknownCourseItem"))) { //$NON-NLS-1$
						mockRunner.setCourse(registry().anyCourse());
						runnerData.getResult().setStatus(defaultCreationStatus);
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
				// always assert we use a unique e-card (in particular if creating a new runner
				// when one exists with the same e-card)
				String uniqueEcardNumber = runnerControl().deriveUniqueChipnumber(ecard);
				String selectedCoursename = getSelectedCoursename();
				Course course = selectedCoursename.equals(Messages.uiGet("MergeRunnerDialog.UnknownCourseItem")) ? //$NON-NLS-1$
					registry().anyCourse() :
					registry().findCourse(selectedCoursename);
				try {
					// Create from scratch a brand new runner
					Runner newRunner = runnerControl().buildAnonymousRunner(uniqueEcardNumber, course);
					// do not run checker as it should have been run
					runnerControl().registerRunner(newRunner, runnerData);
					geco.log("Creation " + runnerData.infoString()); //$NON-NLS-1$
					returnCard = uniqueEcardNumber;
					setVisible(false);
				} catch (RunnerCreationException e1) {
					// should never happen as we cant open a merge dialog without a runner,
					// and we cant have a runner without at least one club, course, category
					e1.printStackTrace();
				}
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
				if( hasData(getTargetRunner()) ) {
					showOverwriteInfo();
				} else {
					showMergeInfo();
				}
			}
		});
		mergeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO: update e-card
				RunnerRaceData updatedData = runnerControl().updateRunnerDataFor(getTargetRunner(), runnerData);
				String selectedCoursename = getSelectedCoursename();
				selectedCoursename = selectedCoursename.equals(Messages.uiGet("MergeRunnerDialog.UnknownCourseItem")) ? //$NON-NLS-1$
					registry().anyCourse().getName() :
					selectedCoursename;
				runnerControl().validateCourse(updatedData, selectedCoursename);
				if( existingRunner != null ) {// offer to delete previous runner if applicable
					int confirm = JOptionPane.showConfirmDialog(
										MergeRunnerDialog.this,
										Messages.uiGet("MergeRunnerDialog.RunnerDeletionLabel") + existingRunner.idString(), //$NON-NLS-1$
										Messages.uiGet("MergeRunnerDialog.RunnerDeletionTitle"), //$NON-NLS-1$
										JOptionPane.YES_NO_OPTION);
					if( confirm == JOptionPane.YES_OPTION ) {
						runnerControl().deleteRunner(getRunnerData(existingRunner));
					}
				}
				geco.log("Merge " + getRunnerData(getTargetRunner()).infoString()); //$NON-NLS-1$
				returnCard = getTargetRunner().getChipnumber();
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
	
	private boolean hasData(Runner runner) {
		return getRunnerData(runner).hasData();
	}

	private String getSelectedCoursename() {
		return (String) courseCB.getSelectedItem();
	}

	
}
