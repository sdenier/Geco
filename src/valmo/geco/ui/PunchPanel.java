/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Date;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import valmo.geco.core.Html;
import valmo.geco.core.TimeManager;
import valmo.geco.model.RunnerRaceData;
import valmo.geco.model.Trace;

/**
 * @author Simon Denier
 * @since Jun 26, 2009
 *
 */
public class PunchPanel extends JPanel {

	private JTable punchesT;
	
	public PunchPanel() {
		punchesT = new JTable();
		initPunchPanel(this);
	}

	public JPanel initPunchPanel(JPanel panel) {
		panel.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(punchesT);
		scrollPane.setPreferredSize(new Dimension(250, 350));
		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	
	public void refreshPunches(RunnerRaceData runnerData) {
		final int[] codes = runnerData.getCourse().getCodes();
		final Trace[] trace = runnerData.getResult().getTrace();
		punchesT.setModel(new AbstractTableModel() {
			public Object getValueAt(int rowIndex, int columnIndex) {
				switch (columnIndex) {
				case 0:
					return rowIndex + 1;
				case 1:
					return (rowIndex < codes.length) ? codes[rowIndex] : "";					
				case 2:
					return traceLabel(trace, rowIndex);
				case 3:
					return traceTime(trace, rowIndex);
				default:
					return "";
				}
			}
			public String traceLabel(final Trace[] trace, int rowIndex) {
				String code = trace[rowIndex].getCode();
				if( code.startsWith("-") )
					return Html.htmlTag("font", "color=red", code);
				if( code.startsWith("+") )
					return Html.htmlTag("font", "color=blue", code);
				return code;
			}
			public String traceTime(final Trace[] trace, int rowIndex) {
				Date time = trace[rowIndex].getTime();
				if( time.getTime() == 0)
					return "";
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
					return "Nº";
				case 1:
					return "Course";
				case 2:
					return "Code";
				case 3:
					return "Time";
				default:
					return "";
				}
			}
		});
		TableColumnModel columnModel = punchesT.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(10);
		columnModel.getColumn(1).setPreferredWidth(10);
		columnModel.getColumn(2).setPreferredWidth(25);
		columnModel.getColumn(3).setPreferredWidth(25);
	}
	
}
