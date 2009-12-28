/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.core;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JPanel;

/**
 * @author Simon Denier
 * @since Jan 31, 2009
 *
 */
public class Util {

	public static JPanel embed(Component comp) {
		JPanel pan = new JPanel();
		pan.add(comp);
		return pan;
	}
	
	public static String[] splitAndTrim(String dataline, String token) {
		String[] data = dataline.split(token);
		for (int i = 0; i < data.length; i++) {
			data[i] = data[i].trim();
		}
		return data;
	}
	
	public static String join(Collection<?> objects, String token, StringBuffer buf) {
		for (Iterator<?> it = objects.iterator(); it.hasNext();) {
			buf.append(it.next()).append(token);
		}
		buf.delete(buf.lastIndexOf(token), buf.length()); // delete last token
		return buf.toString();
	}

	public static <T> String join(T[] objects, String token, StringBuffer buf) {
		return join(Arrays.asList(objects), token, buf);
	}

	public static boolean allDifferent(String[] strings) {
		boolean allDiff = true;
		int i = 0;
		while(allDiff && i<strings.length - 1) {
			int j = i + 1;
			while(allDiff && j<strings.length) {
				allDiff = !strings[i].equals(strings[j]);
				j++;
			}
			i++;
		}
		return allDiff;
	}

	public static <T> boolean different(T t, int numIndex, T[] tArray) {
		boolean allDifferent = true;
		int i = 0;
		while( allDifferent && i<tArray.length ) {
			if( i!=numIndex ) // dont compare against itself
				allDifferent = !t.equals(tArray[i]);
			i++;
		}
		return allDifferent;
	}
	
	public static String italicize(String content) {
		return inHtml(content, "i");
	}
	
	public static String inHtml(String content, String htmlTag) {
		return "<html>" + html(content, htmlTag, new StringBuffer()) + "</html>";
	}

	public static String html(String content, String htmlTag, StringBuffer buf) {
		htmlTag(htmlTag, true, buf);
		buf.append(content);
		htmlTag(htmlTag, false, buf);
		return buf.toString();
	}

	public static String htmlTag(String htmlTag, boolean starting, StringBuffer buf) {
		buf.append("<");
		if( !starting) {
			buf.append("/");
		}
		buf.append(htmlTag);
		buf.append(">");
		return buf.toString();
	}

	public static GridBagConstraints gbConstr() {
		return compConstraint(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE);
	}

	public static GridBagConstraints gbConstr(int gridy) {
		return compConstraint(GridBagConstraints.RELATIVE, gridy);
	}
	
	public static GridBagConstraints compConstraint(int gridx, int gridy) {
		return compConstraint(gridx, gridy, GridBagConstraints.LINE_START);
	}

	public static GridBagConstraints compConstraint(int gridx, int gridy, int anchor) {
		return compConstraint(gridx, gridy, GridBagConstraints.NONE, anchor);
	}
	
	public static GridBagConstraints compConstraint(int gridx, int gridy, int fill, int anchor) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = gridx;
		constraints.gridy = gridy;
		constraints.fill = fill;
		constraints.anchor = anchor;
//		constraints.ipadx = 10 ;
//		constraints.ipady = 10 ;
//		constraints.gridwidth = gridwidth;
//		constraints.gridheight = gridheigth;
		return constraints;
	}

	
}
