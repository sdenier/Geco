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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.geco.basics.Html;
import net.geco.framework.IGecoApp;
import net.geco.model.Messages;
import net.geco.ui.basics.SwingUtils;
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
		GridBagConstraints c = SwingUtils.gbConstr(0);
		c.insets = new Insets(0, 0, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;

		add(new JLabel(Messages.uiGet("StageConfigPanel.StageNameLabel")), c); //$NON-NLS-1$
		final JTextField stagenameF = new JTextField(geco.stage().getName());
		stagenameF.setColumns(12);
		stagenameF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				validateStagename(stagenameF, geco, frame);
			}
		});
		stagenameF.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				return verifyStagename(stagenameF.getText());
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				return validateStagename(stagenameF, geco, frame);
			}
		});
		add(stagenameF, c);

		c.gridy = 1;
		add(new JLabel(Messages.uiGet("StageConfigPanel.ZeroHourLabel")), c); //$NON-NLS-1$
		final SimpleDateFormat formatter = new SimpleDateFormat("H:mm"); //$NON-NLS-1$
		formatter.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
		final JTextField zerohourF = new JTextField(formatter.format(geco.stage().getZeroHour()));
		zerohourF.setColumns(7);
		zerohourF.setToolTipText(Messages.uiGet("StageConfigPanel.ZeroHourTooltip")); //$NON-NLS-1$
		add(zerohourF, c);
		zerohourF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				validateZeroHour(formatter, zerohourF, geco);
			}
		});
		zerohourF.setInputVerifier(new InputVerifier() {
			@Override
			public boolean verify(JComponent input) {
				try {
					formatter.parse(zerohourF.getText());
					return true;
				} catch (ParseException e) {
					return false;
				}
			}
			@Override
			public boolean shouldYieldFocus(JComponent input) {
				return validateZeroHour(formatter, zerohourF, geco);
			}
		});
		
		c.gridy = 2;
		add(new JLabel(Messages.uiGet("StageConfigPanel.RunnerArchiveLabel")), c); //$NON-NLS-1$
		final JTextField archiveF = new JTextField();
		archiveF.setEditable(false);
		archiveF.setText(geco.archiveManager().getArchiveName()); // TODO: refresh
		add(archiveF, c);
		JButton selectArchiveFileB = new JButton(
				new ImageIcon(getClass().getResource("/resources/icons/crystal/db.png"))); //$NON-NLS-1$
		selectArchiveFileB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir")); //$NON-NLS-1$
				fileChooser.setDialogTitle(Messages.uiGet("ArchiveViewer.SelectArchiveLabel")); //$NON-NLS-1$
				int answer = fileChooser.showOpenDialog(frame);
				if( answer==JFileChooser.APPROVE_OPTION ) {
					geco.archiveManager().setArchiveFile(fileChooser.getSelectedFile());
					archiveF.setText(geco.archiveManager().getArchiveName());
				}
			}
		});
		add(selectArchiveFileB, c);

		c.gridy = 3;
		add(new JLabel(Messages.uiGet("StageConfigPanel.CNbaseLabel")), c); //$NON-NLS-1$
		final JTextField cnScoreF = new JTextField();
		cnScoreF.setEditable(false);
		cnScoreF.setText(geco.cnCalculator().getCnFile().getName());
		add(cnScoreF, c);
		JButton selectCNFileB = new JButton(
				new ImageIcon(getClass().getResource("/resources/icons/crystal/db.png"))); //$NON-NLS-1$
		selectCNFileB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir")); //$NON-NLS-1$
				fileChooser.setDialogTitle(Messages.uiGet("StageConfigPanel.SelectCNfileLabel")); //$NON-NLS-1$
				int answer = fileChooser.showOpenDialog(frame);
				if( answer==JFileChooser.APPROVE_OPTION ) {
					geco.cnCalculator().setCnFile(fileChooser.getSelectedFile());
					cnScoreF.setText(geco.cnCalculator().getCnFile().getName());
				}
			}
		});
		add(selectCNFileB, c);

		c.gridy = 4;
		c.insets = new Insets(15, 0, 5, 5);
		add(new JLabel(Messages.uiGet("StageConfigPanel.ConfigurationLabel")), c); //$NON-NLS-1$
		JLabel appNameL = new JLabel(Html.htmlTag("strong", geco.getAppName())); //$NON-NLS-1$
		add(appNameL, c);
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
