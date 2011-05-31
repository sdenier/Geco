/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.app;

import javax.swing.JFrame;

import net.geco.control.ArchiveManager;
import net.geco.control.AutoMergeHandler;
import net.geco.control.CNCalculator;
import net.geco.control.GecoControl;
import net.geco.control.HeatBuilder;
import net.geco.control.PenaltyChecker;
import net.geco.control.RegistryStats;
import net.geco.control.ResultBuilder;
import net.geco.control.ResultExporter;
import net.geco.control.RunnerControl;
import net.geco.control.SIReaderHandler;
import net.geco.control.SingleSplitPrinter;
import net.geco.control.SplitExporter;
import net.geco.control.StageBuilder;
import net.geco.control.StageControl;
import net.geco.control.StartlistImporter;
import net.geco.framework.IGecoApp;
import net.geco.functions.GeneratorFunction;
import net.geco.functions.RecheckFunction;
import net.geco.functions.StartTimeFunction;
import net.geco.model.Factory;
import net.geco.model.impl.POFactory;
import net.geco.ui.config.CategoryConfigPanel;
import net.geco.ui.config.ClubConfigPanel;
import net.geco.ui.config.CourseConfigPanel;
import net.geco.ui.config.PenaltyCheckerConfigPanel;
import net.geco.ui.config.SIReaderConfigPanel;
import net.geco.ui.config.StageConfigPanel;
import net.geco.ui.framework.ConfigPanel;
import net.geco.ui.framework.TabPanel;
import net.geco.ui.tabs.HeatsPanel;
import net.geco.ui.tabs.LogPanel;
import net.geco.ui.tabs.ResultsPanel;
import net.geco.ui.tabs.RunnersPanel;

/**
 * @author Simon Denier
 * @since May 6, 2011
 *
 */
public class OrientShowAppBuilder extends AppBuilder {

	@Override
	protected Factory createFactory() {
		return new POFactory();
	}

	@Override
	@SuppressWarnings("unchecked")
	public PenaltyChecker createChecker(GecoControl gecoControl) {
		return new PenaltyChecker(gecoControl);
	}

	@Override
	public StageBuilder createStageBuilder() {
		return new StageBuilder(getFactory());
	}

	@Override
	public void buildControls(GecoControl gecoControl) {
		new StageControl(gecoControl);
		new RunnerControl(gecoControl);
		new ResultBuilder(gecoControl);
		new ResultExporter(gecoControl);
		new SplitExporter(gecoControl);
		new SingleSplitPrinter(gecoControl);
		new HeatBuilder(gecoControl);
		new RegistryStats(gecoControl);
		new AutoMergeHandler(gecoControl);
		new SIReaderHandler(gecoControl);
		new ArchiveManager(gecoControl);
		new StartlistImporter(gecoControl);
		new CNCalculator(gecoControl);
		
		new StartTimeFunction(gecoControl);
		new RecheckFunction(gecoControl);
		new GeneratorFunction(gecoControl);
	}

	@Override
	public TabPanel[] buildUITabs(IGecoApp geco, JFrame frame) {
		return new TabPanel[]{
				new RunnersPanel(geco, frame),
				new LogPanel(geco, frame),
				new ResultsPanel(geco, frame),
				new HeatsPanel(geco, frame)
		};
	}

	@Override
	public ConfigPanel[] buildConfigPanels(IGecoApp geco, JFrame frame) {
		return new ConfigPanel[] {
				new StageConfigPanel(geco, frame),
				new SIReaderConfigPanel(geco),
				new PenaltyCheckerConfigPanel(geco),
				new CourseConfigPanel(geco, frame),
				new CategoryConfigPanel(geco, frame),
				new ClubConfigPanel(geco, frame),
		};
	}

}
