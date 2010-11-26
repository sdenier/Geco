/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.functions;

import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import valmo.geco.control.Control;
import valmo.geco.control.GecoControl;
import valmo.geco.model.Messages;

/**
 * @author Simon Denier
 * @since Nov 9, 2010
 *
 */
public abstract class GecoFunction extends Control {

	public GecoFunction(GecoControl gecoControl) {
		super(gecoControl);
		registerFunction(this);
	}

	private static Vector<GecoFunction> functions = new Vector<GecoFunction>();
	
	public static Vector<GecoFunction> functions() {
		return functions;
	}
	
	public static void registerFunction(GecoFunction function) {
		functions.add(function);
	}
	
	public abstract String toString();
	
	public abstract void execute();
	
	public abstract String executeTooltip();
	
	public abstract JComponent getParametersConfig();
	
	public JComponent getFunctionUI() {
//		JButton execB = new JButton("Execute");
//		execB.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				execute();
//			}
//		});
//		Box box = Box.createVerticalBox();
//		box.add(execB);
//		JPanel layout = new JPanel(new BorderLayout());
//		layout.add(getParametersConfig(), BorderLayout.CENTER);
//		layout.add(box, BorderLayout.EAST);
//		layout.setBorder(BorderFactory.createTitledBorder("Parameters"));
//		return layout;
		JComponent parametersConfig = getParametersConfig();
		parametersConfig.setBorder(BorderFactory.createTitledBorder(Messages.uiGet("GecoFunction.ParameterLabel"))); //$NON-NLS-1$
		return parametersConfig;
	}
	
	public abstract void updateUI();
	
}
