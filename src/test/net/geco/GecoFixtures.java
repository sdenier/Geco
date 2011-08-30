/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco;

import net.geco.app.AppBuilder;
import net.geco.control.GecoControl;
import net.geco.control.StageBuilder;
import net.geco.model.Factory;
import net.geco.model.Stage;

import org.mockito.Mockito;

/**
 * @author Simon Denier
 * @since Aug 27, 2011
 *
 */
public class GecoFixtures {
	
	public static GecoControl mockGecoControl() {
		return Mockito.mock(GecoControl.class);
	}
	
	public static Stage loadStageFrom(String baseDir, AppBuilder builder, GecoControl gecoControl) {
		return new StageBuilder(builder.getFactory()).loadStage(baseDir, builder.createChecker(gecoControl));
	}
	
	public static GecoControl loadFixtures(String baseDir, AppBuilder builder) {
		Factory factory = builder.getFactory();
		GecoControl gecoControl = mockGecoControl();
		Mockito.when(gecoControl.factory()).thenReturn(factory);
		Stage stage = loadStageFrom(baseDir, builder, gecoControl);
		Mockito.when(gecoControl.stage()).thenReturn(stage);
		Mockito.when(gecoControl.registry()).thenReturn(stage.registry());
		return gecoControl;
	}

}
