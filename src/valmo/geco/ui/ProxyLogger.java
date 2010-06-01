/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.ui;

import valmo.geco.core.Logger;

/**
 * @author Simon Denier
 * @since Jun 1, 2010
 *
 */
public class ProxyLogger extends Logger {

	private LogPanel uiLog;
	
	/**
	 * @param basedir
	 * @param filename
	 */
	public ProxyLogger(String basedir, String filename) {
		super(basedir, filename);
	}
	

	/**
	 * @param basedir
	 * @param filename
	 * @param fileDebug
	 */
	public ProxyLogger(String basedir, String filename, boolean fileDebug) {
		super(basedir, filename, fileDebug);
	}


//	protected LogPanel getUiLog() {
//		return uiLog;
//	}


	protected void setUiLog(LogPanel uiLog) {
		this.uiLog = uiLog;
	}


	@Override
	public void log(String text) {
		super.log(text);
		if( uiLog!=null ) {
			uiLog.displayLog(text);			
		}
	}


}
