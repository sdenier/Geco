/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.operations;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.geco.basics.GecoResources;
import net.geco.control.GecoControl;
import net.geco.control.RegistryStats;
import net.geco.control.RegistryStats.StatItem;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.RunnerRaceData;
import net.geco.model.RunnerResult;
import net.geco.model.Status;
import net.geco.ui.basics.GecoIcon;

/**
 * @author Simon Denier
 * @since Apr 4, 2012
 *
 */
public class RefereeLogOperation extends GecoOperation {

	private JCheckBox displayOnlyCB;
	private JTextField logFileF;

	
	public RefereeLogOperation(GecoControl gecoControl) {
		super(gecoControl, OperationCategory.REFEREE);
	}

	@Override
	public String toString() {
		return Messages.uiGet("RefereeLogFunction.RefereeLogTitle"); //$NON-NLS-1$
	}

	@Override
	public void run() {
		LogStream log;
		if( displayOnlyCB.isSelected() ) {
			log = new LogStream();
		} else {
			try {
				log = new FileLogStream(logFileF.getText());
			} catch (IOException e) {
				log = new LogStream();
				geco().log(e.getLocalizedMessage());
			}
		}
		
		RegistryStats stats = getService(RegistryStats.class);
		for (Course course : registry().getCourses()) {
			List<RunnerRaceData> runnerData = registry().getRunnerDataFromCourse(course);
			log.writeLine(Messages.uiGet("RefereeLogFunction.CourseLabel") + course.getName()); //$NON-NLS-1$
			int nbChanges = findAndWriteManualChanges(runnerData, log);
			writeRunners(runnerData, Status.DNF, log);
			writeRunners(runnerData, Status.DSQ, log);
			writeRunners(runnerData, Status.OOT, log);
			writeCourseSummary(course, stats, nbChanges, log);
			log.writeLine(""); //$NON-NLS-1$
		}
		log.close();
	}

	private int findAndWriteManualChanges(List<RunnerRaceData> runnersData, LogStream log) {
		int nbChanges = 0;
		log.writeLine(Messages.uiGet("RefereeLogFunction.RunnersManualModsMessage")); //$NON-NLS-1$
		for (RunnerRaceData officialData : runnersData) {
			if( officialData.statusIsRecheckable() ) {
				RunnerRaceData autoData = officialData.clone();
				geco().checker().check(autoData);
				nbChanges += detectAndWriteManualChange(officialData, autoData, log);
			}
		}
		return nbChanges;
	}

