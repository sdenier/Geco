/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import valmo.geco.Geco;
import valmo.geco.control.RunnerCreationException;
import valmo.geco.core.Announcer;
import valmo.geco.core.TimeManager;
import valmo.geco.live.LiveComponent;
import valmo.geco.model.Course;
import valmo.geco.model.Messages;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Stage;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Jan 25, 2009
 *
 */
public class RunnersPanel extends TabPanel
		implements Announcer.RunnerListener, Announcer.StageConfigListener, Announcer.CardListener,
					HyperlinkListener {
	
	private JTable table;
	private RunnersTableModel tableModel;
	private TableRowSorter<RunnersTableModel> sorter;	
	private JTextField filterField;
	
	private JCheckBox liveB;
	private RunnerPanel runnerPanel;
	private PunchPanel tracePanel;
	private LiveComponent gecoLiveMap;

	
	public RunnersPanel(Geco geco, JFrame frame) {
		super(geco, frame);
		initRunnersPanel(this);
		geco().announcer().registerRunnerListener(this);
		geco().announcer().registerStageConfigListener(this);
		geco().announcer().registerCardListener(this);
	}


	public void initRunnersPanel(JPanel panel) {
		JPanel tablePan = new JPanel(new BorderLayout());
		tablePan.add(initTableScroll(), BorderLayout.CENTER);
		tablePan.add(SwingUtils.embed(new HyperLog(geco(), this)), BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout());
		panel.add(tablePan, BorderLayout.CENTER);
		panel.add(initTopPanel(), BorderLayout.NORTH);
		panel.add(initInfoPanel(), BorderLayout.EAST);
		setKeybindings();
	}
	
	public JTabbedPane initInfoPanel() {
		this.runnerPanel = new RunnerPanel(geco(), frame(), this);
		this.tracePanel = new PunchPanel();
		final JTabbedPane pane = new JTabbedPane();
		pane.addTab(Messages.uiGet("RunnersPanel.RunnerDataTitle"), this.runnerPanel); //$NON-NLS-1$
		pane.addTab(Messages.uiGet("RunnersPanel.RunnerTraceTitle"), tracePanel); //$NON-NLS-1$
		pane.addTab(Messages.uiGet("RunnersPanel.StatsTitle"), new VStatsPanel(geco(), frame())); //$NON-NLS-1$
		
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_D,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
				"focusData"); //$NON-NLS-1$
		getActionMap().put("focusData", new AbstractAction() { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setSelectedIndex(0);
			}
		});
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_T,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
				"focusTrace"); //$NON-NLS-1$
		getActionMap().put("focusTrace", new AbstractAction() { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setSelectedIndex(1);
			}
		});
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_S,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
				"focusStats"); //$NON-NLS-1$
		getActionMap().put("focusStats", new AbstractAction() { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				pane.setSelectedIndex(2);
			}
		});
		
		return pane;
	}

	public Component initTopPanel() {
		JComponent topPanel = Box.createHorizontalBox();
		JButton addButton = new JButton("+"); //$NON-NLS-1$
		addButton.setToolTipText(Messages.uiGet("RunnersPanel.NewRunnerTooltip")); //$NON-NLS-1$
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				insertNewRunner();
			}
		});
		topPanel.add(addButton);
		
		JButton deleteButton = new JButton("-"); //$NON-NLS-1$
		deleteButton.setToolTipText(Messages.uiGet("RunnersPanel.DeleteRunnerTooltip")); //$NON-NLS-1$
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RunnerRaceData data = selectedData();
				if( data!=null ) {
					int previousSel = table.getSelectedRow();
					// announce runner deletion and remove from tablemodel
					geco().runnerControl().deleteRunner(data);
					int nextSelection = Math.min(table.getModel().getRowCount() - 1, previousSel);
					table.getSelectionModel().setSelectionInterval(nextSelection, nextSelection);
				}
			}
		});
		topPanel.add(deleteButton);
		
		ImageIcon importCsv = new ImageIcon(
				getClass().getResource("/resources/icons/crystal/fileimport.png")); //$NON-NLS-1$
		JButton importCsvB = new JButton(importCsv);
		importCsvB.setToolTipText(Messages.uiGet("RunnersPanel.ImportCsvRunnersTooltip")); //$NON-NLS-1$
		importCsvB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir")); //$NON-NLS-1$
				fileChooser.setDialogTitle(Messages.uiGet("RunnersPanel.ImportStartlistTitle")); //$NON-NLS-1$
				int answer = fileChooser.showOpenDialog(frame());
				if( answer==JFileChooser.APPROVE_OPTION ) {
					try {
						geco().startlistImporter().loadArchiveFrom(fileChooser.getSelectedFile());
					} catch (IOException e1) {
						geco().debug(e1.toString());
					}
				}
			}
		});
		topPanel.add(importCsvB);
		
		ImageIcon archiveOpen = new ImageIcon(
				getClass().getResource("/resources/icons/crystal/db.png")); //$NON-NLS-1$
		JButton archiveB = new JButton(archiveOpen);
		archiveB.setToolTipText(Messages.uiGet("RunnersPanel.ImportArchiveTooltip")); //$NON-NLS-1$
		archiveB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ArchiveViewer(geco()).open();
			}
		});
		topPanel.add(archiveB);

		final JCheckBox fastB = new JCheckBox(Messages.uiGet("RunnersPanel.FastEditLabel")); //$NON-NLS-1$
		fastB.setToolTipText(Messages.uiGet("RunnersPanel.FastEditTooltip")); //$NON-NLS-1$
		fastB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( fastB.isSelected() )
					tableModel.enableFastEdition();
				else
					tableModel.disableFastEdition();
			}
		});
		topPanel.add(fastB);

		final JCheckBox defaultCourseB = new JCheckBox(Messages.uiGet("RunnersPanel.DefaultCourseLabel")); //$NON-NLS-1$
		defaultCourseB.setToolTipText(Messages.uiGet("RunnersPanel.DefaultCourseTooltip")); //$NON-NLS-1$
		defaultCourseB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tableModel.enableDefaultCourseForCategory(defaultCourseB.isSelected());
			}
		});
		topPanel.add(defaultCourseB);
		
		liveB = new JCheckBox(Messages.uiGet("RunnersPanel.LiveLabel")); //$NON-NLS-1$
		liveB.setToolTipText(Messages.uiGet("RunnersPanel.LiveTooltip")); //$NON-NLS-1$
		liveB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( liveModeOn() ) {
					disableRowSorting();
					sortRunnersByReadOrder();
				}
				else
					enableRowSorting();
			}
		});
		topPanel.add(liveB);
		
		topPanel.add(Box.createHorizontalGlue());
		initFilterPanel(topPanel);
		topPanel.setBorder(BorderFactory.createEtchedBorder());
		return topPanel;
	}
	
	public void initFilterPanel(JComponent panel) {
		panel.add(new JLabel(Messages.uiGet("RunnersPanel.FindLabel"))); //$NON-NLS-1$
		filterField = new JTextField(25);
		filterField.setToolTipText(Messages.uiGet("RunnersPanel.FindTooltip")); //$NON-NLS-1$
		filterField.setMaximumSize(new Dimension(250, SwingUtils.SPINNERHEIGHT));
		filterField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
			    try {
					RowFilter<Object,Object> filter = RowFilter.regexFilter("(?i)" + filterField.getText()); //$NON-NLS-1$
					sorter.setRowFilter(filter);
//					table.getSelectionModel().setSelectionInterval(0, 0);
			    } catch (java.util.regex.PatternSyntaxException e1) {
			        return;
			    }				
			}
		});
		panel.add(filterField);
	}

	private void insertNewRunner() {
		try {
			// announce runner creation and add in tablemodel
			geco().runnerControl().createAnonymousRunner();
		} catch (RunnerCreationException e1) {
			JOptionPane.showMessageDialog(
					frame(),
					e1.getMessage(),
					Messages.uiGet("RunnersPanel.NewRunnerWarning"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void setKeybindings() {
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_I,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
				"addRunner"); //$NON-NLS-1$
		getActionMap().put("addRunner", new AbstractAction() { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				insertNewRunner();
			}
		});
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_R,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
				"recheckRunner"); //$NON-NLS-1$
		getActionMap().put("recheckRunner", new AbstractAction() { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				runnerPanel.recheckRunnerStatus();
			}
		});
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_P,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
				"printSplits"); //$NON-NLS-1$
		getActionMap().put("printSplits", new AbstractAction() { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				runnerPanel.printRunnerSplits();
			}
		});
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
				"focusOnFilter"); //$NON-NLS-1$
		getActionMap().put("focusOnFilter", new AbstractAction() { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				filterField.selectAll();
				filterField.requestFocusInWindow();
			}
		});
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() |
						KeyEvent.SHIFT_DOWN_MASK),
				"cancelFilter"); //$NON-NLS-1$
		getActionMap().put("cancelFilter", new AbstractAction() { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				filterField.setText(""); //$NON-NLS-1$
				sorter.setRowFilter(null);
			}
		});
	}
	
	public JScrollPane initTableScroll() {
		tableModel = new RunnersTableModel(geco());
		table = new JTable(tableModel) {
			// Workaround for keyboard activation of JComboBox editors in JTable
			@Override
			public boolean editCellAt(int row, int column, EventObject e) {
				boolean edit = super.editCellAt(row, column, e);
				Component comp = getEditorComponent();
				if( edit && e instanceof KeyEvent && ((KeyEvent) e).getModifiers()==0
						&& comp instanceof JComboBox ) {
					((JComboBox) comp).setPopupVisible(true);
					comp.requestFocusInWindow();
				}
				return edit;
			}
		};
//		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setPreferredScrollableViewportSize(new Dimension(400, 300));
		tableModel.initCellEditors(table);
		tableModel.initTableColumnSize(table);
		enableRowSorting();
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		table.getSelectionModel().setSelectionInterval(0, 0);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting() ) {
					if( table.getSelectedRow()==-1 ){
						runnerPanel.enablePanel(false);
					} else {
						updateRunnerPanel();
					}
				}
			}
		});
		refreshTableData();
		return new JScrollPane(table);
	}

	private void enableRowSorting() {
		sorter = new TableRowSorter<RunnersTableModel>(tableModel);
		sorter.setComparator(1, new Comparator<String>() { // Ecard column
			@Override
			public int compare(String o1, String o2) {
				Integer n1 = null;
				try {
					n1 = Integer.valueOf(o1);
					Integer n2 = Integer.valueOf(o2);
					return n1.compareTo(n2);
				} catch (NumberFormatException e) {
					if( n1==null ){ // n1 is XXXXaa ecard
						return 1;
					} else {		// n2 is XXXXaa ecard
						return -1;
					}
				}
			}
		});
		sorter.setComparator(7, new Comparator<String>() { // Date column
			@Override
			public int compare(String o1, String o2) {
				Date t1 = null;
				try {
					t1 = TimeManager.userParse(o1);
					return t1.compareTo(TimeManager.userParse(o2));
				} catch (ParseException e) {
					if( t1==null ){ // t1 NO_TIME or user mistake
						return 1;
					} else {		// t2 NO_TIME or user mistake
						return -1;
					}
				}
			}
		});
		sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey[] {
					new RowSorter.SortKey(0, SortOrder.ASCENDING) }));
		table.setRowSorter(sorter);
	}
	
	private void disableRowSorting() {
		table.setRowSorter(null);
	}

	private boolean liveModeOn() {
		return liveB.isSelected();
	}

	private void sortRunnersByReadOrder() {
		Vector<RunnerRaceData> data = new Vector<RunnerRaceData>(registry().getRunnersData());
		Collections.sort(data, new Comparator<RunnerRaceData>() {
			@Override
			public int compare(RunnerRaceData o1, RunnerRaceData o2) {
				return -1 * o1.getReadtime().compareTo(o2.getReadtime());
			}
		});
		tableModel.setData(data);
		table.getSelectionModel().setSelectionInterval(0, 0);
	}

	private void refreshTableData() {
		refreshComboBoxes();
		tableModel.setData(new Vector<RunnerRaceData>(registry().getRunnersData()));
	}

	private void refreshComboBoxes() {
		tableModel.updateComboBoxEditors(table);
	}
	
	public void refreshRunnersPanel() {
		refreshTableData();
		table.getSelectionModel().setSelectionInterval(-1, -1);
	}

	
	private void refreshTableIndex(int index) {
		tableModel.fireTableRowsUpdated(index, index);
		if( table.convertRowIndexToView(index) == table.getSelectedRow() ) {
			updateRunnerPanel();	
		}
	}
	
	public void refreshTableRunner(RunnerRaceData runner) {
		refreshTableIndex(tableModel.getData().indexOf(runner));
	}
	
	private void focusTableOnIndex(int index) {
		int newIndex = table.convertRowIndexToView(index);
		table.getSelectionModel().setSelectionInterval(newIndex, newIndex);
		table.scrollRectToVisible(table.getCellRect(newIndex, 0, true));		
	}
	
	public void focusTableOnRunner(RunnerRaceData runner) {
		focusTableOnIndex(tableModel.getData().indexOf(runner));
	}
	
	public void refreshSelectionInTable() {
		int modelRow = table.convertRowIndexToModel(table.getSelectedRow()); 
		this.tableModel.fireTableRowsUpdated(modelRow, modelRow);
	}
	
	private String selectedEcard() {
		String ecard = ""; //$NON-NLS-1$
		int selectedRow = table.getSelectedRow();
		if( selectedRow!=-1 && table.getRowCount() > 0) {
			// we have to test the number of displayed rows too.
			// If user inputs a filter which matches nothins,
			// there is no row to show but table still points to the 0-index.
			ecard = (String) table.getValueAt(selectedRow, 1);
		}
		return ecard;
	}

	public void updateRunnerPanel() {
		String ecard = selectedEcard();
		if( !ecard.equals("") ) { //$NON-NLS-1$
			RunnerRaceData runnerData = registry().findRunnerData(ecard);
			runnerPanel.updateRunner(ecard);
			tracePanel.refreshPunches(runnerData);
			if( gecoLiveMap!=null && gecoLiveMap.isShowing() ) {
				gecoLiveMap.displayRunnerMap(runnerData);
			}
		}
	}
	
	private RunnerRaceData selectedData() {
		String ecard = selectedEcard();
		if( !ecard.equals("") ) //$NON-NLS-1$
			return registry().findRunnerData(ecard);
		return null;
	}
	
	public void openMapWindow() {
		if( gecoLiveMap==null ) {
			gecoLiveMap = new LiveComponent().initWindow(Geco.leisureModeOn());
			gecoLiveMap.setStartDir(geco().getCurrentStagePath());
		}
		gecoLiveMap.openWindow();
	}


	@Override
	public void changed(Stage previous, Stage next) {
		if( gecoLiveMap!=null ) {
			gecoLiveMap.closeWindow();
		}
		gecoLiveMap = null;
		refreshRunnersPanel();
	}


	@Override
	public void courseChanged(Runner runner, Course oldCourse) {
		refreshTableRunner(registry().findRunnerData(runner));
	}

	@Override
	public void runnerCreated(RunnerRaceData data) {
		tableModel.addData(data);
		focusTableOnRunner(data);
	}

	@Override
	public void runnerDeleted(RunnerRaceData data) {
		tableModel.removeData(data);
	}

	@Override
	public void statusChanged(RunnerRaceData runner, Status oldStatus) {
		refreshTableRunner(runner);
	}

	@Override
	public void runnersChanged() {
		refreshRunnersPanel();		
	}

	@Override
	public void coursesChanged() {
		refreshComboBoxes();			
	}
	@Override
	public void categoriesChanged() {
		refreshComboBoxes();
	}
	@Override
	public void clubsChanged() {
		refreshComboBoxes();
	}


	@Override
	public void cardRead(String ecard) {
//		refresh made through statusChanged announcement
//		refreshTableRunner(registry().findRunnerData(ecard));
		focusOnReadCard(ecard);
	}

	@Override
	public void unknownCardRead(String ecard) {
		focusOnReadCard(ecard);
	}

	@Override
	public void cardReadAgain(String ecard) {
		focusOnReadCard(ecard);
	}
	
	@Override
	public void rentedCard(String siIdent) {
		JOptionPane.showMessageDialog(frame(), Messages.uiGet("RunnersPanel.RentedEcardMessage") + siIdent); //$NON-NLS-1$
	}


	private void focusOnReadCard(String ecard) {
		if( liveModeOn() ) {
			RunnerRaceData data = registry().findRunnerData(ecard);
			tableModel.removeData(data);
			tableModel.addDataFirst(data);
			focusTableOnIndex(0);
		}
	}

	@Override
	public void componentShown(ComponentEvent e) {
		table.requestFocusInWindow();
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			RunnerRaceData runnerData = registry().findRunnerData(e.getDescription());
			if( runnerData!=null ){
				focusTableOnRunner(runnerData);
			}
		}
	}
	
}
