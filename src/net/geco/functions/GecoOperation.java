/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public abstract class GecoOperation extends Control {
	// TODO: break Control inheritance from GecoOperation

	public GecoOperation(GecoControl gecoControl, OperationCategory fc) {
		super(gecoControl);
		register(this, fc);
	}

	public static enum OperationCategory {
		STAGE {
			public String toString() {
				return Messages.uiGet("GecoOperation.StageLabel"); //$NON-NLS-1$
			}},
		REFEREE {
			public String toString() {
				return Messages.uiGet("GecoOperation.RefereeLabel"); //$NON-NLS-1$
			}},
		BATCH {
			public String toString() {
				return Messages.uiGet("GecoOperation.BatchLabel"); //$NON-NLS-1$
			}}
	}
	
	private static Map<OperationCategory, List<GecoOperation>> operations = resetAll();	

	public static Map<OperationCategory, List<GecoOperation>> resetAll() {
		operations = new HashMap<GecoOperation.OperationCategory, List<GecoOperation>>();
		return operations;
	}
	
	public static GecoOperation[] getAll(OperationCategory fc) {
		return operations.get(fc).toArray(new GecoOperation[0]);
	}
	
	public static void register(GecoOperation operation, OperationCategory cat) {
		if( !operations.containsKey(cat) ){
			operations.put(cat, new ArrayList<GecoOperation>(5));
		}
		operations.get(cat).add(operation);
	}
	
	public abstract String toString();
	
	public abstract void execute();
	
	public abstract String executeTooltip();
	
	public abstract JComponent getParametersConfig();
	
	public JComponent buildUI() {
		JComponent parametersConfig = getParametersConfig();
		parametersConfig.setBorder(
			BorderFactory.createTitledBorder(Messages.uiGet("GecoOperation.ParameterLabel"))); //$NON-NLS-1$
		return parametersConfig;
	}
	
	public void updateUI() {}
	
}
