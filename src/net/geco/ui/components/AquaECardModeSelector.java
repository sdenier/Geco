/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.components;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import net.geco.framework.IGeco;

/**
 * @author Simon Denier
 * @since Mar 20, 2012
 *
 */
public class AquaECardModeSelector extends JButton implements ECardModeSelector {

	private ECardMode currentMode = ECardMode.OffMode;

	private IGeco geco;

	private ECardModeDialog selectDialog;

	private boolean recovery = false;
	
	public AquaECardModeSelector(IGeco geco, JFrame frame) {
		this.geco = geco;
		setHorizontalAlignment(JLabel.LEFT);
		setPreferredSize(new Dimension(100, 32));
		selectDialog = new ECardModeDialog(frame);
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectDialog.showPopup(currentMode);
			}
		});
		displayMode();
	}
	
	private void selectMode(ECardMode selectedMode) {
		if( currentMode != selectedMode ){
			currentMode = selectedMode;
			displayMode();
			if( !recovery ){
				if( currentMode.isReadMode() ){
					beforeStartingReadMode();
				}
				if( currentMode.isActiveMode() ){
					modeStarted();
				}
				dummyActivation(); // TODO: remove
				currentMode.execute(geco.siHandler());
			}
		}
	}

	private void dummyActivation() {
		new Thread() {
			public void run() {
				try {
					sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				modeActivated();
			}
		}.start();
	}

	public void beforeStartingReadMode() {
		// can be overridden to perform external UI actions
	}
	
	public void modeStarted() {
		setText("Starting...");
	}
	
	public void modeActivated() {
		setText(currentMode.getTitle());
	}
	
	public void recoverOffMode() {
		this.recovery = true;
		selectMode(ECardMode.OffMode);
		modeActivated();
		this.recovery = false;
	}

	private void displayMode() {
		setText(currentMode.getTitle());
		setIcon(currentMode.getIcon());
	}

	class ECardModeDialog extends JDialog {

		private JList modesL;

		public ECardModeDialog(JFrame frame) {
			super(frame, null, false);
			setUndecorated(true);
			setResizable(false);

			modesL = new JList(ECardMode.values());
			modesL.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			modesL.setCellRenderer(new ECardModeRenderer());
			modesL.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					modesL.setSelectedIndex(modesL.locationToIndex(e.getPoint()));
					applySelection();
				}
			});
			modesL.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if( e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER ){
						applySelection();
					}
					if( e.getKeyCode() == KeyEvent.VK_ESCAPE ){
						setVisible(false);
					}
				}
			});
			modesL.addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent e) {
					setVisible(false);
				}
				public void focusGained(FocusEvent e) {}
			});
			getContentPane().add(modesL);
			pack();
		}

		private void applySelection() {
			selectMode((ECardMode) modesL.getSelectedValue());
			setVisible(false);
		}

		public void showPopup(ECardMode selectedMode) {
			modesL.setSelectedValue(selectedMode, true);
			Point bounds = AquaECardModeSelector.this.getLocationOnScreen();
			setLocation(bounds.x, bounds.y + 32);
			setVisible(true);
		}
	}
	
}
