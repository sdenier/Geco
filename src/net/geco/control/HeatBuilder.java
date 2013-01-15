/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.geco.basics.CsvWriter;
import net.geco.basics.GecoResources;
import net.geco.basics.Html;
import net.geco.basics.TimeManager;
import net.geco.model.Heat;
import net.geco.model.HeatSet;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.Runner;


/**
 * @author Simon Denier
 * @since Jan 18, 2009
 *
 */
public class HeatBuilder extends Control {
	
	private List<Heat> heats;

	private ResultBuilder resultBuilder;


	public HeatBuilder(GecoControl gecoControl) {
		super(HeatBuilder.class, gecoControl);
		this.resultBuilder = getService(ResultBuilder.class);
	}
	
	public HeatSet createHeatSet() {
		return factory().createHeatSet();
	}

	private List<Heat> getHeats(HeatSet[] selectedHeatsets) {
		if( heats==null ) {
			buildHeats(selectedHeatsets);
		}
		return heats;
	}

	private List<Heat> buildHeats(HeatSet[] selectedHeatsets) {
		heats = new ArrayList<Heat>();
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
		List<Heat> heats = new ArrayList<Heat>(nbHeats);
		for (String name : heatnames) {
			Heat h = factory().createHeat();
			h.setHeatSetName(heatsetName);
			h.setName(name);
			heats.add(h);
		}
		List<List<RankedRunner>> rankings = new ArrayList<List<RankedRunner>>(results.size());
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
		List<Heat> heats = getHeats(selectedHeatsets);
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
		List<Heat> heats = getHeats(selectedHeatsets);
		CsvWriter writer = new CsvWriter(";"); //$NON-NLS-1$
		writer.initialize(filename);
		writer.open();
		writer.write(startlistCsvHeader());
		writer.write("\n"); //$NON-NLS-1$
		for (Heat heat : heats) {
			appendCsvHeat(heat, writer);
		}
		writer.close();
	}
	
	private String startlistCsvHeader() {
		return "Nº Start;Ecard;Id archive;Last name;First name;Birth year;Sex;" + //$NON-NLS-1$
				"Slot;NC;Start time;Finish time;Time/'Geco-course';Eval/Course;" + //$NON-NLS-1$
				"Nº club;Short club;Long club;Nat;N° cat;Short cat;Long cat"; //$NON-NLS-1$
	}

	private void appendCsvHeat(Heat heat, CsvWriter writer) throws IOException {
		for (Runner runner : heat.getQualifiedRunners()) {
			writer.writeRecord(exportRunnerStartEntry(runner, heat.getName()));
		}
	}

	private String[] exportRunnerStartEntry(Runner runner, String heatName) {
		// [0-4] N° dép.;Puce;Ident. base de données;Nom;Prénom;
		// [5-12] Né;S;Plage;nc;Départ;Arrivée;Temps/"Geco-course";Evaluation/**HEAT COURSE**;
		// [13-19] N° club;Nom;Ville;Nat;N° cat.;Court;Long;
		// ;1061511;10869;DENIER;Simon;;;;;--:--;;Geco-course;Finale A;;5906NO;VALMO;;;H21A;H21 A
		return new String[] {
				"", //$NON-NLS-1$
				runner.getEcard(),
				(runner.getArchiveId() == null) ? "" : runner.getArchiveId().toString(), //$NON-NLS-1$
				runner.getLastname(),
				runner.getFirstname(),
				"", "", "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Boolean.toString(runner.isNC()),
				TimeManager.NO_TIME_STRING,
				"", //$NON-NLS-1$
				"Geco-course", heatName, //$NON-NLS-1$
				"", runner.getClub().getShortname(), runner.getClub().getName(), //$NON-NLS-1$
				"", "", //$NON-NLS-1$ //$NON-NLS-2$
				runner.getCategory().getShortname(), runner.getCategory().getLongname(),
				" ", //$NON-NLS-1$
		};
	}
	
}
