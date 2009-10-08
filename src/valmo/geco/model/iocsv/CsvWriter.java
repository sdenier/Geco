/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.model.iocsv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import valmo.geco.core.Util;


/**
 * @author Simon Denier
 * @since Jan 3, 2009
 * 
 */
public class CsvWriter {

	private BufferedWriter writer;

	private String filepath;

	private String csvSep;


	public CsvWriter initialize(String filePath) {
		this.filepath = filePath;
		return this;
	}
	
	public CsvWriter initialize(String baseDir, String filename) {
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

	public BufferedWriter writer() {
		return this.writer;
	}

	public void open() {
		try {
			this.writer = new BufferedWriter(new FileWriter(filePath()));
		} catch (IOException e) {
			e.printStackTrace();
			close();
		}
	}

	public void close() {
		try {
			if (writer() != null) {
				writer().close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write(String str) {
		try {
			if (writer() != null) {
				writer().write(str);
			}
		} catch (IOException e) {
			e.printStackTrace();
			close();
		}
	}
	
	public void writeRecord(String[] record, String csvSep) {
		write(Util.join(record, csvSep, new StringBuffer()));
		try {
			writer().newLine();
		} catch (IOException e) {
			e.printStackTrace();
			close();
		}
	}
	
	public void writeRecord(String[] record) {
		writeRecord(record, this.csvSep);
	}

}
