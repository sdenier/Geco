/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.live;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import net.geco.basics.TimeManager;
import net.geco.control.GecoControl;
import net.geco.control.StageBuilder;
import net.geco.model.Messages;
import net.geco.model.Registry;
import net.geco.model.RunnerRaceData;
import net.geco.ui.basics.EcardComparator;
import net.geco.ui.basics.StartStopButton;


/**
 * @author Simon Denier
 * @since Aug 26, 2010
 *
 */
public class GecoLive implements LiveListener {

	private GecoControl gecoControl;
	private ExtendedRunnersTableModel tableModel;
	private JTable runnersTable;

	private LiveComponent liveComponent;
	private JFormattedTextField portF;
	private StartStopButton listenB;

	
	public static void main(String[] args) {
		Messages.put("live", "net.geco.live.messages"); //$NON-NLS-1$ //$NON-NLS-2$
		Messages.put("ui", "net.geco.ui.messages"); //$NON-NLS-1$ //$NON-NLS-2$
		GecoLive gecoLive = new GecoLive();
		gecoLive.guiLaunch();
	}

	public GecoLive() {
		String startDir = null;
		startDir = launcher();
		if( startDir==null ){
			System.out.println("Bye bye!"); //$NON-NLS-1$
			System.exit(0);
		}

		gecoControl = new GecoControl();
		gecoControl.openStage(startDir);
		liveComponent = new LiveComponent();
	}
	
	private String launcher() {
		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir")); //$NON-NLS-1$
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle(Messages.liveGet("GecoLive.SelectStageTitle")); //$NON-NLS-1$
		int returnValue = chooser.showDialog(null, Messages.uiGet("GecoLauncher.OpenLabel")); //$NON-NLS-1$
		if( returnValue==JFileChooser.APPROVE_OPTION ) {
			File baseFile = chooser.getSelectedFile();
			String basePath = baseFile.getAbsolutePath();
			if( baseFile.exists() && StageBuilder.directoryHasData(basePath) ) {
				return basePath;
			} else {
				JOptionPane.showMessageDialog(
						chooser,
						Messages.uiGet("GecoLauncher.NoGecoDataWarning"), //$NON-NLS-1$
						Messages.uiGet("GecoLauncher.Error"), //$NON-NLS-1$
						JOptionPane.ERROR_MESSAGE);
			}
		}
		return null;
	}
	
	private Registry registry() {
		return gecoControl.registry();
	}
	
	private void guiLaunch() {
		JFrame jFrame = new JFrame();
//		if( platformIsMacOs() ) {
//		GecoMacos.setupQuitAction(geco);
//	}
		jFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setOneTouchExpandable(true);
		splitPane.add(liveComponent.initGui(new JPanel(), false));
		splitPane.add(initRunnersTable());

		liveComponent.setStartDir(gecoControl.stage().getBaseDir());
		
		jFrame.add(splitPane);
		jFrame.pack();
		jFrame.setLocationRelativeTo(null);
		jFrame.setVisible(true);
	}
	
//	public void exit() {
//		System.exit(0);
//	}

