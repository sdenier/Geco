/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui.components;

import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;

import valmo.geco.basics.Announcer.Logging;
import valmo.geco.basics.Html;
import valmo.geco.framework.IGeco;
import valmo.geco.ui.framework.GecoPanel;

/**
 * @author Simon Denier
 * @since Jun 2, 2010
 *
 */
public class GecoStatusBar extends GecoPanel implements Logging {
	// TODO: simplify - do we need a geco and frame references here?
	
	
	private JLabel status;
	
	public GecoStatusBar(IGeco geco, JFrame frame) {
		super(geco, frame);
		setLayout(new FlowLayout(FlowLayout.LEFT));
		status = new JLabel(" "); //$NON-NLS-1$
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				status.setText(" "); //$NON-NLS-1$
			}
		});
		add(status);
		geco.announcer().registerLogger(this);
		setBorder(BorderFactory.createEtchedBorder());
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
