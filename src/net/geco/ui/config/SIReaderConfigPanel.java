/**
 * Copyright (c) 2011 Simon Denier
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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import net.geco.control.SIReaderHandler.SerialPort;
import net.geco.control.SingleSplitPrinter;
import net.geco.framework.IGecoApp;
import net.geco.model.Messages;
import net.geco.ui.basics.SwingUtils;
import net.geco.ui.framework.ConfigPanel;

/**
 * @author Simon Denier
 * @since May 24, 2011
 *
 */
public class SIReaderConfigPanel extends JPanel implements ConfigPanel {
	
	public SIReaderConfigPanel(final IGecoApp geco) {
		setLayout(new GridBagLayout());
		GridBagConstraints c = SwingUtils.gbConstr(0);
		c.insets = new Insets(0, 0, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		add(new JLabel(Messages.uiGet("SIReaderConfigPanel.StationPortLabel")), c); //$NON-NLS-1$
		final JComboBox stationPortCB = new JComboBox(geco.siHandler().listPorts());
		stationPortCB.setPreferredSize(new Dimension(170, stationPortCB.getPreferredSize().height));
		stationPortCB.setToolTipText(Messages.uiGet("SIReaderConfigPanel.StationPortTooltip")); //$NON-NLS-1$
		add(stationPortCB, c);
		stationPortCB.setSelectedItem(geco.siHandler().getPort());
		stationPortCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.siHandler().setPort( (SerialPort) stationPortCB.getSelectedItem() );
			}
		});
	
		c.gridy = 1;
		add(new JLabel(Messages.uiGet("SIReaderConfigPanel.SplitPrinterLabel")), c); //$NON-NLS-1$
		final JComboBox printersCB = new JComboBox(geco.splitPrinter().listPrinterNames());
		printersCB.setPreferredSize(new Dimension(170, stationPortCB.getPreferredSize().height));
		printersCB.setSelectedItem(geco.splitPrinter().getSplitPrinterName());
		add(printersCB, c);
		printersCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.splitPrinter().setSplitPrinterName((String) printersCB.getSelectedItem());
			}
		});
		
		c.gridy = 2;
		add(new JLabel(Messages.uiGet("SIReaderConfigPanel.SplitFormatLabel")), c); //$NON-NLS-1$
		final JComboBox splitFormatCB = new JComboBox(SingleSplitPrinter.SplitFormat.values());
		splitFormatCB.setPreferredSize(new Dimension(170, stationPortCB.getPreferredSize().height));
		splitFormatCB.setSelectedItem(geco.splitPrinter().getSplitFormat());
		add(splitFormatCB, c);
		splitFormatCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.splitPrinter().setSplitFormat((SingleSplitPrinter.SplitFormat) splitFormatCB.getSelectedItem());
			}
		});
		
		c.gridy = 3;
		add(new JLabel(Messages.uiGet("SIReaderConfigPanel.HeaderLabel")), c); //$NON-NLS-1$
		final JTextField headerF = new JTextField(geco.splitPrinter().getHeaderMessage());
		add(headerF, c);
		headerF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				geco.splitPrinter().setHeaderMessage(headerF.getText());
			}
		});
		headerF.setInputVerifier(new InputVerifier() {
			public boolean verify(JComponent arg0) {
				return true;
			}
			public boolean shouldYieldFocus(JComponent input) {
				geco.splitPrinter().setHeaderMessage(headerF.getText());
				return super.shouldYieldFocus(input);
			}
		});

		c.gridy = 4;
		add(new JLabel(Messages.uiGet("SIReaderConfigPanel.FooterLabel")), c); //$NON-NLS-1$
		final JTextField footerF = new JTextField(geco.splitPrinter().getFooterMessage());
		add(footerF, c);
		footerF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				geco.splitPrinter().setFooterMessage(footerF.getText());
			}
		});
		footerF.setInputVerifier(new InputVerifier() {
			public boolean verify(JComponent arg0) {
				return true;
			}
			public boolean shouldYieldFocus(JComponent input) {
				geco.splitPrinter().setFooterMessage(footerF.getText());
				return super.shouldYieldFocus(input);
			}
		});
		
		c.gridy = 5;
		c.gridwidth = 3;
		c.insets = new Insets(15, 0, 5, 5);
		Box modeConfigBox = Box.createVerticalBox();
		add(modeConfigBox, c);
		modeConfigBox.setBorder(BorderFactory.createTitledBorder("Mode Behavior"));
		modeConfigBox.add(new JLabel("When reading an unregistered ecard in Racing or Training mode"));
		modeConfigBox.add(Box.createVerticalStrut(5));
		JRadioButton archiveLookupB = new JRadioButton("Lookup and insert matching entry from archive, create an anonymous entry otherwise");
		modeConfigBox.add(archiveLookupB);
		archiveLookupB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				geco.siHandler().enableArchiveLookup();
			}
		});
		JRadioButton alwaysCreateB = new JRadioButton("Dont lookup in archive, always create an anonymous entry");
		modeConfigBox.add(alwaysCreateB);
		alwaysCreateB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				geco.siHandler().disableArchiveLookup();
			}
		});
		ButtonGroup modeConfig = new ButtonGroup();
		modeConfig.add(archiveLookupB);
		modeConfig.add(alwaysCreateB);
		archiveLookupB.setSelected(true);
	}

	@Override
	public String getLabel() {
		return Messages.uiGet("SIReaderConfigPanel.Title"); //$NON-NLS-1$
	}

	@Override
	public Component build() {
		return this;
	}

}
