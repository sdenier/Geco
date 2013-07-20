/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.live;

import java.awt.Component;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import net.geco.basics.TimeManager;
import net.geco.model.Messages;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;


/**
 * @author Simon Denier
 * @since Jan 24, 2009
 *
 */
public class ExtendedRunnersTableModel extends AbstractTableModel {

	private String[] headers;
	
	private List<RunnerRaceData> data;
	
	
	public ExtendedRunnersTableModel() {
		this.headers = new String[] {
				Messages.uiGet("RunnersTableModel.StartHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.EcardHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.FirstnameHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.LastnameHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.CategoryHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.CourseHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.ClubHeader"), //$NON-NLS-1$
				Messages.liveGet("ExtendedRunnersTableModel.RacetimeHeader"), //$NON-NLS-1$
				Messages.liveGet("ExtendedRunnersTableModel.MPHeader"), //$NON-NLS-1$
				Messages.liveGet("ExtendedRunnersTableModel.PenaltyHeader"), //$NON-NLS-1$
				Messages.liveGet("ExtendedRunnersTableModel.OfficialtimeHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.StatusHeader"), //$NON-NLS-1$
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

	public List<RunnerRaceData> getData() {
		return data;
	}

	public void setData(List<RunnerRaceData> data) {
		this.data = data;
		fireTableDataChanged();
	}

//	public void addDataFirst(RunnerRaceData data) {
//		this.data.add(0, data);
//		fireTableRowsInserted(0, 0);
//	}
//
//	public void addData(RunnerRaceData data) {
//		this.data.add(data);
//		fireTableRowsInserted(this.data.size() - 1, this.data.size() - 1);
//	}
//
//	public void removeData(RunnerRaceData data) {
//		int deleted = this.data.indexOf(data);
//		this.data.remove(data);
//		fireTableRowsDeleted(deleted, deleted);
//	}

	
	public int getRowCount() {
		return data.size();
	}

	private RunnerRaceData getRunnerData(int rowIndex) {
		return data.get(rowIndex);
	}

	private Runner getRunner(int rowIndex) {
		return getRunnerData(rowIndex).getRunner();
	}
	
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		Runner runner = getRunner(rowIndex);
		switch (columnIndex) {
		case 0: return runner.getStartId();
		case 1: return runner.getEcard();
		case 2: return runner.getFirstname();
		case 3: return runner.getLastname();
		case 4: return runner.getCategory().getShortname();
		case 5: return runner.getCourse().getName();
		case 6: return runner.getClub().getName();
		case 7: return TimeManager.time(getRunnerData(rowIndex).realRaceTime());
		case 8: return getRunnerData(rowIndex).getTraceData().getNbMPs();
		case 9: return getRunnerData(rowIndex).getResult().formatTimePenalty();
		case 10: return getRunnerData(rowIndex).getResult().formatRacetime();
		case 11: return getRunnerData(rowIndex).getResult().getStatus();
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
		case 8: return Integer.class;
		case 9: return String.class;
		case 10: return String.class;
		case 11: return Status.class;
		default: return Object.class;
		}
	}
	
	public void initTableColumnSize(JTable table) {
		TableColumnModel model = table.getColumnModel();
		for (int i = 0; i < headers.length; i++) {
			int width = 0;
			switch (i) {
			case 0: width = 30 ; break;
			case 1: width = 50 ;break;
			case 2: width = 100 ;break;
			case 3: width = 100 ;break;
			case 4: width = 50 ;break;
			case 5: width = 50 ;break;
			case 6: width = 50 ;break;
			case 7: width = 50 ;break;
			case 8: width = 30 ;break;
			case 9: width = 30 ;break;
			case 10: width = 50 ;break;
			case 11: width = 50 ;break;
			default: break;
			}
			model.getColumn(i).setPreferredWidth(width);
		}
	}
	
	public class RacetimeCellRenderer extends JLabel implements TableCellRenderer {
		public RacetimeCellRenderer() {
			setOpaque(true);
		}
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			setHorizontalAlignment(RIGHT);
			setText(value.toString());
			if( isSelected ) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				setBackground(null);
				setForeground(null);
			}
			return this;
		}
	}
	
	public static class StatusCellRenderer extends JLabel implements TableCellRenderer {
		public StatusCellRenderer() {
			setOpaque(true);
		}
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			setBackground( ((Status) value).color() );
			setHorizontalAlignment(CENTER);
			setText(value.toString());
			return this;
		}
	}

	public void initCellRenderers(JTable table) {
		TableColumnModel model = table.getColumnModel();
		// also init some specific cell renderers
		model.getColumn(7).setCellRenderer(new RacetimeCellRenderer());
		model.getColumn(9).setCellRenderer(new RacetimeCellRenderer());
		model.getColumn(10).setCellRenderer(new RacetimeCellRenderer());
		model.getColumn(11).setCellRenderer(new StatusCellRenderer());
	}

}
