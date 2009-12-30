/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
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
		refresh();
	}
	
	public void refresh() {
		this.removeAll();
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
//		setBorder(BorderFactory.createLineBorder(Color.gray));

		GridBagConstraints c = Util.compConstraint(	GridBagConstraints.RELATIVE,
													0,
													GridBagConstraints.BOTH,
													GridBagConstraints.NORTH);
		c.insets = new Insets(10, 10, 0, 0);
		panel.add(stageConfigPanel(), c);
		panel.add(checkerConfigPanel(), c);
		panel.add(sireaderConfigPanel(), c);

		c.gridy = 1;
		panel.add(courseConfigPanel(), c);
		panel.add(clubConfigPanel(), c);
		panel.add(categoryConfigPanel(), c);

		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(panel);
	}

	private JPanel titlePanel(JPanel panel, String title) {
		JPanel embed = Util.embed(panel);
		embed.setBorder(BorderFactory.createTitledBorder(title));
		return embed;
	}

	private JPanel stageConfigPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = Util.gbConstr(0);
		c.insets = new Insets(0, 0, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel("Stage name:"), c);
		final JTextField stagenameF = new JTextField(geco().stage().getName());
		stagenameF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco().stage().setName(stagenameF.getText());
				// TODO: update window title
			}
		});
		panel.add(stagenameF, c);
		c.gridy = 1;
		panel.add(new JLabel("Previous stage:"), c);
		JTextField previousF = new JTextField(geco().getPreviousStageDir());
		previousF.setEditable(false);
		previousF.setToolTipText("Edit 'stages.prop' in parent folder to change stage order");
		panel.add(previousF, c);
		c.gridy = 2;
		panel.add(new JLabel("Next stage:"), c);
		JTextField nextF = new JTextField(geco().getNextStageDir());
		nextF.setEditable(false);
		nextF.setToolTipText("Edit 'stages.prop' in parent folder to change stage order");
		panel.add(nextF, c);
		return titlePanel(panel, "Stage");
	}

	private JPanel checkerConfigPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = Util.gbConstr(0);
		c.insets = new Insets(0, 0, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel("MP limit:"), c);
		// TODO: propose to recheck all punches if mod
		int mpLimit = geco().checker().getMPLimit();
		JTextField mplimitF = new JTextField(new Integer(mpLimit).toString());
		mplimitF.setColumns(7);
		mplimitF.setToolTipText("Number of missing punches authorized before marking the runner as MP.");
		panel.add(mplimitF, c);
		c.gridy = 1;
		panel.add(new JLabel("Time penalty:"), c);
		long penalty = geco().checker().getMPPenalty() / 1000;
		JTextField penaltyF = new JTextField(new Long(penalty).toString());
		penaltyF.setColumns(7);
		penaltyF.setToolTipText("Time penalty per missing punch in seconds");
		panel.add(penaltyF, c);
		return titlePanel(panel, "Orientshow");
	}
	
	private JPanel sireaderConfigPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = Util.gbConstr(0);
		c.insets = new Insets(0, 0, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel("Station port:"), c);
		final JTextField stationPortF = new JTextField(geco().siHandler().getPortName());
		stationPortF.setToolTipText("Com port for the SI station (COMx on Windows platform, /dev/ttyX on Linux/Mac platform)");
		panel.add(stationPortF, c);
		stationPortF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco().siHandler().setPortName(stationPortF.getText());
			}
		});
		return titlePanel(panel, "SI Reader");
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
		tableModel.setData(registry().getSortedClubs());
		announcer.registerStageConfigListener( new Announcer.StageConfigListener() {
			public void coursesChanged() {}
			public void clubsChanged() {
				tableModel.setData(registry().getSortedClubs());
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
				@Override
				public Class<?> getColumnClass(int columnIndex) {
					switch (columnIndex) {
					case 1: return Integer.class;
					default: return super.getColumnClass(columnIndex);
					}

				}
		};
		tableModel.setData(registry().getSortedCourses());
		
		announcer.registerStageConfigListener( new Announcer.StageConfigListener() {
			public void coursesChanged() {
				tableModel.setData(new Vector<Course>(registry().getSortedCourses()));
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
			new ConfigTableModel<Category>(new String[] {"Short name", "Long name"}) {
				@Override
				public Object getValueIn(Category cat, int columnIndex) {
					switch (columnIndex) {
					case 0: return cat.getShortname();
					case 1: return cat.getLongname();
//					case 2: return (cat.getCourse()==null) ? "" : cat.getCourse().getName();
					default: return super.getValueIn(cat, columnIndex);
					}
				}
				@Override
				public void setValueIn(Category cat, Object value, int col) {
					switch (col) {
					case 0: geco().stageControl().updateShortname(cat, (String) value); break;
					case 1: geco().stageControl().updateName(cat, (String) value); break;
					default: break;
					}
				}
		};
		tableModel.setData(registry().getSortedCategories());
		
		announcer.registerStageConfigListener( new Announcer.StageConfigListener() {
			public void coursesChanged() {}
			public void clubsChanged() {}
			public void categoriesChanged() {
				tableModel.setData(registry().getSortedCategories());
			}
		});
	
		ActionListener addAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				geco().stageControl().createCategory();
			}
		};
		ActionListener removeAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Category cat = panel.getSelectedData();
				if( cat!=null ) {
					try {
						geco().stageControl().removeCategory(cat);	
					} catch (Exception e2) {
						JOptionPane.showMessageDialog(frame(),
								"This category can not be deleted because " + e2.getMessage(),
								"Action cancelled",
								JOptionPane.WARNING_MESSAGE);
					}
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
