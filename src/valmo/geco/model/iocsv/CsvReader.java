/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.model.iocsv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import valmo.geco.core.GecoResources;
import valmo.geco.core.Util;


/**
 * @author Simon Denier
 * @since Jan 3, 2009
 * 
 */
public class CsvReader {

	private BufferedReader reader;

	private String filepath;

	private String csvSep;


	public CsvReader() {
		this(","); //$NON-NLS-1$
	}

	public CsvReader(String csvSep) {
		this.csvSep = csvSep;
	}
	
	public CsvReader(String csvSep, String filePath, boolean detectEndoding) throws IOException {
		this(csvSep);
		initialize(filePath);
		if( detectEndoding ){
			safeOpen();
		} else {
			open();
		}
	}

	
	public CsvReader initialize(String filePath) {
		this.filepath = filePath;
		return this;
	}
	
	public CsvReader initialize(String baseDir, String filename) {
		this.filepath = baseDir + GecoResources.sep + filename;
		return this;
	}

	public String filePath() {
		return filepath;
	}


	public String getCsvSep() {
		return csvSep;
	}

	public void setCsvSep(String csvSep) {
		this.csvSep = csvSep;
	}

	public void open() throws FileNotFoundException {
		this.reader = GecoResources.getReaderFor(filePath());
	}

	public void safeOpen() throws FileNotFoundException {
		this.reader = GecoResources.getSafeReaderFor(filePath());
	}

	public void close() throws IOException {
		this.reader.close();
	}

	public String read() throws IOException {
		return this.reader.readLine();
	}
	
	public String[] readRecord(String csvSep) throws IOException {
		String read = read();
		if( read==null ) {
			return null;
		} else {
			return Util.splitAndTrim(read, csvSep);
		}
	}
	
	public String[] readRecord() throws IOException {
		return readRecord(this.csvSep);
	}

}
