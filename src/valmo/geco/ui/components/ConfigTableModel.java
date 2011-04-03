/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui.components;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import valmo.geco.model.Club;

/**
 * @author Simon Denier
 * @since Jan 24, 2009
 *
 */
public abstract class ConfigTableModel<T> extends AbstractTableModel {

	private String[] headers;
	
	private List<T> data;
	
		
	public ConfigTableModel(String[] headers) {
		setHeaders(headers);
	}

	public String[] getHeaders() {
		return headers;
	}

	public void setHeaders(String[] headers) {
		this.headers = headers;
	}

	public int getColumnCount() {
		return this.headers.length;
	}
	
	@Override
	public String getColumnName(int column) {
		return this.headers[column];
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
		fireTableDataChanged();
	}

	public int getRowCount() {
		return data.size();
	}
	
	public void addData(T data) {
		this.data.add(data);
		fireTableRowsInserted(this.data.size() - 1, this.data.size() - 1);
	}

	public void removeData(Club data) {
		int deleted = this.data.indexOf(data);
		this.data.remove(data);
		fireTableRowsDeleted(deleted, deleted);
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		T t= data.get(rowIndex);
		return getValueIn(t, columnIndex);
	}

	public Object getValueIn(T t, int columnIndex) {
		return "Pbm"; //$NON-NLS-1$
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		return true;
	}
	
	public void setValueAt(Object value, int row, int col) {
		T t= data.get(row);
		setValueIn(t, value, col);
        fireTableCellUpdated(row, col);
    }

	public abstract void setValueIn(T t, Object value, int col);
	
}
