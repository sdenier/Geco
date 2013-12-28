/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.config;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterJob;
import java.io.File;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.geco.control.results.RunnerSplitPrinter;
import net.geco.control.results.RunnerSplitPrinter.SplitFormat;
import net.geco.framework.IGecoApp;
import net.geco.model.Messages;
import net.geco.ui.basics.GecoIcon;
import net.geco.ui.basics.SwingUtils;
import net.geco.ui.components.FileSelector;
import net.geco.ui.framework.ConfigPanel;

/**
 * @author Simon Denier
 * @since Sep 10, 2013
 *
 */
public class TemplateConfigPanel extends JPanel implements ConfigPanel {

	public TemplateConfigPanel(final IGecoApp geco, final JFrame frame) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		add(createResultsPanel(geco, frame));
		add(Box.createVerticalStrut(10));
		add(createRunnerSplitPanel(geco, frame));
	}

	public Component createRunnerSplitPanel(final IGecoApp geco, final JFrame frame) {
		JPanel runnerSplitPanel = new JPanel(new GridBagLayout());
		runnerSplitPanel.setBorder(BorderFactory.createTitledBorder(Messages.uiGet("TemplateConfigPanel.RunnerSplitTitle"))); //$NON-NLS-1$
		GridBagConstraints c = SwingUtils.gbConstr();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		final JComboBox printersCB = new JComboBox(geco.splitPrinter().listPrinterNames());
		printersCB.setPreferredSize(new Dimension(170, printersCB.getPreferredSize().height));
		printersCB.setSelectedItem(geco.splitPrinter().getSplitPrinterName());
		printersCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.splitPrinter().setSplitPrinterName((String) printersCB.getSelectedItem());
			}
		});
		final JCheckBox prototypeFormatB = new JCheckBox(Messages.uiGet("SIReaderConfigPanel.PrototypingLabel")); //$NON-NLS-1$
		prototypeFormatB.setToolTipText(Messages.uiGet("SIReaderConfigPanel.PrototypingTooltip")); //$NON-NLS-1$
		prototypeFormatB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean prototyping = prototypeFormatB.isSelected();
				printersCB.setEnabled(! prototyping);
				geco.splitPrinter().enableFormatPrototyping(prototyping);
			}
		});
		setGridBagConstraints(c, 2, 10);
		runnerSplitPanel.add(new JLabel(Messages.uiGet("SIReaderConfigPanel.SplitPrinterLabel")), c); //$NON-NLS-1$
		runnerSplitPanel.add(printersCB, c);
		c.gridwidth = 3;
		runnerSplitPanel.add(prototypeFormatB, c);
		c.gridwidth = 1;

		final JButton printSetupB = new JButton(GecoIcon.createIcon(GecoIcon.SplitSetup));
		printSetupB.setToolTipText(Messages.uiGet("TemplateConfigPanel.SplitFormatTooltip")); //$NON-NLS-1$
		printSetupB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PrintRequestAttributeSet attributes = geco.splitPrinter().getPrintRequestAttributeSet();
				PrinterJob printerJob = PrinterJob.getPrinterJob();
				printerJob.pageDialog(attributes);
			}
		});
		final JComboBox splitFormatCB = new JComboBox(RunnerSplitPrinter.SplitFormat.values());
		splitFormatCB.setPreferredSize(new Dimension(170, splitFormatCB.getPreferredSize().height));
		splitFormatCB.setSelectedItem(geco.splitPrinter().getSplitFormat());
		printSetupB.setEnabled(geco.splitPrinter().getSplitFormat() == SplitFormat.MultiColumns);
		splitFormatCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final SplitFormat selectedFormat = (SplitFormat) splitFormatCB.getSelectedItem();
				printSetupB.setEnabled(selectedFormat == SplitFormat.MultiColumns);
				geco.splitPrinter().setSplitFormat(selectedFormat);
			}
		});
		setGridBagConstraints(c, 3, 0);
		runnerSplitPanel.add(new JLabel(Messages.uiGet("SIReaderConfigPanel.SplitFormatLabel")), c); //$NON-NLS-1$
		runnerSplitPanel.add(splitFormatCB, c);
		runnerSplitPanel.add(printSetupB, c);

		FileSelector columnsTemplateFS = new FileSelector(geco, frame,
				  Messages.uiGet("SIReaderConfigPanel.ColumnTemplateTitle"), //$NON-NLS-1$
				  GecoIcon.OpenSmall) {
			public File currentFile() {
				return geco.splitPrinter().getColumnTemplate();
			}
			public void fileChosen(File selectedFile) {
				geco.splitPrinter().setColumnTemplate(selectedFile);
			}
		};
		final JSpinner nbColumnsS = new JSpinner(new SpinnerNumberModel(geco.splitPrinter().nbColumns(), 1, null, 1));
		nbColumnsS.setPreferredSize(new Dimension(50, SwingUtils.SPINNERHEIGHT));
		nbColumnsS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int newNb = ((Integer) nbColumnsS.getValue()).intValue();
				geco.splitPrinter().setNbColumns(newNb);
			}
		});
		setGridBagConstraints(c, 4, 0);
		runnerSplitPanel.add(new JLabel(Messages.uiGet("SIReaderConfigPanel.ColumnTemplateLabel")), c); //$NON-NLS-1$
		runnerSplitPanel.add(columnsTemplateFS.getFilenameField(), c);
		runnerSplitPanel.add(columnsTemplateFS.getSelectFileButton(), c);
		runnerSplitPanel.add(nbColumnsS, c);
		runnerSplitPanel.add(new JLabel(Messages.uiGet("StageConfigPanel.ColumnsLabel")), c); //$NON-NLS-1$

		FileSelector ticketTemplateFS = new FileSelector(geco, frame,
				 Messages.uiGet("SIReaderConfigPanel.TicketTemplateTitle"), //$NON-NLS-1$
				 GecoIcon.OpenSmall) {
			public File currentFile() {
				return geco.splitPrinter().getTicketTemplate();
			}
			public void fileChosen(File selectedFile) {
				geco.splitPrinter().setTicketTemplate(selectedFile);
			}
		};
		setGridBagConstraints(c, 5, 0);
		runnerSplitPanel.add(new JLabel(Messages.uiGet("SIReaderConfigPanel.TicketTemplateLabel")), c); //$NON-NLS-1$
		runnerSplitPanel.add(ticketTemplateFS.getFilenameField(), c);
		runnerSplitPanel.add(ticketTemplateFS.getSelectFileButton(), c);
		
		return runnerSplitPanel;
	}

	public Component createResultsPanel(final IGecoApp geco, final JFrame frame) {
		JPanel resultsPanel = new JPanel(new GridBagLayout());
		resultsPanel.setBorder(BorderFactory.createTitledBorder(Messages.uiGet("TemplateConfigPanel.ResultsTitle"))); //$NON-NLS-1$
		GridBagConstraints c = SwingUtils.gbConstr();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		FileSelector rankingTemplateFS = new FileSelector(geco, frame,
				Messages.uiGet("StageConfigPanel.RankingTemplateTitle"), //$NON-NLS-1$
				GecoIcon.OpenSmall) {
			public File currentFile() {
				return geco.resultExporter().getRankingTemplate();
			}
			public void fileChosen(File selectedFile) {
				geco.resultExporter().setRankingTemplate(selectedFile);
			}
		};
		resultsPanel.add(new JLabel(Messages.uiGet("StageConfigPanel.RankingTemplateLabel")), c); //$NON-NLS-1$
		resultsPanel.add(rankingTemplateFS.getFilenameField(), c);
		resultsPanel.add(rankingTemplateFS.getSelectFileButton(), c);

		FileSelector splitsTemplateFS = new FileSelector(geco, frame,
				Messages.uiGet("StageConfigPanel.SplitsTemplateTitle"), //$NON-NLS-1$
				GecoIcon.OpenSmall) {
			public File currentFile() {
				return geco.splitsExporter().getSplitsTemplate();
			}
			public void fileChosen(File selectedFile) {
				geco.splitsExporter().setSplitsTemplate(selectedFile);
			}
		};
		final JSpinner nbColumnsS = new JSpinner(new SpinnerNumberModel(geco.splitsExporter().nbColumns(), 1, null, 1));
		nbColumnsS.setPreferredSize(new Dimension(50, SwingUtils.SPINNERHEIGHT));
		nbColumnsS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int newNb = ((Integer) nbColumnsS.getValue()).intValue();
				geco.splitsExporter().setNbColumns(newNb);
			}
		});
		setGridBagConstraints(c, 1, 0);
		resultsPanel.add(new JLabel(Messages.uiGet("StageConfigPanel.SplitsTemplateLabel")), c); //$NON-NLS-1$
		resultsPanel.add(splitsTemplateFS.getFilenameField(), c);
		resultsPanel.add(splitsTemplateFS.getSelectFileButton(), c);
		resultsPanel.add(nbColumnsS, c);
		resultsPanel.add(new JLabel(Messages.uiGet("StageConfigPanel.ColumnsLabel")), c); //$NON-NLS-1$

		FileSelector customTemplateFS = new FileSelector(geco, frame,
				Messages.uiGet("TemplateConfigPanel.CustomTemplateTitle"), //$NON-NLS-1$
				GecoIcon.OpenSmall) {
			public File currentFile() {
				return geco.splitsExporter().getCustomTemplate();
			}
			public void fileChosen(File selectedFile) {
				geco.splitsExporter().setCustomTemplate(selectedFile);
			}
		};
		setGridBagConstraints(c, 2, 0);
		resultsPanel.add(new JLabel(Messages.uiGet("TemplateConfigPanel.CustomTemplateLabel")), c); //$NON-NLS-1$
		resultsPanel.add(customTemplateFS.getFilenameField(), c);
		resultsPanel.add(customTemplateFS.getSelectFileButton(), c);
		
		return resultsPanel;
	}

	private void setGridBagConstraints(GridBagConstraints constraints, int gridY, int paddingTop) {
		constraints.gridy = gridY;
		constraints.insets = new Insets(paddingTop, 0, 5, 5);
	}

	@Override
	public String getLabel() {
		return Messages.uiGet("TemplateConfigPanel.TemplatesTitle"); //$NON-NLS-1$
	}

	@Override
	public Component build() {
		return this;
	}

}
