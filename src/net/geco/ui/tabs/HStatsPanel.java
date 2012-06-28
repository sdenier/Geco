/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.tabs;

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import net.geco.basics.Html;
import net.geco.control.RegistryStats;
import net.geco.control.RegistryStats.StatItem;
import net.geco.framework.IGecoApp;
import net.geco.model.Messages;
import net.geco.model.Stage;
import net.geco.ui.basics.SwingUtils;
import net.geco.ui.framework.StatsPanel;


/**
 * @author Simon Denier
 * @since Sep 13, 2009
 *
 */
public class HStatsPanel extends StatsPanel {

	private static final int STATS_HEIGHT = 120;
	private static final int STATS_WIDTH = 600;
	private static final int MARGIN = (800 - STATS_WIDTH) / 2;

	private HStatsTableModel courseTableModel;

	private JCheckBox viewCh;
	
	/**
	 * @param geco
	 * @param frame
	 * @param clearLogB 
	 */
	public HStatsPanel(IGecoApp geco, JFrame frame, JButton clearLogB) {
		super(geco, frame);
		initStatsPanel(this, clearLogB);
		startAutoUpdate();
	}


	@Override
	protected void updateTable() {
		courseTableModel.fireTableDataChanged();
	}

	protected void initStatsPanel(JPanel panel, JButton clearLogB) {
		// control panel
		JPanel controlP = new JPanel();
		controlP.setLayout(new GridBagLayout());
		GridBagConstraints c = SwingUtils.gbConstr();
		c.insets = new Insets(0, 10, 0, 0);
		controlP.add(Box.createRigidArea(new Dimension(MARGIN, 30)), c);
		
		viewCh = new JCheckBox(Messages.uiGet("StatsPanel.ShortViewLabel")); //$NON-NLS-1$
		viewCh.setToolTipText(Messages.uiGet("StatsPanel.ShortViewTooltip")); //$NON-NLS-1$
		viewCh.setSelected(true);
		viewCh.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if( viewCh.isSelected() ){
					courseTableModel.setShortStatuses();
				} else {
					courseTableModel.setLongStatuses();
				}
				courseTableModel.fireTableStructureChanged();
			}
		});
		c.gridy = 1;
		controlP.add(viewCh, c);
		
		c.gridy = 2;
		controlP.add(Box.createVerticalStrut(15), c);
		
		JButton refreshB = new JButton(Messages.uiGet("StatsPanel.RefreshLabel")); //$NON-NLS-1$
		refreshB.setToolTipText(Messages.uiGet("StatsPanel.RefreshTooltip")); //$NON-NLS-1$
		refreshB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stats().fullUpdate();
				updateTable();
			}
		});
		c.gridy = 3;
		controlP.add(refreshB, c);
		
		c.gridy = 4;
		controlP.add(clearLogB, c);
		
		// Stats table
		courseTableModel = createCourseTableModel();
		JTable table = new JTable(courseTableModel);
		// cant use default sorter with html-formatted content
//		TableRowSorter<AbstractTableModel> sorter = new TableRowSorter<AbstractTableModel>(courseTableModel);
//		table.setRowSorter(sorter);
		((JLabel) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		JScrollPane tableSP = new JScrollPane(table);
		tableSP.setPreferredSize(new Dimension(STATS_WIDTH, STATS_HEIGHT));

		panel.setLayout(new BorderLayout());
		panel.add( tableSP, BorderLayout.CENTER );
		panel.add( SwingUtils.embed(controlP), BorderLayout.WEST );
		panel.add( Box.createHorizontalStrut(MARGIN), BorderLayout.EAST );
	}

	protected HStatsTableModel createCourseTableModel() {
		return new HStatsTableModel();
	}
	
	@Override
	public void changed(Stage previous, Stage next) {
		courseTableModel.refreshCourseKeys();
	}

	public class HStatsTableModel extends AbstractTableModel {

		public HStatsTableModel() {
			setShortStatuses();
			refreshCourseKeys();
		}

		private String[] courseKeys;
		
		private StatItem[] statusKeys;

		public void setShortStatuses() {
			statusKeys = stats().shortStatuses();
		}
		
		public void setLongStatuses() {
			statusKeys = stats().longStatuses();
		}

		public void refreshCourseKeys() {
			courseKeys = stats().sortedEntries();
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			String content;
			if( columnIndex==0 )
				content = courseKeys[rowIndex];
			else 
				content = stats().getCourseStatsFor(courseKeys[rowIndex],
													statusKeys[columnIndex-1]).toString();
			if( courseKeys[rowIndex]==RegistryStats.totalName() ){
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
	}


}
