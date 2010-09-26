/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import valmo.geco.Geco;
import valmo.geco.control.Generator;
import valmo.geco.control.RunnerCreationException;
import valmo.geco.core.Announcer.Logging;

/**
 * @author Simon Denier
 * @since Sep 13, 2009
 *
 */
public class LogPanel extends TabPanel implements Logging {

	private JTextArea logArea;
	
	
	/**
	 * @param geco
	 * @param frame
	 */
	public LogPanel(Geco geco, JFrame frame) {
		super(geco, frame);
		initPanels(this);
		geco().announcer().registerLogger(this);
	}

	
	public void initPanels(JPanel panel) {
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//		splitPane.setOneTouchExpandable(true);
		splitPane.add(initLogArea());
		splitPane.add(initStatsPanel());
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		panel.add(splitPane);
	}
	
	public JPanel initLogArea() {
		logArea = new JTextArea(25, 70);
		logArea.setEditable(false);
		logArea.setLineWrap(true);
		JButton clearB = new JButton("Clear log view");
		clearB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logArea.setText("");
			}
		});

		JPanel logPanel = new JPanel(new BorderLayout());
		logPanel.add(SwingUtils.embed(clearB), BorderLayout.SOUTH);
		logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

		if( Geco.testModeOn() ) {
			JPanel simuPanel = new JPanel();
			simuPanel.setLayout(new BoxLayout(simuPanel, BoxLayout.Y_AXIS));

			final JSpinner nbGeneration = new JSpinner(new SpinnerNumberModel(10, 0, null, 5));
			nbGeneration.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
			nbGeneration.setToolTipText("Number of runners to generate");
			simuPanel.add(SwingUtils.embed(nbGeneration));
			
			final JSpinner genDelay = new JSpinner(new SpinnerNumberModel(1, 0, null, 1));
			genDelay.setToolTipText("Delay in second between two creations");
			genDelay.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
			simuPanel.add(SwingUtils.embed(genDelay));

			final JSpinner mutationS = new JSpinner(new SpinnerNumberModel(40, 0, null, 5));
			mutationS.setPreferredSize(new Dimension(75, SwingUtils.SPINNERHEIGHT));
			mutationS.setToolTipText("Mutation factor");
			simuPanel.add(SwingUtils.embed(mutationS));
			
			final JButton generateB = new JButton("Generate");
			simuPanel.add(generateB);
			
			JButton cUnknownB = new JButton("Create Unknown");
			cUnknownB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					geco().generator().generateUnknownData();
				}
			});
			JButton cOverwriteB = new JButton("Create Overwriting");
			cOverwriteB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					geco().generator().generateOverwriting();
				}
			});
			simuPanel.add(cUnknownB);
			simuPanel.add(cOverwriteB);

			generateB.addActionListener(new ActionListener() {
				private Thread gen;
				private Color defaultColor;

				@Override
				public void actionPerformed(ActionEvent e) {
					if (gen != null && gen.isAlive()) {
						gen.interrupt();
					} else {
						generateB.setSelected(true);
						defaultColor = generateB.getBackground();
						generateB.setBackground(Color.GREEN);
						final int nb = ((Integer) nbGeneration.getValue()).intValue();
						final int delay = 1000 * ((Integer) genDelay.getValue()).intValue();
						gen = new Thread(new Runnable() {
							public synchronized void run() {
								displayLog("--Generating " + nb + " runners--");
								try {
									Generator generator = geco().generator();
									generator.setMutationX(((Integer) mutationS.getValue()).intValue());
									for (int i = 1; i <= nb; i++) {
										if (i % 10 == 0) {
											displayLog(Integer.toString(i));
										}
										generator.generateRunnerData();
										wait(delay);
									}
								} catch (InterruptedException e) {
								} catch (RunnerCreationException e) {
									e.printStackTrace();
								}
								displayLog("--Stop--");
								generateB.setBackground(defaultColor);
								generateB.setSelected(false);
							}
						});
						gen.start();
					}
				}
			});
			logPanel.add(SwingUtils.embed(simuPanel), BorderLayout.WEST);
		}

		return logPanel;
	}

	private JPanel initStatsPanel() {
		return new HStatsPanel(geco(), frame());
	}
	
	public void displayLog(String message) {
		logArea.append("\n");
		logArea.append(message);
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	@Override
	public void info(String message, boolean warning) {
//		displayLog(message);
	}

	@Override
	public void log(String message, boolean warning) {
		displayLog(message);
	}

	@Override
	public void dataInfo(String data) {
		displayLog(data);
	}

}