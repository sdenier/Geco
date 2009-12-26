/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import valmo.geco.control.RunnerControl;
import valmo.geco.core.Geco;
import valmo.geco.core.TimeManager;
import valmo.geco.core.Util;
import valmo.geco.core.Announcer.StageConfigListener;
import valmo.geco.model.Course;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Jan 24, 2009
 *
 */
public class RunnerPanel extends GecoPanel implements StageConfigListener {
	/*
	 * Dev note: since extracting the RunnerControl from RunnerPanel, there remains some
	 * strong implicit dependencies between both (ie how values are checked, what does true/false
	 * means when validateXXX() returns and when to refresh or not a UI component).
	 * Not so good. 
	 */

	private RunnerRaceData runnerData;
	private Runner runner;
	private RunnersPanel parentContainer;
	private PunchPanel punchPanel;
	
	private JTextField startF;
	private JTextField chipF;
	private JTextField fNameF;
	private JTextField lNameF;
	private JComboBox clubCB;
	private JComboBox catCB;
	private JComboBox courseCB;
	private JCheckBox ncB;
	private JTextField cTimeF;
	private JTextField eTimeF;
	private JTextField sTimeF;
	private JTextField fTimeF;
	private JTextField rTimeF;
	private JLabel realTimeL;
	private JComboBox statusCB;
	private JTextField mpF;
	private JLabel penaltyL;
	private JButton resetRTimeB;
	private JButton recheckStatusB;
	
	/*
	 * Hack: flag set up while refreshing the panel so that listeners on comboboxes
	 * will do nothing when activated by refreshing the combo boxes selected item.
	 */  
	private boolean inRefreshMode;
	private JButton mergeDialogB;

	
	public RunnerPanel(Geco geco, JFrame frame, RunnersPanel parent) {
		super(geco, frame);
		this.parentContainer = parent;
		this.punchPanel = new PunchPanel(geco, frame);
		createComponents();
		createListeners();
		initPanel(this);
		geco().announcer().registerStageConfigListener(this);
	}
	
	private RunnerControl control() {
		return geco().runnerControl();
	}
	
	public void updateRunner(String chipnumber) {
		this.runnerData = registry().findRunnerData(chipnumber);
		this.runner = runnerData.getRunner();
		refreshPanel();
	}
	
	/**
	 * 
	 */
	public void refreshPanel() {
		this.inRefreshMode = true; // happy hack :(
		startF.setText(Integer.toString(runner.getStartnumber()));
		chipF.setText(runner.getChipnumber());
		fNameF.setText(runner.getFirstname());
		lNameF.setText(runner.getLastname());
		clubCB.setSelectedItem(runner.getClub().getName());
		catCB.setSelectedItem(runner.getCategory().getShortname());
		courseCB.setSelectedItem(runner.getCourse().getName());
		ncB.setSelected(runner.isNC());
		eTimeF.setText(TimeManager.fullTime(runnerData.getErasetime()));
		cTimeF.setText(TimeManager.fullTime(runnerData.getControltime()));
		sTimeF.setText(TimeManager.fullTime(runnerData.getStarttime()));
		fTimeF.setText(TimeManager.fullTime(runnerData.getFinishtime()));
		rTimeF.setText(TimeManager.time(runnerData.getResult().getRacetime()));
		realTimeL.setText(TimeManager.time(geco().checker().computeRaceTime(runnerData)));
		statusCB.setSelectedItem(runnerData.getResult().getStatus());
		mpF.setText(Integer.toString(runnerData.getResult().getNbMPs()));
		penaltyL.setText(Integer.toString(0)); // TODO: compute penalty here???
		punchPanel.refreshPunches(runnerData);
		this.inRefreshMode = false;
	}
	
	
	public void createComponents() {
		startF = new JTextField(2);
		chipF = new JTextField(7);
		fNameF = new JTextField(10);
		lNameF = new JTextField(10);
		clubCB = new JComboBox(registry().getSortedClubnames());
		catCB = new JComboBox(registry().getSortedCategorynames());
		courseCB = new JComboBox(registry().getSortedCoursenames());
		ncB = new JCheckBox("NC");
		mergeDialogB = new JButton("Merge...");
		
		cTimeF = new JTextField(5);
		cTimeF.setEditable(false);
		eTimeF = new JTextField(5);
		eTimeF.setEditable(false);
		sTimeF = new JTextField(5);
		sTimeF.setEditable(false);
		fTimeF = new JTextField(5);
		fTimeF.setEditable(false);
		rTimeF = new JTextField(5);
		realTimeL = new JLabel();
		statusCB = new JComboBox(Status.values());
		mpF = new JTextField();
		penaltyL = new JLabel();
		
		resetRTimeB = new JButton("Reset Time");
		recheckStatusB = new JButton("Refresh Status");
	}
	
