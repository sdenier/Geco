/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.functions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import valmo.geco.control.GecoControl;
import valmo.geco.control.SIReaderHandler;
import valmo.geco.core.Html;
import valmo.geco.core.TimeManager;
import valmo.geco.model.RunnerRaceData;

/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public class StartTimeFunction extends AbstractRunnerFunction {

	private JCheckBox resetAllRunnersC;
	private JTextField starttimeF;

	public StartTimeFunction(GecoControl gecoControl) {
		super(gecoControl);
	}

	@Override
	public String toString() {
		return "Set start times";
	}

	@Override
	public String executeTooltip() {
		return "Set given start time for runners in selection";
	}

	@Override
	public void execute() {
		try {
			Date startTime = TimeManager.userParse(starttimeF.getText());
			String start = TimeManager.time(startTime);
			for (RunnerRaceData runner : selectedRunners()) {
				if( runner.getStarttime().equals(TimeManager.NO_TIME) || resetAllRunnersC.isSelected() ){
					Date oldStart = runner.getStarttime();
					runner.setStarttime(startTime);
					if( ! oldStart.equals(TimeManager.NO_TIME) ){
						geco().log("Start time change " + start + " for " + runner.toString()
								+ " (was " + TimeManager.time(oldStart) + ")");
					} else {
						geco().log("Start time " + start + " for " + runner.infoString());
					}
				}
			}			
		} catch (ParseException e) {
			geco().debug(e.getLocalizedMessage());
		}
	}

	private SIReaderHandler siHandler() {
		return geco().getService(SIReaderHandler.class);
	}
	
	private String getDefaultStarttime() {
		long zeroTime = siHandler().getZeroTime();
		return TimeManager.time(zeroTime);
	}

	@Override
	public JComponent getParametersConfig() {
		resetAllRunnersC = new JCheckBox("Reset all runners start time");
		resetAllRunnersC.setToolTipText(
				"If enabled, reset all selected runners even if they already have a start time."
				+ " Otherwise, only set runners without a start time.");
		starttimeF = new JTextField(6);
		starttimeF.setText(getDefaultStarttime());

		resetAllRunnersC.setAlignmentX(Component.LEFT_ALIGNMENT);
		starttimeF.setMaximumSize(starttimeF.getPreferredSize());		
		starttimeF.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		final JCheckBox defaultStarttimeB = new JCheckBox(
										Html.htmlTag("i", "[Global] Use Zero Hour as default start time"));
		defaultStarttimeB.setToolTipText(
				"If ecard does not contain a start time and no pre-registered start time is given, "
				+ "use SI zero hour as default start time. Can be used to simulate a single mass start");
		defaultStarttimeB.setSelected(siHandler().useZeroHourAsDefaultStarttime());
		defaultStarttimeB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				siHandler().setZeroHourAsDefaultStartime(defaultStarttimeB.isSelected());
			}
		});

		Box box = Box.createVerticalBox();
		box.add(resetAllRunnersC);
		box.add(Box.createVerticalStrut(5));
		box.add(starttimeF);
		box.add(Box.createVerticalGlue());
		box.add(defaultStarttimeB);
		box.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		box.setAlignmentY(Component.TOP_ALIGNMENT);
		
		JComponent config = super.getParametersConfig();
		config.add(box);
		return config;
	}

	@Override
	public void updateUI() {
		super.updateUI();
		starttimeF.setText(getDefaultStarttime());
	}

}
