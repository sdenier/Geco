/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import valmo.geco.control.TimeManager;
import valmo.geco.control.PenaltyChecker.Trace;
import valmo.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Jun 26, 2009
 *
 */
public class PunchPanel extends GecoPanel {

	private JTable punchesT;
	
	/**
	 * @param geco
	 * @param frame
	 */
	public PunchPanel(Geco geco, JFrame frame) {
		super(geco, frame);
		punchesT = new JTable();
		initPunchPanel(this);
	}

	public JPanel initPunchPanel(JPanel panel) {
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Punches"));
		JScrollPane scrollPane = new JScrollPane(punchesT);
		scrollPane.setPreferredSize(new Dimension(300, 250));
		panel.add(scrollPane, BorderLayout.NORTH);
//		JPanel butPanel = new JPanel();
//		butPanel.add(new JButton("Save"));
//		butPanel.add(new JButton("Cancel"));
//		panel.add(butPanel, BorderLayout.SOUTH);
		return panel;
	}

	
	public void refreshPunches(RunnerRaceData runnerData) {
		final int[] codes = runnerData.getCourse().getCodes();
//		final Punch[] punches = runnerData.getPunches();
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
//					return (rowIndex < trace.length) ? trace[rowIndex].getCode() : "";					
				case 3:
					return traceTime(trace, rowIndex);
//					return (rowIndex < trace.length) ? TimeManager.fullTime(trace[rowIndex].getTime()) : "";
				default:
					return "";
				}
			}
			public String traceLabel(final Trace[] trace, int rowIndex) {
				String code = trace[rowIndex].getCode();
				if( code.startsWith("-") )
					return "<html><font color=red>" + code + "</font></html>";
				if( code.startsWith("+") )
					return "<html><font color=blue>" + code + "</font></html>";
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
//				return Math.max(codes.length, trace.length);
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
	}
	
	
}
