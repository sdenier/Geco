/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.operations;

import static net.geco.basics.Util.safeTrimQuotes;

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

import net.geco.basics.CsvReader;
import net.geco.basics.Html;
import net.geco.control.GecoControl;
import net.geco.control.operations.StationLogChecker;
import net.geco.model.Messages;
import net.geco.ui.basics.GecoIcon;

/**
 * @author Simon Denier
 * @since Nov 19, 2010
 *
 */
public class StationLogOperation extends GecoOperation {

	private DefaultListModel logFiles;
	private JCheckBox simulateB;
	private JCheckBox autoInsertB;
	private JCheckBox setDnsB;

	public StationLogOperation(GecoControl gecoControl) {
		super(gecoControl, OperationCategory.STAGE);
	}

	@Override
	public String toString() {
		return Messages.uiGet("StationLogFunction.CheckStationLogTitle"); //$NON-NLS-1$
	}

	@Override
	public String runTooltip() {
		return Messages.uiGet("StationLogFunction.CheckStationLogTooltip"); //$NON-NLS-1$
	}
	
	@Override
	public void run() {
		boolean simulationMode = simulateB.isSelected();
		if( simulationMode ) {
			geco().announcer().dataInfo(Messages.uiGet("StationLogFunction.StartSimMessage")); //$NON-NLS-1$
		}
		StationLogChecker func = new StationLogChecker(geco(), simulationMode);
		Set<String> ecards = readEcardsFromFiles();
		func.checkECards(ecards, autoInsertB.isSelected());
		if( setDnsB.isSelected() ){
			func.markNotStartedEntriesAsDNS(ecards);
		}
		if( simulationMode ) {
			geco().announcer().dataInfo(Messages.uiGet("StationLogFunction.EndSimMessage")); //$NON-NLS-1$
		}
	}

	private Set<String> readEcardsFromFiles() {
		HashSet<String> ecards = new HashSet<String>();
		@SuppressWarnings("unchecked")
		Enumeration<File> logs = (Enumeration<File>) logFiles.elements();
		while (logs.hasMoreElements()) {
			File file = (File) logs.nextElement();
			try {
				CsvReader reader = new CsvReader(";", file.getAbsolutePath()); //$NON-NLS-1$
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
				ecards.add(safeTrimQuotes(record[1]));
			} catch(Exception e) {
				geco().info(Messages.uiGet("StationLogFunction.WrongRecordMessage") + record, true); //$NON-NLS-1$
			}
			record = reader.readRecord();
		}
	}

	@Override
	public JComponent buildInnerUI() {
		logFiles = new DefaultListModel();
		JList logFilesL = new JList(logFiles);
		logFilesL.setToolTipText(Messages.uiGet("StationLogFunction.SelectLogFilesHelp")); //$NON-NLS-1$
		logFilesL.setEnabled(false);
		JScrollPane fileScroll = new JScrollPane(logFilesL);
		fileScroll.setPreferredSize(new Dimension(200, 50));
		fileScroll.setMaximumSize(fileScroll.getPreferredSize());

		JButton selectB = new JButton(GecoIcon.createIcon(GecoIcon.OpenSmall));
		selectB.setToolTipText(Messages.uiGet("StationLogFunction.SelectLogFilesTooltip")); //$NON-NLS-1$
		selectB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(stage().getBaseDir());
				chooser.setDialogTitle(Messages.uiGet("StationLogFunction.SelectLogFilesTitle")); //$NON-NLS-1$
				chooser.setMultiSelectionEnabled(true);
				int answer = chooser.showDialog(null, Messages.uiGet("StationLogFunction.SelectLabel")); //$NON-NLS-1$
				if( answer == JFileChooser.APPROVE_OPTION ){
					for (File file : chooser.getSelectedFiles()) {
						logFiles.addElement(file);
					}
				}
			}
		});
		JButton resetB = new JButton(GecoIcon.createIcon(GecoIcon.Reset));
		resetB.setToolTipText(Messages.uiGet("StationLogFunction.ResetLogFilesTooltip")); //$NON-NLS-1$
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
		
		simulateB = new JCheckBox(Messages.uiGet("StationLogFunction.SimulateLabel")); //$NON-NLS-1$
		simulateB.setToolTipText(Messages.uiGet("StationLogFunction.SimulateTooltip")); //$NON-NLS-1$
		autoInsertB = new JCheckBox(Messages.uiGet("StationLogFunction.ArchiveLookupLabel")); //$NON-NLS-1$
		autoInsertB.setToolTipText(Messages.uiGet("StationLogFunction.ArchiveLookupTooltip")); //$NON-NLS-1$
		setDnsB = new JCheckBox(Html.htmlTag("b", Messages.uiGet("StationLogFunction.MarkDNSLabel"))); //$NON-NLS-1$ //$NON-NLS-2$
		setDnsB.setToolTipText(Messages.uiGet("StationLogFunction.MarkDNSTooltip")); //$NON-NLS-1$

		Box optionsBox = Box.createVerticalBox();
		optionsBox.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
		optionsBox.add(simulateB);
		optionsBox.add(autoInsertB);
		optionsBox.add(setDnsB);
		embedRunButton(optionsBox);

		JPanel configPanel = new JPanel(new BorderLayout());
		configPanel.add(filePanel, BorderLayout.CENTER);
		configPanel.add(optionsBox, BorderLayout.EAST);
		return configPanel;
	}

	private void showInformationMessage() {
		Html help = new Html();
		help.open("ul"); //$NON-NLS-1$
		help.tag("li", Messages.uiGet("StationLogFunction.AboutItem1")); //$NON-NLS-1$ //$NON-NLS-2$
		help.tag("li", Messages.uiGet("StationLogFunction.AboutItem2")); //$NON-NLS-1$ //$NON-NLS-2$
		help.tag("li", Messages.uiGet("StationLogFunction.AboutItem3")); //$NON-NLS-1$ //$NON-NLS-2$
		help.close("ul"); //$NON-NLS-1$
		JLabel listL = new JLabel(help.close());

		Object[] message = new Object[] {
				Messages.uiGet("StationLogFunction.AboutLine1"), //$NON-NLS-1$
				Messages.uiGet("StationLogFunction.AboutLine2"), //$NON-NLS-1$
				listL,
				new JLabel(Html.htmlTag("font", "color=\"red\"", //$NON-NLS-1$ //$NON-NLS-2$
						Messages.uiGet("StationLogFunction.AboutWarning1"))), //$NON-NLS-1$
				new JLabel(Html.htmlTag("font", "color=\"red\"", //$NON-NLS-1$ //$NON-NLS-2$
						Messages.uiGet("StationLogFunction.AboutWarning2"))) //$NON-NLS-1$
		};
		JOptionPane.showMessageDialog(null,
									  message,
									  Messages.uiGet("StationLogFunction.AboutTitle"), //$NON-NLS-1$
									  JOptionPane.INFORMATION_MESSAGE);
	}

}
