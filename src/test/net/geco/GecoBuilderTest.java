/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco;

import javax.swing.JFrame;

import junit.framework.Assert;
import net.geco.Geco;
import net.geco.GecoLauncher;
import net.geco.app.AppBuilder;
import net.geco.app.OrientShowAppBuilder;
import net.geco.basics.Announcer;
import net.geco.control.GecoControl;
import net.geco.framework.IGecoApp;
import net.geco.ui.GecoWindow;
import net.geco.ui.framework.ConfigPanel;
import net.geco.ui.framework.TabPanel;
import net.geco.ui.tabs.StagePanel;

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
	private IGecoApp mockGeco;

	@Before
	public void setUp(){
		mockBuilder = Mockito.mock(AppBuilder.class);
		mockGeco = Mockito.mock(IGecoApp.class);
		Mockito.when(mockGeco.announcer()).thenReturn(new Announcer());
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
		Geco geco = new Geco("testData/valid", mockBuilder);
		Mockito.verify(mockBuilder, Mockito.times(1)).buildControls(Matchers.any(GecoControl.class));
	}

	@Test
	public void gecoWindowInitialization(){
		GecoWindow window = new GecoWindow(mockGeco, mockBuilder);
		Mockito.verify(mockBuilder, Mockito.times(1)).buildUITabs(Matchers.eq(mockGeco), window);
		Mockito.verify(mockBuilder, Mockito.times(1)).buildConfigPanels(Matchers.eq(mockGeco), window);
	}

	@Test
	public void gecoUiTabsInitialization(){
		StagePanel stagePanel = new StagePanel(mockGeco, Mockito.mock(JFrame.class));
		TabPanel mockTab = Mockito.mock(TabPanel.class);
		GecoWindow window = new GecoWindow(Mockito.mock(IGecoApp.class), mockBuilder);
		window.guiInit(stagePanel, new TabPanel[] { mockTab }, new ConfigPanel[0]);
		Mockito.verify(mockTab, Mockito.times(1)).getTabTitle();
		Assert.assertEquals(mockTab, window.getComponent(0));
	}

	@Test
	public void stagePanelSetupWithTabs(){
		StagePanel stagePanel = new StagePanel(mockGeco, Mockito.mock(JFrame.class));
		ConfigPanel mockConfig = Mockito.mock(ConfigPanel.class);
		stagePanel.addConfigPanels(new ConfigPanel[] { mockConfig });
		Mockito.verify(mockConfig, Mockito.times(1)).getLabel();
	}
	
}
