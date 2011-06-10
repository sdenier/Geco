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
import javax.swing.JPanel;

import junit.framework.Assert;
import net.geco.Geco;
import net.geco.GecoAppLauncher;
import net.geco.app.AppBuilder;
import net.geco.app.OrientShowAppBuilder;
import net.geco.basics.Announcer;
import net.geco.control.GecoControl;
import net.geco.control.SIReaderHandler;
import net.geco.control.SingleSplitPrinter;
import net.geco.control.StageBuilder;
import net.geco.framework.IGecoApp;
import net.geco.model.Stage;
import net.geco.ui.GecoWindow;
import net.geco.ui.framework.ConfigPanel;
import net.geco.ui.framework.TabPanel;
import net.geco.ui.tabs.StagePanel;

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
		SingleSplitPrinter splitPrinter = mock(SingleSplitPrinter.class);
		when(mockGeco.announcer()).thenReturn(ann);
		when(mockGeco.stage()).thenReturn(stage);
		when(mockGeco.siHandler()).thenReturn(si);
		when(mockGeco.splitPrinter()).thenReturn(splitPrinter);
		return mockGeco;
	}
	
	@Test
	public void gecoLauncherLoadProperties() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		GecoAppLauncher.loadStageProperties("testData/valid");
		Assert.assertEquals("testData/valid", GecoAppLauncher.getStageDir());
		Assert.assertTrue(GecoAppLauncher.getAppBuilder() instanceof OrientShowAppBuilder);
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
		when(mockBuilder.buildUITabs(eq(mockGeco), any(JFrame.class))).thenReturn(new TabPanel[0]);
		when(mockBuilder.buildConfigPanels(eq(mockGeco), any(JFrame.class))).thenReturn(new ConfigPanel[0]);
		GecoWindow window = new GecoWindow(mockGeco);
		window.initGUI(mockBuilder);
		verify(mockBuilder, times(1)).buildUITabs(eq(mockGeco), eq(window));
		verify(mockBuilder, times(1)).buildConfigPanels(eq(mockGeco), eq(window));
	}

	// Disable mock test with Swing interactions
//	@Test
//	public void gecoUITabsInitialization(){
////		Mockito.doNothing().when(mockStagePanel).checkGD(any(String.class));
//		TabPanel mockTab = mock(TabPanel.class);
//		GecoWindow window = new GecoWindow(mockGeco);
//		StagePanel stagePanel = new StagePanel(mockGeco, window);
//		window.buildGUI(stagePanel, new TabPanel[] { mockTab }, new ConfigPanel[0]);
//		verify(mockTab, times(1)).getTabTitle();
//		Assert.assertEquals(mockTab, window.getComponent(0));
//	}

	@Test
	public void stagePanelSetupWithTabs(){
		ConfigPanel mockConfig = mock(ConfigPanel.class);
		JPanel mockPanel = mock(JPanel.class);
		when(mockConfig.build()).thenReturn(mockPanel);
		when(mockConfig.getLabel()).thenReturn("mock config");
		StagePanel stagePanel = new StagePanel(mockGeco, mock(JFrame.class));
		stagePanel.buildConfigPanels(new ConfigPanel[] { mockConfig });
		verify(mockConfig, times(1)).getLabel();
		JPanel c = stagePanel.getConfigFor("mock config");
		Assert.assertEquals(mockPanel, c.getComponent(0));
	}
	
}
