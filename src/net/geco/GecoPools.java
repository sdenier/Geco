/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import net.geco.basics.GecoResources;
import net.geco.basics.Html;
import net.geco.basics.Util;
import net.geco.control.GecoControl;
import net.geco.control.HeatBuilder;
import net.geco.control.PenaltyChecker;
import net.geco.control.PoolMerger;
import net.geco.control.ResultBuilder;
import net.geco.control.StageBuilder;
import net.geco.model.HeatSet;
import net.geco.model.Messages;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;


/**
 * @author Simon Denier
 * @since Aug 22, 2010
 *
 */
public class GecoPools {

	private Vector<Stage> poolStages;

	private GecoControl gecoControl;
	
	
	public static void main(String[] args) {
		new GecoPools().run(args[0]);
	}
	
	public void run(String baseDir) {
		gecoControl = new GecoControl(baseDir);
		try {
			importStages(Util.readLines(StageBuilder.filepath(baseDir, "pools.prop"))); //$NON-NLS-1$
			mergePools();
			gecoControl.saveCurrentStage();
			exportMergedResults();
			buildHeats();
			System.out.println(Messages.getString("GecoPools.MergeOkLabel")); //$NON-NLS-1$
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void importStages(Collection<String> poolDirs) {
		poolStages = new Vector<Stage>(poolDirs.size());
		StageBuilder stageBuilder = new StageBuilder(gecoControl.factory());
		for (String dir : poolDirs) {
			poolStages.add( stageBuilder.loadStage(dir, new PenaltyChecker(gecoControl.factory())) );
		}
	}
	
	public void mergePools() {
		new PoolMerger(gecoControl).merge(poolStages);
	}
	
	public void exportMergedResults() {
		ResultBuilder resultBuilder = new ResultBuilder(gecoControl);
		Html html = new Html();
		for (String cat : gecoControl.registry().getCategoryNames()) {
			exportMergedResult(cat, resultBuilder, html);
		}
		try {
			String filepath = StageBuilder.filepath(gecoControl.stage().getBaseDir(), "merged_results.html"); //$NON-NLS-1$
			BufferedWriter writer = GecoResources.getSafeWriterFor(filepath);
			writer.write(html.close());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void exportMergedResult(String category, ResultBuilder resultBuilder, Html html) {
		List<Result> results = resultBuilder.buildResultForCategoryByCourses(gecoControl.registry().findCategory(category));
		for (Result result : results) {
			html.tag("h1", result.getIdentifier()); //$NON-NLS-1$
			html.open("table"); //$NON-NLS-1$
			for (RankedRunner runner : result.getRanking()) {
				writeResult(runner.getRunnerData(), Integer.toString(runner.getRank()), html);
			}
			html.openTr().closeTr();
		
			for (RunnerRaceData runnerData : result.getNRRunners()) {
				writeResult(runnerData, "", html); //$NON-NLS-1$
			}
			html.openTr().closeTr();
		
			for (RunnerRaceData runnerData : result.getOtherRunners()) {
				writeResult(runnerData, "", html); //$NON-NLS-1$
			}
			html.close("table"); //$NON-NLS-1$
		}
	}

	private void writeResult(RunnerRaceData runnerData, String rank, Html html) {
		html.openTr().td(rank).td(runnerData.getRunner().getName());
		for (Stage pool : poolStages) {
			html.td(findRunnerDataIn(runnerData, pool).getResult().shortFormat());
		}
		html.td(runnerData.getResult().shortFormat());
		html.closeTr();
	}
	private RunnerRaceData findRunnerDataIn(RunnerRaceData data, Stage pool) {
		return pool.registry().findRunnerData(data.getRunner().getEcard());
	}
	
	
	public void buildHeats() {
		new ResultBuilder(gecoControl); // used by HeatBuilder
		HeatBuilder heatBuilder = new HeatBuilder(gecoControl);
		try {
			String filepath = StageBuilder.filepath(gecoControl.stage().getBaseDir(), "heat_startlists.csv"); //$NON-NLS-1$
			heatBuilder.generateCsvHeats(filepath, gecoControl.registry().getHeatSets().toArray(new HeatSet[0]));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
