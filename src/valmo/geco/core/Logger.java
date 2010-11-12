/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * @author Simon Denier
 * @since Feb 6, 2009
 *
 */
public class Logger {

	private boolean fileDebug;
	private String baseDir;
	private BufferedWriter writer;
	private BufferedWriter debug;
	
	public Logger(String basedir, String filename, boolean fileDebug) {
		this.fileDebug = fileDebug;
		this.baseDir = basedir;
		initLogFile(basedir + File.separator + filename);
	}

	public Logger(String basedir, String filename) {
		this(basedir, filename, true);
	}

	public void initLogFile(String filename) {
		if( writer!=null ) {
			close();
		}
		try {
			this.writer = new BufferedWriter(new FileWriter(filename, true));
		} catch (IOException e) {
			debug(e);
		}
	}
	
	public void log(String text) {
		if( writer!=null ) {
			try {
				this.writer.write(text);
				this.writer.newLine();
				this.writer.flush();
			} catch (IOException e) {
				debug(e);
			}
		}
	}

	public void logWithTime(String text) {
		log(new Date().toString() + " - " + text);	 //$NON-NLS-1$
	}
	
	public void initSessionLog(String header) {
		log("*** Log Session for " + header //$NON-NLS-1$
				+ " started at " + new Date().toString() //$NON-NLS-1$
				+ " ***"); //$NON-NLS-1$
	}

	public void close() {
		if( writer!=null ) {
			try {
				log("*** Log Session closed at " + new Date().toString() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				this.writer.close();
			} catch (IOException e) {
				debug(e);
			}
		}
		if( debug!=null ) {
			try {
				this.debug.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void debug(String text) {
		if( fileDebug ) {
			try {
				initDebugFile();
				this.debug.write(new Date().toString() + " - " + text + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (IOException e) {
				System.err.println(text);
				e.printStackTrace();
			}
		} else {
			System.err.println(text);
		}
	}
	
	public void debug(Exception e) {
		if( fileDebug ) {
			try {
				initDebugFile();
				printStackTrace(e);
			} catch (IOException e1) {
				System.err.println(e.toString());
				e1.printStackTrace();
			}
		} else {
			e.printStackTrace();
		}		
	}

	private void initDebugFile() throws IOException {
		if( debug==null ) {
			debug = new BufferedWriter(
						new FileWriter(this.baseDir + File.separator + "debug.log", true)); //$NON-NLS-1$
		}
	}
	
    private void printStackTrace(Exception e) throws IOException {
		this.debug.write(new Date().toString() + " - " + e.toString() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		StackTraceElement[] trace = e.getStackTrace();
		for (StackTraceElement el : trace) {
			this.debug.write("\t" + el + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		Throwable cause = e.getCause();
		if( cause!=null ) {
			this.debug.write("Caused by " + cause.toString() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
    }
	
}
