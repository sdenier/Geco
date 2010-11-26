/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

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

import valmo.geco.Geco;
import valmo.geco.core.Announcer.Logging;
import valmo.geco.model.Messages;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Sep 13, 2009
 *
 */
public class LogPanel extends TabPanel implements Logging {

	private FunctionsPanel funtionsPanel;

	private JTextArea logArea;
	
	
	public LogPanel(Geco geco, JFrame frame) {
		super(geco, frame);
		initPanels(this);
		geco().announcer().registerLogger(this);
	}

	public void initPanels(JPanel panel) {
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		tabbedPane.add("Stats", initStatsPanel());
		tabbedPane.add("Functions", initFunctionsPanel());
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.add(tabbedPane);
		splitPane.add(initLogArea());
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		panel.add(splitPane);
	}
	
	private FunctionsPanel initFunctionsPanel() {
		funtionsPanel = new FunctionsPanel(geco(), frame(), createClearLogButton());
		return funtionsPanel;
	}

	private JPanel initStatsPanel() {
		return new HStatsPanel(geco(), frame(), createClearLogButton());
	}

	public JPanel initLogArea() {
		logArea = new JTextArea(22, 70);
		logArea.setEditable(false);
		logArea.setLineWrap(true);

		JPanel logPanel = new JPanel(new BorderLayout());
		logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
//		logPanel.add(SwingUtils.embed(initClearLogButton()), BorderLayout.SOUTH);

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
//		displayLog(message);
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
		funtionsPanel.componentShown();
		logArea.requestFocusInWindow();
	}

	@Override
	public void changed(Stage previous, Stage current) {
		clear();
	}
	
}