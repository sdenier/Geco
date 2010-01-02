/**
 * Copyright (c) 2009 Simon Denier
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
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import valmo.geco.control.RegistryStats;
import valmo.geco.core.Announcer;
import valmo.geco.core.Geco;
import valmo.geco.core.Util;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Sep 13, 2009
 *
 */
public class StatsPanel extends TabPanel implements Announcer.StageConfigListener {

	private AbstractTableModel courseTableModel;

	private String[] courseKeys;
	
	private String[] statusKeys;

	private Thread updateThread;

	private JCheckBox viewCh;
	
	/**
	 * @param geco
	 * @param frame
	 */
	public StatsPanel(Geco geco, JFrame frame) {
		super(geco, frame);
		refreshTableKeys();
		initStatsPanel(this);
		geco().announcer().registerStageConfigListener(this);
		updateThread = startAutoUpdate();
	}

	
	public Thread startAutoUpdate() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public synchronized void run() {
				while( true ){
					courseTableModel.fireTableDataChanged();
					try {
						wait(5000);
					} catch (InterruptedException e) {
						return;
					}					
				}
			}});
		thread.start();
		return thread;
	}
	
	
	private void initStatsPanel(JPanel panel) {
		panel.setLayout(new BorderLayout());
		
		JPanel controlP = new JPanel();
		controlP.setLayout(new GridBagLayout());
		GridBagConstraints c = Util.gbConstr(1);
		c.insets = new Insets(0, 0, 10, 0);
		c.anchor = GridBagConstraints.CENTER;
		c.gridy = 0;
		controlP.add(Box.createRigidArea(new Dimension(200, 20)), c);
		
		viewCh = new JCheckBox("Short view");
		viewCh.setToolTipText("Switch between short stats and full stats");
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
		
		JButton refreshB = new JButton("Refresh");
		refreshB.setToolTipText("Manually refresh stats");
		refreshB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stats().fullUpdate();
				courseTableModel.fireTableDataChanged();
			}
		});
		c.gridy = 2;
		controlP.add(refreshB, c);
		panel.add( Util.embed(controlP), BorderLayout.WEST );
		
		courseTableModel = createCourseTableModel();
		JTable table = new JTable(courseTableModel);
		// cant use default sorter with html-formatted content
//		TableRowSorter<AbstractTableModel> sorter = new TableRowSorter<AbstractTableModel>(courseTableModel);
//		table.setRowSorter(sorter);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		JScrollPane jsp = new JScrollPane(table);
		jsp.setPreferredSize(new Dimension(500, 250));
		panel.add( jsp, BorderLayout.CENTER );
		
		panel.add( Box.createHorizontalStrut(200), BorderLayout.EAST );
	}
	
	private AbstractTableModel createCourseTableModel() {
		return new AbstractTableModel() {
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				String content;
				if( columnIndex==0 )
					content = courseKeys[rowIndex];
				else 
					content = stats().getCourseStatsFor(	courseKeys[rowIndex],
														statusKeys[columnIndex-1]).toString();
				if( courseKeys[rowIndex]=="Total" ){
					return Util.inHtml(content, "b");
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
					return "Course";
				else
					return statusKeys[column-1];
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

	private RegistryStats stats() {
		return geco().registryStats();
	}
	
	private void refreshTableKeys() {
		courseKeys = stats().sortedEntries();
		statusKeys = stats().shortStatuses();
	}
	
	@Override
	public void saving(Stage stage, Properties properties) {
	}

	@Override
	public void changed(Stage previous, Stage next) {
		refreshTableKeys();
		viewCh.setSelected(true);
//		courseTableModel.fireTableStructureChanged(); // called by viewCh item listener
	}

	@Override
	public void closing(Stage stage) {
		updateThread.interrupt();
		try {
			updateThread.join();
		} catch (InterruptedException e) {
			geco().logger().debug(e);
		}
	}

	
	@Override
	public void categoriesChanged() {}

	@Override
	public void clubsChanged() {}

	@Override
	public void coursesChanged() {
		changed(null, null);
	}

	
}
