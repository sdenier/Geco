/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.tabs;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import net.geco.control.RunnerControl;
import net.geco.framework.IGeco;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;
import net.geco.model.Status;


/**
 * @author Simon Denier
 * @since Jan 24, 2009
 *
 */
public class RunnersTableModel extends AbstractTableModel {

	private static final int NUMBER_WIDTH = 50;
	private static final int FIELD_WIDTH = 70;
	private static final int NAME_WIDTH = 100;

	private IGeco geco;
	private String[] headers;
	private List<RunnerRaceData> data;

	private int clickCountToEdit;
	private boolean useDefaultCourseForCategory;
	private DefaultCellEditor categoryEditor;
	private DefaultCellEditor courseEditor;
	private DefaultCellEditor clubEditor;
	private DefaultCellEditor statusEditor;

	
	public RunnersTableModel(IGeco geco) {
		this.geco = geco;
		this.headers = new String[] {
				Messages.uiGet("RunnersTableModel.StartHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.EcardHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.FirstnameHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.LastnameHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.CategoryHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.CourseHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.ClubHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.TimeHeader"), //$NON-NLS-1$
				Messages.uiGet("RunnersTableModel.StatusHeader"), //$NON-NLS-1$
		};
		this.data = new Vector<RunnerRaceData>();
		this.clickCountToEdit = 2;
		this.useDefaultCourseForCategory = false;
	}
	
	private RunnerControl control() {
		return geco.runnerControl();
	}
	
	private Registry registry() {
		return geco.registry();
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

	public void addDataFirst(RunnerRaceData data) {
		this.data.add(0, data);
		fireTableRowsInserted(0, 0);
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
		case 7: return getRunnerData(rowIndex).getResult().formatResultTime();
		case 8: return getRunnerData(rowIndex).getResult().getStatus();
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
		case 8: return Status.class;
		default: return Object.class;
		}
	}
	
