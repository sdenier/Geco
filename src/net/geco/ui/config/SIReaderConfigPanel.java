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

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
		add(new JLabel(Messages.uiGet("StagePanel.StationPortLabel")), c); //$NON-NLS-1$
		final JComboBox stationPortCB = new JComboBox(geco.siHandler().listPorts());
		stationPortCB.setPreferredSize(new Dimension(170, stationPortCB.getPreferredSize().height));
		stationPortCB.setToolTipText(Messages.uiGet("StagePanel.StationPortTooltip")); //$NON-NLS-1$
		add(stationPortCB, c);
		stationPortCB.setSelectedItem(geco.siHandler().getPort());
		stationPortCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.siHandler().setPort( (SerialPort) stationPortCB.getSelectedItem() );
			}
		});
	
		c.gridy = 1;
		add(new JLabel(Messages.uiGet("StagePanel.SplitPrinterLabel")), c); //$NON-NLS-1$
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
		add(new JLabel(Messages.uiGet("StagePanel.SplitFormatLabel")), c); //$NON-NLS-1$
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
	}

	@Override
	public String getLabel() {
		return Messages.uiGet("StagePanel.SIReaderConfigTitle"); //$NON-NLS-1$
	}

	@Override
	public Component build() {
		return this;
	}

}
