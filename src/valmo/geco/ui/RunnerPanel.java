/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import valmo.geco.control.RunnerControl;
import valmo.geco.core.Geco;
import valmo.geco.core.TimeManager;
import valmo.geco.core.Util;
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
		this.punchPanel = new PunchPanel(geco, frame);
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
//		eTimeF.setText(TimeManager.fullTime(runnerData.getErasetime()));
//		cTimeF.setText(TimeManager.fullTime(runnerData.getControltime()));
//		sTimeF.setText(TimeManager.fullTime(runnerData.getStarttime()));
//		fTimeF.setText(TimeManager.fullTime(runnerData.getFinishtime()));
		displayRacetime();
		int nbMPs = runnerData.getResult().getNbMPs();
		mpF.setText(Integer.toString(nbMPs));
		penaltyF.setText(TimeManager.time(geco().checker().timePenalty(nbMPs)));
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
		realTimeF.setText(TimeManager.time(geco().checker().computeRealRaceTime(runnerData)));
		if( geco().checker().computeOfficialRaceTime(runnerData) != runnerData.getResult().getRacetime() ) {
			realTimeF.setBackground(new Color(1, 1, 0.5f));
		} else
			realTimeF.setBackground(Color.white);
	}
	
	public void createComponents() {
		rTimeF = new JTextField(5);
		rTimeF.setEditable(false);
		cTimeF = new JTextField(5);
		cTimeF.setEditable(false);
		eTimeF = new JTextField(5);
		eTimeF.setEditable(false);
		sTimeF = new JTextField(5);
		sTimeF.setEditable(false);
		fTimeF = new JTextField(5);
		fTimeF.setEditable(false);
		realTimeF = new JTextField(5);
		realTimeF.setEditable(false);
		mpF = new JTextField();
		mpF.setEditable(false);
		penaltyF = new JTextField(5);
		penaltyF.setEditable(false);
		
		resetRTimeB = new JButton("Reset Time");
		recheckStatusB = new JButton("Refresh Status");
		mergeDialogB = new JButton("Merge...");
	}
	
	public void createListeners() {
		mergeDialogB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( runnerData!=null ) {
					new MergeRunnerDialog(geco(), frame(), "Merge runner").showMergeDialogFor(
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
					displayRacetime(); // update background color
				}
			}
		});
		recheckStatusB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( runnerData!=null && control().resetStatus(runnerData) ){
					parentContainer.refreshSelectionInTable();					
				}
			}
		});
	}
	
	public JPanel initPanel(JPanel panel) {
		panel.setLayout(new BorderLayout());
		panel.add(Util.embed(initRunnerPanel(new JPanel())), BorderLayout.NORTH);
		panel.add(Util.embed(this.punchPanel), BorderLayout.CENTER);
		return panel;
	}

	
	public JPanel initRunnerPanel(JPanel panel) {
		panel.setLayout(new GridLayout(0,2));
//		panel.setBorder(BorderFactory.createTitledBorder("Runner"));

		Component[] comps = new Component[] {
				new JLabel("Read time"),
				rTimeF,
				new JLabel("Erase time"),
				eTimeF,
				new JLabel("Control time"),
				cTimeF,
				new JLabel("Start time"),
				sTimeF,
				new JLabel("Finish time"),
				fTimeF,
				new JLabel("Real race time"),
				realTimeF,
				new JLabel("MPs"),
				mpF,
				new JLabel("Time penalty"),
				penaltyF,
				
//				Box.createHorizontalGlue(),
//				Box.createHorizontalGlue(),

				resetRTimeB,
				recheckStatusB,
				mergeDialogB
		};
		
		for (int i = 0; i < comps.length; i++) {
			panel.add(comps[i]);
		}
		return panel;
	}
	
}
