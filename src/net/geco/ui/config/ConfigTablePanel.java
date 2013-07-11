/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.config;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import net.geco.model.Messages;


/**
 * @author Simon Denier
 * @since Dec 24, 2009
 * 
 */
public class ConfigTablePanel<T> extends JPanel {

	private ConfigTableModel<T> tableModel;

	private JTable table;

	public ConfigTablePanel<T> initialize(ConfigTableModel<T> tableModel, Dimension tableDimension,
			JButton... actions) {
		initTable(tableModel, tableDimension);
		initButtonBar(actions);
		return this;
	}

	public ConfigTablePanel<T> initialize(String panelTitle, ConfigTableModel<T> tableModel,
			Dimension tableDimension, ActionListener addAction, ActionListener removeAction, JButton... moreActions) {
		initTable(tableModel, tableDimension);
		initButtonBar(panelTitle, addAction, removeAction, moreActions);
		return this;
	}
	
	private void initTable(ConfigTableModel<T> tableModel, Dimension tableDimension) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.tableModel = tableModel;
		table = new JTable(tableModel);
		((JLabel) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

		TableRowSorter<ConfigTableModel<T>> sorter = new TableRowSorter<ConfigTableModel<T>>(
				tableModel);
		table.setRowSorter(sorter);
		table.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		JScrollPane jsp = new JScrollPane(table);
		jsp.setPreferredSize(tableDimension);
		add(jsp);
	}

	private JPanel initButtonBar(JButton... actions) {
		JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEADING));
		buttonBar.setPreferredSize(new Dimension(250, 70));
		add(buttonBar);
		for (JButton button : actions) {
			buttonBar.add(button);
		}
		return buttonBar;
	}
	
	private JPanel initButtonBar(String panelTitle, ActionListener addAction,
			ActionListener removeAction, JButton... moreActions) {
		JButton addB = new JButton("+"); //$NON-NLS-1$
		addB.setToolTipText(Messages.uiGet("ConfigTablePanel.CreateTooltip") + panelTitle); //$NON-NLS-1$
		addB.addActionListener(addAction);
		JButton removeB = new JButton("-"); //$NON-NLS-1$
		removeB.setToolTipText(Messages.uiGet("ConfigTablePanel.DeleteTooltip") + panelTitle); //$NON-NLS-1$
		removeB.addActionListener(removeAction);

		JButton[] actionButtons = new JButton[moreActions.length + 2];
		actionButtons[0] = addB;
		actionButtons[1] = removeB;
		System.arraycopy(moreActions, 0, actionButtons, 2, moreActions.length);
		
		return initButtonBar(actionButtons);
	}
	
	public void refreshTableData(List<T> data) {
		tableModel.setData(data);
	}

	public T getSelectedData() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow != -1) {
			return tableModel.getData().get(table.convertRowIndexToModel(selectedRow));
		}
		return null;
	}
	
	public JTable table() {
		return table;
	}

}
