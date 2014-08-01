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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.geco.basics.Html;
import net.geco.basics.TimeManager;
import net.geco.control.checking.PenaltyChecker;
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
		return Messages.uiGet("PenaltyCheckerConfigPanel.Title"); //$NON-NLS-1$
	}
	
	public PenaltyCheckerConfigPanel(final IGeco geco) {
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = SwingUtils.gbConstr(0);
		c.insets = new Insets(0, 0, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		
		c.gridy = 1;
		add(new JLabel(Messages.uiGet("PenaltyCheckerConfigPanel.MPLimitLabel")), c); //$NON-NLS-1$
		int mpLimit = checker(geco).getMPLimit();
		final JSpinner mpLimitS = new JSpinner(new SpinnerNumberModel(mpLimit, 0, null, 1));
		mpLimitS.setPreferredSize(new Dimension(100, SwingUtils.SPINNERHEIGHT));
		mpLimitS.setToolTipText(Messages.uiGet("PenaltyCheckerConfigPanel.MPLimitTooltip")); //$NON-NLS-1$
		mpLimitS.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int oldLimit = checker(geco).getMPLimit();
				int newLimit = ((Integer) mpLimitS.getValue()).intValue();
				if( oldLimit!=newLimit ) {
					checker(geco).setMPLimit(newLimit);
				}
			}
		});
		add(SwingUtils.embed(mpLimitS), c);
		final JCheckBox noMpLimitCB = new JCheckBox(Messages.uiGet("PenaltyCheckerConfigPanel.DontCheckMPLabel")); //$NON-NLS-1$
		noMpLimitCB.setToolTipText(Messages.uiGet("PenaltyCheckerConfigPanel.DontCheckMpTooltip")); //$NON-NLS-1$
		noMpLimitCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mpLimitS.setEnabled(!noMpLimitCB.isSelected());
				checker(geco).useMPLimit(!noMpLimitCB.isSelected());
			}
		});
		if( checker(geco).noMPLimit() ){ // init time
			noMpLimitCB.setSelected(true);
			mpLimitS.setEnabled(false);
		}
		add(SwingUtils.embed(noMpLimitCB), c);

		c.gridy = 2;
		if( ! geco.getConfig().sectionsEnabled ) {
			add(new JLabel(Messages.uiGet("PenaltyCheckerConfigPanel.TimePenaltyLabel")), c); //$NON-NLS-1$
			long penalty = checker(geco).getMPPenalty();
			final JLabel penaltyMinuteL = new JLabel(TimeManager.time(penalty) + Messages.uiGet("PenaltyCheckerConfigPanel.PerMpLabel")); //$NON-NLS-1$
			final JSpinner penaltyS = new JSpinner(new SpinnerNumberModel(penalty / 1000, 0l, null, 10));
			penaltyS.setPreferredSize(new Dimension(100, SwingUtils.SPINNERHEIGHT));
			penaltyS.setToolTipText(Messages.uiGet("PenaltyCheckerConfigPanel.TimePenaltyTooltip")); //$NON-NLS-1$
			penaltyS.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					long oldPenalty = checker(geco).getMPPenalty();
					long newPenalty = 1000 * ((Long) penaltyS.getValue()).longValue();
					if( oldPenalty != newPenalty ) {
						checker(geco).setMPPenalty(newPenalty);
						penaltyMinuteL.setText(TimeManager.time(newPenalty) + Messages.uiGet("PenaltyCheckerConfigPanel.PerMpLabel")); //$NON-NLS-1$
					}
				}
			});
			add(SwingUtils.embed(penaltyS), c);
			add(penaltyMinuteL, c);

			c.gridy = 3;
			add(new JLabel("Extra Penalty:"), c);
			long xPenalty = checker(geco).getExtraPenalty();
			final JLabel xPenaltyMinuteL = new JLabel(TimeManager.time(xPenalty) + " per extraneous punch");
			final JSpinner xPenaltyS = new JSpinner(new SpinnerNumberModel(xPenalty / 1000, 0l, null, 10));
			xPenaltyS.setPreferredSize(new Dimension(100, SwingUtils.SPINNERHEIGHT));
			xPenaltyS.setToolTipText("Penalty for extraneous punches: added punches whose codes are not in course");
			xPenaltyS.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					long oldPenalty = checker(geco).getExtraPenalty();
					long newPenalty = 1000 * ((Long) xPenaltyS.getValue()).longValue();
					if( oldPenalty != newPenalty ) {
						checker(geco).setExtraPenalty(newPenalty);
						xPenaltyMinuteL.setText(TimeManager.time(newPenalty) + " per extraneous punch");
					}
				}
			});
			add(SwingUtils.embed(xPenaltyS), c);
			add(xPenaltyMinuteL, c);

			c.gridy = 4;
		}

		c.gridwidth = 2;
		String helpL = new Html()
			.open("i") //$NON-NLS-1$
			.contents(Messages.uiGet("PenaltyCheckerConfigPanel.MPConfigHelp1")) //$NON-NLS-1$
			.tag("b", Messages.uiGet("PenaltyCheckerConfigPanel.MPConfigHelp2")) //$NON-NLS-1$ //$NON-NLS-2$
			.contents(Messages.uiGet("PenaltyCheckerConfigPanel.MPConfigHelp3")) //$NON-NLS-1$
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
