/**
 * Copyright (c) 2012 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package test.net.geco.control.functions;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import net.geco.basics.CsvReader;
import net.geco.basics.TimeManager;
import net.geco.basics.Util;
import net.geco.control.ecardmodes.ECardMode;
import net.geco.control.functions.ECardLogImporter;
import net.gecosi.SiDataFrame;
import net.gecosi.SiPunch;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author Simon Denier
 * @since Jun 12, 2012
 *
 */
public class ECardLogImporterTest {

	private static final String DATA = "69;11/04/2012 00:32:15;1061511;0;;Simon;DENIER;;;;;;;;;;" +
	"17;Su;11:42:11;17;Su;11:42:12;1;Su;11:44:42;10;Su;12:02:43;15;" +
	"160;Su;11:45:46;161;Su;11:47:18;162;Su;11:49:44;163;Su;11:50:32;" +
	"164;Su;11:51:13;165;Su;11:52:17;166;Su;11:53:31;167;Su;11:54:55;" +
	"168;Su;11:55:50;169;Su;11:56:24;176;Su;11:56:55;173;Su;11:59:34;" +
	"170;Su;12:01:34;171;Su;12:01:58;172;Su;12:02:27;" +
	";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;";

	private static final String[] SAMPLE_RECORD = Util.splitAndTrim(DATA, ";");


	protected ECardLogImporter subject() {
		return new ECardLogImporter(null);
	}
	
	@Test
	public void convertRecord_shouldCreateResultData() {
		SiDataFrame resultData = subject().convertRecord(SAMPLE_RECORD);
		
		checkResultData(resultData);
	}

	private void checkResultData(SiDataFrame resultData) {
		assertEquals("1061511", resultData.getSiNumber());
		assertEquals(time("11:42:12"), resultData.getCheckTime());
		assertEquals(time("11:44:42"), resultData.getStartTime());
		assertEquals(time("12:02:43"), resultData.getFinishTime());
		
		SiPunch[] punches = resultData.getPunches();
		assertEquals(15, punches.length);
		SiPunch firstPunch = punches[0];
		assertEquals(160, firstPunch.code());
		assertEquals(time("11:45:46"), firstPunch.timestamp());
		SiPunch lastPunch = punches[punches.length - 1];
		assertEquals(172, lastPunch.code());
		assertEquals(time("12:02:27"), lastPunch.timestamp());
	}

	private long time(String time) {
		return TimeManager.safeParse(time).getTime();
	}

	
	@Test
	public void processECardData_shouldCallECardMode() {
		try {
			ECardMode readingMode = mock(ECardMode.class);
			CsvReader csvReader = mock(CsvReader.class);
			when(csvReader.readRecord()).thenReturn(new String[]{"Header1;Header2"}, SAMPLE_RECORD, null);
			ArgumentCaptor<SiDataFrame> data = ArgumentCaptor.forClass(SiDataFrame.class);
			
			subject().processECardData(readingMode, csvReader);

			verify(readingMode).processECard(data.capture());
			checkResultData(data.getValue());
		} catch (IOException e) {
			fail();
		}
	}

}
