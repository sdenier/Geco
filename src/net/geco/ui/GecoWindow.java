/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
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

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.geco.app.AppBuilder;
import net.geco.basics.Announcer;
import net.geco.basics.GecoResources;
import net.geco.basics.GecoWarning;
import net.geco.basics.Html;
import net.geco.framework.IGecoApp;
import net.geco.live.LiveClient;
import net.geco.live.LiveClientDialog;
import net.geco.model.Messages;
import net.geco.model.Stage;
import net.geco.ui.basics.GecoLauncher;
import net.geco.ui.basics.GecoStatusBar;
import net.geco.ui.basics.StartStopButton;
import net.geco.ui.framework.ConfigPanel;
import net.geco.ui.framework.TabPanel;
import net.geco.ui.tabs.StagePanel;



/**
 * GecoWindow is the main frame of the application and primarily responsible for initializing the main
 * GecoPanel in tabs. It also takes care of the main toolbar.
 * 
 * @author Simon Denier
 * @since Jan 23, 2009
 */
public class GecoWindow extends JFrame implements Announcer.StageListener, Announcer.StationListener {

	static { // Just in case
		Messages.put("ui", "net.geco.ui.messages"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private IGecoApp geco;

	private StartStopButton startB;

	private static final String THEME = "crystal/"; //$NON-NLS-1$

	private static Hashtable<String,String[]> ICONS;


	{
		ICONS = new Hashtable<String, String[]>();
		ICONS.put("crystal/", new String[] { //$NON-NLS-1$
			"folder_new.png", //$NON-NLS-1$
			"folder_sent_mail.png", //$NON-NLS-1$
			"undo.png", //$NON-NLS-1$ // TODO: remove
			"redo.png", //$NON-NLS-1$ // remove
			"quick_restart.png", //$NON-NLS-1$
			"cnr.png", //$NON-NLS-1$
			"exit.png", //$NON-NLS-1$
			"search.png", //$NON-NLS-1$
			"irkick.png", //$NON-NLS-1$
			"irkickflash.png", //$NON-NLS-1$
			"fileprint.png", //$NON-NLS-1$
			"filequickprint.png", //$NON-NLS-1$
			"advanced.png", //$NON-NLS-1$
			"restart.png", //$NON-NLS-1$
		});
	}
	

	public GecoWindow(IGecoApp geco, AppBuilder builder) {
		this.geco = geco;
		setLookAndFeel();
		StagePanel stagePanel = new StagePanel(this.geco, this);
		geco.announcer().registerStageListener(this);
		geco.announcer().registerStationListener(this);
		guiInit(stagePanel, builder.buildUITabs(geco, this), builder.buildConfigPanels(geco, this));
	}

	private void setLookAndFeel() {
		if( ! GecoResources.platformIsMacOs() ) { // try to use Nimbus unless on Mac Os
			try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"); //$NON-NLS-1$
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

	public void guiInit(StagePanel stagePanel, TabPanel[] uiTabs, ConfigPanel[] configPanels) {
		updateWindowTitle();
		getContentPane().add(initToolbar(), BorderLayout.NORTH);
		final JTabbedPane pane = new JTabbedPane();
		pane.addTab(Messages.uiGet("GecoWindow.Stage"), stagePanel); //$NON-NLS-1$
		for (TabPanel tabPanel : uiTabs) {
			pane.addTab(tabPanel.getTabTitle(), tabPanel);
		}
		stagePanel.addConfigPanels(configPanels);
		
		setKeybindings(pane);
		getContentPane().add(pane, BorderLayout.CENTER);
		
		getContentPane().add(new GecoStatusBar(this.geco.announcer()), BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				geco.exit();
			}
		});
	}

	private void setKeybindings(final JTabbedPane pane) {
		InputMap inputMap = ((JComponent) getContentPane())
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap actionMap = ((JComponent) getContentPane()).getActionMap();
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
				"focusReaderButton"); //$NON-NLS-1$
		actionMap.put("focusReaderButton", new AbstractAction() { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent arg0) {
				startB.requestFocusInWindow();
			}
		});

		for( int i=1; i<=pane.getTabCount() && i<=9; i++ ) {
			String focusCmd = "focusTab" + i; //$NON-NLS-1$
			inputMap.put(KeyStroke.getKeyStroke(Character.forDigit(i, 10),
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
					focusCmd);
			AbstractAction action = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					pane.setSelectedIndex((Integer) getValue("index")); //$NON-NLS-1$
				}
			};
			action.putValue("index", i-1); //$NON-NLS-1$
			actionMap.put(focusCmd, action);
		}
	}

	public void updateWindowTitle() {
		setTitle("Geco - " + geco.stage().getName()); //$NON-NLS-1$
	}
	
	public void repaint() { // Bad smell: updating something in the repaint call
		System.err.println("window repaint");
		updateWindowTitle();
		super.repaint();
	}

	public void launchGUI() {
		pack();
		setLocationRelativeTo(null); // center on screen
		setVisible(true);
	}

	private JToolBar initToolbar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		
		initNavigationToolbar(toolBar);
		toolBar.addSeparator();
		
		JButton recheckB = new JButton(Messages.uiGet("GecoWindow.RecheckButton"), createIcon(4)); //$NON-NLS-1$
		recheckB.setToolTipText(Messages.uiGet("GecoWindow.RecheckToolTip")); //$NON-NLS-1$
		recheckB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.runnerControl().recheckOkMpRunners();
			}
		});
		toolBar.add(recheckB);
		
		toolBar.add(Box.createHorizontalGlue());

		initLiveToolbar(toolBar);
		toolBar.addSeparator();
		
		initReaderToolbar(toolBar);
		initVersionDialog(toolBar);
		return toolBar;
	}

	private void initNavigationToolbar(JToolBar toolBar) {
		JButton openB = new JButton(Messages.uiGet("GecoWindow.NewOpenButton"), createIcon(0)); //$NON-NLS-1$
		openB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					geco.openStage(new GecoLauncher(
							new File(geco.getCurrentStagePath()).getParentFile()).open(GecoWindow.this));
				} catch (GecoWarning w) {
					// ok, do nothing
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(
							GecoWindow.this,
							e1.toString() + Messages.uiGet("GecoWindow.FatalOpenError"), //$NON-NLS-1$
							Messages.uiGet("GecoWindow.LoadErrorTitle"), //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
					System.exit(-1);
				}
			}
		});
		toolBar.add(openB);
		JButton saveB = new JButton(Messages.uiGet("GecoWindow.SaveButton"), createIcon(1)); //$NON-NLS-1$
		toolBar.add(saveB);
		saveB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco.saveCurrentStage();
			}
		});
	}

	private void initLiveToolbar(JToolBar toolBar) {
		JButton liveMapB = new JButton(createIcon(7));
		liveMapB.setToolTipText(Messages.uiGet("GecoWindow.LivemapTooltip")); //$NON-NLS-1$
		liveMapB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				runnersPanel.openMapWindow(); // TODO: extract Map from RunnersPanel
			}
		});
		toolBar.add(liveMapB);
		final ImageIcon offliveIcon = createIcon(8);
		final ImageIcon onliveIcon = createIcon(9);
		StartStopButton liveClientB = new StartStopButton() {
			private LiveClient liveClient;
			@Override
			public void initialize() {
				setIcon(offliveIcon);
				setToolTipText(Messages.uiGet("GecoWindow.StartLiveclientTooltip")); //$NON-NLS-1$
			}
			@Override
			public void actionOn() {
				liveClient = new LiveClient(geco, this);
				if( new LiveClientDialog(GecoWindow.this, liveClient).open() ) {
					setIcon(onliveIcon);
					setToolTipText(Messages.uiGet("GecoWindow.StopLiveclientTooltip")); //$NON-NLS-1$
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
	}

	private void initReaderToolbar(JToolBar toolBar) {
		final ImageIcon autoOff = createIcon(12);
		final ImageIcon autoOn = createIcon(13);
		final StartStopButton autoModeB = new StartStopButton() {
			@Override
			public void initialize() {
				doOnAction();
			}
			@Override
			public void actionOn() {
				geco.siHandler().setRequestHandler(geco.autoMergeHandler());
				setIcon(autoOn);
				setToolTipText(Messages.uiGet("GecoWindow.AutoMergeTooltip")); //$NON-NLS-1$
			}
			@Override
			public void actionOff() {
				geco.siHandler().setRequestHandler(geco.defaultMergeHandler());
				setIcon(autoOff);
				setToolTipText(Messages.uiGet("GecoWindow.ManualMergeTooltip")); //$NON-NLS-1$
			}
		};
		toolBar.add(autoModeB);
		
		final ImageIcon splitOff = createIcon(10);
		final ImageIcon splitOn = createIcon(11);
		final StartStopButton autoSplitB = new StartStopButton() {
			@Override
			public void initialize() {
				setToolTipText(Messages.uiGet("GecoWindow.AutoprintTooltip")); //$NON-NLS-1$
				doOffAction();
			}
			@Override
			public void actionOn() {
				setIcon(splitOn);
				geco.splitPrinter().enableAutoprint();
			}
			@Override
			public void actionOff() {
				geco.splitPrinter().disableAutoprint();
				setIcon(splitOff);
			}
		};
		toolBar.add(autoSplitB);
		
		final ImageIcon startIcon = createIcon(5);
		final ImageIcon stopIcon = createIcon(6);
		startB = new StartStopButton() {
			@Override
			public void initialize() {
				setSelected(false);
				setText(Messages.uiGet("GecoWindow.StartReaderButton")); //$NON-NLS-1$
				setIcon(startIcon);
			}
			@Override
			public void actionOn() {
				if( ! autoSplitB.isSelected() ) {
					int confirm = JOptionPane.showConfirmDialog(
												GecoWindow.this,
												Messages.uiGet("GecoWindow.AutoprintConfirm1"), //$NON-NLS-1$
												Messages.uiGet("GecoWindow.AutoprintConfirm2"), //$NON-NLS-1$
												JOptionPane.YES_NO_OPTION);
					if( confirm==JOptionPane.YES_OPTION ) {
						autoSplitB.doOnAction();
					}
				}
				geco.siHandler().start();
				setText(Messages.uiGet("GecoWindow.StartingButton")); //$NON-NLS-1$
				setIcon(stopIcon);				
			}
			@Override
			public void actionOff() {
				geco.siHandler().stop();
				initialize();
			}
		};
		toolBar.add(startB);
	}

	private void initVersionDialog(JToolBar toolBar) {
		final JLabel versionL = new JLabel(" v" + geco.version()); //$NON-NLS-1$
		versionL.setBorder(BorderFactory.createLineBorder(versionL.getBackground()));
		versionL.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			@Override
			public void mousePressed(MouseEvent e) {
				Html html = new Html();
				html.open("div", "align=center"); //$NON-NLS-1$ //$NON-NLS-2$
				html.b("Geco version " + geco.version()).br().br(); //$NON-NLS-1$
				html.contents("Copyright (c) 2008-2010 Simon Denier.").br(); //$NON-NLS-1$
				html.contents(Messages.uiGet("GecoWindow.AboutLicenseText")).br(); //$NON-NLS-1$
				html.contents(Messages.uiGet("GecoWindow.AboutReadmeText")); //$NON-NLS-1$
				html.close("div"); //$NON-NLS-1$
				JOptionPane.showMessageDialog(
						GecoWindow.this,
						html.close(),
						Messages.uiGet("GecoWindow.AboutTitle"), //$NON-NLS-1$
						JOptionPane.INFORMATION_MESSAGE);
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
	}

	public ImageIcon createIcon(int i) {
		return createImageIcon(THEME, ICONS.get(THEME)[i]);
	}
	
	public ImageIcon createImageIcon(String theme, String path) {
		URL url = getClass().getResource("/resources/icons/" + theme + path); //$NON-NLS-1$
		return new ImageIcon(url);
	}

	@Override
	public void stationStatus(String status) {
		if( status.equals("Ready") ) { //$NON-NLS-1$
			startB.setText(Messages.uiGet("GecoWindow.StopReaderButton")); //$NON-NLS-1$
			geco.info(Messages.uiGet("GecoWindow.StationReadyStatus"), false); //$NON-NLS-1$
			return;
		}
		if( status.equals("NotFound") ) { //$NON-NLS-1$
			geco.info(
					Messages.uiGet("GecoWindow.StationNotFoundStatus") //$NON-NLS-1$
					+ geco.siHandler().getPort(),
					false);
			startB.initialize();
			return;
		}
		if( status.equals("Failed") ) { //$NON-NLS-1$
			geco.info(
					Messages.uiGet("GecoWindow.StationOffline1Status") //$NON-NLS-1$
					+ geco.siHandler().getPort()
					+ Messages.uiGet("GecoWindow.StationOffline2Status"), //$NON-NLS-1$
					false);
			startB.initialize();
		}
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
