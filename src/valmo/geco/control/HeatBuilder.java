/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import valmo.geco.model.Factory;
import valmo.geco.model.Heat;
import valmo.geco.model.RankedRunner;
import valmo.geco.model.Result;
import valmo.geco.model.Runner;

/**
 * @author Simon Denier
 * @since Jan 18, 2009
 *
 */
public class HeatBuilder extends Control {
	
	/**
	 * @param factory
	 * @param stage
	 */
	public HeatBuilder(Factory factory) {
		super(factory);
	}

	public List<Heat> buildHeatsFromResults(List<Result> results, String[] heatnames, int qualifyingRank) {
		int nbHeats = heatnames.length;
		Vector<Heat> heats = new Vector<Heat>(nbHeats);
		for (String name : heatnames) {
			Heat h = factory().createHeat();
			h.setName(name);
			heats.add(h);
		}
//		for (int i = 0; i < nbHeats; i++) {
//			heats.add(new Heat());
//		}
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
//			for (List<RankedRunner> ranking : rankings) {
//			}
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
	
	public List<List<Runner>> buildHeatsFromResults(List<Result> results, int qualifyingRank, int nbHeats) {
		List<List<Runner>> heats = new Vector<List<Runner>>(nbHeats);
		for (int i = 0; i < nbHeats; i++) {
			heats.add(new Vector<Runner>());
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
//			for (List<RankedRunner> ranking : rankings) {
//			}
			int[] series = getRandomSeries(nbRankings);
			for (int j : series) {
				try {
					RankedRunner runner = rankings.get(j).get(pos);
					if( runner.getRank() <= qualifyingRank ) {
						heats.get(heat).add(runner.getRunnerData().getRunner());
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

//		int result = 0;
//		int nbResults = results.size();
//		int heat = 0;
//		int i = 0;
//		int miss = 0;
//		while( miss<nbResults ) {
//			try {
//				RankedRunner runner = rankings.get(result).get(i / nbResults);
//				System.out.println((i / nbResults) + " " + runner.getRunnerData().getRunner());
//				if( runner.getRank() <= qualifyingRank ) {
//					heats.get(heat).add(runner.getRunnerData().getRunner());
//					heat = (heat + 1) % nbHeats;
//					i++;
//					miss = 0;
//				} else {
//					miss++;
//				}
//			} catch (IndexOutOfBoundsException e) {
//				miss++;
//			}
//
//			result = (result + 1) % nbResults;
//		}
		
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

}
