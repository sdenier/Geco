/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import javax.swing.table.AbstractTableModel;

import valmo.geco.core.Messages;
import valmo.geco.model.ArchiveRunner;

/**
 * @author Simon Denier
 * @since Jan 24, 2009
 *
 */
public class ArchiveTableModel extends AbstractTableModel {

	private String[] headers;
	
	private ArchiveRunner[] data;

	public ArchiveTableModel() {
		this.headers = new String[] {
				"Id",
				Messages.uiGet("RunnersTableModel.EcardHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.FirstnameHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.LastnameHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.CategoryHeader"), //$NON-NLS-1$
				"Club ID",
				Messages.uiGet("RunnersTableModel.ClubHeader"), //$NON-NLS-1$
				"Year",
				"Sex"
		};
		this.data = new ArchiveRunner[0];
	}
	
	public int getColumnCount() {
		return headers.length;
	}
	
	@Override
	public String getColumnName(int column) {
		return this.headers[column];
	}

	public void setData(ArchiveRunner[] archiveRunners) {
		this.data = archiveRunners;
		fireTableDataChanged();
	}

	public int getRowCount() {
		return data.length;
	}
	
	private ArchiveRunner getRunner(int index) {
		return data[index];
	}

	
	public Object getValueAt(int rowIndex, int columnIndex) {
		ArchiveRunner runner = getRunner(rowIndex);
		switch (columnIndex) {
		case 0: return runner.getArchiveId();
		case 1: return runner.getChipnumber();
		case 2: return runner.getFirstname();
		case 3: return runner.getLastname();
		case 4: return runner.getCategory().getShortname();
		case 5: return runner.getClub().getShortname();
		case 6: return runner.getClub().getName();
		case 7: return runner.getBirthYear();
		case 8: return runner.getSex();
		default: return "Pbm"; //$NON-NLS-1$
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
		case 8: return String.class;
		default: return Object.class;
		}
	}
	
//	public void initTableColumnSize(JTable table) {
//		TableColumnModel model = table.getColumnModel();
//		for (int i = 0; i < headers.length; i++) {
//			int width = 0;
//			switch (i) {
//			case 0: width = 50 ; break;
//			case 1: width = 75 ;break;
//			case 2: width = 100 ;break;
//			case 3: width = 100 ;break;
//			case 4: width = 75 ;break;
//			case 5: width = 75 ;break;
//			case 6: width = 75 ;break;
//			case 7: width = 75 ;break;
//			case 8: width = 75 ;break;
//			case 9: width = 30 ;break;
//			default: break;
//			}
//			model.getColumn(i).setPreferredWidth(width);
//		}
//	}
	
}