	public void initTableColumnSize(JTable table) {
		TableColumnModel model = table.getColumnModel();
		for (int i = 0; i < headers.length; i++) {
			int width = 0;
			switch (i) {
			case 0: width = NUMBER_WIDTH ; break;
			case 1: width = FIELD_WIDTH ;break;
			case 2: width = NAME_WIDTH ;break;
			case 3: width = NAME_WIDTH ;break;
			case 4: width = FIELD_WIDTH ;break;
			case 5: width = FIELD_WIDTH ;break;
			case 6: width = FIELD_WIDTH ;break;
			case 7: width = FIELD_WIDTH ;break;
			case 8: width = FIELD_WIDTH ;break;
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

	public void initCellEditors(JTable table) {
		TableColumnModel model = table.getColumnModel();
		model.getColumn(0).setCellEditor(new StartIdEditor());
		model.getColumn(1).setCellEditor(new EcardEditor());
		model.getColumn(2).setCellEditor(new FirstnameEditor());
		model.getColumn(3).setCellEditor(new LastnameEditor());
		model.getColumn(7).setCellEditor(new RacetimeEditor());
		statusEditor = new DefaultCellEditor(new JComboBox(Status.values()));
		model.getColumn(8).setCellEditor(statusEditor);
		updateComboBoxEditors(table); // init cell clicks to edit

		// also init some specific cell renderers
		model.getColumn(7).setCellRenderer(new RacetimeCellRenderer());
		model.getColumn(8).setCellRenderer(new StatusCellRenderer());
	}

	protected void updateComboBoxEditors(JTable table) {
		TableColumnModel model = table.getColumnModel();
		categoryEditor = new DefaultCellEditor(new JComboBox(registry().getSortedCategoryNames().toArray()));
		model.getColumn(4).setCellEditor(categoryEditor);
		courseEditor = new DefaultCellEditor(new JComboBox(registry().getSortedCourseNames().toArray()));
		model.getColumn(5).setCellEditor(courseEditor);
		clubEditor = new DefaultCellEditor(new JComboBox(registry().getSortedClubNames().toArray()));
		model.getColumn(6).setCellEditor(clubEditor);
		// (re)set clicks for new cell editors
		setClickCountToEdit(clickCountToEdit);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}
	
	public void enableFastEdition() {
		setClickCountToEdit(1);
	}

	public void disableFastEdition() {
		setClickCountToEdit(2);
	}
	
	private void setClickCountToEdit(int clicks) {
		clickCountToEdit = clicks;
		categoryEditor.setClickCountToStart(clicks);
		courseEditor.setClickCountToStart(clicks);
		clubEditor.setClickCountToStart(clicks);
		statusEditor.setClickCountToStart(clicks);
	}
	
	public void enableDefaultCourseForCategory(boolean flag){
		useDefaultCourseForCategory = flag;
	}
	

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0: break;
		case 1: break;
		case 2: break;
		case 3: break;
		case 4: setCategory(getRunnerData(rowIndex), (String) aValue); break;
		case 5: setCourse(getRunnerData(rowIndex), (String) aValue); break;
		case 6: setClub(getRunner(rowIndex), (String) aValue); break;
		case 7: break;
		case 8: setStatus(getRunnerData(rowIndex), (Status) aValue); break;
		default: break;
		}
	}

	private void setCategory(RunnerRaceData runnerData, String newCategory) {
		Runner runner = runnerData.getRunner();
		control().validateCategory(runner, newCategory);
		// check course even if no change in category
		if( useDefaultCourseForCategory ){
			Course catCourse = runner.getCategory().getCourse();
			if( catCourse!=null ){
				control().validateCourse(runnerData, catCourse.getName());
			}
		}
	}

	private void setCourse(RunnerRaceData runnerData, String newCourse) {
		control().validateCourse(runnerData, newCourse);
	}
	
	private void setClub(Runner runner, String newClub) {
		control().validateClub(runner, newClub);
	}
	
	private void setStatus(RunnerRaceData runnerData, Status newStatus) {
		control().validateStatus(runnerData, newStatus);
	}
	

	public abstract class RunnersTableEditor extends AbstractCellEditor implements TableCellEditor {

		protected Runner selectedRunner;
		protected JTextField editField;
		
		public RunnersTableEditor() {
			editField = new JTextField();
			editField.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if( validateInput() ) {
						fireEditingStopped();
					} else {
						editField.setBorder(new LineBorder(Color.red));
					}
				}
			});
		}
		
		@Override
		public boolean stopCellEditing() {
			if( validateInput() ) {
				return super.stopCellEditing();
			} else {
				editField.setBorder(new LineBorder(Color.red));
				return false;
			}
		}
		
		public abstract boolean validateInput();

		public String getValue() {
			return editField.getText();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {
			selectedRunner = getRunner(table.convertRowIndexToModel(row));
			editField.setText((String) getCellEditorValue());
			editField.setBorder(new LineBorder(Color.black));
			return editField;
		}

		@Override
		public boolean isCellEditable(EventObject e) {
		    if (e instanceof MouseEvent) { 
				return ((MouseEvent) e).getClickCount() >= clickCountToEdit;
			}
		    return true;
		}
		
	}


	public class StartIdEditor extends RunnersTableEditor {
		@Override
		public Object getCellEditorValue() {
			return selectedRunner.getStartId().toString();
		}
		@Override
		public boolean validateInput() {
			return control().validateStartId(selectedRunner, getValue());
		}
	}
	
	public class EcardEditor extends RunnersTableEditor {
		@Override
		public Object getCellEditorValue() {
			return selectedRunner.getEcard();
		}
		@Override
		public boolean validateInput() {
			return control().validateEcard(selectedRunner, getValue());
		}
	}

	public class FirstnameEditor extends RunnersTableEditor {
		@Override
		public Object getCellEditorValue() {
			return selectedRunner.getFirstname();
		}
		@Override
		public boolean validateInput() {
			return control().validateFirstname(selectedRunner, getValue());
		}
	}
	
	public class LastnameEditor extends RunnersTableEditor {
		@Override
		public Object getCellEditorValue() {
			return selectedRunner.getLastname();
		}
		@Override
		public boolean validateInput() {
			return control().validateLastname(selectedRunner, getValue());
		}
	}
	
	public class RacetimeEditor extends RunnersTableEditor {
		private RunnerRaceData selectedData;
		@Override
		public Object getCellEditorValue() {
			return selectedData.getResult().formatResultTime();
		}
		@Override
		public boolean validateInput() {
			return control().validateResultTime(selectedData, getValue());
		}
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {
			selectedData = getRunnerData(table.convertRowIndexToModel(row));
			return super.getTableCellEditorComponent(table, value, isSelected, row, column);
		}
	}
	
}
