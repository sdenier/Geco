/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import valmo.geco.Geco;
import valmo.geco.control.RunnerControl;
import valmo.geco.core.Messages;
import valmo.geco.core.TimeManager;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Jan 24, 2009
 *
 */
public class RunnerPanel extends GecoPanel {

	private RunnerRaceData runnerData;
	private Runner runner;
	private RunnersPanel parentContainer;

	// Registration
	private JTextField regStartF;
	private JCheckBox rentedBx;
	private JCheckBox ncBx;

	// E-card
	private JTextField readTimeF;
	private JTextField checkTimeF;
	private JTextField clearTimeF;
	private JTextField startTimeF;
	private JTextField finishTimeF;
	private JTextField raceTimeF;

	// Result
	private JTextField mpF;
	private JTextField penaltyF;
	private JTextField fullTimeF;

	// Actions
	private JButton recheckStatusB;
	private JButton mergeDialogB;
	private JButton resetTimeB;
	private JButton quickPrintB;
	
	private static final int FIELDSIZE = 5;
	private static final Color PALE_YELLOW = new Color(1, 1, 0.5f);
	private static final Color PALE_BLUE = new Color(0.9f, 0.9f, 1.0f);
	private static final Color PALE_RED = new Color(1.0f, 0.9f, 0.9f);

	
	public RunnerPanel(Geco geco, JFrame frame, RunnersPanel parent) {
		super(geco, frame);
		this.parentContainer = parent;
		initPanel(this);
		enablePanel(false);
	}
	
	public JPanel initPanel(JPanel panel) {
			Container panel2 = new JPanel();
			panel2.setLayout(new GridBagLayout());
			GridBagConstraints c = SwingUtils.gbConstr();
			c.fill = GridBagConstraints.HORIZONTAL;
//			c.insets = new Insets(5, 0, 0, 0);
			panel2.add(initRegistrationPanel(), c);
			c.gridy = 1;
			panel2.add(initEcardPanel(), c);
			c.gridy = 2;
			panel2.add(initResultPanel(), c);
			c.gridy = 3;
			panel2.add(initButtonBar(), c);
	
			panel.setLayout(new BorderLayout());
			panel.add(panel2, BorderLayout.NORTH);
			
			return panel;
		}

	public void updateRunner(String chipnumber) {
		this.runnerData = registry().findRunnerData(chipnumber);
		this.runner = runnerData.getRunner();
		if( ! isEnabled() ){
			enablePanel(true);
		}
		refreshPanel();
	}
	
	public void enablePanel(boolean enabled) {
		setEnabled(enabled,
				this,
				regStartF,
				rentedBx,
				ncBx,
				recheckStatusB,
				mergeDialogB,
				resetTimeB,
				quickPrintB);
	}
	
	private void setEnabled(boolean enabled, Component... components) {
		for (Component component : components) {
			component.setEnabled(enabled);
		}
	}
	
	protected void refreshPanel() {
		refreshRegistrationPanel();
		refreshEcardPanel();
		refreshResultPanel();
	}
	
	protected void refreshRegistrationPanel() {
		displayRegTime(regStartF, runner.getRegisteredStarttime());
		rentedBx.setSelected(runner.rentedEcard());
		ncBx.setSelected(runner.isNC());
	}

	protected void refreshEcardPanel() {
		displayCardTime(readTimeF, runnerData.getReadtime());
		displayCardTime(clearTimeF, runnerData.getErasetime());
		displayCardTime(checkTimeF, runnerData.getControltime());
		displayCardTimeWithMissingHint(startTimeF, runnerData.getStarttime());
		displayCardTimeWithMissingHint(finishTimeF, runnerData.getFinishtime());
		displayRacetime(raceTimeF, runnerData.realRaceTime());
	}

	protected void refreshResultPanel() {
		mpF.setText(Integer.toString(runnerData.getResult().getNbMPs()));
		penaltyF.setText(runnerData.getResult().formatTimePenalty());
		displayOfficialRacetime(fullTimeF, runnerData.officialRaceTime());
	}

	private void prvDisplayTime(JTextField timeF, String text, Color bgColor) {
		timeF.setText(text);
		timeF.setBackground(bgColor);
	}
	
	protected void displayRegTime(JTextField timeF, Date time) {
		if( time.equals(TimeManager.NO_TIME) ) {
			prvDisplayTime(timeF, "", Color.white);
		} else {
			prvDisplayTime(timeF, TimeManager.fullTime(time), PALE_BLUE);
		}
	}
	
