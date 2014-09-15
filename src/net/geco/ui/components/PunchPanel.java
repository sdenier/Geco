/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import net.geco.basics.Html;
import net.geco.basics.TimeManager;
import net.geco.framework.IGeco;
import net.geco.model.Messages;
import net.geco.model.RunnerRaceData;
import net.geco.model.Section;
import net.geco.model.SectionTraceData;
import net.geco.model.Trace;
import net.geco.ui.framework.RunnersTableAnnouncer.RunnersTableListener;
import net.geco.ui.tabs.RunnersPanel;


/**
 * @author Simon Denier
 * @since Jun 26, 2009
 *
 */
public class PunchPanel extends JPanel implements RunnersTableListener {

	private JTable punchesT;
	private IGeco geco;
	private RunnersPanel parentContainer;
	
	public PunchPanel(IGeco geco, RunnersPanel parentContainer) {
		this.geco = geco;
		this.parentContainer = parentContainer;
		this.punchesT = new JTable();
		this.punchesT.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.punchesT.addMouseListener(new TableMouseListener(punchesT));
		initPunchPanel(this);
	}

	private void refreshSectionPopupMenu(final RunnerRaceData raceData) {
		JPopupMenu popupMenu = new JPopupMenu();
		
		for (final Section section : raceData.getCourse().getSections()) {
			JMenuItem sectionItem = new JMenuItem(section.displayString());
			popupMenu.add(sectionItem);
			
			sectionItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int selectedRow = punchesT.getSelectedRow();
					Trace trace = raceData.getTraceData().getTrace()[selectedRow];
					if ( ! trace.isTruePunch() ) {
						System.out.println("cant start section on MP " + trace);
					} else {
						geco.sectionManualChecker().refreshTraceWithUpdatedSection(raceData, section, selectedRow);
						parentContainer.refreshSelectionInTable();
						refreshPunches(raceData);
					}
				}
			});
		}
		
		punchesT.setComponentPopupMenu(popupMenu);
	}

	public JPanel initPunchPanel(JPanel panel) {
		panel.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(punchesT);
		scrollPane.setPreferredSize(new Dimension(250, 350));
		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	@Override
	public void selectedRunnerChanged(RunnerRaceData raceData) {
		if( raceData!=null ){
			refreshPunches(raceData);
			refreshSectionPopupMenu(raceData);
		}
	}

	public void refreshPunches(RunnerRaceData runnerData) {
		final Trace[] trace = runnerData.getTraceData().getTrace();
		final String[] sequence = new String[trace.length];
		int seq = 1;
		for (int i = 0; i < trace.length; i++) {
			if( trace[i].isAdded() ) { //$NON-NLS-1$
				sequence[i] = ""; //$NON-NLS-1$
			} else {
				sequence[i] = Integer.toString(seq);
				seq++;
			}
		}
		if( geco.getConfig().sectionsEnabled ) {
			setSectionsModel((SectionTraceData) runnerData.getTraceData(), trace, sequence);
		} else {
			setClassicModel(trace, sequence);
		}
	}

	private void setSectionsModel(final SectionTraceData sectionTrace, final Trace[] trace, final String[] sequence) {
		punchesT.setModel(new AbstractTableModel() {
			public Object getValueAt(int rowIndex, int columnIndex) {
				switch (columnIndex) {
				case 0:
					return sequence[rowIndex];
				case 1:
					return traceLabel(trace, rowIndex);
				case 2:
					return traceTime(trace, rowIndex);
				case 3:
					return sectionTrace.sectionLabelAt(rowIndex);
				default:
					return ""; //$NON-NLS-1$
				}
			}
			public String traceLabel(final Trace[] trace, int rowIndex) {
				String code = trace[rowIndex].getCode();
				if( trace[rowIndex].isMP() ) //$NON-NLS-1$
					return Html.htmlTag("font", "color=red", code); //$NON-NLS-1$ //$NON-NLS-2$
				if( trace[rowIndex].isAdded() ) //$NON-NLS-1$
					return Html.htmlTag("font", "color=blue", code); //$NON-NLS-1$ //$NON-NLS-2$
				return code;
			}
			public String traceTime(final Trace[] trace, int rowIndex) {
				Date time = trace[rowIndex].getTime();
				if( time.getTime() == 0)
					return ""; //$NON-NLS-1$
				return TimeManager.fullTime(time);
			}
			public int getRowCount() {
				return trace.length;
			}
			public int getColumnCount() {
				return 4;
			}
			@Override
			public String getColumnName(int column) {
				switch (column) {
				case 0:
					return Messages.uiGet("PunchPanel.NumLabel"); //$NON-NLS-1$
				case 1:
					return Messages.uiGet("PunchPanel.CodeLabel"); //$NON-NLS-1$
				case 2:
					return Messages.uiGet("PunchPanel.TimeLabel"); //$NON-NLS-1$
				case 3:
					return Messages.uiGet("PunchPanel.SectionLabel"); //$NON-NLS-1$
				default:
					return ""; //$NON-NLS-1$
				}
			}
		});
		TableColumnModel columnModel = punchesT.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(10);
		columnModel.getColumn(1).setPreferredWidth(50);
		columnModel.getColumn(2).setPreferredWidth(75);
		columnModel.getColumn(3).setPreferredWidth(75);
	}

	private void setClassicModel(final Trace[] trace, final String[] sequence) {
		punchesT.setModel(new AbstractTableModel() {
			public Object getValueAt(int rowIndex, int columnIndex) {
				switch (columnIndex) {
				case 0:
					return sequence[rowIndex];
				case 1:
					return traceLabel(trace, rowIndex);
				case 2:
					return traceTime(trace, rowIndex);
				default:
					return ""; //$NON-NLS-1$
				}
			}
			public String traceLabel(final Trace[] trace, int rowIndex) {
				String code = trace[rowIndex].getCode();
				if( trace[rowIndex].isMP() ) //$NON-NLS-1$
					return Html.htmlTag("font", "color=red", code); //$NON-NLS-1$ //$NON-NLS-2$
				if( trace[rowIndex].isAdded() ) //$NON-NLS-1$
					return Html.htmlTag("font", "color=blue", code); //$NON-NLS-1$ //$NON-NLS-2$
				return code;
			}
			public String traceTime(final Trace[] trace, int rowIndex) {
				Date time = trace[rowIndex].getTime();
				if( time.getTime() == 0)
					return ""; //$NON-NLS-1$
				return TimeManager.fullTime(time);
			}
			public int getRowCount() {
				return trace.length;
			}
			public int getColumnCount() {
				return 3;
			}
			@Override
			public String getColumnName(int column) {
				switch (column) {
				case 0:
					return Messages.uiGet("PunchPanel.NumLabel"); //$NON-NLS-1$
				case 1:
					return Messages.uiGet("PunchPanel.CodeLabel"); //$NON-NLS-1$
				case 2:
					return Messages.uiGet("PunchPanel.TimeLabel"); //$NON-NLS-1$
				default:
					return ""; //$NON-NLS-1$
				}
			}
		});
		TableColumnModel columnModel = punchesT.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(10);
		columnModel.getColumn(1).setPreferredWidth(75);
		columnModel.getColumn(2).setPreferredWidth(75);
	}
	
	public static class TableMouseListener extends MouseAdapter {
		private JTable table;

		public TableMouseListener(JTable table) {
	        this.table = table;
	    }
				
		@Override
		public void mouseReleased(MouseEvent event) {
	        Point point = event.getPoint();
	        int currentRow = table.rowAtPoint(point);
	        table.setRowSelectionInterval(currentRow, currentRow);
		}
	}
	
}
