/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.live;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.geco.model.Messages;
import net.geco.ui.basics.SwingUtils;


/**
 * @author Simon Denier
 * @since Sep 7, 2010
 *
 */
public class LiveClientDialog extends JDialog {

	static {
		Messages.put("live", "net.geco.live.messages"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private JTextField nameF;
	private JFormattedTextField portF;

	private boolean started;

	public LiveClientDialog(JFrame frame, final LiveClient liveClient) {
		super(frame, Messages.liveGet("LiveClientDialog.DialogTitle"), true); //$NON-NLS-1$
		setResizable(false);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				cancel();
			}
		});
		
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = SwingUtils.gbConstr();

		getContentPane().add(new JLabel(Messages.liveGet("LiveClientDialog.ServernameLabel")), c); //$NON-NLS-1$
		nameF = new JTextField("localhost"); //$NON-NLS-1$
		nameF.setColumns(7);
		getContentPane().add(nameF, c);
		
		c.gridy = 1;
		getContentPane().add(new JLabel(Messages.liveGet("LiveClientDialog.ServerportLabel")), c); //$NON-NLS-1$
		DecimalFormat format = new DecimalFormat();
		format.setGroupingUsed(false);
		portF = new JFormattedTextField(format);
		portF.setText("4444"); //$NON-NLS-1$
		portF.setColumns(5);
		getContentPane().add(portF, c);
		
		c.gridy = 2;
		JButton startB = new JButton(Messages.liveGet("LiveClientDialog.StartLabel")); //$NON-NLS-1$
		startB.requestFocusInWindow();
		startB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					liveClient.setupNetworkParameters(nameF.getText(), Integer.parseInt(portF.getText()));
					liveClient.start();
					started = true;
					setVisible(false);
				} catch (NumberFormatException e1) {
					JOptionPane.showMessageDialog(
									LiveClientDialog.this,
									e1.toString(),
									Messages.liveGet("LiveClientDialog.BadPortNumberWarning"), //$NON-NLS-1$
									JOptionPane.WARNING_MESSAGE);
				} catch (UnknownHostException e1) {
					JOptionPane.showMessageDialog(
									LiveClientDialog.this,
									e1.toString(),
									Messages.liveGet("LiveClientDialog.NoConnectionWarning") + nameF.getText(), //$NON-NLS-1$
									JOptionPane.ERROR_MESSAGE);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(
									LiveClientDialog.this,
									e1.toString(),
									Messages.liveGet("LiveClientDialog.IOError"), //$NON-NLS-1$
									JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		getContentPane().add(startB, c);
		JButton cancelB = new JButton(Messages.liveGet("LiveClientDialog.CancelLabel")); //$NON-NLS-1$
		cancelB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		getContentPane().add(cancelB, c);
		pack();
		setLocationRelativeTo(null);
	}
	
	public boolean open() {
		setVisible(true);
		return started;
	}

	private void cancel() {
		started = false;
		setVisible(false);
	}
	
}
