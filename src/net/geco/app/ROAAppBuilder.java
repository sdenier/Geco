/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.app;

import javax.swing.JFrame;

import net.geco.basics.GecoConfig;
import net.geco.control.ArchiveManager;
import net.geco.control.GecoControl;
import net.geco.control.MergeControl;
import net.geco.control.RegistryStats;
import net.geco.control.RunnerControl;
import net.geco.control.SIReaderHandler;
import net.geco.control.SectionService;
import net.geco.control.StageBuilder;
import net.geco.control.StageControl;
import net.geco.control.StartlistExporter;
import net.geco.control.StartlistImporter;
import net.geco.control.checking.SectionsChecker;
import net.geco.control.results.CNCalculator;
import net.geco.control.results.ResultBuilder;
import net.geco.control.results.ResultExporter;
import net.geco.control.results.RunnerSplitPrinter;
import net.geco.control.results.SectionsExporter;
import net.geco.control.results.SplitExporter;
import net.geco.framework.IGecoApp;
import net.geco.functions.DeleteFunction;
import net.geco.functions.ECardLogFunction;
import net.geco.functions.GeneratorFunction;
import net.geco.functions.LegNeutralizationFunction;
import net.geco.functions.RecheckFunction;
import net.geco.functions.RefereeLogFunction;
import net.geco.functions.ResetECardFunction;
import net.geco.functions.StartTimeFunction;
import net.geco.functions.StationLogFunction;
import net.geco.model.Factory;
import net.geco.model.Messages;
import net.geco.model.impl.SectionFactory;
import net.geco.ui.config.CategoryConfigPanel;
import net.geco.ui.config.ClubConfigPanel;
import net.geco.ui.config.CourseConfigPanel;
import net.geco.ui.config.PenaltyCheckerConfigPanel;
import net.geco.ui.config.SIReaderConfigPanel;
import net.geco.ui.config.StageConfigPanel;
import net.geco.ui.framework.ConfigPanel;
import net.geco.ui.framework.TabPanel;
import net.geco.ui.framework.UIAnnouncers;
import net.geco.ui.tabs.LogPanel;
import net.geco.ui.tabs.ResultsPanel;
import net.geco.ui.tabs.RunnersPanel;

/**
 * @author Simon Denier
 * @since May 6, 2011
 *
 */
public class ROAAppBuilder extends AppBuilder {

	public static String getName() {
		return Messages.getString("ROAAppBuilder.RoaAppName"); //$NON-NLS-1$
	}
	
	@Override
	public String getAppName() {
		return getName();
	}

	@Override
	public GecoConfig getConfig() {
		return new GecoConfig(true);
	}

	@Override
	protected Factory createFactory() {
		return new SectionFactory();
	}

	@Override
	public SectionsChecker createChecker(GecoControl gecoControl) {
		return new SectionsChecker(gecoControl);
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
		new RunnerSplitPrinter(gecoControl);
		new RegistryStats(gecoControl);
		new ArchiveManager(gecoControl);
		new SIReaderHandler(gecoControl);
		new StartlistImporter(gecoControl);
		new CNCalculator(gecoControl);
		new MergeControl(gecoControl);
		new StartlistExporter(gecoControl);
		new SectionService(gecoControl);
		new SectionsExporter(gecoControl);
		
		new RefereeLogFunction(gecoControl);
		new StartTimeFunction(gecoControl);
		new LegNeutralizationFunction(gecoControl);
		new StationLogFunction(gecoControl);
		new ECardLogFunction(gecoControl);
		new RecheckFunction(gecoControl);
		new ResetECardFunction(gecoControl);
		new DeleteFunction(gecoControl);
		new GeneratorFunction(gecoControl);
	}

	@Override
	public TabPanel[] buildUITabs(IGecoApp geco, JFrame frame, UIAnnouncers announcers) {
		return new TabPanel[]{
				new RunnersPanel(geco, frame, announcers),
				new LogPanel(geco, frame),
				new ResultsPanel(geco, frame),
		};
	}

	@Override
	public ConfigPanel[] buildConfigPanels(IGecoApp geco, JFrame frame) {
		return new ConfigPanel[] {
				new StageConfigPanel(geco, frame),
				new SIReaderConfigPanel(geco, frame),
				new PenaltyCheckerConfigPanel(geco),
				new CourseConfigPanel(geco, frame),
				new CategoryConfigPanel(geco, frame),
				new ClubConfigPanel(geco, frame),
		};
	}

}
