/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.framework;

import valmo.geco.basics.Announcer;
import valmo.geco.basics.GecoRequestHandler;
import valmo.geco.basics.Logger;
import valmo.geco.control.ArchiveManager;
import valmo.geco.control.CNCalculator;
import valmo.geco.control.HeatBuilder;
import valmo.geco.control.PenaltyChecker;
import valmo.geco.control.RegistryStats;
import valmo.geco.control.ResultBuilder;
import valmo.geco.control.ResultExporter;
import valmo.geco.control.RunnerControl;
import valmo.geco.control.SIReaderHandler;
import valmo.geco.control.SingleSplitPrinter;
import valmo.geco.control.SplitExporter;
import valmo.geco.control.StageControl;
import valmo.geco.control.StartlistImporter;
import valmo.geco.model.Registry;

/**
 * @author Simon Denier
 * @since Apr 3, 2011
 *
 */
public interface IGeco {

	public Registry registry();
	public Announcer announcer();
	public GecoRequestHandler defaultMergeHandler();
	public GecoRequestHandler autoMergeHandler();
	
	public Logger logger();
	public void debug(String message);
	public void log(String message);
	public void info(String message, boolean warning);
	
	public PenaltyChecker checker();
	public StageControl stageControl();
	public RunnerControl runnerControl();
	public ResultBuilder resultBuilder();
	public ResultExporter resultExporter();
	public SplitExporter splitsExporter();
	public SingleSplitPrinter splitPrinter();
	public HeatBuilder heatBuilder();	
	public RegistryStats registryStats();
	public SIReaderHandler siHandler();
	public ArchiveManager archiveManager();
	public StartlistImporter startlistImporter();
	public CNCalculator cnCalculator();
	
}
