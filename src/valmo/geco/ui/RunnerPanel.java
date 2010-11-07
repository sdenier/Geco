/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
	private PunchPanel punchPanel;
	
	private JTextField rTimeF;
	private JTextField cTimeF;
	private JTextField eTimeF;
	private JTextField sTimeF;
	private JTextField fTimeF;
	private JTextField realTimeF;
	private JTextField mpF;
	private JTextField penaltyF;
	private JButton resetRTimeB;
	private JButton recheckStatusB;
	
	private JButton mergeDialogB;

	
	public RunnerPanel(Geco geco, JFrame frame, RunnersPanel parent) {
		super(geco, frame);
		this.parentContainer = parent;
		this.punchPanel = new PunchPanel();
		createComponents();
		createListeners();
		initPanel(this);
	}
	
	private RunnerControl control() {
		return geco().runnerControl();
	}
	
	public void updateRunner(String chipnumber) {
		this.runnerData = registry().findRunnerData(chipnumber);
		this.runner = runnerData.getRunner();
		refreshPanel();
	}
	
	public void refreshPanel() {
		rTimeF.setText(TimeManager.fullTime(runnerData.getReadtime()));
		displayTime(eTimeF, runnerData.getErasetime());
		displayTime(cTimeF, runnerData.getControltime());
		displayTime(sTimeF, runnerData.getStarttime());
		displayTime(fTimeF, runnerData.getFinishtime());
		displayRacetime();
		mpF.setText(Integer.toString(runnerData.getResult().getNbMPs()));
		penaltyF.setText(runnerData.getResult().formatTimePenalty());
		punchPanel.refreshPunches(runnerData);
	}
	
	private void displayTime(JTextField timeF, Date time) {
		timeF.setText(TimeManager.fullTime(time));
		if( time.equals(TimeManager.NO_TIME) ) {
			timeF.setBackground(new Color(1.0f, 0.9f, 0.9f));
		} else {
			timeF.setBackground(Color.white);
		}
	}

	private void displayRacetime() {
		realTimeF.setText(TimeManager.time(runnerData.realRaceTime()));
		if( runnerData.realRaceTime() + runnerData.getResult().getTimePenalty()
				!= runnerData.getResult().getRacetime() ) {
			realTimeF.setBackground(new Color(1, 1, 0.5f));
		} else
			realTimeF.setBackground(Color.white);
	}
	
	public void createComponents() {
		eTimeF = new JTextField(4);
		eTimeF.setEditable(false);
		cTimeF = new JTextField(4);
		cTimeF.setEditable(false);
		sTimeF = new JTextField(4);
		sTimeF.setEditable(false);
		fTimeF = new JTextField(4);
		fTimeF.setEditable(false);
		rTimeF = new JTextField(4);
		rTimeF.setEditable(false);
		realTimeF = new JTextField(4);
		realTimeF.setEditable(false);
		realTimeF.setToolTipText(Messages.uiGet("RunnerPanel.RealRacetimeTooltip1") //$NON-NLS-1$
				+ Messages.uiGet("RunnerPanel.RealRacetimeTooltip2")); //$NON-NLS-1$
		mpF = new JTextField();
		mpF.setEditable(false);
		penaltyF = new JTextField(4);
		penaltyF.setEditable(false);
		
		resetRTimeB = new JButton(Messages.uiGet("RunnerPanel.ResetTimeLabel")); //$NON-NLS-1$
		resetRTimeB.setToolTipText(Messages.uiGet("RunnerPanel.ResetTimeTooltip")); //$NON-NLS-1$
		recheckStatusB = new JButton(Messages.uiGet("RunnerPanel.RecheckLabel")); //$NON-NLS-1$
		recheckStatusB.setToolTipText(Messages.uiGet("RunnerPanel.RecheckTooltip")); //$NON-NLS-1$
		mergeDialogB = new JButton(Messages.uiGet("RunnerPanel.MergeLabel")); //$NON-NLS-1$
		mergeDialogB.setToolTipText(Messages.uiGet("RunnerPanel.MergeTooltip")); //$NON-NLS-1$
	}
	
	public void createListeners() {
		mergeDialogB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( runnerData!=null ) {
					new MergeRunnerDialog(
							geco(),
							frame(),
							Messages.uiGet("RunnerPanel.MergeCardTitle")) //$NON-NLS-1$
						.showMergeDialogFor(runnerData.clone(), runner.getChipnumber());
				}
			}
		});
		resetRTimeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( runnerData!=null && control().resetRaceTime(runnerData) ){
					parentContainer.refreshSelectionInTable();
					refreshPanel();
				}
			}
		});
		recheckStatusB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				recheckRunnerStatus();
			}
		});
	}

	public void recheckRunnerStatus() {
		if( runnerData!=null && control().recheckRunner(runnerData) ){
			parentContainer.refreshSelectionInTable();
			refreshPanel();
		}
	}
	
	public JPanel initPanel(JPanel panel) {
		panel.setLayout(new BorderLayout());
		panel.add(SwingUtils.embed(initRunnerPanel()), BorderLayout.NORTH);
		panel.add(SwingUtils.embed(this.punchPanel), BorderLayout.CENTER);
		panel.add(SwingUtils.embed(initQuickPrintButton()), BorderLayout.EAST);
		panel.add(Box.createHorizontalStrut(25), BorderLayout.WEST); // balance layout
		return panel;
	}

	public JButton initQuickPrintButton() {
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

	public void printRunnerSplits() {
		geco().splitsBuilder().printSingleSplits(runnerData);
	}

	
	public JPanel initRunnerPanel() {
		JPanel dataPanel = new JPanel();
		dataPanel.setLayout(new GridLayout(0,4));

		Component[] comps = new Component[] {
				new JLabel(Messages.uiGet("RunnerPanel.EraseLabel")), //$NON-NLS-1$
				eTimeF,
				new JLabel(Messages.uiGet("RunnerPanel.ControlLabel")), //$NON-NLS-1$
				cTimeF,
				new JLabel(Messages.uiGet("RunnerPanel.StartLabel")), //$NON-NLS-1$
				sTimeF,
				new JLabel(Messages.uiGet("RunnerPanel.FinishLabel")), //$NON-NLS-1$
				fTimeF,
				new JLabel(Messages.uiGet("RunnerPanel.ReadLabel")), //$NON-NLS-1$
				rTimeF,
				new JLabel(Messages.uiGet("RunnerPanel.RaceLabel")), //$NON-NLS-1$
				realTimeF,
				new JLabel(Messages.uiGet("RunnerPanel.MPLabel")), //$NON-NLS-1$
				mpF,
				new JLabel(Messages.uiGet("RunnerPanel.PenaltyLabel")), //$NON-NLS-1$
				penaltyF,
		};
		for (int i = 0; i < comps.length; i++) {
			dataPanel.add(comps[i]);
		}
		
		JPanel runnerPanel = new JPanel(new BorderLayout());
		runnerPanel.add(dataPanel, BorderLayout.NORTH);
		runnerPanel.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
		runnerPanel.add(
				SwingUtils.makeButtonBar(FlowLayout.CENTER, resetRTimeB, recheckStatusB, mergeDialogB),
				BorderLayout.SOUTH);
		return runnerPanel;
	}
	
}
