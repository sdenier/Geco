/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.geco.basics.Util;
import net.geco.control.GecoControl;
import net.geco.control.RunnerControl;
import net.geco.control.StageControl;
import net.geco.control.StartlistImporter;
import net.geco.model.Registry;
import net.geco.model.Runner;
import net.geco.model.Stage;
import net.geco.model.impl.POFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Simon Denier
 * @since Mar 15, 2012
 *
 */
public class StartlistImporterTest {

	@Mock private Stage stage;
	@Mock private Registry registry;
	@Mock private GecoControl geco;
	@Mock private RunnerControl runnerControl;
	@Mock private StageControl stageControl;
	
	private StartlistImporter startlistImporter;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(stage.registry()).thenReturn(registry);
		when(geco.stage()).thenReturn(stage);
		when(geco.getService(RunnerControl.class)).thenReturn(runnerControl);
		when(geco.getService(StageControl.class)).thenReturn(stageControl);
		
		startlistImporter = new StartlistImporter(geco);
	}
	
	@Test
	public void importRunnerRecordWithoutStartId() {
		Runner runner = new POFactory().createRunner();
		runner.setStartId(Integer.valueOf(100));
		when(runnerControl.buildBasicRunner("1061511")).thenReturn(runner);
		String[] record = Util.splitAndTrim(";1061511;10869;DENIER;Simon;80;H;;;00:46:00;;;;5906;5906NO;VALMO;France;11;H21A;H21A", ";");
		startlistImporter.importRunnerRecord(record);
		verify(runnerControl).registerNewRunner(runner);
		assertEquals("Start id should be the one given at creation by registry",
					 Integer.valueOf(100), runner.getStartId());
	}
	
}
