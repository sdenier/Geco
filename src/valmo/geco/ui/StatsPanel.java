/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.ui;

import java.util.Arrays;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import valmo.geco.control.RegistryStats;
import valmo.geco.core.Announcer;
import valmo.geco.core.Geco;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Sep 13, 2009
 *
 */
public class StatsPanel extends TabPanel implements Announcer.StageConfigListener {

	private AbstractTableModel courseTableModel;

	private String[] courseKeys;

	private Thread updateThread;
	
	/**
	 * @param geco
	 * @param frame
	 */
	public StatsPanel(Geco geco, JFrame frame, Announcer announcer) {
		super(geco, frame, announcer);
		initStatsPanel(this);
		announcer.registerStageConfigListener(this);
		changed(null, null);
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
		courseTableModel = createCourseTableModel();
		panel.add( new JScrollPane(new JTable(courseTableModel)) );
	}

	private RegistryStats stats() {
		return geco().registryStats();
	}
	
	private AbstractTableModel createCourseTableModel() {
		return new AbstractTableModel() {
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				if( columnIndex==0 )
					return courseKeys[rowIndex];
				else 
					return stats().getCourseStatsFor(	courseKeys[rowIndex],
														stats().statuses()[columnIndex-1].toString());
			}
			@Override
			public int getRowCount() {
				return stats().entries().size();
			}
			@Override
			public int getColumnCount() {
				return stats().statuses().length + 1;
			}
			@Override
			public String getColumnName(int column) {
				if( column==0 )
					return "Course";
				else
					return stats().statuses()[column-1].toString();
			}
		};
	}

	
	@Override
	public void saving(Stage stage, Properties properties) {
	}

	@Override
	public void changed(Stage previous, Stage next) {
		courseKeys = stats().entries().toArray(new String[0]);
		Arrays.sort(courseKeys);
		courseTableModel.fireTableStructureChanged();
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
