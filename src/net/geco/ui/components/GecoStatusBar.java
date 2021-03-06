/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.ui.components;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.geco.basics.Announcer.Logging;
import net.geco.basics.Html;
import net.geco.framework.IGeco;


/**
 * @author Simon Denier
 * @since Jun 2, 2010
 *
 */
public class GecoStatusBar extends JPanel implements Logging {

	private JLabel status;

	private GecoStatusStats statsSummary;

	public GecoStatusBar(IGeco geco) {
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		status = new JLabel(" "); //$NON-NLS-1$
		statsSummary = new GecoStatusStats(geco);
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				status.setText(" "); //$NON-NLS-1$
				statsSummary.updateView();
			}
		});
		add(status);
		add(Box.createHorizontalGlue());
		add(statsSummary.getView());
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
													 BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		geco.announcer().registerLogger(this);
	}

	private void display(String message, boolean warning) {
		if( warning )
			message = Html.htmlTag("font", "color=red", message); //$NON-NLS-1$ //$NON-NLS-2$
		status.setText(message);
		status.repaint();
	}

	@Override
	public void info(String message, boolean warning) {
		display(message, warning);
	}

	@Override
	public void log(String message, boolean warning) {
		display(message, warning);
	}

	@Override
	public void dataInfo(String data) {	}

}
