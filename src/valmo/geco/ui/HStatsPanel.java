/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import valmo.geco.Geco;
import valmo.geco.control.RegistryStats.StatItem;
import valmo.geco.core.Html;
import valmo.geco.core.Messages;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Sep 13, 2009
 *
 */
public class HStatsPanel extends StatsPanel {

	private AbstractTableModel courseTableModel;

	private String[] courseKeys;
	
	private StatItem[] statusKeys;

	private JCheckBox viewCh;
	
	/**
	 * @param geco
	 * @param frame
	 */
	public HStatsPanel(Geco geco, JFrame frame) {
		super(geco, frame);
		refreshTableKeys();
		initStatsPanel(this);
		startAutoUpdate();
	}


	@Override
	protected void updateTable() {
		courseTableModel.fireTableDataChanged();
	}

	protected void initStatsPanel(JPanel panel) {
		// control panel
		JPanel controlP = new JPanel();
		controlP.setLayout(new GridBagLayout());
		GridBagConstraints c = SwingUtils.gbConstr(1);
		c.insets = new Insets(0, 0, 10, 0);
		c.anchor = GridBagConstraints.CENTER;
		c.gridy = 0;
		controlP.add(Box.createRigidArea(new Dimension(200, 20)), c);
		
		viewCh = new JCheckBox(Messages.uiGet("StatsPanel.ShortViewLabel")); //$NON-NLS-1$
		viewCh.setToolTipText(Messages.uiGet("StatsPanel.ShortViewTooltip")); //$NON-NLS-1$
		viewCh.setSelected(true);
		viewCh.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if( viewCh.isSelected() ){
					statusKeys = stats().shortStatuses();
				} else {
					statusKeys = stats().longStatuses();
				}
				courseTableModel.fireTableStructureChanged();
			}
		});
		c.gridy = 1;
		controlP.add(viewCh, c);
		
		JButton refreshB = new JButton(Messages.uiGet("StatsPanel.RefreshLabel")); //$NON-NLS-1$
		refreshB.setToolTipText(Messages.uiGet("StatsPanel.RefreshTooltip")); //$NON-NLS-1$
		refreshB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stats().fullUpdate();
				updateTable();
			}
		});
		c.gridy = 2;
		controlP.add(refreshB, c);
		
		// Stats table
		courseTableModel = createCourseTableModel();
		JTable table = new JTable(courseTableModel);
		// cant use default sorter with html-formatted content
//		TableRowSorter<AbstractTableModel> sorter = new TableRowSorter<AbstractTableModel>(courseTableModel);
//		table.setRowSorter(sorter);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		JScrollPane tableSP = new JScrollPane(table);
		tableSP.setPreferredSize(new Dimension(550, 120));

		panel.setLayout(new BorderLayout());
		panel.add( tableSP, BorderLayout.CENTER );
		panel.add( SwingUtils.embed(controlP), BorderLayout.EAST );
		panel.add( Box.createHorizontalStrut(200), BorderLayout.WEST );
	}
	
	protected AbstractTableModel createCourseTableModel() {
		return new AbstractTableModel() {
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				String content;
				if( columnIndex==0 )
					content = courseKeys[rowIndex];
				else 
					content = stats().getCourseStatsFor(courseKeys[rowIndex],
														statusKeys[columnIndex-1]).toString();
				if( courseKeys[rowIndex]=="Total" ){ //$NON-NLS-1$
					return Html.htmlTag("b", content); //$NON-NLS-1$
				} else {
					return content;
				}

			}
			@Override
			public int getRowCount() {
				return courseKeys.length;
			}
			@Override
			public int getColumnCount() {
				return statusKeys.length + 1;
			}
			@Override
			public String getColumnName(int column) {
				if( column==0 )
					return Messages.uiGet("StatsPanel.CourseHeader"); //$NON-NLS-1$
				else
					return statusKeys[column-1].toString();
			}
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if( columnIndex==0 ){
					return String.class;
				} else {
					return Integer.class;
				}
			}
		};
	}
	
	protected void refreshTableKeys() {
		courseKeys = stats().sortedEntries();
		statusKeys = stats().shortStatuses();
	}

	@Override
	public void changed(Stage previous, Stage next) {
		refreshTableKeys();
		viewCh.setSelected(true);
//		courseTableModel.fireTableStructureChanged(); // called by viewCh item listener
	}


}
