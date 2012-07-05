/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.functions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.geco.basics.Html;
import net.geco.control.GecoControl;
import net.geco.control.functions.StationLogChecker;
import net.geco.model.iocsv.CsvReader;
import net.geco.ui.basics.GecoIcon;


/**
 * @author Simon Denier
 * @since Nov 19, 2010
 *
 */
public class StationLogFunction extends GecoFunction {

	private DefaultListModel logFiles;
	private JCheckBox simulateB;
	private JCheckBox autoInsertB;
	private JCheckBox setDnsB;

	public StationLogFunction(GecoControl gecoControl) {
		super(gecoControl, FunctionCategory.STAGE);
	}

	@Override
	public String toString() {
		return "Check Station Log";
	}

	@Override
	public String executeTooltip() {
		return "Check e-cards from stations log and update status of found and not found e-cards";
	}
	
	@Override
	public void execute() {
		boolean simulationMode = simulateB.isSelected();
		if( simulationMode ) {
			geco().announcer().dataInfo("SIMULATION BEGINS");
		}
		StationLogChecker func = new StationLogChecker(geco(), simulationMode);
		Set<String> ecards = readEcardsFromFiles();
		func.checkECards(ecards, autoInsertB.isSelected());
		if( setDnsB.isSelected() ){
			func.markNotStartedEntriesAsDNS(ecards);
		}
		if( simulationMode ) {
			geco().announcer().dataInfo("SIMULATION ENDS HERE");
		}
	}

	private Set<String> readEcardsFromFiles() {
		HashSet<String> ecards = new HashSet<String>();
		@SuppressWarnings("unchecked")
		Enumeration<File> logs = (Enumeration<File>) logFiles.elements();
		while (logs.hasMoreElements()) {
			File file = (File) logs.nextElement();
			try {
				CsvReader reader = new CsvReader(";", file.getAbsolutePath());
				retrieveEcardsFromFile(ecards, reader);
			} catch (IOException e) {
				geco().info(e.getLocalizedMessage(), true);
			}
		}
		return ecards;
	}

	private void retrieveEcardsFromFile(HashSet<String> ecards, CsvReader reader) throws IOException {
		String[] record = reader.readRecord(); // bypass headers No;SI_card;Wd;Punch;
		record = reader.readRecord();
		while( record!=null ){
			try {
				ecards.add(record[1]);
				record = reader.readRecord();
			} catch(IndexOutOfBoundsException e) {
				geco().info("Wrong record " + record, true);
			}
		}
	}

	@Override
	public JComponent getParametersConfig() {
		logFiles = new DefaultListModel();
		JList logFilesL = new JList(logFiles);
		logFilesL.setToolTipText("Select log files using the file button on right");
		logFilesL.setEnabled(false);
		JScrollPane fileScroll = new JScrollPane(logFilesL);
		fileScroll.setPreferredSize(new Dimension(200, 50));
		fileScroll.setMaximumSize(fileScroll.getPreferredSize());

		JButton selectB = new JButton(GecoIcon.createIcon(GecoIcon.SelectFiles));
		selectB.setToolTipText("Select log files");
		selectB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(stage().getBaseDir());
				chooser.setDialogTitle("Select CSV log files from stations backup memory");
				chooser.setMultiSelectionEnabled(true);
				int answer = chooser.showDialog(null, "Select");
				if( answer == JFileChooser.APPROVE_OPTION ){
					for (File file : chooser.getSelectedFiles()) {
						logFiles.addElement(file);
					}
				}
			}
		});
		JButton resetB = new JButton(GecoIcon.createIcon(GecoIcon.Reset));
		resetB.setToolTipText("Reset the list of log files");
		resetB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				logFiles.removeAllElements();
			}
		});
		JButton infoB = new JButton(GecoIcon.createIcon(GecoIcon.Help));
		infoB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showInformationMessage();
			}
		});

		Box fileButtons = Box.createVerticalBox();
		fileButtons.add(selectB);
		fileButtons.add(resetB);
		fileButtons.add(infoB);
		
		JPanel filePanel = new JPanel(new BorderLayout());
		filePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
		filePanel.add(fileScroll, BorderLayout.CENTER);
		filePanel.add(fileButtons, BorderLayout.EAST);
		
		simulateB = new JCheckBox("Simulate");
		simulateB.setToolTipText("Show what would be done but don't actually change statuses or insert new entry");
		autoInsertB = new JCheckBox("Insert unregistered e-cards, with archive lookup");
		autoInsertB.setToolTipText("If an unknown e-card is detected in station memory, add a new entry with e-card."
								 + " Function will also fill in data from archive if available");
		setDnsB = new JCheckBox(Html.htmlTag("b", "Mark remaining Not Started as DNS"));
		setDnsB.setToolTipText("Mark remaining Not Started entries as DNS."
							+ " Check this option only if all log files have been checked in");

		Box optionsBox = Box.createVerticalBox();
		optionsBox.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
		optionsBox.add(simulateB);
		optionsBox.add(autoInsertB);
		optionsBox.add(setDnsB);

		JPanel configPanel = new JPanel(new BorderLayout());
		configPanel.add(filePanel, BorderLayout.CENTER);
		configPanel.add(optionsBox, BorderLayout.EAST);
		return configPanel;
	}

	private void showInformationMessage() {
		Html help = new Html();
		help.open("ul");
		help.tag("li", "registered runners which are still running (started but not yet arrived)");
		help.tag("li", "registered runners which did not start (check option \"Mark...\")");
		help.tag("li", "unregistered e-cards (check option \"Insert...\")");
		help.close("ul");
		JLabel listL = new JLabel(help.close());

		Object[] message = new Object[] {
				"Select log files from Start, Check, or Clear stations. ",
				"This function reads ecards from logs to detect:",
				listL,
				new JLabel(Html.htmlTag("font", "color=\"red\"",
						"Import log files only if station memories have been erased before the stage.")),
				new JLabel(Html.htmlTag("font", "color=\"red\"",
						"Do not check the \"Mark DNS\" option until all logs have been checked in."))
		};
		JOptionPane.showMessageDialog(null,
									  message,
									  "About Checking Station Log",
									  JOptionPane.INFORMATION_MESSAGE);
	}

}
