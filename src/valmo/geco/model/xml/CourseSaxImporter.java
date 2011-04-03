/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import valmo.geco.basics.GecoResources;
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

	private HashMap<String, Float[]> controls;
	private Float[] coord;

	public static void main(String args[]) throws Exception {
//		importFromXml("testData/IOFdata-2.0.3/CourseData_example1.xml", new POFactory());
		importFromXml("hellemmes.xml", new POFactory()); //$NON-NLS-1$
	}
	
	public CourseSaxImporter(Factory factory) {
		this.factory = factory;
		resetBuffer();
		controls = new HashMap<String, Float[]>();
		courses = new Vector<Course>();
		variations = new HashMap<String, Course>();
		codes = new Vector<Integer>();
	}
	
	public Vector<Course> courses() {
		return courses;
	}
	
	public Map<String, Float[]> controls() {
		return controls;
	}
	
	public static Vector<Course> importFromXml(String xmlFile, Factory factory) throws Exception {
		return new CourseSaxImporter(factory).importFromXml(xmlFile);
	}
	
	public Vector<Course> importFromXml(String xmlFile) throws Exception {
		XMLReader xr = XMLReaderFactory.createXMLReader();
		xr.setContentHandler(this);
		xr.setErrorHandler(this);
//		if( ! new File("IOFdata.dtd").exists() ) {
			xr.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); //$NON-NLS-1$
			xr.setFeature("http://xml.org/sax/features/validation", false); //$NON-NLS-1$
//		}
		xr.parse(new InputSource(GecoResources.getSafeReaderFor(xmlFile)));
		return courses();
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
		if( "MapPosition".equals(name) ) { //$NON-NLS-1$
			coord = new Float[] {
						new Float(atts.getValue("x")).floatValue(), //$NON-NLS-1$
						new Float(atts.getValue("y")).floatValue() }; //$NON-NLS-1$
			return;
		}
		if( "StartPoint".equals(name) ) { //$NON-NLS-1$
			resetBuffer();
			return;
		}
// buffer reset through ControlCode
//		if( "Control".equals(name) ) {
//			resetBuffer();
//			return;
//		}
		if( "FinishPoint".equals(name) ) { //$NON-NLS-1$
			resetBuffer();
			return;
		}

		if( "CourseName".equals(name) ) { //$NON-NLS-1$
			resetBuffer();
			return;
		}
		if( "CourseVariationId".equals(name) ) { //$NON-NLS-1$
			resetBuffer();
			return;
		}
		if( "Name".equals(name) ) { //$NON-NLS-1$
			resetBuffer();
			return;
		}
		if( "CourseLength".equals(name) ) { //$NON-NLS-1$
			resetBuffer();
			return;
		}
		if( "Sequence".equals(name) ) { //$NON-NLS-1$
			resetBuffer();
			return;
		}
		if( "ControlCode".equals(name) ) { //$NON-NLS-1$
			resetBuffer();
			return;
		}
	}

	public void endElement(String uri, String name, String qName) {
		if( "Map".equals(name) ) { //$NON-NLS-1$
			controls.put("Map", coord ); //$NON-NLS-1$
			return;
		}
		if( "StartPoint".equals(name) ) { //$NON-NLS-1$
			controls.put(buffer(), coord );
			return;
		}
		if( "Control".equals(name) ) { //$NON-NLS-1$
			controls.put(buffer(), coord );
			return;
		}
		if( "FinishPoint".equals(name) ) { //$NON-NLS-1$
			controls.put(buffer(), coord );
			return;
		}
		
		if( "Course".equals(name) ) { //$NON-NLS-1$
			registerCourses();
			return;
		}
		if( "CourseName".equals(name) ) { //$NON-NLS-1$
			saveNamePrefix();
			return;
		}
		if( "CourseVariation".equals(name) ) { //$NON-NLS-1$
			saveCourseVariation();
			return;
		}
		if( "CourseVariationId".equals(name) ) { //$NON-NLS-1$
			saveCourseId();
			return;
		}
		if( "Name".equals(name) ) { //$NON-NLS-1$
			saveVariationName();
			return;
		}
		if( "CourseLength".equals(name) ) { //$NON-NLS-1$
			saveCourseLength();
			return;
		}
		if( "Sequence".equals(name) ) { //$NON-NLS-1$
			saveSeqNumber();
			return;
		}
		if( "ControlCode".equals(name) ) { //$NON-NLS-1$
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
				c.setName(namePrefix + " " + id); //$NON-NLS-1$
				courses.add(c);
			}
		}
		variations.clear();
	}

    
}
