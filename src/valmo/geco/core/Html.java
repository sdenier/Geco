/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.core;



/**
 * This class implements some very dumb helper methods for html tags. Basically, it does not support nesting.
 * One has to openTag, write contents, closeTag to enable nesting.
 * Fortunately, it is designed to be concise and readable.
 * 
 * @author Simon Denier
 * @since Aug 19, 2010
 *
 */
public final class Html {

	private StringBuffer buffer;
	
	public Html() {
		buffer = new StringBuffer();
		open("html");
	}
	
	public static StringBuffer openTag(String tag, StringBuffer buffer) {
		buffer.append('<');
		buffer.append(tag);
		buffer.append('>');
		return buffer;
	}

	public static StringBuffer openTag(String tag, String attributes, StringBuffer buffer) {
		buffer.append('<');
		buffer.append(tag);
		buffer.append(' ');
		buffer.append(attributes);
		buffer.append('>');
		return buffer;
	}
	
	public static StringBuffer closeTag(String tag, StringBuffer buffer) {
		buffer.append('<');
		buffer.append('/');
		buffer.append(tag);
		buffer.append('>');
		return buffer;
	}
	
	public static StringBuffer tag(String tag, String contents, StringBuffer buffer) {
		openTag(tag, buffer);
		buffer.append(contents);
		closeTag(tag, buffer);
		return buffer;
	}

	public static StringBuffer tag(String tag, String attributes, String contents, StringBuffer buffer) {
		openTag(tag, attributes, buffer);
		buffer.append(contents);
		closeTag(tag, buffer);
		return buffer;
	}
	
	public static String htmlTag(String tag, String contents) {
		StringBuffer buf = new StringBuffer();
		openTag("html", buf);
		tag(tag, contents, buf);
		closeTag("html", buf);
		return buf.toString();
	}

	public static String htmlTag(String tag, String attributes, String contents) {
		StringBuffer buf = new StringBuffer();
		openTag("html", buf);
		tag(tag, attributes, contents, buf);
		closeTag("html", buf);
		return buf.toString();
	}

	
	public Html open(String tag) {
		openTag(tag, buffer);
		return this;
	}

	public Html open(String tag, String attributes) {
		openTag(tag, attributes, buffer);
		return this;
	}

	public Html close(String tag) {
		closeTag(tag, buffer);
		return this;
	}
	
	public Html tag(String tag, String contents) {
		tag(tag, contents, buffer);
		return this;
	}
	
	public Html tag(String tag, String attributes, String contents) {
		tag(tag, attributes, contents, buffer);
		return this;
	}
	
	public String close() {
		close("html");
		return buffer.toString();
	}	
	
	public Html contents(String contents) {
		buffer.append(contents);
		return this;
	}
	
	public Html br() {
		buffer.append("<br />");
		return this;
	}

	public Html i(String contents) {
		tag("i", contents);
		return this;
	}

	public Html b(String contents) {
		tag("b", contents);
		return this;
	}

	public Html em(String contents) {
		tag("em", contents);
		return this;
	}
	
	public Html td(String contents) {
		tag("td", contents);
		return this;
	}

	public Html td(String contents, String attributes) {
		tag("td", attributes, contents);
		return this;
	}

	public Html th(String contents) {
		tag("th", contents);
		return this;
	}

	public Html th(String contents, String attributes) {
		tag("th", attributes, contents);
		return this;
	}
	
	public Html openTr() {
		open("tr");
		return this;
	}
	
	public Html closeTr() {
		close("tr");
		buffer.append("\n");
		return this;
	}
	
	public Html p(String contents) {
		tag("p", contents);
		buffer.append("\n");
		return this;
	}

	
}
