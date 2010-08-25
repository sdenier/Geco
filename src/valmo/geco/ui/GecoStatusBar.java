/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;

import valmo.geco.Geco;
import valmo.geco.core.Announcer.Logger;
import valmo.geco.core.Html;

/**
 * @author Simon Denier
 * @since Jun 2, 2010
 *
 */
public class GecoStatusBar extends GecoPanel implements Logger {
	
	JLabel status;
	
	public GecoStatusBar(Geco geco, JFrame frame) {
		super(geco, frame);
		setLayout(new FlowLayout(FlowLayout.LEFT));
		status = new JLabel(" ");
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				status.setText(" ");
			}
		});
		add(status);
		geco.announcer().registerLogger(this);
		setBorder(BorderFactory.createEtchedBorder());
	}

	private void display(String message, boolean warning) {
		if( warning )
			message = Html.htmlTag("font", "color=red", message);
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
