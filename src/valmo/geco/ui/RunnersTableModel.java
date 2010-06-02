/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

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
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import valmo.geco.control.RunnerControl;
import valmo.geco.core.Geco;
import valmo.geco.core.TimeManager;
import valmo.geco.model.Registry;
import valmo.geco.model.Runner;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Status;

/**
 * @author Simon Denier
 * @since Jan 24, 2009
 *
 */
public class RunnersTableModel extends AbstractTableModel {

	private Geco geco;

	private String[] headers;
	
	private List<RunnerRaceData> data;

	private boolean isEditable;
	
	
	public RunnersTableModel(Geco geco) {
		this.geco = geco;
		this.headers = new String[] {
				"Startnumber", "Chip", "First name", "Last name", "Category", "Course", "Club", "Racetime", "Status", "NC" 
		};
		this.data = new Vector<RunnerRaceData>();
		unlock();
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
		case 0: return runner.getStartnumber();
		case 1: return runner.getChipnumber();
		case 2: return runner.getFirstname();
		case 3: return runner.getLastname();
		case 4: return runner.getCategory().getShortname();
		case 5: return runner.getCourse().getName();
		case 6: return runner.getClub().getName();
		case 7: return TimeManager.time(getRunnerData(rowIndex).getResult().getRacetime()); // optimize
		case 8: return getRunnerData(rowIndex).getResult().getStatus();
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
	}
	
