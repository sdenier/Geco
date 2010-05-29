/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.xml;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import valmo.geco.model.Course;
import valmo.geco.model.Factory;
import valmo.geco.model.impl.POFactory;

/**
 * @author Simon Denier
 * @since Feb 19, 2010
 *
 */
public class CourseSaxImporter extends DefaultHandler {

	private Vector<Course> courses;
	private StringBuilder buffer;
	private String namePrefix;
	private Factory factory;
	private Course course;
	private int seq;
	private Vector<Integer> codes;
	private HashMap<String, Course> variations;
	private String courseId;
	private String variationName;
	private int courseLength;

	public static void main(String args[]) throws Exception {
		importFromXml("testData/IOFdata-2.0.3/CourseData_example1.xml", new POFactory());
	}
	
	public CourseSaxImporter(Factory factory) {
		this.factory = factory;
		courses = new Vector<Course>();
		resetBuffer();
		variations = new HashMap<String, Course>();
		codes = new Vector<Integer>();
	}
	
	public Vector<Course> courses() {
		return courses;
	}
	
	public static Vector<Course> importFromXml(String xmlFile, Factory factory) throws Exception {
		XMLReader xr = XMLReaderFactory.createXMLReader();
		CourseSaxImporter handler = new CourseSaxImporter(factory);
		xr.setContentHandler(handler);
		xr.setErrorHandler(handler);
		FileReader r = new FileReader(xmlFile);
		xr.parse(new InputSource(r));
		return handler.courses();
	}
	
	@Override
	public void error(SAXParseException e) throws SAXException {
		System.err.println(e);
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		System.err.println(e);
		super.fatalError(e);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		System.out.println(e);
	}

	
	public void characters (char ch[], int start, int length) {
    	buffer.append(ch, start, length);
    }
    
    private String buffer() {
		return buffer.toString().trim();
	}

	private void resetBuffer() {
    	buffer = new StringBuilder();
    }
	
	
	public void startElement(String uri, String name, String qName, Attributes atts) {
//		if( "Course".equals(name) ) {
//			return;
//		}
		if( "CourseName".equals(name) ) {
			resetBuffer();
			return;
		}
//		if( "CourseVariation".equals(name) ) {
//			resetBuffer();
//			return;
//		}
		if( "CourseVariationId".equals(name) ) {
			resetBuffer();
			return;
		}
		if( "Name".equals(name) ) {
			resetBuffer();
			return;
		}
		if( "CourseLength".equals(name) ) {
			resetBuffer();
			return;
		}
		if( "Sequence".equals(name) ) {
			resetBuffer();
			return;
		}
		if( "ControlCode".equals(name) ) {
			resetBuffer();
			return;
		}
	}

	public void endElement(String uri, String name, String qName) {
		if( "Course".equals(name) ) {
			registerCourses();
			return;
		}
		if( "CourseName".equals(name) ) {
			saveNamePrefix();
			return;
		}
		if( "CourseVariation".equals(name) ) {
			saveCourseVariation();
			return;
		}
		if( "CourseVariationId".equals(name) ) {
			saveCourseId();
			return;
		}
		if( "Name".equals(name) ) {
			saveVariationName();
			return;
		}
		if( "CourseLength".equals(name) ) {
			saveCourseLength();
			return;
		}
		if( "Sequence".equals(name) ) {
			saveSeqNumber();
			return;
		}
		if( "ControlCode".equals(name) ) {
			saveControlCode();
			return;
		}
	}
	
	private void saveNamePrefix() {
		namePrefix = buffer();
	}

	private void saveCourseId() {
		courseId = buffer();
	}

	private void saveVariationName() {
		variationName = buffer();
	}

	private void saveCourseLength() {
		courseLength = Integer.parseInt(buffer());
	}

	private void saveSeqNumber() {
		seq = Integer.parseInt(buffer());
	}

	private void saveControlCode() {
		if( courseId!=null ) {
			if( seq >= codes.size() )
				codes.ensureCapacity(seq);
			codes.add(seq - 1, Integer.valueOf(buffer()));			
		}
	}

	private void saveCourseVariation() {
		course = factory.createCourse();
		course.setLength(courseLength);
		setCodes();
		if( variationName==null ) {
			variations.put(courseId, course);
		} else {
			variations.put(variationName, course);
		}
		courseId = null;
		variationName = null;
	}

	private void setCodes() {
		int[] codez = new int[codes.size()];
		for (int i = 0; i < codes.size(); i++) {
			codez[i] = codes.get(i);
		}
		course.setCodes(codez);
		codes = new Vector<Integer>();
	}

	private void registerCourses() {
		Course c = null;
		if( variations.size()==1 ) {
			c = variations.values().iterator().next();
			c.setName(namePrefix);
			courses.add(c);
		} else {
			for (String id : variations.keySet()) {
				c = variations.get(id);
				c.setName(namePrefix + " " + id);
				courses.add(c);
			}
		}
		variations.clear();
	}

    
}
