/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.iocsv;

import java.util.Collection;

import valmo.geco.model.Factory;
import valmo.geco.model.Registry;

/**
 * AbstractIO is a generic class for importing and exporting records between Registry and csv files.
 * Subclassing this class for usage is straightforward and should only require implementation of
 * abstract methods.
 * 
 * @author Simon Denier
 * @since Jan 11, 2009
 *
 */
public abstract class AbstractIO<T> {

	protected Factory factory;
	protected CsvReader reader;
	protected CsvWriter writer;
	protected Registry registry;

	public AbstractIO(Factory factory, CsvReader reader, CsvWriter writer, Registry registry) {
		this.factory = factory;
		this.reader = reader;
		if( this.reader!=null )
			this.reader.setCsvSep(",");
		this.writer = writer;
		if( this.writer!=null )
			this.writer.setCsvSep(",");
		this.registry = registry;
	}
	
	public void importData() {
		this.reader.open();
		while( true ) {
			T t = importOne();
			if( t==null ) break;
			register(t, this.registry);
		}
		this.reader.close();
	}
	
	public T importOne() {
		String[] record = this.reader.readRecord();
		if( record!=null ) {
			return importTData(record);
		} else {
			return null;
		}
	}

	public abstract T importTData(String[] record);
	
	public abstract void register(T data, Registry registry);

	public void exportData(Collection<T> data) {
		this.writer.open();
		for (T t : data) {
			this.writer.writeRecord(exportTData(t));
		}
		this.writer.close();
	}
	
	public abstract String[] exportTData(T t);

}