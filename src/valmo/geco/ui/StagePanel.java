/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import valmo.geco.core.Announcer;
import valmo.geco.core.Geco;
import valmo.geco.core.Util;
import valmo.geco.model.Category;
import valmo.geco.model.Club;
import valmo.geco.model.Course;
import valmo.geco.model.Stage;

/**
 * @author Simon Denier
 * @since Feb 8, 2009
 *
 */
public class StagePanel extends TabPanel {

	Announcer announcer;
	
	public StagePanel(Geco geco, JFrame frame, Announcer announcer) {
		super(geco, frame, announcer);
		this.announcer = announcer;
		setLayout(new BorderLayout()); // TODO: use gridbaglayout
		refresh();
	}
	
	public void refresh() {
		this.removeAll();
		JPanel north = new JPanel();
//		north.setLayout(new BoxLayout(north, BoxLayout.X_AXIS));
//		north.add(Util.embed(stageConfigPanel()));
//		north.add(Util.embed(checkerConfigPanel()));
//		north.add(Util.embed(sireaderConfigPanel()));
		north.setLayout(new FlowLayout(FlowLayout.LEFT));
		north.add(stageConfigPanel());
		north.add(checkerConfigPanel());
		north.add(sireaderConfigPanel());
		//		north.add(Box.createHorizontalGlue());
		add(north, BorderLayout.NORTH);
		JPanel south = new JPanel();
		south.setLayout(new FlowLayout(FlowLayout.LEADING));
		south.add(Util.embed(courseConfigPanel()));
		south.add(Util.embed(clubConfigPanel()));
		south.add(Util.embed(categoryConfigPanel()));
		south.add(Box.createHorizontalGlue());
		add(south, BorderLayout.CENTER);
	}

	private JPanel stageConfigPanel() {
		JPanel panel = new JPanel(new GridLayout(0,2));
		panel.setBorder(BorderFactory.createTitledBorder("Stage"));
		panel.setAlignmentY(Component.TOP_ALIGNMENT);
		panel.add(new JLabel("Stage name:"));
		panel.add(new JTextField(geco().stage().getName()));
		panel.add(new JLabel("Previous stage:"));
		panel.add(new JLabel(geco().getPreviousStageDir()));
		panel.add(new JLabel("Next stage:"));
		panel.add(new JLabel(geco().getNextStageDir()));
		return panel;
	}

	private JPanel checkerConfigPanel() {
		JPanel panel = new JPanel(new GridLayout(0,2));
		panel.setBorder(BorderFactory.createTitledBorder("Orientshow"));
		panel.setAlignmentY(Component.TOP_ALIGNMENT);
		panel.add(new JLabel("MP limit:"));
		JTextField mplimitF = new JTextField("0");
		mplimitF.setToolTipText("Number of missing punches authorized before marking the runner as MP.");
		panel.add(mplimitF);
		panel.add(new JLabel("Time penalty:"));
		JTextField penaltyF = new JTextField("30");
		penaltyF.setToolTipText("Time penalty per missing punch in seconds");
		panel.add(penaltyF);
		return panel;
	}
	
