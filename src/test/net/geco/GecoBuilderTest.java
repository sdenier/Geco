/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.swing.JFrame;

import junit.framework.Assert;
import net.geco.Geco;
import net.geco.app.AppBuilder;
import net.geco.app.GecoStageLaunch;
import net.geco.app.OrientShowAppBuilder;
import net.geco.basics.Announcer;
import net.geco.control.GecoControl;
import net.geco.control.SIReaderHandler;
import net.geco.control.StageBuilder;
import net.geco.control.results.RunnerSplitPrinter;
import net.geco.framework.IGecoApp;
import net.geco.model.Stage;
import net.geco.ui.GecoWindow;
import net.geco.ui.framework.ConfigPanel;
import net.geco.ui.framework.TabPanel;
import net.geco.ui.framework.UIAnnouncers;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Simon Denier
 * @since May 1, 2011
 *
 */
public class GecoBuilderTest {
	
	private AppBuilder mockBuilder;
	private IGecoApp mockGeco;

	@Before
	public void setUp(){
		mockBuilder = mock(OrientShowAppBuilder.class);
		mockGeco = mockGeco();
	}
	
	public IGecoApp mockGeco(){
		mockGeco = mock(IGecoApp.class);
		Announcer ann = mock(Announcer.class);
		Stage stage = mock(Stage.class);
		SIReaderHandler si = mock(SIReaderHandler.class);
		RunnerSplitPrinter splitPrinter = mock(RunnerSplitPrinter.class);
		when(mockGeco.announcer()).thenReturn(ann);
		when(mockGeco.stage()).thenReturn(stage);
		when(mockGeco.siHandler()).thenReturn(si);
		when(mockGeco.splitPrinter()).thenReturn(splitPrinter);
		return mockGeco;
	}
	
	@Test
	public void gecoLauncherLoadProperties() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		GecoStageLaunch stageLaunch = new GecoStageLaunch();
		stageLaunch.loadFromFileSystem("testData/valid");
		Assert.assertEquals("testData/valid", stageLaunch.getStageDir());
		Assert.assertTrue(stageLaunch.getAppBuilder() instanceof OrientShowAppBuilder);
	}

	
	@Test
	public void gecoControlInitialization(){
		GecoControl geco = new GecoControl(mockBuilder);
		verify(mockBuilder, times(1)).getFactory();
		verify(mockBuilder, times(1)).createStageBuilder();
		verify(mockBuilder, times(1)).createChecker(geco);
	}

	@Test
	public void gecoInitialization(){
		StageBuilder mockStageBuilder = mock(StageBuilder.class);
		when(mockBuilder.createStageBuilder()).thenReturn(mockStageBuilder);
		Geco geco = new Geco();
		geco.initControls(mockBuilder, mock(GecoControl.class));
		verify(mockBuilder, times(1)).buildControls(any(GecoControl.class));
	}

	@Test
	public void gecoWindowInitialization(){
		when(mockBuilder.buildUITabs(eq(mockGeco), any(JFrame.class), any(UIAnnouncers.class))).thenReturn(new TabPanel[0]);
		when(mockBuilder.buildConfigPanels(eq(mockGeco), any(JFrame.class))).thenReturn(new ConfigPanel[0]);
		GecoWindow window = new GecoWindow(mockGeco);
		window.initGUI(mockBuilder);
		verify(mockBuilder, times(1)).buildUITabs(eq(mockGeco), eq(window), eq(window));
		verify(mockBuilder, times(1)).buildConfigPanels(eq(mockGeco), eq(window));
	}

}
