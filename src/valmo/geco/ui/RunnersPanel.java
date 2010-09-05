/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import valmo.geco.Geco;
import valmo.geco.core.Announcer;
import valmo.geco.core.TimeManager;
import valmo.geco.live.GecoLiveComponent;
import valmo.geco.model.Course;
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
		implements Announcer.RunnerListener, Announcer.StageConfigListener {
	
	private JTable table;
	private RunnersTableModel tableModel;
	private TableRowSorter<RunnersTableModel> sorter;	
	private JTextField filterField;
	
	private JCheckBox liveB;
	private RunnerPanel runnerPanel;
	private GecoLiveComponent gecoLiveMap;

	
	/**
	 * @param geco
	 * @param frame 
	 * @param announcer 
	 */
	public RunnersPanel(Geco geco, JFrame frame) {
		super(geco, frame);
		initRunnersPanel(this);
		geco().announcer().registerRunnerListener(this);
		geco().announcer().registerStageConfigListener(this);
	}


	public void initRunnersPanel(JPanel panel) {
//		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//		splitPane.setOneTouchExpandable(true);
		JPanel tablePan = new JPanel(new BorderLayout());
		tablePan.add(initTableScroll(), BorderLayout.CENTER);
		tablePan.add(initTopPanel(), BorderLayout.NORTH);
		tablePan.add(initInfoPanel(), BorderLayout.EAST);
		panel.add(tablePan);
//		splitPane.add(tablePan);
//		splitPane.add(initInfoPanel());
//		panel.add(splitPane);
//		splitPane.setBorder(BorderFactory.createEmptyBorder());
//		updateRunnerPanel();
	}
	
	public JTabbedPane initInfoPanel() {
		this.runnerPanel = new RunnerPanel(geco(), frame(), this);
		JTabbedPane pane = new JTabbedPane();
		pane.addTab("Runner Data", this.runnerPanel);
		pane.addTab("Stats", new VStatsPanel(geco(), frame()));
		return pane;
	}

	public Box initTopPanel() {
		Box topPanel = Box.createHorizontalBox();
		JButton addButton = new JButton("+");
		addButton.setToolTipText("Create new runner");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// announce runner creation and add in tablemodel
				geco().runnerControl().createAnonymousRunner();
//				focusTableOnIndex(tableModel.getData().size()-1);
			}
		});
		topPanel.add(addButton);
		
		JButton deleteButton = new JButton("-");
		deleteButton.setToolTipText("Delete selected runner");
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RunnerRaceData data = selectedData();
				if( data!=null ) {
					int nextSelection = Math.max(0, table.getSelectedRow()-1);
					// announce runner deletion and remove from tablemodel
					geco().runnerControl().deleteRunner(data);
					table.getSelectionModel().setSelectionInterval(nextSelection, nextSelection);
				}
			}
		});
		topPanel.add(deleteButton);
		
		liveB = new JCheckBox("Live");
		liveB.setToolTipText("Enable live mode");
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

		final JCheckBox lockB = new JCheckBox("Lock");
		lockB.setToolTipText("Lock edition in table");
		lockB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( lockB.isSelected() )
					tableModel.lock();
				else
					tableModel.unlock();
			}
		});
		topPanel.add(lockB);

		//		topPanel.add(new JCheckBox("Auto mode"));
		topPanel.add(Box.createHorizontalStrut(500));
		topPanel.add(initFilterPanel());
		topPanel.setBorder(BorderFactory.createEtchedBorder());
		return topPanel;
	}

	
	public Box initFilterPanel() {
		Box filterPanel = Box.createHorizontalBox();
		filterPanel.add(new JLabel(" Find: "));
		filterField = new JTextField(20);
		filterField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    try {
					RowFilter<Object,Object> filter = RowFilter.regexFilter("(?i)" + filterField.getText());
					sorter.setRowFilter(filter);
					table.getSelectionModel().setSelectionInterval(0, 0);
			    } catch (java.util.regex.PatternSyntaxException e1) {
			        return;
			    }
			}
		});
		filterPanel.add(filterField);
//		filterPanel.add(Box.createHorizontalStrut(600));
		return filterPanel;
	}
	
	public JScrollPane initTableScroll() {
		tableModel = new RunnersTableModel(geco());
		table = new JTable(tableModel);
//		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setPreferredScrollableViewportSize(new Dimension(800, 600));
		tableModel.initCellEditors(table);
		tableModel.initTableColumnSize(table);
		enableRowSorting();
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().setSelectionInterval(0, 0);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting() ) {
					updateRunnerPanel();
				}
			}
		});
		refreshTableData();
		return new JScrollPane(table);
	}

	private void enableRowSorting() {
		sorter = new TableRowSorter<RunnersTableModel>(tableModel);
		sorter.setComparator(1, new Comparator<String>() { // Chip column
			@Override
			public int compare(String o1, String o2) {
				try {
					Integer n1 = new Integer(o1);
					Integer n2 = new Integer(o2);
					return n1.compareTo(n2);
				} catch (NumberFormatException e) {
					// TODO: hackish dealing with xxxxaa chip numbers
					return 0;
				}
			}
		});
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
		table.getSelectionModel().setSelectionInterval(0, 0);
//		runnerPanel.updateStageData();
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
	
	private String selectedChip() {
		String chip = "";
		int selectedRow = table.getSelectedRow();
		if( selectedRow!=-1 && table.getRowCount() > 0) {
			// we have to test the number of displayed rows too.
			// If user inputs a filter which matches nothins,
			// there is no row to show but table still points to the 0-index.
			chip = (String) table.getValueAt(selectedRow, 1);
		}
		return chip;
	}

	public void updateRunnerPanel() {
		String chip = selectedChip();
		if( !chip.equals("") ) {
			runnerPanel.updateRunner(chip);
			if( gecoLiveMap!=null && gecoLiveMap.isShowing() ) {
				gecoLiveMap.displayRunnerMap(registry().findRunnerData(chip));
			}
		}
	}
	
	private RunnerRaceData selectedData() {
		String chip = selectedChip();
		if( !chip.equals("") )
			return registry().findRunnerData(chip);
		return null;
	}
	
	public void openMapWindow() {
		if( gecoLiveMap==null ) {
			gecoLiveMap = new GecoLiveComponent().initWindow();
		}
		gecoLiveMap.openWindow();
	}


	@Override
	public void changed(Stage previous, Stage next) {
		refreshRunnersPanel();
	}


	@Override
	public void courseChanged(Runner runner, Course oldCourse) {
		// NOTE: for now, only RunnerPanel can change a runner's course and
		// produce events. We dont need to deal with this kind of event here
		// since it has already been taken care of.
//		int index = tableModel.getData().indexOf(registry().findRunnerData(runner));
//		tableModel.fireTableRowsUpdated(index, index);
//		if( table.convertRowIndexToView(index) == table.getSelectedRow() ) {
//			updateRunnerPanel();	
//		}
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
	public void cardRead(String chip) {
//		refresh made through statusChanged announcement
//		refreshTableRunner(registry().findRunnerData(chip));
		if( liveModeOn() ) {
			RunnerRaceData data = registry().findRunnerData(chip);
			tableModel.removeData(data);
			tableModel.addDataFirst(data);
			focusTableOnIndex(0);
		}
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
	
}