	public static class StatusCellRenderer extends JLabel implements TableCellRenderer {
		public StatusCellRenderer() {
			setOpaque(true);
		}
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Color bg = Color.white;
			Status status = (Status) value;
			switch (status) {
			case OK: bg = new Color(0.6f, 1, 0.6f); break;
			case MP: bg = new Color(0.75f, 0.5f, 0.75f); break;
			case Unknown: bg = new Color(1, 1, 0.5f); break;
			default: break;
			}
			setBackground(bg);
			setHorizontalAlignment(CENTER);
			setText(value.toString());
			return this;
		}
	}

	public void initCellEditors(JTable table) {
		TableColumnModel model = table.getColumnModel();
		model.getColumn(0).setCellEditor(new StartnumberEditor());
		model.getColumn(1).setCellEditor(new ChipnumberEditor());
		model.getColumn(3).setCellEditor(new LastnameEditor());
		updateComboBoxEditors(table);
		model.getColumn(7).setCellEditor(new RacetimeEditor());
		model.getColumn(8).setCellEditor(new DefaultCellEditor(new JComboBox(Status.values())));
		// also init cell renderer for Status cell
		model.getColumn(8).setCellRenderer(new StatusCellRenderer());
	}

	protected void updateComboBoxEditors(JTable table) {
		TableColumnModel model = table.getColumnModel();
		model.getColumn(4).setCellEditor(new DefaultCellEditor(new JComboBox(registry().getSortedCategorynames())));
		model.getColumn(5).setCellEditor(new DefaultCellEditor(new JComboBox(registry().getSortedCoursenames())));
		model.getColumn(6).setCellEditor(new DefaultCellEditor(new JComboBox(registry().getSortedClubnames())));
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return isEditable;
	}
	
	public void lock() {
		isEditable = false;
	}

	public void unlock() {
		isEditable = true;
	}
	

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0: break;
		case 1: break;
		case 2: setFirstName(getRunner(rowIndex), (String) aValue); break;
		case 3: break;
		case 4: setCategory(getRunner(rowIndex), (String) aValue); break;
		case 5: setCourse(getRunnerData(rowIndex), (String) aValue); break;
		case 6: setClub(getRunner(rowIndex), (String) aValue); break;
		case 7: break;
		case 8: setStatus(getRunnerData(rowIndex), (Status) aValue); break;
		case 9: setNC(getRunner(rowIndex), ((Boolean) aValue).booleanValue() ); break;
		default: break;
		}
	}


	private void setFirstName(Runner runner, String newName) {
		control().validateFirstname(runner, newName);
	}
	
	private void setCategory(Runner runner, String newCategory) {
		control().validateCategory(runner, newCategory);
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
	
	private void setNC(Runner runner, boolean nc) {
		control().validateNCStatus(runner, nc);
	}
	

	public abstract class RunnersTableEditor extends AbstractCellEditor implements TableCellEditor {

		protected Runner selectedRunner;
		protected JTextField editField;
		
		public RunnersTableEditor() {
			editField = new JTextField();
			editField.addActionListener(getActionListener());
			editField.setInputVerifier(getInputVerifier());
		}
		
		public String getValue() {
			return editField.getText();
		}
		
		public abstract ActionListener getActionListener();

		public abstract InputVerifier getInputVerifier();

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
				return ((MouseEvent) e).getClickCount() >= 2;
			}
		    return true;
		}
		
	}


	public class StartnumberEditor extends RunnersTableEditor {
		@Override
		public Object getCellEditorValue() {
			return Integer.toString(selectedRunner.getStartnumber());
		}
		@Override
		public ActionListener getActionListener() {
			return new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						if( control().validateStartnumber(selectedRunner, getValue())) {
							fireEditingStopped();
						} else {
							editField.setBorder(new LineBorder(Color.red));
							JOptionPane.showMessageDialog(null,
								    "Start number already used.",
								    "Invalid Entry",
								    JOptionPane.ERROR_MESSAGE);
						}					
					} catch (NumberFormatException ex) {
						editField.setBorder(new LineBorder(Color.red));
						JOptionPane.showMessageDialog(null,
							    "Bad format for start number.",
							    "Invalid Entry",
							    JOptionPane.ERROR_MESSAGE);
					}
				}
			};
		}
		@Override
		public InputVerifier getInputVerifier() {
			return new InputVerifier() {
				@Override
				public boolean shouldYieldFocus(JComponent input) {
					try {
						return control().validateStartnumber(selectedRunner, getValue());	
					} catch (NumberFormatException e) {
						return false; // "Bad format for start number."
					}
				}
				@Override
				public boolean verify(JComponent input) {
					return control().verifyStartnumber(selectedRunner, getValue());
				}
			};
		}
	}
	
	public class ChipnumberEditor extends RunnersTableEditor {
		@Override
		public Object getCellEditorValue() {
			return selectedRunner.getChipnumber();
		}
		@Override
		public ActionListener getActionListener() {
			return new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if( control().validateChipnumber(selectedRunner, getValue())) {
						fireEditingStopped();
					} else {
						editField.setBorder(new LineBorder(Color.red));
						JOptionPane.showMessageDialog(null,
								"Chip number empty or already used.",
								"Invalid Entry",
								JOptionPane.ERROR_MESSAGE);
					}			
				}
			};
		}
		@Override
		public InputVerifier getInputVerifier() {
			return new InputVerifier() {
				@Override
				public boolean shouldYieldFocus(JComponent input) {
					return control().validateChipnumber(selectedRunner, getValue());	
				}
				@Override
				public boolean verify(JComponent input) {
					return control().verifyChipnumber(selectedRunner, getValue());
				}
			};
		}
	}
	
	public class LastnameEditor extends RunnersTableEditor {
		@Override
		public Object getCellEditorValue() {
			return selectedRunner.getLastname();
		}
		@Override
		public ActionListener getActionListener() {
			return new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if( control().validateLastname(selectedRunner, getValue()) ) {
						fireEditingStopped();
					} else {
						editField.setBorder(new LineBorder(Color.red));
						JOptionPane.showMessageDialog(null,
							    "Last name can not be empty.",
							    "Invalid Entry",
							    JOptionPane.ERROR_MESSAGE);
					}
				}
			};
		}
		@Override
		public InputVerifier getInputVerifier() {
			return new InputVerifier() {
				@Override
				public boolean shouldYieldFocus(JComponent input) {
					return control().validateLastname(selectedRunner, getValue());
				}
				@Override
				public boolean verify(JComponent input) {
					return control().verifyLastname(getValue());
				}
			};
		}
	}
	
	public class RacetimeEditor extends RunnersTableEditor {
		private RunnerRaceData selectedData;
		@Override
		public Object getCellEditorValue() {
			return TimeManager.time(selectedData.getResult().getRacetime());
		}
		@Override
		public ActionListener getActionListener() {
			return new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if( control().validateRaceTime(selectedData, getValue()) ){
						fireEditingStopped();
					} else {
						editField.setBorder(new LineBorder(Color.red));
						JOptionPane.showMessageDialog(null,
							    "Bad time format.",
							    "Invalid Entry",
							    JOptionPane.ERROR_MESSAGE);
					}
				}
			};
		}
		@Override
		public InputVerifier getInputVerifier() {
			return  new InputVerifier() {
				@Override
				public boolean shouldYieldFocus(JComponent input) {
					return control().validateRaceTime(selectedData, getValue());
				}
				@Override
				public boolean verify(JComponent input) {
					return control().verifyRaceTime(getValue());
				}
			};
		}
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {
			selectedData = getRunnerData(table.convertRowIndexToModel(row));
			return super.getTableCellEditorComponent(table, value, isSelected, row, column);
		}
	}
	
}
