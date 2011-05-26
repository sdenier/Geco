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

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
		return Messages.uiGet("StagePanel.StageConfigTitle");
	}

	public StageConfigPanel(final IGecoApp geco, final JFrame frame) {
		setLayout(new GridBagLayout());
		GridBagConstraints c = SwingUtils.gbConstr(0);
		c.insets = new Insets(0, 0, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		add(new JLabel(Messages.uiGet("StagePanel.StageNameLabel")), c); //$NON-NLS-1$
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
			geco.info(Messages.uiGet("StagePanel.StageNameEmptyWarning"), true); //$NON-NLS-1$
			stagenameF.setText(geco.stage().getName());
			return false;
		}	
	}

	@Override
	public Component get() {
		return this;
	}

}
