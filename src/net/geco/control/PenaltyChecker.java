/**
 * Copyright (c) 2008 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import net.geco.basics.Announcer.StageListener;
import net.geco.basics.TimeManager;
import net.geco.model.Factory;
import net.geco.model.Punch;
import net.geco.model.RunnerRaceData;
import net.geco.model.Stage;
import net.geco.model.Status;
import net.geco.model.Trace;


/**
 * @author Simon Denier
 * @since Dec 7, 2008
 *
 */
public class PenaltyChecker extends PunchChecker implements StageListener {	
	
	private long MPPenalty;

	private int MPLimit;
	
	private int nbMP;

	private long timePenalty;

	private Vector<Trace> trace;
	
	
	public PenaltyChecker(Factory factory) {
		super(factory);
		setMPLimit(defaultMPLimit());
		setMPPenalty(defaultMPPenalty());
	}

	public PenaltyChecker(GecoControl gecoControl) {
		this(gecoControl.factory());
		gecoControl.announcer().registerStageListener(this);
	}
	
	protected void postInitialize(Stage stage) {
		setNewProperties(stage);
	}
	
	/*
	 * This method has side-effect.
	 */
	@Override
	protected Status computeStatus(RunnerRaceData data) {
		Status status = super.computeStatus(data);
		data.getResult().setNbMPs(this.nbMP);
		data.getResult().setTrace(this.trace.toArray(new Trace[0]));
		return status;
	}
	
	@Override
	public long computeOfficialRaceTime(RunnerRaceData data) {
		long time = officialRaceTime(data);
		data.getResult().setTimePenalty(timePenalty);
		return time;
	}	
	
	public long officialRaceTime(RunnerRaceData data) {
		timePenalty = timePenalty(data.getResult().getNbMPs());
		long time = super.computeOfficialRaceTime(data);
		if( time==TimeManager.NO_TIME_l ) {
			return time;
		}
		time += timePenalty;
		return time;
	}
	
	public long timePenalty(int nbMPs) {
		return nbMPs * getMPPenalty();
	}

	/**
	 * This is a utility method to build a trace without checking codes, typically because
	 * the data comes from an unknown chip without a course.
	 * 
	 * @param data
	 */
	public void normalTrace(RunnerRaceData data) {
		Trace[] nTrace = new Trace[data.getPunches().length];
		for (int i = 0; i < nTrace.length; i++) {
			nTrace[i] = factory().createTrace(data.getPunches()[i]);
		}
		data.getResult().setTrace(nTrace);
	}


	/**
	 * This algorithm is able to compute an exact number of MPs for any course configuration 
	 * including butterflies, and for any MP event (jumping a control, inverting controls, missing
	 * a central control in a loop).
	 */
	@Override
	public Status checkCodes(int[] codes, Punch[] punches) {
		this.nbMP = 0;

		int[][] matrix = lcssMatrix(codes, punches);
		this.nbMP = codes.length - matrix[punches.length][codes.length];
		this.trace = trace2(codes, punches, matrix);
		return (this.nbMP <= getMPLimit()) ? Status.OK : Status.MP;
	}
	
	/**
	 * This is based on the same principles as the edit distance algorithms (Levenshtein), except it 
	 * computes the longest common (non-contiguous) subsequence rather than the edit distance (for
	 * intuitive reason). The number of MPs is then computed as the difference between the number of
	 * course codes and the length of this longest subsequence.
	 * 
	 * @param codes
	 * @param punches
	 * @return
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

		String[] path = trace(codes, punches, matrix);
		if( showTrace ) {
			for (int i = 0; i < path.length; i++) {
				System.out.format(":%4s", path[i]); //$NON-NLS-1$
			}
			System.out.println();			
		}
		return path;
	}
	

	public String[] trace(int[] codes, Punch[] punches, int[][] matrix) {
		StringBuffer path = new StringBuffer();
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

	public Vector<Trace> trace2(int[] codes, Punch[] punches, int[][] matrix) {
		Vector<Trace> path = new Vector<Trace>();
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
						path.add(0, factory().createTrace("-" + codes[i], new Date(0))); //$NON-NLS-1$
						i--;
						break choice;
					}
				}
			}
		}
		while( i>=0 ) {
			path.add(0, factory().createTrace("-" + codes[i], new Date(0))); //$NON-NLS-1$
			i--;			
		}
		while( j>=0 ) {
			path.add(0, factory().createTrace("+" + punches[j].getCode(), punches[j].getTime())); //$NON-NLS-1$
			j--;
		}
		return path;
	}

	public int defaultMPLimit() {
		return 0;
	}

	public long defaultMPPenalty() {
		return 0;
	}


	public long getMPPenalty() {
		return MPPenalty;
	}

	public void setMPPenalty(long penalty) {
		MPPenalty = penalty;
	}

	public int getMPLimit() {
		return MPLimit;
	}

	public void setMPLimit(int limit) {
		MPLimit = limit;
	}

	protected void setNewProperties(Stage stage) {
		String limit = stage.getProperties().getProperty(mpLimitProperty());
		if( limit!=null ) {
			try {
				setMPLimit(new Integer(limit));				
			} catch (NumberFormatException e) {
				setMPLimit(defaultMPLimit());
				System.err.println(e);
			}
		} else {
			setMPLimit(defaultMPLimit());
		}
		String penalty = stage.getProperties().getProperty(mpPenaltyProperty());
		if( penalty!=null ) {
			try {
				setMPPenalty(new Long(penalty));				
			} catch (NumberFormatException e) {
				setMPPenalty(defaultMPPenalty());
				System.err.println(e);
			}
		} else {
			setMPPenalty(defaultMPPenalty());
		}		
	}
	
	@Override
	public void changed(Stage previous, Stage next) {
		setNewProperties(next);
	}

	@Override
	public void saving(Stage stage, Properties properties) {
		properties.setProperty(mpLimitProperty(), new Integer(getMPLimit()).toString());
		properties.setProperty(mpPenaltyProperty(), new Long(getMPPenalty()).toString());
	}

	public static String mpLimitProperty() {
		return "MPLimit"; //$NON-NLS-1$
	}

	public static String mpPenaltyProperty() {
		return "MPPenalty"; //$NON-NLS-1$
	}

	@Override
	public void closing(Stage stage) {	}
	
}