	private Component initRunnersTable() {
		tableModel = new ExtendedRunnersTableModel();
		runnersTable = new JTable(tableModel);
//		table.setPreferredScrollableViewportSize(new Dimension(800, 300));
		tableModel.initCellRenderers(runnersTable);
		tableModel.initTableColumnSize(runnersTable);
		enableRowSorting(tableModel);
		runnersTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		runnersTable.getSelectionModel().setSelectionInterval(0, 0);
		runnersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting() ) {
					updateRunnerMap();
				}
			}
		});
		refreshTable();
		
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.LINE_AXIS));
		row.add(Box.createHorizontalStrut(100));
		row.add(new JScrollPane(runnersTable));
		row.add(Box.createHorizontalStrut(100));
		
		JScrollPane scrollPane = new JScrollPane(runnersTable);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(initNetworkPanel(), BorderLayout.SOUTH);
		return panel;
	}

	private void refreshTable() {
		tableModel.setData(new Vector<RunnerRaceData>(registry().getRunnersData()));
	}
	
	private Component initNetworkPanel() {
		DecimalFormat format = new DecimalFormat();
		format.setGroupingUsed(false);
		portF = new JFormattedTextField(format);
		portF.setText("4444"); //$NON-NLS-1$
		portF.setColumns(5);
		listenB = new StartStopButton() {
			private Color defaultColor;
			LiveServerMulti server;
			@Override
			public void actionOn() {
				try {
					server = new LiveServerMulti(gecoControl, Integer.parseInt(portF.getText())).accept();
					server.registerListener(GecoLive.this);
					defaultColor = listenB.getBackground();
					listenB.setBackground(Color.GREEN);
				} catch (NumberFormatException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}				
			}
			@Override
			public void actionOff() {
				server.stop();
				listenB.setBackground(defaultColor);
			}
		};
		listenB.setText(Messages.liveGet("GecoLive.ListenLabel")); //$NON-NLS-1$

		JPanel networkConfigP = new JPanel(new FlowLayout(FlowLayout.LEFT));
		networkConfigP.add(listenB);
		networkConfigP.add(Box.createHorizontalStrut(20));
		networkConfigP.add(new JLabel(Messages.liveGet("GecoLive.PortLabel"))); //$NON-NLS-1$
		networkConfigP.add(portF);
		return networkConfigP;
	}

	private void enableRowSorting(ExtendedRunnersTableModel tableModel) {
		TableRowSorter<ExtendedRunnersTableModel> sorter = new TableRowSorter<ExtendedRunnersTableModel>(tableModel);
		sorter.setComparator(1, new EcardComparator()); // Chip column
		sorter.setComparator(7, new Comparator<String>() { // Date column
			@Override
			public int compare(String o1, String o2) {
				try {
					return TimeManager.userParse(o1).compareTo(TimeManager.userParse(o2));
				} catch (ParseException e) {
					return 0;
				}
			}
		});
		sorter.setComparator(9, new Comparator<String>() { // Date column
			@Override
			public int compare(String o1, String o2) {
				try {
					return TimeManager.userParse(o1).compareTo(TimeManager.userParse(o2));
				} catch (ParseException e) {
					return 0;
				}
			}
		});
		sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey[] {
					new RowSorter.SortKey(0, SortOrder.ASCENDING) }));
		runnersTable.setRowSorter(sorter);
	}

	
	private void updateRunnerMap() {
		RunnerRaceData runnerData = registry().findRunnerData(selectedChip());
		if( runnerData!=null ) {
			liveComponent.displayRunnerMap(runnerData);
		}
	}

	private String selectedChip() {
		String chip = ""; //$NON-NLS-1$
		int selectedRow = runnersTable.getSelectedRow();
		if( selectedRow!=-1 && runnersTable.getRowCount() > 0) {
			// we have to test the number of displayed rows too.
			// If user inputs a filter which matches nothins,
			// there is no row to show but table still points to the 0-index.
			chip = (String) runnersTable.getValueAt(selectedRow, 1);
		}
		return chip;
	}

	@Override
	public void dataReceived(RunnerRaceData data) {
		int index = tableModel.getData().indexOf(data);
		if( index!=-1 ) {
			int newIndex = runnersTable.convertRowIndexToView(index);
			runnersTable.getSelectionModel().setSelectionInterval(newIndex, newIndex);
			runnersTable.scrollRectToVisible(runnersTable.getCellRect(newIndex, 0, true));
		} else {
			// TODO: temp check
			System.err.println(Messages.liveGet("GecoLive.UnregisteredWarning") + data.infoString()); //$NON-NLS-1$
		}
	}

	@Override
	public void newDataIncoming() {
		refreshTable();
	}

}
