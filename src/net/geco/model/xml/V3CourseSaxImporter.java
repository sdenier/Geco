/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.geco.basics.GecoResources;
import net.geco.model.Course;
import net.geco.model.Factory;
import net.geco.model.impl.POFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * @author Simon Denier
 * @since Jun 20, 2012
 *
 */
public class V3CourseSaxImporter extends DefaultHandler implements XMLCourseImporter {

	// Memoized data for clients
	private List<Course> courses;
	private HashMap<String, Float[]> controls;

	// Internal state for importer
	private Factory factory;
	private StringBuilder buffer;
	private boolean courseDescriptionContext;

	// Internal state for Map description
	private Float[] mapCoordTL;
	private Float[] mapCoordBR;

	@SuppressWarnings("unused")
	private String mapUnitTL;
	@SuppressWarnings("unused")
	private String mapUnitBR;
	
	// Internal state for Control description
	private String controlId;
	private Float[] coord;

	@SuppressWarnings("unused")
	private String unit;

	// Internal state for Course description
	private String courseName;
	private int courseLength;
	private int courseClimb;
	private String controlType;
	private List<String> courseCodes;

	public static void main(String args[]) throws Exception {
//		List<Course> courses = importFromXml("testData/IOFdata-3.beta/testOMR.Courses.xml", new POFactory());
		List<Course> courses = importFromXml("testData/IOFdata-3.beta/TestOMR4._parcours.xml", new POFactory());
		for (Course course : courses) {
			System.out.println(course);
		}
	}
	
	public V3CourseSaxImporter(Factory factory) {
		this.factory = factory;
		resetBuffer();
		controls = new HashMap<String, Float[]>();
		courses = new ArrayList<Course>(10);
		courseDescriptionContext = false;
	}
	
	public List<Course> courses() {
		return courses;
	}
	
	public Map<String, Float[]> controls() {
		return controls;
	}
	
	public static List<Course> importFromXml(String xmlFile, Factory factory) throws Exception {
		return new V3CourseSaxImporter(factory).importFromXml(xmlFile);
	}
	
