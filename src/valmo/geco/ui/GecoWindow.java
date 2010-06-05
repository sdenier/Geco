/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import valmo.geco.core.Announcer;
import valmo.geco.core.Geco;
import valmo.geco.model.Stage;


/**
 * GecoWindow is the main frame of the application and primarily responsible for initializing the main
 * GecoPanel in tabs. It also takes care of the main toolbar.
 * 
 * @author Simon Denier
 * @since Jan 23, 2009
 */
public class GecoWindow extends JFrame implements Announcer.StageListener {
	
	private Geco geco;

	private StagePanel stagePanel;
	
	private RunnersPanel runnersPanel;
	
	private LogPanel logPanel;
	
	private ResultsPanel resultsPanel;

	private HeatsPanel heatsPanel;

	
	/**
	 * 
	 */
	public GecoWindow(Geco geco) {
		this.geco = geco;
		this.stagePanel = new StagePanel(this.geco, this);
		this.runnersPanel = new RunnersPanel(this.geco, this);
		this.resultsPanel = new ResultsPanel(this.geco, this);
		this.logPanel = new LogPanel(this.geco, this);
		this.heatsPanel = new HeatsPanel(this.geco, this);
		geco.announcer().registerStageListener(this);
		guiInit();
	}
	
	public void guiInit() {
		updateWindowTitle();
		getContentPane().add(initToolbar(), BorderLayout.NORTH);
		JTabbedPane pane = new JTabbedPane();
		pane.addTab("Stage", this.stagePanel);
		pane.setMnemonicAt(0, KeyEvent.VK_S);
		pane.addTab("Runners", this.runnersPanel);
		pane.setMnemonicAt(1, KeyEvent.VK_R);
		pane.addTab("Results", this.resultsPanel);
		pane.setMnemonicAt(2, KeyEvent.VK_E);
		pane.addTab("Heats", this.heatsPanel);
		pane.setMnemonicAt(3, KeyEvent.VK_H);
		pane.addTab("Log", this.logPanel);
		pane.setMnemonicAt(4, KeyEvent.VK_L);
		getContentPane().add(pane, BorderLayout.CENTER);
		
		getContentPane().add(new GecoStatusBar(this.geco, this), BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				geco.exit();
			}
		});
	}

	public void updateWindowTitle() {
		setTitle("Geco - " + geco.stage().getName());
	}

	public void launchGUI() {
		setVisible(true);
		pack();
	}

	private JToolBar initToolbar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		JButton importB = new JButton("import");
		importB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.importStage();
			}
		});
		toolBar.add(importB);
		JButton saveB = new JButton("save");
		toolBar.add(saveB);
		saveB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.saveCurrentStage();
			}
		});
		toolBar.addSeparator();
		JButton previousB = new JButton("previous stage");
		previousB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.switchToPreviousStage();
			}
		});
		toolBar.add(previousB);
		JButton nextB = new JButton("next stage");
		nextB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.switchToNextStage();
			}
		});
		toolBar.add(nextB);
		toolBar.addSeparator();
		JButton statusB = new JButton("Recheck All");
		statusB.setToolTipText("Recheck all OK/MP to update statuses");
		statusB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.runnerControl().recheckAllRunners();
			}
		});
		toolBar.add(statusB);
		toolBar.add(Box.createHorizontalGlue());
		final JButton startB = new JButton("Start reader");
		startB.addActionListener(new ActionListener() {
			private boolean started = false;
			@Override
			public void actionPerformed(ActionEvent e) {
				if( started ) {
					geco.siHandler().stop();
					startB.setSelected(false);
					startB.setText("Start reader");
				} else {
					geco.siHandler().start();
					startB.setSelected(true);
					startB.setText("Stop reader");
				}
				started = !started;
			}
		});
		toolBar.add(startB);
		toolBar.add(new JLabel(" v" + Geco.VERSION));
		return toolBar;
	}

	@Override
	public void changed(Stage previous, Stage next) {
		updateWindowTitle();
	}

	@Override
	public void saving(Stage stage, Properties properties) {
	}

	@Override
	public void closing(Stage stage) {
	}


}
