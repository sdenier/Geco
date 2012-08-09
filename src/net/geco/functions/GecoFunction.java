/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.functions;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import net.geco.control.Control;
import net.geco.control.GecoControl;
import net.geco.model.Messages;


/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public abstract class GecoFunction extends Control {

	public GecoFunction(GecoControl gecoControl, FunctionCategory fc) {
		super(gecoControl);
		registerFunction(this, fc);
	}

	public static enum FunctionCategory {
		STAGE {
			public String toString() {
				return Messages.uiGet("GecoFunction.StageLabel"); //$NON-NLS-1$
			}},
		REFEREE {
			public String toString() {
				return Messages.uiGet("GecoFunction.RefereeLabel"); //$NON-NLS-1$
			}},
		BATCH {
			public String toString() {
				return Messages.uiGet("GecoFunction.BatchLabel"); //$NON-NLS-1$
			}}
	}
	
	private static Map<FunctionCategory, Vector<GecoFunction>> functions =	resetAll();	

	public static Map<FunctionCategory, Vector<GecoFunction>> resetAll() {
		functions = new HashMap<GecoFunction.FunctionCategory, Vector<GecoFunction>>();
		return functions;
	}
	
	public static Vector<GecoFunction> getFunctions(FunctionCategory fc) {
		return functions.get(fc);
	}
	
	public static void registerFunction(GecoFunction function, FunctionCategory fc) {
		if( !functions.containsKey(fc) ){
			functions.put(fc, new Vector<GecoFunction>(5));
		}
		functions.get(fc).add(function);
	}
	
	public abstract String toString();
	
	public abstract void execute();
	
	public abstract String executeTooltip();
	
	public abstract JComponent getParametersConfig();
	
	public JComponent getFunctionUI() {
		JComponent parametersConfig = getParametersConfig();
		parametersConfig.setBorder(
			BorderFactory.createTitledBorder(Messages.uiGet("GecoFunction.ParameterLabel"))); //$NON-NLS-1$
		return parametersConfig;
	}
	
	public void updateUI() {}
	
}
