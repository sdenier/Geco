/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import net.geco.basics.GecoResources;
import net.geco.basics.Html;
import net.geco.model.Course;
import net.geco.model.Heat;
import net.geco.model.HeatSet;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.Runner;
import net.geco.model.iocsv.CsvWriter;
import net.geco.model.iocsv.RunnerIO;


/**
 * @author Simon Denier
 * @since Jan 18, 2009
 *
 */
public class HeatBuilder extends Control {
	
	private int startId;

	private Vector<Heat> heats;

	private ResultBuilder resultBuilder;


	public HeatBuilder(GecoControl gecoControl) {
		super(HeatBuilder.class, gecoControl);
		this.resultBuilder = getService(ResultBuilder.class);
	}
	
	public HeatSet createHeatSet() {
		return factory().createHeatSet();
	}

	private Vector<Heat> getHeats(HeatSet[] selectedHeatsets) {
		if( heats==null ) {
			buildHeats(selectedHeatsets);
		}
		return heats;
	}

	private Vector<Heat> buildHeats(HeatSet[] selectedHeatsets) {
		heats = new Vector<Heat>();
		for (HeatSet heatset : selectedHeatsets) {
			List<Result> heatsetResults
							= resultBuilder.buildResults(heatset.getSelectedPools(), heatset.getSetType());
			List<Heat> heatsForCurrentHeatset = buildHeatsFromResults(
					heatsetResults, 
					heatset.getName(),
					heatset.getHeatNames(), 
					heatset.getQualifyingRank());
			heats.addAll(heatsForCurrentHeatset);
		}
		return heats;
	}

	public List<Heat> buildHeatsFromResults(
							List<Result> results,
							String heatsetName,
							String[] heatnames,
							int qualifyingRank) {
		int nbHeats = heatnames.length;
		Vector<Heat> heats = new Vector<Heat>(nbHeats);
		for (String name : heatnames) {
			Heat h = factory().createHeat();
			h.setHeatSetName(heatsetName);
			h.setName(name);
			heats.add(h);
		}
		Vector<List<RankedRunner>> rankings = new Vector<List<RankedRunner>>();
		for (Result result : results) {
			rankings.add(result.getRanking());
		}

		int nbRankings = rankings.size();
		int miss = 0; // number of misses per pass through rankings
		int pos = 0; // current position requested in each ranking
		int heat = 0; // current heat where to add next qualified runner
		while( miss<nbRankings ) {
			miss = 0;
			int[] series = getRandomSeries(nbRankings);
			for (int j : series) {
				try {
					RankedRunner runner = rankings.get(j).get(pos);
					if( runner.getRank() <= qualifyingRank ) {
						heats.get(heat).addQualifiedRunner(runner.getRunnerData().getRunner());
						heat = (heat + 1) % nbHeats;
					} else {
						miss++;
					}					
				} catch (IndexOutOfBoundsException e) {
					miss++;
				}				
			}
			pos++;
		}

		return heats;
	}
	
	private int[] getRandomSeries(int nb) {
		// makes nb random permutations in an array of [0,..,nb-1]
		int[] series = new int[nb];
		for (int i = 0; i < series.length; i++) {
			series[i] = i;
		}
		Random random = new Random();
		for (int i = 0; i < nb; i++) {
			int j = random.nextInt(nb);
			int old = series[j];
			series[j] = series[i];
			series[i] = old;
		}
		return series;
	}

	
	public String refreshHtmlHeats(HeatSet[] selectedHeatsets) {
		heats = null;
		return generateHtmlHeats(selectedHeatsets, false);
	}
	
	public void exportFile(String filename, String format, HeatSet[] selectedHeatsets) throws IOException {
		if( !filename.endsWith(format) ) {
			filename = filename + "." + format; //$NON-NLS-1$
		}
		if( format.equals("html") ) { //$NON-NLS-1$
			BufferedWriter writer = GecoResources.getSafeWriterFor(filename);
			writer.write(generateHtmlHeats(selectedHeatsets, true));	
			writer.close();
		}
		if( format.equals("csv") ) { //$NON-NLS-1$
			generateCsvHeats(filename, selectedHeatsets);
		}
	}

	public String generateHtmlHeats(HeatSet[] selectedHeatsets, boolean forFileExport) {
		Vector<Heat> heats = getHeats(selectedHeatsets);
		Html html = new Html();

		html.open("head").nl(); //$NON-NLS-1$
		if( forFileExport ){
			html.contents("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">"); //$NON-NLS-1$
		}
		html.close("head").nl(); //$NON-NLS-1$

		for (Heat heat : heats) {
			appendHtmlHeat(heat, html);
		}
		return html.close();
	}
	private void appendHtmlHeat(Heat heat, Html html) {
		html.tag("h1", heat.getHeatSetName() + " " + heat.getName()).nl(); //$NON-NLS-1$ //$NON-NLS-2$
		html.open("table").nl(); //$NON-NLS-1$
		int i = 1;
		for (Runner runner : heat.getQualifiedRunners()) {
			html.open("tr").td(Integer.toString(i)).td(runner.getName()).close("tr").nl(); //$NON-NLS-1$ //$NON-NLS-2$
			i++;
		}
		html.close("table").nl(); //$NON-NLS-1$
	}
	
	public void generateCsvHeats(String filename, HeatSet[] selectedHeatsets) throws IOException {
		Vector<Heat> heats = getHeats(selectedHeatsets);
		resetStartId();
		CsvWriter writer = new CsvWriter();
		writer.initialize(filename);
		writer.open();
		for (Heat heat : heats) {
			appendCsvHeat(heat, writer);
		}
		writer.close();
	}
	private void appendCsvHeat(Heat heat, CsvWriter writer) throws IOException {
		RunnerIO runnerIO = new RunnerIO(null, null, writer, null, stage().getZeroHour());
		Course heatCourse = factory().createCourse();
		heatCourse.setName(heat.getName());
		for (Runner runner : heat.getQualifiedRunners()) {
			writer.writeRecord(runnerIO.exportTData(cloneRunnerForHeat(runner, heatCourse)));
		}
	}
	private Runner cloneRunnerForHeat(Runner runner, Course heatCourse) {
		return runner.copyWith(newStartId(), runner.getEcard(), heatCourse);
	}
	
	private void resetStartId() {
		startId = 0;
	}
	private Integer newStartId() {
		return ++startId;
	}
	
}