	protected void displayCardTime(JTextField timeF, Date time) {
		timeF.setText(TimeManager.fullTime(time));
	}

	protected void displayCardTimeWithMissingHint(JTextField timeF, Date time) {
		if( time.equals(TimeManager.NO_TIME) ) {
			prvDisplayTime(timeF, TimeManager.fullTime(time), PALE_RED);
		} else {
			prvDisplayTime(timeF, TimeManager.fullTime(time), Color.white);
		}
	}
	
	protected void displayRacetime(JTextField timeF, long time) {
		timeF.setText(TimeManager.time(time));
	}

//	protected void displayStarttime(JTextField timeF, Date time) {
//		displayTime(timeF, time);
//		if( runnerData.useRegisteredStarttime() && ! time.equals(TimeManager.NO_TIME) ) {
//			timeF.setBackground(PALE_BLUE);
//		}
//	}

	protected void displayOfficialRacetime(JTextField timeF, long computedTime) {
		if( computedTime != runnerData.getResult().getRacetime() ) {
			prvDisplayTime(timeF, TimeManager.time(computedTime), PALE_YELLOW);
		} else {
			prvDisplayTime(timeF, TimeManager.time(computedTime), Color.white);
		}
	}
	
	
	private GridBagConstraints buildGBConstraint() {
		GridBagConstraints c = SwingUtils.gbConstr();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 10, 0, 0);
		return c;
	}

	private void addRow(Container cont, GridBagConstraints c, Component...components ) {
		for (Component component : components) {
			cont.add(component, c);
		}
	}
	
	public JPanel initRegistrationPanel() {
		regStartF = new JTextField(FIELDSIZE);
		rentedBx = new JCheckBox("Rented E-card");
		ncBx = new JCheckBox("NC");
		
		regStartF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				control().validateRegisteredStartTime(runner, regStartF.getText());
				refreshRegistrationPanel();
			}
		});
		regStartF.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				try {
					TimeManager.userParse(regStartF.getText());
					return true;
				} catch (ParseException e) {
					return false;
				}
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean ret = control().validateRegisteredStartTime(runner, regStartF.getText());
				refreshRegistrationPanel();
				return ret;
			}
		});

		rentedBx.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runner.setRentedEcard(rentedBx.isSelected());
			}
		});
		ncBx.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				control().validateNCStatus(runner, ncBx.isSelected());
			}
		});

		JPanel regPanel = new JPanel();
		regPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = buildGBConstraint();
		addRow(regPanel, c, new JLabel("Registered Start"), regStartF);
		c.gridy = 1;
		addRow(regPanel, c, rentedBx, ncBx);
		
		regPanel.setBorder(BorderFactory.createTitledBorder("Registration"));
		return regPanel;
	}

	public JPanel initEcardPanel() {
		clearTimeF = new JTextField(FIELDSIZE);
		clearTimeF.setEditable(false);
		checkTimeF = new JTextField(FIELDSIZE);
		checkTimeF.setEditable(false);
		startTimeF = new JTextField(FIELDSIZE);
		startTimeF.setEditable(false);
		finishTimeF = new JTextField(FIELDSIZE);
		finishTimeF.setEditable(false);
		readTimeF = new JTextField(FIELDSIZE);
		readTimeF.setEditable(false);
		raceTimeF = new JTextField(FIELDSIZE);
		raceTimeF.setEditable(false);
		raceTimeF.setToolTipText(Messages.uiGet("RunnerPanel.RealRacetimeTooltip1") //$NON-NLS-1$
				+ Messages.uiGet("RunnerPanel.RealRacetimeTooltip2")); //$NON-NLS-1$		
		
		JPanel ecardPanel = new JPanel();
		ecardPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = buildGBConstraint();
		addRow(ecardPanel, c, 
				new JLabel(Messages.uiGet("RunnerPanel.EraseLabel")), //$NON-NLS-1$
				clearTimeF,
				new JLabel(Messages.uiGet("RunnerPanel.ControlLabel")), //$NON-NLS-1$
				checkTimeF);
		c.gridy = 1;
		addRow(ecardPanel, c,
				new JLabel(Messages.uiGet("RunnerPanel.StartLabel")), //$NON-NLS-1$
				startTimeF,
				new JLabel(Messages.uiGet("RunnerPanel.FinishLabel")), //$NON-NLS-1$
				finishTimeF);
		c.gridy = 2;
		addRow(ecardPanel, c,
				new JLabel(Messages.uiGet("RunnerPanel.ReadLabel")), //$NON-NLS-1$
				readTimeF,
				new JLabel(Messages.uiGet("RunnerPanel.RaceLabel")), //$NON-NLS-1$
				raceTimeF);
		
		ecardPanel.setBorder(BorderFactory.createTitledBorder("E-card"));
		return ecardPanel;
	}
	
	
	public JPanel initResultPanel() {
		mpF = new JTextField(FIELDSIZE);
		mpF.setEditable(false);
		penaltyF = new JTextField(FIELDSIZE);
		penaltyF.setEditable(false);
		fullTimeF = new JTextField(FIELDSIZE);
		fullTimeF.setEditable(false);
		
		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = buildGBConstraint();
		addRow(resultPanel, c,
				new JLabel(Messages.uiGet("RunnerPanel.MPLabel")), //$NON-NLS-1$
				mpF,
				new JLabel(Messages.uiGet("RunnerPanel.PenaltyLabel")), //$NON-NLS-1$
				penaltyF);
		c.gridy = 1;
		addRow(resultPanel, c, 
				Box.createGlue(),
				Box.createGlue(),
				new JLabel("Official"),
				fullTimeF);

//		c.gridy = 2;
//		addRow(resultPanel, c, 
//				new JLabel(Messages.uiGet("RunnerPanel.StartLabel")), //$NON-NLS-1$
//				new JTextField(4),
//				new JLabel(Messages.uiGet("RunnerPanel.FinishLabel")), //$NON-NLS-1$
//				new JTextField(4));

		resultPanel.setBorder(BorderFactory.createTitledBorder("Result"));
		return resultPanel;
	}

	public Container initButtonBar() {
		recheckStatusB = createRecheckStatusButton();
		mergeDialogB = createMergeDialogButton();
		resetTimeB = createResetTimeButton();
		quickPrintB = createQuickPrintButton();
		return SwingUtils.makeButtonBar(FlowLayout.CENTER,
							recheckStatusB,
							mergeDialogB,
							resetTimeB,
							quickPrintB);
	}
	
	private RunnerControl control() {
		return geco().runnerControl();
	}

	public void recheckRunnerStatus() {
		if( control().recheckRunner(runnerData) ){
			parentContainer.refreshSelectionInTable();
			refreshPanel();
		}
	}

	public void printRunnerSplits() {
		geco().splitsBuilder().printSingleSplits(runnerData);
	}

	private JButton createRecheckStatusButton() {
		JButton recheckStatusB = new JButton(Messages.uiGet("RunnerPanel.RecheckLabel")); //$NON-NLS-1$
		recheckStatusB.setToolTipText(Messages.uiGet("RunnerPanel.RecheckTooltip")); //$NON-NLS-1$
		recheckStatusB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				recheckRunnerStatus();
			}
		});
		return recheckStatusB;
	}

	private JButton createMergeDialogButton() {
		JButton mergeDialogB = new JButton(Messages.uiGet("RunnerPanel.MergeLabel")); //$NON-NLS-1$
		mergeDialogB.setToolTipText(Messages.uiGet("RunnerPanel.MergeTooltip")); //$NON-NLS-1$
		mergeDialogB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new MergeRunnerDialog(
						geco(),
						frame(),
						Messages.uiGet("RunnerPanel.MergeCardTitle")) //$NON-NLS-1$
					.showMergeDialogFor(
							runnerData.clone(),
							runner.getChipnumber(),
							runnerData.getResult().getStatus());
			}
		});
		return mergeDialogB;
	}

	private JButton createResetTimeButton() {
		ImageIcon time = new ImageIcon(
				getClass().getResource("/resources/icons/crystal/history.png")); //$NON-NLS-1$
		JButton resetTimeB = new JButton(time);
		resetTimeB.setToolTipText(Messages.uiGet("RunnerPanel.ResetTimeTooltip")); //$NON-NLS-1$
		resetTimeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( control().resetRaceTime(runnerData) ){
					parentContainer.refreshSelectionInTable();
					refreshPanel();
				}
			}
		});
		return resetTimeB;
	}

	private JButton createQuickPrintButton() {
		ImageIcon splitPrint = new ImageIcon(
				getClass().getResource("/resources/icons/crystal/filequickprint_small.png")); //$NON-NLS-1$
		JButton splitPrintB = new JButton(splitPrint);
		splitPrintB.setToolTipText(Messages.uiGet("RunnerPanel.SplitprintTooltip")); //$NON-NLS-1$
		splitPrintB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				printRunnerSplits();
			}
		});
		return splitPrintB;
	}
	
}