	private int detectAndWriteManualChange(RunnerRaceData officialData, RunnerRaceData autoData, LogStream log) {
		RunnerResult officialResult = officialData.getResult();
		RunnerResult autoResult = autoData.getResult();
		boolean statusChanged = ! officialResult.is(autoResult.getStatus());
		boolean timeChanged = officialResult.getResultTime() != autoResult.getResultTime();
		if( statusChanged || timeChanged ) {
			StringBuilder string = new StringBuilder(officialData.getRunner().idString());
			if( statusChanged ) {
				string.append(", ").append(autoResult.formatStatus() + " -> " + officialResult.formatStatus()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if( timeChanged ) {
				string.append(", ").append(autoResult.formatResultTime() + " -> " + officialResult.formatResultTime()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			log.writeLine(string.toString());	
			return 1;
		}
		return 0;
	}

	private void writeRunners(List<RunnerRaceData> runnerData, Status status, LogStream log) {
		log.writeLine(Messages.uiGet("RefereeLogFunction.RunnersStatutsMessage") + status.toString()); //$NON-NLS-1$
		for (RunnerRaceData data : runnerData) {
			if( data.getStatus() == status ) {
				log.writeLine(data.getRunner().idString());
			}
		}
	}

	private void writeCourseSummary(Course course, RegistryStats stats, int nbChanges, LogStream log) {
		Map<StatItem, Integer> courseStats = stats.getCourseStatsFor(course.getName());
		StringBuilder string = new StringBuilder(Messages.uiGet("RefereeLogFunction.StatisticsMessage")); //$NON-NLS-1$
		appendItemStat(Messages.uiGet("RefereeLogFunction.PresentLabel"), StatItem.Present, courseStats, string).append(", "); //$NON-NLS-1$ //$NON-NLS-2$
		appendItemStat(Messages.uiGet("RefereeLogFunction.OKLabel"), StatItem.OK, courseStats, string).append(", "); //$NON-NLS-1$ //$NON-NLS-2$
		appendItemStat(Messages.uiGet("RefereeLogFunction.MPLabel"), StatItem.MP, courseStats, string).append(", "); //$NON-NLS-1$ //$NON-NLS-2$
		appendItemStat(Messages.uiGet("RefereeLogFunction.DNFLabel"), StatItem.DNF, courseStats, string).append(", "); //$NON-NLS-1$ //$NON-NLS-2$
		appendItemStat(Messages.uiGet("RefereeLogFunction.DSQLabel"), StatItem.DSQ, courseStats, string).append(", "); //$NON-NLS-1$ //$NON-NLS-2$
		appendItemStat(Messages.uiGet("RefereeLogFunction.OOTLabel"), StatItem.OOT, courseStats, string).append(", "); //$NON-NLS-1$ //$NON-NLS-2$
		string.append(Messages.uiGet("RefereeLogFunction.ManualChangesMessage") + nbChanges); //$NON-NLS-1$
		log.writeLine(string.toString());
	}
	
	private StringBuilder appendItemStat(String title, StatItem item, Map<StatItem, Integer> courseStats, StringBuilder string) {
		string.append(title).append(" ").append(courseStats.get(item)); //$NON-NLS-1$
		return string;
	}

	@Override
	public String runTooltip() {
		return Messages.uiGet("RefereeLogFunction.RefereeLogTooltip"); //$NON-NLS-1$
	}

	@Override
	public JComponent buildInnerUI() {
		displayOnlyCB = new JCheckBox(Messages.uiGet("RefereeLogFunction.DisplayOnlyLabel")); //$NON-NLS-1$
		displayOnlyCB.setAlignmentX(Component.LEFT_ALIGNMENT);
		logFileF = new JTextField(15);
		logFileF.setMaximumSize(logFileF.getPreferredSize());
		logFileF.setText(stage().getBaseDir() + Messages.uiGet("RefereeLogFunction.LogFilename")); //$NON-NLS-1$
		final JButton selectLogFileB = new JButton(GecoIcon.createIcon(GecoIcon.OpenSmall));

		final Box fileBox = Box.createHorizontalBox();
		fileBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		fileBox.add(new JLabel(Messages.uiGet("RefereeLogFunction.FilePathLabel"))); //$NON-NLS-1$
		fileBox.add(logFileF);
		fileBox.add(selectLogFileB);

		displayOnlyCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				logFileF.setEnabled(! displayOnlyCB.isSelected());
				selectLogFileB.setEnabled(! displayOnlyCB.isSelected());
			}
		});
		selectLogFileB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser(stage().getBaseDir());
				fileChooser.setDialogTitle(Messages.uiGet("RefereeLogFunction.SelectFileTitle")); //$NON-NLS-1$
				int answer = fileChooser.showDialog(fileBox, Messages.uiGet("RefereeLogFunction.SelectLabel")); //$NON-NLS-1$
				if( answer==JFileChooser.APPROVE_OPTION ) {
					logFileF.setText(fileChooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		
		JLabel help = new JLabel(Messages.uiGet("RefereeLogFunction.HelpLabel")); //$NON-NLS-1$
		help.setAlignmentX(Component.LEFT_ALIGNMENT);

		Box config = Box.createVerticalBox();
		config.add(displayOnlyCB);
		config.add(Box.createVerticalStrut(5));
		config.add(fileBox);
		config.add(Box.createVerticalStrut(5));
		config.add(help);
		embedRunButton(config);
		return config;
	}

	public class LogStream {
		public void writeLine(String string) {
			geco().announcer().log(string, false);
		}

		public void close() {}
	}

	public class FileLogStream extends LogStream {

		private BufferedWriter logFile;

		public FileLogStream(String filePath) throws IOException {
			logFile = GecoResources.getSafeWriterFor(filePath);
		}

		@Override
		public void writeLine(String string) {
			super.writeLine(string);
			try {
				logFile.write(string);
				logFile.newLine();
			} catch (IOException e) {
				geco().debug(e.getLocalizedMessage());
			}
		}

		@Override
		public void close() {
			try {
				logFile.close();
			} catch (IOException e) {
				geco().debug(e.getLocalizedMessage());
			}
		}
	}

}
