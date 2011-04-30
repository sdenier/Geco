/**
 * Copyright (c) 2009 Simon Denier
 */
package net.geco.basics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;


/**
 * @author Simon Denier
 * @since Jan 31, 2009
 *
 */
public class Util {

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
	
	public static Vector<String> readLines(String filename) throws IOException {
		Vector<String> lines = new Vector<String>();
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line = reader.readLine();
		while( line!=null ) {
			lines.add(line);
			line = reader.readLine();
		}
		reader.close();
		return lines;
	}
	
}