	private JPanel sireaderConfigPanel() {
		JPanel panel = new JPanel(new GridLayout(0,2));
		panel.setAlignmentY(Component.TOP_ALIGNMENT);
		panel.setBorder(BorderFactory.createTitledBorder("SI Reader"));
		panel.add(new JLabel("Station port:"));
		JTextField stationPortF = new JTextField("/dev/tty/");
//		stationPortF.setToolTipText("");
		panel.add(stationPortF);
		return panel;		
	}

	
	private JPanel clubConfigPanel() {
		final ConfigTablePanel<Club> panel = new ConfigTablePanel<Club>(geco(), frame());
		
		final ConfigTableModel<Club> tableModel = 
			new ConfigTableModel<Club>(new String[] {"Short name", "Long name"}) {
				@Override
				public Object getValueIn(Club club, int columnIndex) {
					switch (columnIndex) {
					case 0: return club.getShortname();
					case 1: return club.getName();
					default: return super.getValueIn(club, columnIndex);
					}
				}
				@Override
				public void setValueIn(Club club, Object value, int col) {
					switch (col) {
					case 0: geco().stageControl().updateShortname(club, (String) value); break;
					case 1: geco().stageControl().updateName(club, (String) value); break;
					default: break;
					}
				}
		};
		tableModel.setData(new Vector<Club>(registry().getClubs()));
		announcer.registerStageConfigListener( new Announcer.StageConfigListener() {
			public void coursesChanged() {}
			public void clubsChanged() {
				tableModel.setData(new Vector<Club>(registry().getClubs()));
//				panel.refreshTableData(new Vector<Club>(registry().getClubs()));
			}
			public void categoriesChanged() {}
		});
		
		ActionListener addAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco().stageControl().createClub();
			}
		};
		ActionListener removeAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Club club = panel.getSelectedData();
				if( club!=null ) {
					boolean removed = geco().stageControl().removeClub(club);
					if( !removed ) {
						JOptionPane.showMessageDialog(frame(),
							    "This club can not be deleted because some runners are registered with it.",
							    "Action cancelled",
							    JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		};

		panel.initialize("Club", tableModel, addAction, removeAction);
		return panel;
	}

	private JPanel courseConfigPanel() {
		final ConfigTablePanel<Course> panel = new ConfigTablePanel<Course>(geco(), frame());
		
		final ConfigTableModel<Course> tableModel = 
			new ConfigTableModel<Course>(new String[] {"Name", "Nb controls"}) {
				@Override
				public Object getValueIn(Course course, int columnIndex) {
					switch (columnIndex) {
					case 0: return course.getName();
					case 1: return course.getCodes().length;
					default: return super.getValueIn(course, columnIndex);
					}
				}
				@Override
				public boolean isCellEditable(int row, int col) {
					return col == 0;
				}
				@Override
				public void setValueIn(Course course, Object value, int col) {
					switch (col) {
					case 0: geco().stageControl().updateName(course, (String) value); break;
//					case 1: geco().stageControl().updateName(course, (String) value); break;
					default: break;
					}
				}
		};
		tableModel.setData(new Vector<Course>(registry().getCourses()));
		announcer.registerStageConfigListener( new Announcer.StageConfigListener() {
			public void coursesChanged() {
				tableModel.setData(new Vector<Course>(registry().getCourses()));
//				panel.refreshTableData(new Vector<Club>(registry().getClubs()));
			}
			public void clubsChanged() {}
			public void categoriesChanged() {}
		});
		
		ActionListener addAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco().stageControl().createCourse();
			}
		};
		ActionListener removeAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Course course = panel.getSelectedData();
				if( course!=null ) {
					try {
						geco().stageControl().removeCourse(course);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(frame(),
							"This course can not be deleted because " + e1.getMessage(),
							"Action cancelled",
							JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		};

		panel.initialize("Course", tableModel, addAction, removeAction);
		return panel;
	}
	
	private JPanel categoryConfigPanel() {
		final ConfigTablePanel<Category> panel = new ConfigTablePanel<Category>(geco(), frame());
		
		final ConfigTableModel<Category> tableModel = 
			new ConfigTableModel<Category>(new String[] {"Short name", "Long name", "Course"}) {
				@Override
				public Object getValueIn(Category cat, int columnIndex) {
					switch (columnIndex) {
					case 0: return cat.getShortname();
					case 1: return cat.getLongname();
					case 2: return (cat.getCourse()==null) ? "" : cat.getCourse().getName();
					default: return super.getValueIn(cat, columnIndex);
					}
				}
				@Override
				public void setValueIn(Category cat, Object value, int col) {
					switch (col) {
					case 0: cat.setShortname((String) value); break;
					case 1: cat.setLongname((String) value); break;
					default: break;
					}
					// TODO: announce change in club name
				}
		};
		tableModel.setData(new Vector<Category>(registry().getCategories()));
		
		ActionListener addAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				geco().stageControl().createCategory();
			}
		};
		ActionListener removeAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Category cat = panel.getSelectedData();
				if( cat!=null ) {
//					geco().stageControl().removeCategory(cat);
				}
			}
		};

		panel.initialize("Category", tableModel, addAction, removeAction);
		return panel;
	}
	
	@Override
	public void changed(Stage previous, Stage next) {
		refresh();
		frame().repaint();
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		// TODO save stage properties
		super.saving(stage, properties);
	}
	

}
