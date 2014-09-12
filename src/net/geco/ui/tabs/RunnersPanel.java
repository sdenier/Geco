/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.tabs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
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

import net.geco.basics.Announcer.CardListener;
import net.geco.basics.Announcer.RunnerListener;
import net.geco.basics.Announcer.StageConfigListener;
import net.geco.basics.TimeManager;
import net.geco.control.RunnerCreationException;
import net.geco.framework.IGecoApp;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.Status;
import net.geco.ui.basics.EcardComparator;
import net.geco.ui.basics.GecoIcon;
import net.geco.ui.basics.HyperLog;
import net.geco.ui.basics.SwingUtils;
import net.geco.ui.components.ArchiveViewer;
import net.geco.ui.components.PunchPanel;
import net.geco.ui.framework.RunnersTableAnnouncer;
import net.geco.ui.framework.TabPanel;
import net.geco.ui.framework.UIAnnouncers;


/**
 * @author Simon Denier
 * @since Jan 25, 2009
 *
 */
public class RunnersPanel extends TabPanel
	implements RunnerListener, StageConfigListener, CardListener, HyperlinkListener {
	
	private RunnersTableAnnouncer announcer;
	private JTable table;
	private RunnersTableModel tableModel;
	private TableRowSorter<RunnersTableModel> sorter;	
	private JTextField filterField;
	
	private JCheckBox finishModeB;
	private RunnerPanel runnerPanel;
	private PunchPanel tracePanel;


	@Override
	public String getTabTitle() {
		return Messages.uiGet("RunnersPanel.Title"); //$NON-NLS-1$
	}

	
	public RunnersPanel(IGecoApp geco, JFrame frame, UIAnnouncers uiAnnouncers) {
		super(geco, frame);
		announcer = new RunnersTableAnnouncer();
		uiAnnouncers.registerAnnouncer(announcer);
		initRunnersPanel(this);
		geco().announcer().registerRunnerListener(this);
		geco().announcer().registerStageConfigListener(this);
		geco().announcer().registerCardListener(this);
	}


	public void initRunnersPanel(JPanel panel) {
		JPanel tablePan = new JPanel(new BorderLayout());
		tablePan.add(initTableScroll(), BorderLayout.CENTER);
		tablePan.add(SwingUtils.embed(new HyperLog(geco().announcer(), this)), BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout());
		panel.add(tablePan, BorderLayout.CENTER);
		panel.add(initTopPanel(), BorderLayout.NORTH);
		panel.add(initInfoPanel(), BorderLayout.EAST);
		setKeybindings();
	}
	
	public JTabbedPane initInfoPanel() {
		this.runnerPanel = new RunnerPanel(geco(), frame(), this);
		this.tracePanel = new PunchPanel(geco());
		final JTabbedPane pane = new JTabbedPane();
		pane.addTab(Messages.uiGet("RunnersPanel.RunnerDataTitle"), this.runnerPanel); //$NON-NLS-1$
		pane.addTab(Messages.uiGet("RunnersPanel.RunnerTraceTitle"), tracePanel); //$NON-NLS-1$
		pane.addTab(Messages.uiGet("RunnersPanel.StatsTitle"), new VStatsPanel(geco(), frame())); //$NON-NLS-1$
		announcer.registerRunnersTableListener(runnerPanel);
		announcer.registerRunnersTableListener(tracePanel);
		
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
					int confirm = JOptionPane.showConfirmDialog(
							frame(),
							Messages.uiGet("RunnersPanel.DeleteConfirmMessage") //$NON-NLS-1$
								+ data.getRunner().idString(),
							Messages.uiGet("RunnersPanel.DeleteConfirmTitle") //$NON-NLS-1$
								+ data.getRunner().getName(),
							JOptionPane.YES_NO_OPTION);
					if( confirm==JOptionPane.YES_OPTION ) {
						int previousSel = table.getSelectedRow();
						// announce runner deletion and remove from tablemodel
						geco().runnerControl().deleteRunner(data);
						int nextSelection = Math.min(table.getModel().getRowCount() - 1, previousSel);
						table.getSelectionModel().setSelectionInterval(nextSelection, nextSelection);
					}
				}
			}
		});
		topPanel.add(deleteButton);
		
		JButton importCsvB = new JButton(GecoIcon.createIcon(GecoIcon.ImportFile));
		importCsvB.setToolTipText(Messages.uiGet("RunnersPanel.ImportCsvRunnersTooltip")); //$NON-NLS-1$
		importCsvB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(geco().getCurrentStagePath());
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
		
		JButton archiveB = new JButton(GecoIcon.createIcon(GecoIcon.OpenArchive));
		archiveB.setToolTipText(Messages.uiGet("RunnersPanel.ImportArchiveTooltip")); //$NON-NLS-1$
		archiveB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ArchiveViewer(geco()).open();
			}
		});
		topPanel.add(archiveB);

		JButton exportStartlistB = new JButton(GecoIcon.createIcon(GecoIcon.Startlist));
		exportStartlistB.setToolTipText(Messages.uiGet("RunnersPanel.ExportStartlistsTooltip")); //$NON-NLS-1$
		exportStartlistB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle(Messages.uiGet("RunnersPanel.ExportStartlistsTitle")); //$NON-NLS-1$
				fileChooser.setSelectedFile(new File(geco().getCurrentStagePath()
						+ File.separator + Messages.uiGet("RunnersPanel.ExportStartlistsFile")).getAbsoluteFile()); //$NON-NLS-1$
				int answer = fileChooser.showSaveDialog(frame());
				if( answer==JFileChooser.APPROVE_OPTION ) {
					try {
						geco().startlistExporter().exportTo(fileChooser.getSelectedFile().getAbsolutePath());
					} catch (IOException e1) {
						geco().debug(e1.toString());
					}
				}
			}
		});
		topPanel.add(exportStartlistB);

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
		
		finishModeB = new JCheckBox(Messages.uiGet("RunnersPanel.FinishModeLabel")); //$NON-NLS-1$
		finishModeB.setToolTipText(Messages.uiGet("RunnersPanel.FinishModeTooltip")); //$NON-NLS-1$
		finishModeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( finishModeOn() ) {
					disableRowSorting();
					sortRunnersByReadOrder();
				}
				else
					enableRowSorting();
			}
		});
		topPanel.add(finishModeB);
		
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
		filterField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {}
			@Override
			public void focusGained(FocusEvent e) {
				if( finishModeOn() ){
					finishModeB.doClick(); // disable Finish mode to enable filtering
				}
			}
		});
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
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
				"toggleFinishMode"); //$NON-NLS-1$
		getActionMap().put("toggleFinishMode", new AbstractAction() { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				finishModeB.doClick();
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
					announcer.announceSelectedRunnerChange(selectedData());
				}
			}
		});
		refreshTableData();
		return new JScrollPane(table);
	}

	private void enableRowSorting() {
		sorter = new TableRowSorter<RunnersTableModel>(tableModel);
		sorter.setComparator(1, new EcardComparator()); // Ecard column
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

	private boolean finishModeOn() {
		return finishModeB.isSelected();
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
			announcer.announceSelectedRunnerChange(selectedData());
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
	
	private RunnerRaceData selectedData() {
		int selectedRow = table.getSelectedRow();
		if( selectedRow!=-1 && table.getRowCount() > 0) {
			// we have to test the number of displayed rows too.
			// If user inputs a filter which matches nothing,
			// there is no row to show but table still points to the 0-index.
			Integer selectedId = (Integer) table.getValueAt(selectedRow, 0);
			return registry().findRunnerData(selectedId);
		}
		return null;
	}
	
	@Override
	public void changed(Stage previous, Stage next) {
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
	public void coursesetsChanged() {}

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
	public void registeredCard(String ecard) {
		focusOnRegisteredCard(ecard);
	}
	
	@Override
	public void rentedCard(String siIdent) {
//		JOptionPane.showMessageDialog(frame(), Messages.uiGet("RunnersPanel.RentedEcardMessage") + siIdent); //$NON-NLS-1$
	}


	private void focusOnReadCard(String ecard) {
		if( finishModeOn() ) {
			RunnerRaceData data = registry().findRunnerData(ecard);
			tableModel.removeData(data);
			tableModel.addDataFirst(data);
			focusTableOnIndex(0);
		}
	}

	private void focusOnRegisteredCard(String ecard) {
		RunnerRaceData data = registry().findRunnerData(ecard);
		focusTableOnRunner(data);
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
