/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.tabs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import net.geco.basics.Announcer.Logging;
import net.geco.framework.IGecoApp;
import net.geco.functions.GecoOperation.OperationCategory;
import net.geco.model.Messages;
import net.geco.model.Stage;
import net.geco.ui.components.OperationsPanel;
import net.geco.ui.framework.TabPanel;
import net.geco.ui.framework.TabbedSubpane;


/**
 * @author Simon Denier
 * @since Sep 13, 2009
 *
 */
public class LogPanel extends TabPanel implements Logging {

	private JTabbedPane tabbedPanes;

	private JTextArea logArea;
	
	@Override
	public String getTabTitle() {
		return Messages.uiGet("LogPanel.Title"); //$NON-NLS-1$
	}

	public LogPanel(IGecoApp geco, JFrame frame) {
		super(geco, frame);
		initPanels(this);
		geco().announcer().registerLogger(this);
	}

	public void initPanels(JPanel panel) {
		tabbedPanes = new JTabbedPane(JTabbedPane.BOTTOM);
		tabbedPanes.add(Messages.uiGet("LogPanel.StatsTitle"), initStatsPanel()); //$NON-NLS-1$
		for (OperationCategory category : OperationCategory.values()) {
			tabbedPanes.add(category.toString(), createOperationsPanel(category)); //$NON-NLS-1$
		}
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.add(tabbedPanes);
		splitPane.add(initLogArea());
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		panel.add(splitPane);
	}

	private JPanel initStatsPanel() {
		return new HStatsPanel(geco(), frame(), createClearLogButton());
	}
	
	private OperationsPanel createOperationsPanel(OperationCategory category) {
		return new OperationsPanel(category, frame(), createClearLogButton());
	}

	public JPanel initLogArea() {
		logArea = new JTextArea(22, 70);
		logArea.setEditable(false);
		logArea.setLineWrap(true);

		JPanel logPanel = new JPanel(new BorderLayout());
		logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

		return logPanel;
	}

	private JButton createClearLogButton() {
		JButton clearB = new JButton(Messages.uiGet("LogPanel.ClearLogLabel")); //$NON-NLS-1$
		clearB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clear();
			}
		});
		return clearB;
	}

	private void clear() {
		logArea.setText(""); //$NON-NLS-1$
	}

	public void displayLog(String message) {
		logArea.append("\n"); //$NON-NLS-1$
		logArea.append(message);
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	@Override
	public void info(String message, boolean warning) {
		String header = warning ? "[warn] " : "[info] "; //$NON-NLS-1$ //$NON-NLS-2$
		displayLog(header + message);
	}

	@Override
	public void log(String message, boolean warning) {
		displayLog(message);
	}

	@Override
	public void dataInfo(String data) {
		displayLog(data);
	}

	@Override
	public void componentShown(ComponentEvent e) {
		((TabbedSubpane) tabbedPanes.getSelectedComponent()).componentShown();
		logArea.requestFocusInWindow();
	}

	@Override
	public void changed(Stage previous, Stage current) {
		clear();
	}
	
}