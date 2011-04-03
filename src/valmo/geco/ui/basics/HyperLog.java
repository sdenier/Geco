/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui.basics;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkListener;

import valmo.geco.basics.Announcer;
import valmo.geco.basics.Announcer.Logging;

/**
 * @author Simon Denier
 * @since Nov 25, 2010
 *
 */
public class HyperLog extends JTextPane implements Logging {

	private static final int LOGSIZE = 6;
	
	private static final Pattern ECARD_PATTERN = java.util.regex.Pattern.compile("(\\d{5,}\\w*)"); //$NON-NLS-1$
	
	private List<String> logRing;
	
	public HyperLog(Announcer announcer, HyperlinkListener hlListener) {
		logRing = new ArrayList<String>(LOGSIZE);
		for (int i = 0; i < LOGSIZE; i++) {
			logRing.add(""); // fill-in log //$NON-NLS-1$
		}
		setContentType("text/html"); //$NON-NLS-1$
		setEditable(false);
		setPreferredSize(new Dimension(700, 100));
		setBorder(BorderFactory.createEtchedBorder());
		announcer.registerLogger(this);
		addHyperlinkListener(hlListener);
	}
	
	private void displayLog() {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < LOGSIZE; i++) {
			buffer.append(logRing.get(i));
		}
		setText(buffer.toString());
	}
	
	private void append(String formatted) {
		logRing.remove(0);
		logRing.add(formatted);
	}
	
	private String format(String message) {
		Set<String> ecards = detectEcardNumbers(message);
		for (String ecard : ecards) {
			message = message.replaceAll(ecard, "<a href=\"" + ecard + "\">" + ecard + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return message + "<br />"; //$NON-NLS-1$
	}
	
	public Set<String> detectEcardNumbers(String source) {
		Matcher matcher = ECARD_PATTERN.matcher(source);
		HashSet<String> ecards = new HashSet<String>();
		while( matcher.find() ) {
			ecards.add( matcher.group() );
		}
		return ecards;
	}

	
	public void update(String message) {
		append(format(message));
		displayLog();
	}

	@Override
	public void log(String message, boolean warning) {
		update(message);
	}

	@Override
	public void info(String message, boolean warning) {
		update(message);
	}

	@Override
	public void dataInfo(String data) {
		update(data);
	}

}
