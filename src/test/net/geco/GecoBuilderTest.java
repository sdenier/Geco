/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco;

import junit.framework.Assert;
import net.geco.Geco;
import net.geco.GecoLauncher;
import net.geco.app.AppBuilder;
import net.geco.app.OrientShowAppBuilder;
import net.geco.basics.Announcer;
import net.geco.control.GecoControl;
import net.geco.framework.IGecoApp;
import net.geco.ui.GecoWindow;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * @author Simon Denier
 * @since May 1, 2011
 *
 */
public class GecoBuilderTest {
	
	private AppBuilder mockBuilder;
	private GecoLauncher mockLauncher;

	@Before
	public void setUp(){
		mockLauncher = Mockito.mock(GecoLauncher.class);
		mockBuilder = Mockito.mock(AppBuilder.class);
	}
	
	@Test
	public void gecoLauncherLoadProperties() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		GecoLauncher<AppBuilder> launcher = new GecoLauncher<AppBuilder>();
		launcher.loadStageProperties("testData/valid");
		Assert.assertEquals("testData/valid", launcher.getStageDir());
		Assert.assertTrue(launcher.getAppBuilder() instanceof OrientShowAppBuilder);
	}

	
	@Test
	public void gecoControlInitialization(){
		GecoControl geco = new GecoControl(mockBuilder);
		Mockito.verify(mockBuilder, Mockito.times(1)).getFactory();
		Mockito.verify(mockBuilder, Mockito.times(1)).createStageBuilder();
		Mockito.verify(mockBuilder, Mockito.times(1)).createChecker(geco);
	}

	@Test
	public void gecoInitialization(){
		Geco geco = Geco.withMock(mockBuilder);
		Mockito.verify(mockBuilder, Mockito.times(1)).buildControls(Matchers.any(GecoControl.class));
	}

	@Test
	public void gecoWindowInitialization(){
		IGecoApp mockGeco = Mockito.mock(IGecoApp.class);
		Mockito.when(mockGeco.announcer()).thenReturn(new Announcer());
//		Mockito.when(mockGeco.stage()).thenReturn(new StageImpl());
		GecoWindow window = new GecoWindow(mockGeco, mockBuilder);
		Mockito.verify(mockBuilder, Mockito.times(1)).buildUITabs(Matchers.eq(mockGeco), window);
	}

}
