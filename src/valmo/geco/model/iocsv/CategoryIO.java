/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.iocsv;

import valmo.geco.model.Category;
import valmo.geco.model.Factory;
import valmo.geco.model.Registry;

/**
 * @author Simon Denier
 * @since Jan 4, 2009
 *
 */
public class CategoryIO extends AbstractIO<Category> {

	public static String sourceFilename() {
		return "Classes.csv";
	}
	
	public CategoryIO(Factory factory, CsvReader reader, CsvWriter writer, Registry registry) {
		super(factory, reader, writer, registry);
	}
	
	@Override
	public Category importTData(String[] record) {
		Category cat = this.factory.createCategory();
		cat.setShortname(record[0]);
		cat.setLongname("");
		if( record.length==2 ) {
			cat.setCourse(registry.findCourse(record[1]));
		}
		return cat;
	}

	/* (non-Javadoc)
	 * @see valmo.geco.csv.AbstractImporter#register(java.lang.Object, valmo.geco.csv.Registry)
	 */
	@Override
	public void register(Category data, Registry registry) {
		registry.addCategory(data);
	}

	/* (non-Javadoc)
	 * @see valmo.geco.csv.AbstractIO#exportTData(java.lang.Object)
	 */
	@Override
	public String[] exportTData(Category c) {
		if( c.getCourse()!=null ) {
			return new String[] {
					c.getShortname(),
					c.getCourse().getName()
			};
		} else {
			return new String[] {
					c.getShortname(),
			};
		}
	}


}
