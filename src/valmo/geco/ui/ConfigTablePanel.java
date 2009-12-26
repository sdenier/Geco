/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import valmo.geco.core.Geco;


/**
 * @author Simon Denier
 * @since Dec 24, 2009
 *
 */
public class ConfigTablePanel<T> extends GecoPanel {

	private ConfigTableModel<T> tableModel;
	private JTable table;

	public ConfigTablePanel(Geco geco, JFrame frame) {
		super(geco, frame);
	}
	
	public ConfigTablePanel<T> initialize(	String panelTitle,
											ConfigTableModel<T> tableModel,
											ActionListener addAction,
											ActionListener removeAction) {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(panelTitle));
		this.tableModel = tableModel;
		table = new JTable(tableModel);

//		TableColumnModel columns = table.getColumnModel();
//		for (int i = 0; i <= columns.getColumnCount(); i++) {
//			JTextField textField = new JTextField();
//			textField.setInputVerifier(new InputVerifier() {
//				@Override
//				public boolean verify(JComponent input) {
//					return false;
//				}
//			});
//			switch(i) {
//				case 0: columns.getColumn(i).setCellEditor(new DefaultCellEditor(textField));
//			}
//		}

		TableRowSorter<ConfigTableModel<T>> sorter = new TableRowSorter<ConfigTableModel<T>>(tableModel);
		table.setRowSorter(sorter);
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		JScrollPane jsp = new JScrollPane(table);
		jsp.setPreferredSize(new Dimension(75, 300));
		add(jsp, BorderLayout.CENTER);
		
		JPanel buttonBar = new JPanel();
		JButton addB = new JButton("+");
		addB.setToolTipText("Create " + panelTitle);
		addB.addActionListener(addAction);
		buttonBar.add(addB);
		
		JButton removeB = new JButton("-");
		removeB.setToolTipText("Delete " + panelTitle);
		removeB.addActionListener(removeAction);
		buttonBar.add(removeB);
		add(buttonBar, BorderLayout.SOUTH);
		return this;
	}
	
	public void refreshTableData(List<T> data) {
		tableModel.setData(data);
	}
	
	public T getSelectedData() {
		int selectedRow = table.getSelectedRow();
		if( selectedRow!=-1 ) {
			return tableModel.getData().get(selectedRow);
		}
		return null;
	}
	
}
