/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.tabs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
		JRadioButton summaryB = new JRadioButton("Summary", true);
		summaryB.setToolTipText("Display a summary with most important stats");
		JRadioButton unresolvedB = new JRadioButton("Unresolved");
		unresolvedB.setToolTipText("Display a report with all unresolved statuses");
		JRadioButton resultsB = new JRadioButton("Results");
		resultsB.setToolTipText("Display a report with all definitive statuses");
		ButtonGroup reportGroup = new ButtonGroup();
		reportGroup.add(summaryB);
		reportGroup.add(unresolvedB);
		reportGroup.add(resultsB);
		summaryB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				courseTableModel.selectSummaryStatuses();
			}
		});
		unresolvedB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				courseTableModel.selectUnresolvedStatuses();
			}
		});
		resultsB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				courseTableModel.selectResultsStatuses();
			}
		});
	
		JButton refreshB = new JButton(Messages.uiGet("StatsPanel.RefreshLabel")); //$NON-NLS-1$
		refreshB.setToolTipText(Messages.uiGet("StatsPanel.RefreshTooltip")); //$NON-NLS-1$
		refreshB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stats().fullUpdate();
				updateTable();
			}
		});
		
		Box controlP = Box.createVerticalBox();
		controlP.add(summaryB);
		controlP.add(unresolvedB);
		controlP.add(resultsB);
		controlP.add(refreshB);
		controlP.add(clearLogB);
		
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

		private String[] courseKeys;
		
		private StatItem[] statusKeys;

		public HStatsTableModel() {
			selectSummaryStatuses();
			refreshCourseKeys();
		}

		public void selectSummaryStatuses() {
			refreshStatusKeys(stats().summaryStatuses());			
		}

		public void selectUnresolvedStatuses() {
			refreshStatusKeys(stats().unresolvedStatuses());
		}

		public void selectResultsStatuses() {
			refreshStatusKeys(stats().resultsStatuses());			
		}

		protected void refreshStatusKeys(StatItem[] statItems) {
			statusKeys = statItems;
			fireTableStructureChanged();
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
