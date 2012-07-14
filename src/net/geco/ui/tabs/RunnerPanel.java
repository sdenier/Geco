/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.tabs;

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
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.geco.basics.TimeManager;
import net.geco.control.RunnerControl;
import net.geco.framework.IGeco;
import net.geco.model.Messages;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.ui.basics.GecoIcon;
import net.geco.ui.basics.SwingUtils;
import net.geco.ui.framework.GecoPanel;
import net.geco.ui.framework.RunnersTableAnnouncer.RunnersTableListener;
import net.geco.ui.merge.MergeWizard;


/**
 * @author Simon Denier
 * @since Jan 24, 2009
 *
 */
public class RunnerPanel extends GecoPanel implements RunnersTableListener {

	private RunnerRaceData runnerData;
	private Runner runner;
	private RunnersPanel parentContainer;

	// Registration
	private JTextField regStartF;
	private JTextField archiveF;
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
	private static final Color PALE_BLUE = new Color(0.8f, 0.8f, 1.0f);
	private static final Color PALE_RED = new Color(1.0f, 0.8f, 0.8f);

	
	public RunnerPanel(IGeco geco, JFrame frame, RunnersPanel parent) {
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

	@Override
	public void selectedRunnerChanged(RunnerRaceData raceData) {
		if( raceData==null ){
			enablePanel(false);
		} else {
			this.runnerData = raceData;
			this.runner = raceData.getRunner();
			if( ! isEnabled() ){
				enablePanel(true);
			}
			refreshPanel();
		}
	}
	
	public void enablePanel(boolean enabled) {
		setEnabled(enabled,
				this,
				regStartF,
				archiveF,
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
		archiveF.setText( ( runner.getArchiveId() != null ) ? runner.getArchiveId().toString() : ""); //$NON-NLS-1$
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
			prvDisplayTime(timeF, "", Color.white); //$NON-NLS-1$
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
		archiveF = new JTextField(FIELDSIZE);
		rentedBx = new JCheckBox(Messages.uiGet("RunnerPanel.RentedEcardLabel")); //$NON-NLS-1$
		ncBx = new JCheckBox(Messages.uiGet("RunnerPanel.NCLabel")); //$NON-NLS-1$
		
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
				control().validateRegisteredStartTime(runner, regStartF.getText());
				refreshRegistrationPanel();
				return true; // always yield focus
			}
		});
		
		archiveF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				control().validateArchiveId(runner, archiveF.getText());
				refreshRegistrationPanel();
			}
		});
		archiveF.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				control().validateArchiveId(runner, archiveF.getText());
				refreshRegistrationPanel();
				return true; // always yield focus
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
		addRow(regPanel, c, new JLabel(Messages.uiGet("RunnerPanel.RegisteredStartLabel")), regStartF); //$NON-NLS-1$
		c.gridy = 1;
		addRow(regPanel, c, new JLabel(Messages.uiGet("RunnerPanel.ArchiveIdLabel")), archiveF); //$NON-NLS-1$
		c.gridy = 2;
		addRow(regPanel, c, rentedBx, ncBx);
		
		regPanel.setBorder(BorderFactory.createTitledBorder(Messages.uiGet("RunnerPanel.RegistrationTitle"))); //$NON-NLS-1$
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
		raceTimeF.setToolTipText(Messages.uiGet("RunnerPanel.RealRacetimeTooltip1")); //$NON-NLS-1$
		
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
		
		ecardPanel.setBorder(BorderFactory.createTitledBorder(Messages.uiGet("RunnerPanel.EcardTitle"))); //$NON-NLS-1$
		return ecardPanel;
	}
	
	
	public JPanel initResultPanel() {
		mpF = new JTextField(FIELDSIZE);
		mpF.setEditable(false);
		penaltyF = new JTextField(FIELDSIZE);
		penaltyF.setEditable(false);
		fullTimeF = new JTextField(FIELDSIZE);
		fullTimeF.setEditable(false);
		fullTimeF.setToolTipText(Messages.uiGet("RunnerPanel.OfficialTimeTooltip"));		 //$NON-NLS-1$

		
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
				new JLabel(Messages.uiGet("RunnerPanel.OfficialTimeLabel")), //$NON-NLS-1$
				fullTimeF);

//		c.gridy = 2;
//		addRow(resultPanel, c, 
//				new JLabel(Messages.uiGet("RunnerPanel.StartLabel")), //$NON-NLS-1$
//				new JTextField(4),
//				new JLabel(Messages.uiGet("RunnerPanel.FinishLabel")), //$NON-NLS-1$
//				new JTextField(4));

		resultPanel.setBorder(BorderFactory.createTitledBorder(Messages.uiGet("RunnerPanel.ResultTitle"))); //$NON-NLS-1$
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
		geco().splitPrinter().printSingleSplits(runnerData);
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
				new MergeWizard(
						geco(),
						frame(),
						Messages.uiGet("RunnerPanel.MergeCardTitle")) //$NON-NLS-1$
					.showMergeRunner(runnerData.clone());
			}
		});
		return mergeDialogB;
	}

	private JButton createResetTimeButton() {
		JButton resetTimeB = new JButton(GecoIcon.createIcon(GecoIcon.ResetTime));
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
		JButton splitPrintB = new JButton(GecoIcon.createIcon(GecoIcon.SplitPrint));
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
