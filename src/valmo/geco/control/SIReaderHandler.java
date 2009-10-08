/**
 * Copyright (c) 2009 Simon Denier
 */
package valmo.geco.control;

import org.martin.sireader.common.ResultData;
import org.martin.sireader.server.IResultData;
import org.martin.sireader.server.PortMessage;
import org.martin.sireader.server.SIPortHandler;
import org.martin.sireader.server.SIReaderListener;

import valmo.geco.model.Factory;
import valmo.geco.model.Stage;
import valmo.geco.ui.Announcer;

/**
 * @author Simon Denier
 * @since Oct 8, 2009
 *
 */
public class SIReaderHandler extends Control implements SIReaderListener {

	private Announcer announcer;
	
	private SIPortHandler portHandler;
	
	/**
	 * @param factory
	 * @param stage
	 * @param announcer
	 */
	public SIReaderHandler(Factory factory, Stage stage, Announcer announcer) {
		super(factory, stage, announcer);
		this.announcer = announcer;
		this.portHandler = new SIPortHandler(new ResultData());
		this.portHandler.addListener(this);
	}

	public void configure() {
		portHandler.setPortName("/dev/tty.SLAB_USBtoUART");		// TODO: take parameter from stage properties
		portHandler.setDebugDir(".");
	}

	public void reconfigure() {
		stop();
		configure();
		portHandler.restart();
	}

	public void start() {
		configure();
		if (!portHandler.isAlive())
			portHandler.start();
		PortMessage m = new PortMessage(SIPortHandler.START);
		portHandler.sendMessage(m);
	}
	
	public void stop() {
		try {
			portHandler.interrupt();
			portHandler.join();
		} catch (InterruptedException e) {
			// TODO log
			e.printStackTrace();
		}
	}

	@Override
	public void newCardRead(IResultData card) {
		/* 
		 * get chip number
		 * retrieve runner from chip number
		 * check if already has result (or no runner found)
		 * createRunnerRaceData()
		 */
	}


	@Override
	public void portStatusChanged(String status) {
		
	}
	
}
