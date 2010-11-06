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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import valmo.geco.Geco;
import valmo.geco.control.RunnerControl;
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
		realTimeF.setToolTipText("Real race time, computed as finish - start. "
				+ "Yellow background indicates official time has been edited.");
		mpF = new JTextField();
		mpF.setEditable(false);
		penaltyF = new JTextField(4);
		penaltyF.setEditable(false);
		
		resetRTimeB = new JButton("Reset Time");
		resetRTimeB.setToolTipText("Reset official time to real race time + penalty");
		recheckStatusB = new JButton("Recheck");
		recheckStatusB.setToolTipText("Recheck runner status and reset official time");
		mergeDialogB = new JButton("Merge...");
		mergeDialogB.setToolTipText("Open Merge dialog");
	}
	
	public void createListeners() {
		mergeDialogB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( runnerData!=null ) {
					new MergeRunnerDialog(geco(), frame(), "Merge Card Data").showMergeDialogFor(
														runnerData.clone(), 
														runner.getChipnumber());
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
		
//		ImageIcon splitPrint = new ImageIcon(getClass().getResource("/resources/icons/crystal/filequickprint.png")); //$NON-NLS-1$
//		JButton splitPrintB = new JButton(splitPrint);
//		splitPrintB.setToolTipText("Quickprint runner splits");
//		panel.add(SwingUtils.embed(splitPrintB), BorderLayout.EAST);
//		panel.add(Box.createHorizontalStrut(25), BorderLayout.WEST);
		
		return panel;
	}

	
	public JPanel initRunnerPanel() {
		JPanel dataPanel = new JPanel();
		dataPanel.setLayout(new GridLayout(0,4));

		Component[] comps = new Component[] {
				new JLabel("Erase"),
				eTimeF,
				new JLabel("Control"),
				cTimeF,
				new JLabel("Start"),
				sTimeF,
				new JLabel("Finish"),
				fTimeF,
				new JLabel("Read"),
				rTimeF,
				new JLabel("Race"),
				realTimeF,
				new JLabel("MPs"),
				mpF,
				new JLabel("Penalty"),
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
