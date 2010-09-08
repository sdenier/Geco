/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import valmo.geco.ui.StartStopButton;
import valmo.geco.ui.SwingUtils;

/**
 * @author Simon Denier
 * @since Sep 7, 2010
 *
 */
public class LiveClientDialog extends JDialog {
	
	private JTextField nameF;
	private JFormattedTextField portF;
	
	private LiveClient liveClient;

	public LiveClientDialog(JFrame frame, final StartStopButton serverB) {
		super(frame, "Connection to Live Server", true);
		setLocationRelativeTo(frame);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = SwingUtils.gbConstr();

		getContentPane().add(new JLabel("Server Name"), c);
		nameF = new JTextField("localhost");
		nameF.setColumns(7);
		getContentPane().add(nameF, c);
		
		c.gridy = 1;
		getContentPane().add(new JLabel("Server Port"), c);
		DecimalFormat format = new DecimalFormat();
		format.setGroupingUsed(false);
		portF = new JFormattedTextField(format);
		portF.setText("4444");
		portF.setColumns(5);
		getContentPane().add(portF, c);
		
		c.gridy = 2;
		JButton startB = new JButton("Start");
		startB.requestFocusInWindow();
		startB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					liveClient = new LiveClient(nameF.getText(), Integer.parseInt(portF.getText()), serverB);
					liveClient.start();
					setVisible(false);
				} catch (NumberFormatException e1) {
					JOptionPane.showMessageDialog(LiveClientDialog.this, e1.getLocalizedMessage(), "Bad port number", JOptionPane.WARNING_MESSAGE);
				} catch (UnknownHostException e1) {
					JOptionPane.showMessageDialog(LiveClientDialog.this, e1.getLocalizedMessage(), "Can't connect to " + nameF.getText(), JOptionPane.ERROR_MESSAGE);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(LiveClientDialog.this, e1.getLocalizedMessage(), "IO Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		getContentPane().add(startB, c);
		JButton cancelB = new JButton("Cancel");
		cancelB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				liveClient = null;
				setVisible(false);
			}
		});
		getContentPane().add(cancelB, c);
		pack();
	}
	
	public LiveClient open() {
		setVisible(true);
		return liveClient;
	}
	
}
