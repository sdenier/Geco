/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;

import valmo.geco.control.RegistryStats;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Sep 13, 2009
 *
 */
public class LivePanel extends TabPanel {

	private JTextArea chiplogArea;
	
	private AbstractTableModel stageTableModel;

	private AbstractTableModel courseTableModel;

	private String[] stageKeys;

	private String[] courseKeys;

	private Thread updateThread;
	
	/**
	 * @param geco
	 * @param frame
	 */
	public LivePanel(Geco geco, JFrame frame, Announcer announcer) {
		super(geco, frame, announcer);
		initLivePanel(this);
		changed(null, null);
		updateThread = startAutoUpdate();
	}

	
	public Thread startAutoUpdate() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public synchronized void run() {
				while( true ){
					stageTableModel.fireTableDataChanged();
					courseTableModel.fireTableDataChanged();
					System.out.println("sent");
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
	
	// Needs two tables where tablemodels are 1) status+extra 2) courses
	// for each table the getObject retrieves data from registrystats
	
	
	public void initLivePanel(JPanel panel) {
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setOneTouchExpandable(true);
		splitPane.add(initChipPanel());
		splitPane.add(initStatsPanel());
		panel.add(splitPane);
	}
	
	public JPanel initChipPanel() {
		chiplogArea = new JTextArea(30, 80);
		chiplogArea.setText("Waiting for data...");
		JButton clearB = new JButton("Clear view");
		clearB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chiplogArea.setText("");
			}
		});
		chiplogArea.setEditable(false);
		JPanel chipPanel = new JPanel(new BorderLayout());
		chipPanel.add(Util.embed(clearB), BorderLayout.NORTH);
		chipPanel.add(new JScrollPane(chiplogArea), BorderLayout.CENTER);
		return chipPanel;
	}

	
	/**
	 * @return
	 */
	private JPanel initStatsPanel() {
		JPanel panel = new JPanel(new GridLayout(0,2));
		stageTableModel = createStageTableModel();
		courseTableModel = createCourseTableModel();
		JTable stageTable = new JTable(stageTableModel);
		panel.add( new JScrollPane(stageTable) );
		panel.add( new JScrollPane(new JTable(courseTableModel)) );
		return panel;
		
	}

	private RegistryStats stats() {
		return geco().registryStats();
	}
	
	public AbstractTableModel createStageTableModel() {
		return new AbstractTableModel() {
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				if( columnIndex==0 )
					return stageKeys[rowIndex];
				else
					return stats().getStageStatsFor(stageKeys[rowIndex]);
			}
			@Override
			public int getRowCount() {
				return stageKeys.length;
			}
			@Override
			public int getColumnCount() {
				return 2;
			}
			@Override
			public String getColumnName(int column) {
				if( column==0 )
					return "Status";
				else
					return "Count";
			}
		};
	}

	public AbstractTableModel createCourseTableModel() {
		return new AbstractTableModel() {
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				if( columnIndex==0 )
					return courseKeys[rowIndex];
				else if( columnIndex==1 )
					return stats().getCourseStatsFor(courseKeys[rowIndex]);
				else
					return stats().getCourseStatsFor(courseKeys[rowIndex]+"running");
			}
			@Override
			public int getRowCount() {
				return courseKeys.length;
			}
			@Override
			public int getColumnCount() {
				return 3;
			}
			@Override
			public String getColumnName(int column) {
				if( column==0 )
					return "Course";
				else if( column==1 )
					return "Total";
				else return "Running";
			}
		};
	}

	/* (non-Javadoc)
	 * @see valmo.geco.ui.Announcer.StageListener#saving(valmo.geco.model.Stage, java.util.Properties)
	 */
	@Override
	public void saving(Stage stage, Properties properties) {
	}

	/* (non-Javadoc)
	 * @see valmo.geco.ui.Announcer.Listener#changed(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void changed(Stage previous, Stage next) {
		// perhaps stop/start the thread here
		stageKeys = stats().stageStatsKeys().toArray(new String[0]);
		courseKeys = stats().courseStatsKeys().toArray(new String[0]);
		stageTableModel.fireTableDataChanged();
		courseTableModel.fireTableDataChanged();
	}


	/* (non-Javadoc)
	 * @see valmo.geco.ui.Announcer.StageListener#closing(valmo.geco.model.Stage)
	 */
	@Override
	public void closing(Stage stage) {
		updateThread.interrupt();
		try {
			updateThread.join();
			System.out.println("interrupted");
		} catch (InterruptedException e) {
			geco().logger().debug(e);
		}
	}

	
}
