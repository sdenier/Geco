/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.iocsv;

import net.geco.model.Category;
import net.geco.model.Factory;
import net.geco.model.Registry;

/**
 * @author Simon Denier
 * @since Jan 4, 2009
 *
 */
public class CategoryIO extends AbstractIO<Category> {

	public static String sourceFilename() {
		return "Classes.csv"; //$NON-NLS-1$
	}
	
	public CategoryIO(Factory factory, CsvReader reader, CsvWriter writer, Registry registry) {
		super(factory, reader, writer, registry);
	}
	
	@Override
	public Category importTData(String[] record) {
		Category cat = this.factory.createCategory();
		cat.setShortname(record[0]);
		if( record.length==1 ){
			cat.setLongname("");
		} else {
			cat.setLongname(record[1]);
			if( record.length==3 ){
				cat.setCourse(registry.findCourse(record[2]));
			}			
		}
		return cat;
	}

	@Override
	public void register(Category data, Registry registry) {
		registry.addCategory(data);
	}

	@Override
	public String[] exportTData(Category c) {
		if( c.getCourse()!=null ) {
			return new String[] {
					c.getShortname(),
					c.getLongname(),
					c.getCourse().getName()
			};
		} else {
			return new String[] {
					c.getShortname(),
					c.getLongname()
			};
		}
	}


}
