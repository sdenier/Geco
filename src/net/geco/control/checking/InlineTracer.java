/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control.checking;

import java.util.LinkedList;
import java.util.List;

import net.geco.basics.TimeManager;
import net.geco.control.BasicControl;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.RunnerRaceData;
import net.geco.model.Trace;
import net.geco.model.TraceData;

/**
 * @author Simon Denier
 * @since Aug 9, 2011
 *
 */
public class InlineTracer extends BasicControl implements Tracer {

	public InlineTracer(Factory factory) {
		super(factory);
	}

	/**
	 * This algorithm is able to compute an exact number of MPs for any course configuration 
	 * including butterflies, and for any MP event (jumping a control, inverting controls, missing
	 * a central control in a loop).
	 */
	@Override
	public TraceData computeTrace(int[] codes, Punch[] punches) {
		int[][] matrix = lcssMatrix(codes, punches);
		TraceData traceData = factory().createTraceData();
		traceData.setNbMPs(codes.length - matrix[punches.length][codes.length]);
		List<Trace> trace = backtrace(codes, punches, matrix);
		traceData.setTrace(trace.toArray(new Trace[0]));
		traceData.setNbExtraneous(ExtraneousPunchTracer.compute(codes, trace).size());
		return traceData;
	}

	/**
	 * This is based on the same principles as the edit distance algorithms (Levenshtein), except it 
	 * computes the longest common (non-contiguous) subsequence rather than the edit distance (for
	 * intuitive reason). The number of MPs is then computed as the difference between the number of
	 * course codes and the length of this longest subsequence.
	 */
	public int[][] lcssMatrix(int[] codes, Punch[] punches) {
		int n = codes.length;
		int m = punches.length;
		int[][] matrix = new int[m+1][n+1];
		for (int i = 1; i < matrix.length; i++) {
			for (int j = 1; j < matrix[0].length; j++) {
				int commonLength = matrix[i-1][j-1];
				if( punches[i-1].getCode() != codes[j-1] || lengthLimit(commonLength, i, j) ) {
					// codes diff, or length exceeds index potential
					matrix[i][j] = max(matrix[i-1][j-1], matrix[i-1][j], matrix[i][j-1]);
				} else {
					// longest sequence (in context) len += 1
					matrix[i][j] = commonLength + 1;
				}
			}
		}
		return matrix;
	}
	
	private boolean lengthLimit(int cLength, int i, int j) {
		return cLength > Math.min(i, j); // cLength > minimal sequence length
	}
	
	private int max(int a, int b, int c) {
		return Math.max(a, Math.max(b, c));
	}

	public List<Trace> backtrace(int[] codes, Punch[] punches, int[][] matrix) {
		List<Trace> path = new LinkedList<Trace>();
		int i = codes.length - 1;
		int j = punches.length - 1;
		
		while( i>=0 && j>=0 ) {
			if( codes[i]==punches[j].getCode() ) {
				path.add(0, factory().createTrace(punches[j]));
				i--;
				j--;
			} else {
				int max = max(matrix[j][i], matrix[j+1][i], matrix[j][i+1]);
				choice: {
					if( max==matrix[j][i] ) {
						Trace t = factory().createTrace("-" + codes[i] + "+" + punches[j].getCode(), //$NON-NLS-1$ //$NON-NLS-2$
															punches[j].getTime());
						path.add(0, t);
						i--;
						j--;
						break choice;
					}
					if( max==matrix[j][i+1] ) {
						path.add(0, factory().createTrace("+" + punches[j].getCode(),  //$NON-NLS-1$
															punches[j].getTime()));
						j--;
						break choice;
					}
					if( max==matrix[j+1][i] ) {
						path.add(0, factory().createTrace("-" + codes[i], TimeManager.NO_TIME)); //$NON-NLS-1$
						i--;
						break choice;
					}
				}
			}
		}
		while( i>=0 ) {
			path.add(0, factory().createTrace("-" + codes[i], TimeManager.NO_TIME)); //$NON-NLS-1$
			i--;			
		}
		while( j>=0 ) {
			path.add(0, factory().createTrace("+" + punches[j].getCode(), punches[j].getTime())); //$NON-NLS-1$
			j--;
		}
		return path;
	}

	/**
	 * Produce a human readable trace. For legacy.
	 * 
	 * @see #getTraceAsString
	 */
	public String[] humanReadableTrace(int[] codes, Punch[] punches, int[][] matrix) {
		StringBuilder path = new StringBuilder();
		int i = codes.length - 1;
		int j = punches.length - 1;
		
		while( i>=0 && j>=0 ) {
			if( codes[i]==punches[j].getCode() ) {
				path.insert(0, punches[j].getCode());
				i--;
				j--;
			} else {
				int max = max(matrix[j][i], matrix[j+1][i], matrix[j][i+1]);
				choice: {
					if( max==matrix[j][i] ) {
						path.insert(0, "-" + codes[i] + "+" + punches[j].getCode()); //$NON-NLS-1$ //$NON-NLS-2$
						i--;
						j--;
						break choice;
					}
					if( max==matrix[j][i+1] ) {
						path.insert(0, "+" + punches[j].getCode()); //$NON-NLS-1$
						j--;
						break choice;
					}
					if( max==matrix[j+1][i] ) {
						path.insert(0, "-" + codes[i]); //$NON-NLS-1$
						i--;
						break choice;
					}
				}
			}
			path.insert(0, ","); //$NON-NLS-1$
		}
		while( i>=0 ) {
			path.insert(0, ",-" + codes[i]); //$NON-NLS-1$
			i--;			
		}
		while( j>=0 ) {
			path.insert(0, ",+" + punches[j].getCode()); //$NON-NLS-1$
			j--;
		}
		
		return path.substring(1).split(","); //$NON-NLS-1$
	}

	public void showMatrix(int[] codes, Punch[] punches, int[][] matrix) {
		String f = "%4d"; //$NON-NLS-1$
		System.out.print("\n        "); //$NON-NLS-1$
		for (int i = 0; i < codes.length; i++) {
			System.out.format(f, codes[i]);
		}
		System.out.println();
		System.out.print("    "); //$NON-NLS-1$

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				System.out.format(f, matrix[i][j]);				
			}
			if( i < matrix.length -1 ) {
				System.out.format("\n%4s", punches[i].getCode()); //$NON-NLS-1$
			}
		}
		System.out.println();
	}
	
	public String[] explainTrace(RunnerRaceData data) {
		return explainTrace(data, false, false);
	}
	
	public String[] explainTrace(RunnerRaceData data, boolean showMatrix, boolean showTrace) {
		return explainTrace(data.getCourse().getCodes(), data.getPunches(), showMatrix, showTrace);
	}
	
	public String[] explainTrace(int[] codes, Punch[] punches) {
		return explainTrace(codes, punches, false, false);
	}
	
	public String[] explainTrace(int[] codes, Punch[] punches, boolean showMatrix, boolean showTrace) {
		int[][] matrix = lcssMatrix(codes, punches);
		if( showMatrix ) {
			showMatrix(codes, punches, matrix);
			System.out.println();			
		}

		String[] path = humanReadableTrace(codes, punches, matrix);
		if( showTrace ) {
			for (int i = 0; i < path.length; i++) {
				System.out.format(":%4s", path[i]); //$NON-NLS-1$
			}
			System.out.println();			
		}
		return path;
	}

}
