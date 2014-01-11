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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import net.geco.basics.Html;
import net.geco.control.SIReaderHandler.SerialPort;
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
	
	public SIReaderConfigPanel(final IGecoApp geco, final JFrame frame) {
		setLayout(new GridBagLayout());
		GridBagConstraints c = SwingUtils.gbConstr(0);
		c.insets = new Insets(0, 0, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		
		final JComboBox stationPortCB = new JComboBox();
		populateCommPorts(geco, stationPortCB);
		stationPortCB.setPreferredSize(new Dimension(170, stationPortCB.getPreferredSize().height));
		stationPortCB.setToolTipText(Messages.uiGet("SIReaderConfigPanel.StationPortTooltip")); //$NON-NLS-1$
		stationPortCB.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				populateCommPorts(geco, stationPortCB);
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {}
			public void popupMenuCanceled(PopupMenuEvent arg0) {}
		});
		stationPortCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.siHandler().setPort( (SerialPort) stationPortCB.getSelectedItem() );
			}
		});
		add(new JLabel(Messages.uiGet("SIReaderConfigPanel.StationPortLabel")), c); //$NON-NLS-1$
		add(stationPortCB, c);
		
		c.gridy = 5;
		c.gridwidth = 5;
		c.insets = new Insets(15, 0, 5, 5);
		Box modeConfigBox = Box.createVerticalBox();
		add(modeConfigBox, c);
		
		JRadioButton manualB = new JRadioButton(Messages.uiGet("SIReaderConfigPanel.ManualHandlerConfig")); //$NON-NLS-1$
		JRadioButton autoB = new JRadioButton(Messages.uiGet("SIReaderConfigPanel.AutoHandlerConfig")); //$NON-NLS-1$
		final JRadioButton archiveLookupB = new JRadioButton(Messages.uiGet("SIReaderConfigPanel.ArchiveLookupConfig")); //$NON-NLS-1$
		final JRadioButton alwaysCreateB = new JRadioButton(Messages.uiGet("SIReaderConfigPanel.NoLookupConfig")); //$NON-NLS-1$

		manualB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				geco.siHandler().enableManualHandler();
				archiveLookupB.setEnabled(false);
				alwaysCreateB.setEnabled(false);
			}
		});
		autoB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				geco.siHandler().enableAutoHandler();
				archiveLookupB.setEnabled(true);
				alwaysCreateB.setEnabled(true);
			}
		});
		ButtonGroup modeConfigGroup = new ButtonGroup();
		modeConfigGroup.add(manualB);
		modeConfigGroup.add(autoB);
		if ( geco.siHandler().autoHandlerEnabled() ) {
			autoB.doClick();
		} else {
			manualB.doClick();
		}
		
		modeConfigBox.setBorder(BorderFactory.createTitledBorder(Messages.uiGet("SIReaderConfigPanel.ModeBehaviorTitle"))); //$NON-NLS-1$
		modeConfigBox.add(new JLabel(Html.htmlTag("i", Messages.uiGet("SIReaderConfigPanel.ModeBehaviorLabel")))); //$NON-NLS-1$ //$NON-NLS-2$
		modeConfigBox.add(Box.createVerticalStrut(5));
		modeConfigBox.add(manualB);
		modeConfigBox.add(autoB);
		
		archiveLookupB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				geco.siHandler().enableArchiveLookup();
			}
		});
		alwaysCreateB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				geco.siHandler().disableArchiveLookup();
			}
		});
		ButtonGroup insertConfigGroup = new ButtonGroup();
		insertConfigGroup.add(archiveLookupB);
		insertConfigGroup.add(alwaysCreateB);
		if ( geco.siHandler().archiveLookupEnabled() ) {
			archiveLookupB.setSelected(true);
		} else {
			alwaysCreateB.setSelected(true);
		}
		
		Box insertConfigBox = Box.createVerticalBox();
		insertConfigBox.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
		insertConfigBox.add(new JLabel(Html.htmlTag("i", Messages.uiGet("SIReaderConfigPanel.ArchiveLookupLabel")))); //$NON-NLS-1$ //$NON-NLS-2$
		insertConfigBox.add(Box.createVerticalStrut(5));
		insertConfigBox.add(archiveLookupB);
		insertConfigBox.add(alwaysCreateB);
		
		modeConfigBox.add(Box.createVerticalStrut(10));
		modeConfigBox.add(insertConfigBox);

		final JCheckBox coursesetB = new JCheckBox("Restrict course detection to category course set");
		coursesetB.setSelected(geco.siHandler().detectionByCourseSetEnabled());
		coursesetB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				geco.siHandler().toggleDetectionByCourseSet(coursesetB.isSelected());
			}
		});
		modeConfigBox.add(Box.createVerticalStrut(10));
		modeConfigBox.add(new JLabel(Html.htmlTag("i", "When reading a runner in auto course with a known category (registered/duplicate/archive)")));
		modeConfigBox.add(coursesetB);
	}

	private void populateCommPorts(final IGecoApp geco, final JComboBox stationPortCB) {
		stationPortCB.setModel(new DefaultComboBoxModel(geco.siHandler().refreshPorts().toArray()));
		stationPortCB.setSelectedItem(geco.siHandler().getPort());
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