	public void createListeners() {
		startF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Integer newStart = new Integer(startF.getText());
					if( control().validateStartnumber(runner, newStart)) {
						parentContainer.refreshSelectionInTable();
					} else {
						JOptionPane.showMessageDialog(frame(),
							    "Start number already used. Reverting to previous start number.",
							    "Invalid Entry",
							    JOptionPane.ERROR_MESSAGE);
					}					
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(frame(),
						    "Bad format for start number.",
						    "Invalid Entry",
						    JOptionPane.ERROR_MESSAGE);
				}
				startF.setText(Integer.toString(runner.getStartnumber()));
			}
		});
		chipF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newChip = chipF.getText();
				if( control().validateChipnumber(runner, newChip)) {
					parentContainer.refreshSelectionInTable();
				} else {
					JOptionPane.showMessageDialog(frame(),
						    "Chip number already in use. Reverting to previous chip number.",
						    "Invalid Entry",
						    JOptionPane.ERROR_MESSAGE);
					chipF.setText(runner.getChipnumber());
				}
			}
		});
		fNameF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( control().validateFirstname(runner, fNameF.getText()) ) {
					parentContainer.refreshSelectionInTable();					
				}
			}
		});
		lNameF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newName = lNameF.getText();
				if( control().validateLastname(runner, newName)) {
					parentContainer.refreshSelectionInTable();
				} else {
					JOptionPane.showMessageDialog(frame(),
						    "Last name can not be empty. Reverting.",
						    "Invalid Entry",
						    JOptionPane.ERROR_MESSAGE);
					lNameF.setText(runner.getLastname());
				}
			}
		});
		clubCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( !inRefreshMode ) {
					if( control().validateClub(runner, (String) clubCB.getSelectedItem())) {
						parentContainer.refreshSelectionInTable();						
					}
				}
			}
		});
		catCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( !inRefreshMode ) {
					String newCat = (String) catCB.getSelectedItem();
					if( control().validateCategory(runner, newCat)) {
						parentContainer.refreshSelectionInTable();						
					}
				}
			}
		});
		courseCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( !inRefreshMode ) {
					Course oldCourse = runner.getCourse();
					String newCourse = (String) courseCB.getSelectedItem();
					if( !oldCourse.getName().equals(newCourse) ) {
						int confirm = JOptionPane.showConfirmDialog(frame(), "This will trigger a status check. Please confirm the change of runner's course?", "Update Runner", JOptionPane.YES_NO_OPTION);
						if( confirm==JOptionPane.YES_OPTION ) {
							if( control().validateCourse(runnerData, newCourse)) {
								parentContainer.refreshSelectionInTable();
							}
						}
						refreshPanel(); // refresh panel in any case
					}					
				}
			}
		});
		ncB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				control().validateNCStatus(runner, ncB.isSelected());
			}
		});
		mergeDialogB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new MergeRunnerDialog(geco(), frame());
			}
		});
		rTimeF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( control().validateRaceTime(runnerData, rTimeF.getText()) ){
					parentContainer.refreshSelectionInTable();
				} else {
					showBadTimeDialog();
				}
				// reformat the entry from current data anyway
				rTimeF.setText( TimeManager.time(runnerData.getResult().getRacetime()) );
			}
		});
		resetRTimeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( control().resetRaceTime(runnerData) ){
					// reformat the entry
					rTimeF.setText( TimeManager.time(runnerData.getResult().getRacetime()) );
					parentContainer.refreshSelectionInTable();					
				}
			}
		});
		
		statusCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( !inRefreshMode ) {
					Status newStatus = (Status) statusCB.getSelectedItem();
					if( !newStatus.equals(runnerData.getResult().getStatus()) ) {
						int confirm = JOptionPane.showConfirmDialog(frame(), "Please confirm the change of runner's status", "Update Runner", JOptionPane.YES_NO_OPTION);
						if( confirm==JOptionPane.YES_OPTION ) {
							if( control().validateStatus(runnerData, newStatus)) {
								parentContainer.refreshSelectionInTable();	
							}
						} else {
							refreshPanel(); //reset status CB
						}
					}	
				}
			}
		});
		recheckStatusB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( control().resetStatus(runnerData) ){
					refreshPanel();
					parentContainer.refreshSelectionInTable();					
				}
			}
		});
	}
	
	private void showBadTimeDialog() {
		JOptionPane.showMessageDialog(frame(),
			    "Bad time format. Reverting.",
			    "Invalid Entry",
			    JOptionPane.ERROR_MESSAGE);
	}

	public JPanel initPanel(JPanel panel) {
		panel.setLayout(new BorderLayout());
		panel.add(Util.embed(initRunnerPanel(new JPanel())), BorderLayout.WEST);
		panel.add(Util.embed(this.punchPanel), BorderLayout.EAST);
		return panel;
	}

	
	public JPanel initRunnerPanel(JPanel panel) {
		panel.setLayout(new GridLayout(0,5));
		panel.setBorder(BorderFactory.createTitledBorder("Runner"));

		Component[] comps = new Component[] {
				new JLabel("Startnumber"),
				new JLabel("Chip"),
				new JLabel("First name"),
				new JLabel("Last name"),
				Box.createGlue(),
				startF,
				chipF,
				fNameF,
				lNameF,
				new JButton("Lookup"),
				
				new JLabel("Club"),
				new JLabel("Category"),
				new JLabel("Course"),
				Box.createGlue(),
				mergeDialogB,
				clubCB,
				catCB,
				courseCB,
				ncB,
				new JButton("Guess course"),
				
				Box.createGlue(),
				Box.createGlue(),
				Box.createGlue(),
				Box.createGlue(),
				Box.createGlue(),
				
				new JLabel("Erase time"),
				new JLabel("Control time"),
				new JLabel("Start time"),
				new JLabel("Finish time"),
				new JLabel("Real race time"),
				eTimeF,
				cTimeF,
				sTimeF,
				fTimeF,
				realTimeL,

				recheckStatusB,
				new JLabel("Status"),
				new JLabel("MPs"),
				new JLabel("Time penalty"),
				new JLabel("Race time"),
				resetRTimeB,
				statusCB,
				mpF,
				penaltyL,
				rTimeF,
		};
		
		for (int i = 0; i < comps.length; i++) {
			panel.add(comps[i]);
		}
//		JPanel panel2 = new JPanel(new BorderLayout());
//		panel2.add(panel, BorderLayout.CENTER);
//		JPanel buttonsP = new JPanel();
//		buttonsP.add(new JButton("Lookup"));
//		buttonsP.add(new JButton("Guess course"));
//		buttonsP.add(new JButton("Merge"));
//		panel2.add(buttonsP, BorderLayout.SOUTH);
		return panel;
	}
	
	public JPanel initRunnerPanel2(JPanel panel) {
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Runner"));

		panel.add(new JLabel("Startnumber"), Util.compConstraint(0, 0));
		panel.add(startF, Util.compConstraint(0, 1));
		panel.add(new JLabel("Chip"), Util.compConstraint(1, 0));
		panel.add(chipF, Util.compConstraint(1, 1));
		panel.add(new JLabel("First name"), Util.compConstraint(2, 0));
		panel.add(fNameF, Util.compConstraint(2, 1));
		panel.add(new JLabel("Last name"), Util.compConstraint(3, 0));
		panel.add(lNameF, Util.compConstraint(3, 1));
		panel.add(new JButton("Lookup"), Util.compConstraint(4, 1));
		
		panel.add(new JLabel("Club"), Util.compConstraint(0, 2));
		panel.add(clubCB, Util.compConstraint(0, 3));
		panel.add(new JLabel("Category"), Util.compConstraint(1, 2));
		panel.add(catCB, Util.compConstraint(1, 3));
		panel.add(new JLabel("Course"), Util.compConstraint(2, 2));
		panel.add(courseCB, Util.compConstraint(2, 3));
		panel.add(ncB, Util.compConstraint(3, 3));
		panel.add(new JButton("Guess"), Util.compConstraint(4, 3));

		panel.add(new JLabel("Erase time"), Util.compConstraint(0, 4));
		panel.add(eTimeF, Util.compConstraint(0, 5));
		panel.add(new JLabel("Check time"), Util.compConstraint(1, 4));
		panel.add(cTimeF, Util.compConstraint(1, 5));
		panel.add(new JLabel("Start time"), Util.compConstraint(2, 4));
		panel.add(sTimeF, Util.compConstraint(2, 5));
		panel.add(new JLabel("Finish time"), Util.compConstraint(3, 4));
		panel.add(fTimeF, Util.compConstraint(3, 5));
		panel.add(new JLabel("Real race time"), Util.compConstraint(4, 4));
		panel.add(realTimeL, Util.compConstraint(4, 5));

		panel.add(recheckStatusB, Util.compConstraint(0, 6));
		panel.add(resetRTimeB, Util.compConstraint(0, 7));
		panel.add(new JLabel("Status"), Util.compConstraint(1, 6));
		panel.add(statusCB, Util.compConstraint(1, 7));
		panel.add(new JLabel("MPs"), Util.compConstraint(2, 6));
		panel.add(mpF, Util.compConstraint(2, 7));
		panel.add(new JLabel("Time penalty"), Util.compConstraint(3, 6));
		panel.add(penaltyL, Util.compConstraint(3, 7));
		panel.add(new JLabel("Official race time"), Util.compConstraint(4, 6));
		panel.add(rTimeF, Util.compConstraint(4, 7));

		return panel;
	}

	@Override
	public void categoriesChanged() {
		catCB.setModel(new DefaultComboBoxModel(registry().getSortedCategorynames()));
		refreshPanel();
	}

	@Override
	public void clubsChanged() {
		clubCB.setModel(new DefaultComboBoxModel(registry().getSortedClubnames()));
		refreshPanel();
	}

	@Override
	public void coursesChanged() {
		courseCB.setModel(new DefaultComboBoxModel(registry().getSortedCoursenames()));
		refreshPanel();
	}

}
