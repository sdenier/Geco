/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.util.List;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import valmo.geco.core.TimeManager;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Jan 24, 2009
 *
 */
public class RunnersTableModel extends AbstractTableModel {

	private String[] headers;
	
	// we should have both?
	private List<RunnerRaceData> data;
	
	
	/**
	 * 
	 */
	public RunnersTableModel() {
		this.headers = new String[] {
				"Startnumber", "Chip", "First name", "Last name", "Category", "Course", "Club", "Racetime", "Status", "NC" 
		};
		this.data = new Vector<RunnerRaceData>();
	}
	
	public int getColumnCount() {
		return headers.length;
	}
	
	@Override
	public String getColumnName(int column) {
		return this.headers[column];
	}

	public int getRowCount() {
		return data.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		RunnerRaceData runnerData = data.get(rowIndex);
		Runner runner = runnerData.getRunner();
		// horrible, have to do a manual lookup with an index, 80's style
		// other solution would be a hashtable with key->function to evaluate - verbose in Java but less cumbersome to maintain
		switch (columnIndex) {
		case 0: return runner.getStartnumber();
		case 1: return runner.getChipnumber();
		case 2: return runner.getFirstname();
		case 3: return runner.getLastname();
		case 4: return runner.getCategory().getShortname();
		case 5: return runner.getCourse().getName();
		case 6: return runner.getClub().getName();
		case 7: return TimeManager.time(runnerData.getResult().getRacetime()); // optimize
		case 8: return runnerData.getResult().getStatus();
		case 9: return runner.isNC();
		default: return "Pbm";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0: return Integer.class;
		case 1: return Integer.class;
		case 2: return String.class;
		case 3: return String.class;
		case 4: return String.class;
		case 5: return String.class;
		case 6: return String.class;
		case 7: return String.class;
		case 8: return Status.class;
		case 9: return Boolean.class;
		default: return Object.class;
		}
	}

	public void initTableColumnSize(JTable table) {
		TableColumnModel model = table.getColumnModel();
		for (int i = 0; i < headers.length; i++) {
			int width = 0;
			switch (i) {
			case 0: width = 10 ; break;
			case 1: width = 20 ;break;
			case 2: width = 100 ;break;
			case 3: width = 100 ;break;
			case 4: width = 50 ;break;
			case 5: width = 50 ;break;
			case 6: width = 50 ;break;
			case 7: width = 20 ;break;
			case 8: width = 10 ;break;
			case 9: width = 10 ;break;
			default: break;
			}
			model.getColumn(i).setPreferredWidth(width);
		}
//		model.getColumn(8).setCellEditor(new DefaultCellEditor(new JComboBox(Status.values())));
	}

//	@Override
//	public boolean isCellEditable(int rowIndex, int columnIndex) {
//		return columnIndex>=8;
//	}

	public List<RunnerRaceData> getData() {
		return data;
	}


	public void setData(List<RunnerRaceData> data) {
		this.data = data;
		fireTableDataChanged();
	}
	
	public void addData(RunnerRaceData data) {
		this.data.add(data);
		fireTableRowsInserted(this.data.size() - 1, this.data.size() - 1);
	}

	public void removeData(RunnerRaceData data) {
		int deleted = this.data.indexOf(data);
		this.data.remove(data);
		fireTableRowsDeleted(deleted, deleted);
	}
	
}
