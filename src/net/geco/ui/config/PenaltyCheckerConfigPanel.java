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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.geco.basics.Html;
import net.geco.control.PenaltyChecker;
import net.geco.framework.IGeco;
import net.geco.model.Messages;
import net.geco.ui.basics.SwingUtils;
import net.geco.ui.framework.ConfigPanel;

/**
 * @author Simon Denier
 * @since May 24, 2011
 *
 */
public class PenaltyCheckerConfigPanel extends JPanel implements ConfigPanel {

	@Override
	public String getLabel() {
		return Messages.uiGet("StagePanel.OrientshowConfigTitle"); //$NON-NLS-1$
	}
	
	public PenaltyCheckerConfigPanel(final IGeco geco) {
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = SwingUtils.gbConstr(0);
		c.insets = new Insets(0, 0, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		add(new JLabel(Messages.uiGet("StagePanel.MPLimitLabel")), c); //$NON-NLS-1$
		int mpLimit = checker(geco).getMPLimit();
		final JSpinner mplimitS = new JSpinner(new SpinnerNumberModel(mpLimit, 0, null, 1));
		mplimitS.setPreferredSize(new Dimension(100, SwingUtils.SPINNERHEIGHT));
		mplimitS.setToolTipText(Messages.uiGet("StagePanel.MPLimitTooltip")); //$NON-NLS-1$
		mplimitS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int oldLimit = checker(geco).getMPLimit();
				int newLimit = ((Integer) mplimitS.getValue()).intValue();
				if( oldLimit!=newLimit ) {
					checker(geco).setMPLimit(newLimit);
				}
			}
		});
		add(SwingUtils.embed(mplimitS), c);
		
		c.gridy = 1;
		add(new JLabel(Messages.uiGet("StagePanel.TimePenaltyLabel")), c); //$NON-NLS-1$
		long penalty = checker(geco).getMPPenalty() / 1000;
		final JSpinner penaltyS = new JSpinner(new SpinnerNumberModel(penalty, 0l, null, 10));
		penaltyS.setPreferredSize(new Dimension(100, SwingUtils.SPINNERHEIGHT));
		penaltyS.setToolTipText(Messages.uiGet("StagePanel.TimePenaltyTooltip")); //$NON-NLS-1$
		penaltyS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				long oldPenalty = checker(geco).getMPPenalty();
				long newPenalty = 1000 * ((Long) penaltyS.getValue()).longValue();
				if( oldPenalty!=newPenalty ) {
					checker(geco).setMPPenalty(newPenalty);
				}
			}
		});
		add(SwingUtils.embed(penaltyS), c);

		c.gridy = 2;
		c.gridwidth = 2;
		String helpL = new Html()
			.open("i") //$NON-NLS-1$
			.contents(Messages.uiGet("StagePanel.MPConfigHelp1")) //$NON-NLS-1$
			.tag("b", Messages.uiGet("StagePanel.MPConfigHelp2")) //$NON-NLS-1$ //$NON-NLS-2$
			.contents(Messages.uiGet("StagePanel.MPConfigHelp3")) //$NON-NLS-1$
			.close("i").close(); //$NON-NLS-1$
		add(new JLabel(helpL), c);
	}

	private PenaltyChecker checker(final IGeco geco) {
		return (PenaltyChecker) geco.checker();
	}

	@Override
	public Component build() {
		return this;
	}

}
