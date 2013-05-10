/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.config;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.geco.basics.Html;
import net.geco.framework.IGecoApp;
import net.geco.model.Messages;
import net.geco.ui.basics.GecoIcon;
import net.geco.ui.basics.SwingUtils;
import net.geco.ui.components.FileSelector;
import net.geco.ui.framework.ConfigPanel;

/**
 * @author Simon Denier
 * @since May 24, 2011
 *
 */
public class StageConfigPanel extends JPanel implements ConfigPanel {

	@Override
	public String getLabel() {
		return Messages.uiGet("StageConfigPanel.Title"); //$NON-NLS-1$
	}

	public StageConfigPanel(final IGecoApp geco, final JFrame frame) {
		setLayout(new GridBagLayout());

		final JTextField stagenameF = new JTextField(geco.stage().getName());
		stagenameF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				validateStagename(stagenameF, geco, frame);
			}
		});
		stagenameF.setInputVerifier(new InputVerifier() {
			@Override public boolean verify(JComponent input) { return true; }
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				return validateStagename(stagenameF, geco, frame);
			}
		});
		GridBagConstraints c = SwingUtils.gbConstr();
		c.fill = GridBagConstraints.HORIZONTAL;
		setGridBagConstraints(c, 0, 0);
		add(new JLabel(Messages.uiGet("StageConfigPanel.StageNameLabel")), c); //$NON-NLS-1$
		add(stagenameF, c);

		final SimpleDateFormat formatter = new SimpleDateFormat("H:mm"); //$NON-NLS-1$
		formatter.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
		final JTextField zerohourF = new JTextField(formatter.format(geco.stage().getZeroHour()));
		zerohourF.setToolTipText(Messages.uiGet("StageConfigPanel.ZeroHourTooltip")); //$NON-NLS-1$
		zerohourF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				validateZeroHour(formatter, zerohourF, geco);
			}
		});
		zerohourF.setInputVerifier(new InputVerifier() {
			@Override public boolean verify(JComponent input) { return true; }
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				return validateZeroHour(formatter, zerohourF, geco);
			}
		});
		setGridBagConstraints(c, 1, 0);
		add(new JLabel(Messages.uiGet("StageConfigPanel.ZeroHourLabel")), c); //$NON-NLS-1$
		add(zerohourF, c);
		
		FileSelector archiveFS = new FileSelector(geco, frame,
												Messages.uiGet("ArchiveViewer.SelectArchiveLabel"), //$NON-NLS-1$
												GecoIcon.OpenArchive) {
			@Override
			public String filenameValue() {
				return geco.archiveManager().getArchiveName();
			}
			@Override
			public void fileChosen(File selectedFile) {
				geco.archiveManager().setArchiveFile(selectedFile);
			}
		};
		setGridBagConstraints(c, 2, 10);
		add(new JLabel(Messages.uiGet("StageConfigPanel.RunnerArchiveLabel")), c); //$NON-NLS-1$
		add(archiveFS.getFilenameField(), c);
		add(archiveFS.getSelectFileButton(), c);

		FileSelector cnScoreFS = new FileSelector(geco, frame,
												Messages.uiGet("StageConfigPanel.SelectCNfileLabel"), //$NON-NLS-1$
												GecoIcon.OpenArchive) {
			@Override
			public String filenameValue() {
				return geco.cnCalculator().getCnFile().getName();
			}
			@Override
			public void fileChosen(File selectedFile) {
				geco.cnCalculator().setCnFile(selectedFile);
			}
		};
		setGridBagConstraints(c, 3, 0);
		add(new JLabel(Messages.uiGet("StageConfigPanel.CNbaseLabel")), c); //$NON-NLS-1$
		add(cnScoreFS.getFilenameField(), c);
		add(cnScoreFS.getSelectFileButton(), c);

		FileSelector rankingTemplateFS = new FileSelector(geco, frame,
														"Select Mustache template for ranking results",
														GecoIcon.OpenSmall) {
			public String filenameValue() {
				return geco.resultExporter().getRankingTemplate().getName();
			}
			public void fileChosen(File selectedFile) {
				geco.resultExporter().setRankingTemplate(selectedFile);
			}
		};
		setGridBagConstraints(c, 4, 10);
		add(new JLabel("Ranking Template:"), c);
		add(rankingTemplateFS.getFilenameField(), c);
		add(rankingTemplateFS.getSelectFileButton(), c);
		
		FileSelector splitsTemplateFS = new FileSelector(geco, frame,
														"Select Mustache template for splits results",
														GecoIcon.OpenSmall) {
			public String filenameValue() {
				return null;
//				return geco.splitsExporter().getSplitsTemplate().getName();
			}
			public void fileChosen(File selectedFile) {
//				geco.splitsExporter().setSplitsTemplate(selectedFile);
			}
		};
		setGridBagConstraints(c, 5, 0);
		add(new JLabel("Splits Template:"), c);
		add(splitsTemplateFS.getFilenameField(), c);
		add(splitsTemplateFS.getSelectFileButton(), c);
		
		JLabel appNameL = new JLabel(Html.htmlTag("strong", geco.getAppName())); //$NON-NLS-1$
		setGridBagConstraints(c, 6, 20);
		add(new JLabel(Messages.uiGet("StageConfigPanel.ConfigurationLabel")), c); //$NON-NLS-1$
		add(appNameL, c);
		
		JTextField dataPathL = new JTextField(geco.stage().getBaseDir());
		dataPathL.setColumns(20);
		dataPathL.setEditable(false);
		setGridBagConstraints(c, 7, 0);
		add(new JLabel(Messages.uiGet("StageConfigPanel.DataPathLabel")), c); //$NON-NLS-1$
		add(dataPathL, c);		
	}
	
	private void setGridBagConstraints(GridBagConstraints constraints, int gridY, int paddingTop) {
		constraints.gridy = gridY;
		constraints.insets = new Insets(paddingTop, 0, 5, 5);
	}
	
	private boolean verifyStagename(String text) {
		return ! text.trim().isEmpty();
	}

	private boolean validateStagename(JTextField stagenameF, IGecoApp geco, JFrame frame) {
		if( verifyStagename(stagenameF.getText()) ){
			geco.stage().setName(stagenameF.getText().trim());
			frame.repaint(); // bad smell, we call repaint so that the GecoWindow updates its title
			return true;					
		} else {
			geco.info(Messages.uiGet("StageConfigPanel.StageNameEmptyWarning"), true); //$NON-NLS-1$
			stagenameF.setText(geco.stage().getName());
			return false;
		}	
	}

	private boolean validateZeroHour(SimpleDateFormat formatter, JTextField zerohourF, IGecoApp geco) {
		try {
			long oldTime = geco.stage().getZeroHour();
			long zeroTime = formatter.parse(zerohourF.getText()).getTime();
			geco.stage().setZeroHour(zeroTime);
			geco.siHandler().changeZeroTime();
			geco.runnerControl().updateRegisteredStarttimes(zeroTime, oldTime);
			return true;
		} catch (ParseException e1) {
			geco.info(Messages.uiGet("StageConfigPanel.ZeroHourBadFormatWarning"), true); //$NON-NLS-1$
			zerohourF.setText(formatter.format(geco.stage().getZeroHour()));
			return false;
		}
	}
	
	@Override
	public Component build() {
		return this;
	}

}