	public List<Course> importFromXml(String xmlFile) throws Exception {
		XMLReader xr = XMLReaderFactory.createXMLReader();
		xr.setContentHandler(this);
		xr.setErrorHandler(this);
		xr.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); //$NON-NLS-1$
		xr.setFeature("http://xml.org/sax/features/validation", false); //$NON-NLS-1$
		BufferedReader reader = consumeBOM(GecoResources.getSafeReaderFor(xmlFile));
		xr.parse(new InputSource(reader));
		return courses();
	}
	
	private BufferedReader consumeBOM(BufferedReader reader) throws IOException {
		// http://mark.koli.ch/2009/02/resolving-orgxmlsaxsaxparseexception-content-is-not-allowed-in-prolog.html
		reader.mark(1);
		while( reader.read() != '<' ) { reader.mark(1); }
		reader.reset();
		return reader;
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

	
	public void characters(char ch[], int start, int length) {
    	buffer.append(ch, start, length);
    }
    
    private String buffer() {
		return buffer.toString().trim();
	}

	private void resetBuffer() {
    	buffer = new StringBuilder();
    }

	private Float importLocalizedFloat(String f) {
		try {
			return Float.valueOf(f);
		} catch (NumberFormatException e) {
			// handle float encoded as "65,22" instead of "65.22"
			return Float.valueOf(f.replace(',', '.'));
		}
	}
	
	public void startElement(String uri, String name, String qName, Attributes atts) {
		startMapDescription(name, atts);
		startControlDescription(name, atts);
		startCourseDescription(name, atts);
	}

	public void endElement(String uri, String name, String qName) {
		endMapDescription(name);
		endControlDescription(name);
		endCourseDescription(name);
	}

	private void startMapDescription(String name, Attributes atts) {
		if( "MapPositionTopLeft".equals(name) ) { //$NON-NLS-1$
			mapCoordTL = new Float[] {
						importLocalizedFloat(atts.getValue("x")), //$NON-NLS-1$
						importLocalizedFloat(atts.getValue("y")) }; //$NON-NLS-1$
			mapUnitTL  = atts.getValue("unit");
			return;
		}
		if( "MapPositionBottomRight".equals(name) ) { //$NON-NLS-1$
			mapCoordBR = new Float[] {
						importLocalizedFloat(atts.getValue("x")), //$NON-NLS-1$
						importLocalizedFloat(atts.getValue("y")) }; //$NON-NLS-1$
			mapUnitBR  = atts.getValue("unit");
			return;
		}
		if( "Scale".equals(name) ) { //$NON-NLS-1$
			resetBuffer();
			return;
		}
	}

	private void endMapDescription(String name) {
		if( "Scale".equals(name) ) { //$NON-NLS-1$
			return;
		}
		if( "Map".equals(name) ) { //$NON-NLS-1$
			controls.put("Map", mapCoordTL ); //$NON-NLS-1$
			controls.put("MapTL", mapCoordTL ); //$NON-NLS-1$
			controls.put("MapBR", mapCoordBR ); //$NON-NLS-1$
			return;
		}
	}

	private void startControlDescription(String name, Attributes atts) {
		if( "Id".equals(name) ) { //$NON-NLS-1$
			resetBuffer();
			return;
		}
		if( "MapPosition".equals(name) ) {
			coord = new Float[] {
					importLocalizedFloat(atts.getValue("x")), //$NON-NLS-1$
					importLocalizedFloat(atts.getValue("y")) }; //$NON-NLS-1$
			unit  = atts.getValue("unit");
			return;
		}
	}

	private void endControlDescription(String name) {
		if( "Id".equals(name) ) { //$NON-NLS-1$
			controlId = buffer();
			return;
		}
		if( "Control".equals(name) && ! courseDescriptionContext ) { //$NON-NLS-1$
			controls.put(controlId, coord);
			return;
		}
	}

	private void startCourseDescription(String name, Attributes atts) {
		if( "Course".equals(name) ) { //$NON-NLS-1$
			courseDescriptionContext = true;
			courseCodes = new ArrayList<String>(20);
			return;
		}
		if( "Name".equals(name) ) { //$NON-NLS-1$
			resetBuffer();
			return;
		}
		if( "Length".equals(name) ) { //$NON-NLS-1$
			resetBuffer();
			return;
		}
		if( "Climb".equals(name) ) { //$NON-NLS-1$
			resetBuffer();
			return;
		}
		if( "CourseControl".equals(name) ) { //$NON-NLS-1$
			controlType = atts.getValue("type");
		}
		if( "Control".equals(name) ) { //$NON-NLS-1$
			resetBuffer();
			return;
		}
	}

	private void endCourseDescription(String name) {
		if( "Name".equals(name) ) { //$NON-NLS-1$
			courseName = buffer();
			return;
		}
		if( "Length".equals(name) ) { //$NON-NLS-1$
			courseLength = Integer.parseInt(buffer());
			return;
		}
		if( "Climb".equals(name) ) { //$NON-NLS-1$
			courseClimb = Integer.parseInt(buffer());
			return;
		}
		if( "Control".equals(name) && courseDescriptionContext ) { //$NON-NLS-1$
			addControlCodeToCourse(buffer());
			return;
		}
		if( "Course".equals(name) ) { //$NON-NLS-1$
			createCourse();
			courseDescriptionContext = false;
			return;
		}
	}

	private void addControlCodeToCourse(String controlCode) {
		if( "Control".equals(controlType) ) { //$NON-NLS-1$
			courseCodes.add(controlCode);
		}
		if( "Start".equals(controlType) ) {} //$NON-NLS-1$
		if( "Finish".equals(controlType) ) {} //$NON-NLS-1$
	}

	private int[] getCourseCodes() {
		int[] codes = new int[courseCodes.size()];
		for (int i = 0; i < courseCodes.size(); i++) {
			codes[i] = Integer.parseInt(courseCodes.get(i));
		}
		return codes;
	}

	private void createCourse() {
		Course course = factory.createCourse();
		course.setName(courseName);
		course.setLength(courseLength);
		course.setClimb(courseClimb);
		course.setCodes(getCourseCodes());
		courses.add(course);
	}

}
