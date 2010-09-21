/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
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

import valmo.geco.Geco;

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

	public ConfigTablePanel<T> initialize(String panelTitle,
			ConfigTableModel<T> tableModel, ActionListener addAction,
			ActionListener removeAction) {
		
		initTable(panelTitle, tableModel);
		initButtonBar(panelTitle, addAction, removeAction);
		return this;
	}

	public ConfigTablePanel<T> initialize(String panelTitle,
			ConfigTableModel<T> tableModel, ActionListener addAction,
			ActionListener removeAction, JButton... moreActions) {
		
		initTable(panelTitle, tableModel);
		initButtonBar(panelTitle, addAction, removeAction, moreActions);
		return this;
	}

	private void initTable(String panelTitle, ConfigTableModel<T> tableModel) {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(panelTitle));
		this.tableModel = tableModel;
		table = new JTable(tableModel);

		TableRowSorter<ConfigTableModel<T>> sorter = new TableRowSorter<ConfigTableModel<T>>(
				tableModel);
		table.setRowSorter(sorter);
		table.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		JScrollPane jsp = new JScrollPane(table);
		jsp.setPreferredSize(new Dimension(75, 300));
		add(jsp, BorderLayout.CENTER);
	}
	
	private JPanel initButtonBar(String panelTitle, ActionListener addAction,
			ActionListener removeAction) {
		
		JPanel buttonBar = new JPanel(new GridLayout(0, 3));
		add(buttonBar, BorderLayout.SOUTH);
		
		JButton addB = new JButton("+");
		addB.setToolTipText("Create " + panelTitle);
		addB.addActionListener(addAction);
		buttonBar.add(addB);

		JButton removeB = new JButton("-");
		removeB.setToolTipText("Delete " + panelTitle);
		removeB.addActionListener(removeAction);
		buttonBar.add(removeB);
		
		return buttonBar;
	}
	
	private JPanel initButtonBar(String panelTitle, ActionListener addAction,
			ActionListener removeAction, JButton... moreActions) {
		
		JPanel buttonBar = initButtonBar(panelTitle, addAction, removeAction);		
		for (JButton button : moreActions) {
			buttonBar.add(button);
		}
		
		return buttonBar;
	}


	public void refreshTableData(List<T> data) {
		tableModel.setData(data);
	}

	public T getSelectedData() {
		int selectedRow = table.convertRowIndexToModel(table.getSelectedRow());
		if (selectedRow != -1) {
			return tableModel.getData().get(selectedRow);
		}
		return null;
	}

}
