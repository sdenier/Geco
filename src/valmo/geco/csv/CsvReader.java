/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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


	public CsvReader initialize(String filePath) {
		this.filepath = filePath;
		return this;
	}
	
	public CsvReader initialize(String baseDir, String filename) {
		this.filepath = baseDir + File.separator + filename;
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

	public BufferedReader reader() {
		return this.reader;
	}

	public void open() {
		try {
			this.reader = new BufferedReader(new FileReader(filePath()));
		} catch (IOException e) {
			e.printStackTrace();
			close();
		}
	}

	public void close() {
		try {
			if (reader() != null) {
				reader().close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String read() {
		try {
			if (reader() != null) {
				return reader().readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
			close();
		}
		return null;
	}
	
	public String[] readRecord(String csvSep) {
		String read = read();
		if( read==null ) {
			return null;
		} else {
			return Util.splitAndTrim(read, csvSep);
		}
	}
	
	public String[] readRecord() {
		return readRecord(this.csvSep);
	}

}
