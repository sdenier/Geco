/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import valmo.geco.Geco;
import valmo.geco.core.Announcer;
import valmo.geco.core.Html;
import valmo.geco.live.LiveClient;
import valmo.geco.live.LiveClientDialog;
import valmo.geco.model.Stage;


/**
 * GecoWindow is the main frame of the application and primarily responsible for initializing the main
 * GecoPanel in tabs. It also takes care of the main toolbar.
 * 
 * @author Simon Denier
 * @since Jan 23, 2009
 */
public class GecoWindow extends JFrame implements Announcer.StageListener, Announcer.StationListener {
	
	private Geco geco;

	private StagePanel stagePanel;
	
	private RunnersPanel runnersPanel;
	
	private LogPanel logPanel;
	
	private ResultsPanel resultsPanel;

	private HeatsPanel heatsPanel;

	private JButton nextB;

	private JButton previousB;

	private StartStopButton startB;

	private static final String THEME = "crystal/";

	private static Hashtable<String,String[]> ICONS;


	{
		ICONS = new Hashtable<String, String[]>();
		ICONS.put("crystal/", new String[] {
			"folder_new.png",
			"folder_sent_mail.png",
			"undo.png",
			"redo.png",
			"quick_restart.png",
			"cnr.png",
			"exit.png",
			"search.png",
			"irkick.png",
			"irkickflash.png",
		});
	}
	
	public GecoWindow(Geco geco) {
		this.geco = geco;
		setLookAndFeel();
		this.stagePanel = new StagePanel(this.geco, this);
		this.runnersPanel = new RunnersPanel(this.geco, this);
		this.resultsPanel = new ResultsPanel(this.geco, this);
		this.logPanel = new LogPanel(this.geco, this);
		this.heatsPanel = new HeatsPanel(this.geco, this);
		geco.announcer().registerStageListener(this);
		geco.announcer().registerStationListener(this);
		guiInit();
	}
	
	private void setLookAndFeel() {
		if( ! Geco.platformIsMacOs() ) { // try to use Nimbus unless on Mac Os
			try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			} catch (Exception e) {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				} catch (InstantiationException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public void guiInit() {
		updateWindowTitle();
		getContentPane().add(initToolbar(), BorderLayout.NORTH);
		checkButtonsStatus();
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
		pack();
		setLocationRelativeTo(null); // center on screen
		setVisible(true);
	}

	private JToolBar initToolbar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		JButton openB = new JButton("New/Open", createIcon(0));
		openB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					geco.openStage(new GecoLauncher(new File(geco.getCurrentStagePath()).getParentFile()).open(GecoWindow.this));
				} catch (Exception e1) {
					System.out.println(e1.getLocalizedMessage());
				}
			}
		});
		toolBar.add(openB);
		JButton saveB = new JButton("Save", createIcon(1));
		toolBar.add(saveB);
		saveB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.saveCurrentStage();
			}
		});
		toolBar.addSeparator();
		
		previousB = new JButton("Previous stage", createIcon(2));
		previousB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.switchToPreviousStage();
			}
		});
		toolBar.add(previousB);
		nextB = new JButton("Next stage", createIcon(3));
		nextB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.switchToNextStage();
			}
		});
		toolBar.add(nextB);
		toolBar.addSeparator();
		
		JButton statusB = new JButton("Recheck All", createIcon(4));
		statusB.setToolTipText("Recheck all OK/MP to update statuses");
		statusB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.runnerControl().recheckAllRunners();
			}
		});
		toolBar.add(statusB);
		
		toolBar.add(Box.createHorizontalGlue());

		JButton liveMapB = new JButton(createIcon(7));
		liveMapB.setToolTipText("Open LiveMap window");
		liveMapB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runnersPanel.openMapWindow();
			}
		});
		toolBar.add(liveMapB);
		final ImageIcon offliveIcon = createIcon(8);
		final ImageIcon onliveIcon = createIcon(9);
		StartStopButton liveClientB = new StartStopButton() {
			private LiveClient liveClient;
			@Override
			protected void initialize() {
				setIcon(offliveIcon);
				setToolTipText("Start Live broadcast");
			}
			@Override
			public void actionOn() {
				liveClient = new LiveClient(geco, this);
				if( new LiveClientDialog(GecoWindow.this, liveClient).open() ) {
					setIcon(onliveIcon);
					setToolTipText("Stop Live broadcast");
				} else {
					setSelected(false);
				}
			}
			@Override
			public void actionOff() {
				if( liveClient.isActive() ) {
					liveClient.stop();
				}
				initialize();
			}
		};
		toolBar.add(liveClientB);
		toolBar.addSeparator();
		
		final ImageIcon startIcon = createIcon(5);
		final ImageIcon stopIcon = createIcon(6);
		startB = new StartStopButton() {
			@Override
			protected void initialize() {
				setSelected(false);
				setText("Start reader");
				setIcon(startIcon);
			}
			@Override
			public void actionOn() {
				geco.siHandler().start();
				setText("Starting...");
				setIcon(stopIcon);				
			}
			@Override
			public void actionOff() {
				geco.siHandler().stop();
				initialize();
			}
		};
		toolBar.add(startB);
		final JLabel versionL = new JLabel(" v" + Geco.VERSION);
		versionL.setBorder(BorderFactory.createLineBorder(versionL.getBackground()));
		versionL.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			@Override
			public void mousePressed(MouseEvent e) {
				Html html = new Html();
				html.open("div", "align=center");
				html.b("Geco version " + Geco.VERSION).br().br();
				html.contents("Copyright (c) 2008-2010 Simon Denier.").br();
				html.contents("Source code released under MIT License.").br();
				html.contents("See readme.txt for details.");
				html.close("div");
				JOptionPane.showMessageDialog(GecoWindow.this, html.close(), "Geco Information", JOptionPane.INFORMATION_MESSAGE);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				versionL.setBorder(BorderFactory.createLineBorder(versionL.getBackground()));
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				versionL.setBorder(BorderFactory.createLineBorder(Color.gray));
			}
			@Override
			public void mouseClicked(MouseEvent e) { }
		});
		toolBar.add(versionL);
		return toolBar;
	}
	
	public ImageIcon createIcon(int i) {
		return createImageIcon(THEME, ICONS.get(THEME)[i]);
	}
	
	public ImageIcon createImageIcon(String theme, String path) {
		URL url = getClass().getResource("/resources/icons/" + theme + path);
		return new ImageIcon(url);
	}

	@Override
	public void stationStatus(String status) {
		if( status.equals("Ready") ) {
			startB.setText("Stop reader");
			geco.info("SI Station ready", false);
			return;
		}
		if( status.equals("NotFound") ) {
			geco.info("Could not open port " + geco.siHandler().getPortName(), false);
			startB.initialize();
			return;
		}
		if( status.equals("Failed") ) {
			geco.info("Station " + geco.siHandler().getPortName() + " is offline", false);
			startB.initialize();
		}
	}

	@Override
	public void changed(Stage previous, Stage next) {
		updateWindowTitle();
		checkButtonsStatus();
	}

	private void checkButtonsStatus() {
		if( geco.hasPreviousStage() ) {
			previousB.setEnabled(true);
		} else {
			previousB.setEnabled(false);
		}
		if( geco.hasNextStage() ) {
			nextB.setEnabled(true);
		} else {
			nextB.setEnabled(false);
		}
	}

	@Override
	public void saving(Stage stage, Properties properties) {
	}

	@Override
	public void closing(Stage stage) {
	}

}
