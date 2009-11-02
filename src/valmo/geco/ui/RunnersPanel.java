/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import valmo.geco.core.Announcer;
import valmo.geco.core.Geco;
import valmo.geco.core.TimeManager;
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
public class RunnersPanel extends TabPanel implements Announcer.RunnerListener {
	
	private RunnersTableModel tableModel;
	private TableRowSorter<RunnersTableModel> sorter;	
	private JTable table;
	private JTextField filterField;
	
	private RunnerPanel runnerPanel;

	
	/**
	 * @param geco
	 * @param frame 
	 * @param announcer 
	 */
	public RunnersPanel(Geco geco, JFrame frame, Announcer announcer) {
		super(geco, frame, announcer);
		initRunnersPanel(this, announcer);
		announcer.registerRunnerListener(this);
	}


	public void initRunnersPanel(JPanel panel, Announcer announcer) {
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//		splitPane.setOneTouchExpandable(true);
		JPanel tablePan = new JPanel(new BorderLayout());
		tablePan.add(initTableScroll(), BorderLayout.CENTER);
		tablePan.add(initTopPanel(), BorderLayout.NORTH);
		splitPane.add(tablePan);
		splitPane.add(initBottomPanel(announcer));
		updateRunnerPanel();
		panel.add(splitPane);
	}
	
	public JTabbedPane initBottomPanel(Announcer announcer) {
		this.runnerPanel = new RunnerPanel(geco(), frame(), this);
		JTabbedPane pane = new JTabbedPane();
		pane.addTab("Runner Data", this.runnerPanel);
		pane.addTab("Stats", new StatsPanel(geco(), frame(), announcer));
		return pane;
	}

	public Box initTopPanel() {
		Box topPanel = Box.createHorizontalBox();
		JButton addButton = new JButton("+");
		addButton.setToolTipText("Create new runner");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco().runnerControl().createDummyRunner();
				// announce runner creation and add in tablemodel
				int newIndex = table.convertRowIndexToView(tableModel.getData().size()-1);
				table.getSelectionModel().setSelectionInterval(newIndex, newIndex);
				table.scrollRectToVisible(table.getCellRect(newIndex, 0, true));
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
					geco().runnerControl().deleteRunner(data);
					// announce runner deletion and remove from tablemodel
					table.getSelectionModel().setSelectionInterval(nextSelection, nextSelection);
				}
			}
		});
		topPanel.add(deleteButton);
		topPanel.add(new JCheckBox("Live mode"));
		topPanel.add(new JCheckBox("Auto mode"));
		topPanel.add(Box.createHorizontalStrut(400));
		topPanel.add(initFilterPanel());
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
		tableModel = new RunnersTableModel();
		refreshTableData();
		sorter = new TableRowSorter<RunnersTableModel>(tableModel);
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
		table = new JTable(tableModel);
		table.setRowSorter(sorter);
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().setSelectionInterval(0, 0);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting() ) {
					updateRunnerPanel();
				}
			}
		});
		return new JScrollPane(table);
	}


	public void refreshTableData() {
		tableModel.setData(new Vector<RunnerRaceData>(registry().getRunnersData()));
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
		if( !chip.equals("") )
			runnerPanel.updateRunner(chip);
	}
	
	private RunnerRaceData selectedData() {
		String chip = selectedChip();
		if( !chip.equals("") )
			return registry().findRunnerData(chip);
		return null;
	}


	@Override
	public void changed(Stage previous, Stage next) {
		refreshTableData();
		table.getSelectionModel().setSelectionInterval(0, 0);
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
	}

	@Override
	public void runnerDeleted(RunnerRaceData data) {
		tableModel.removeData(data);
	}


	@Override
	public void statusChanged(RunnerRaceData runner, Status oldStatus) {
		// TODO: this makes some refreshing process in RunnerPanel redundant
		// if RunnerPanel itself is at the origin of the event.
		int index = tableModel.getData().indexOf(runner);
		tableModel.fireTableRowsUpdated(index, index);
		if( table.convertRowIndexToView(index) == table.getSelectedRow() ) {
			updateRunnerPanel();	
		}
	}


	/* (non-Javadoc)
	 * @see valmo.geco.core.Announcer.RunnerListener#cardRead(java.lang.String)
	 */
	@Override
	public void cardRead(String chip) {
		// TODO Auto-generated method stub
		
	}

	
}
