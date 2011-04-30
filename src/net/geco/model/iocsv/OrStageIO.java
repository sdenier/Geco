/**
 * Copyright (c) 2009 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.iocsv;

import java.io.IOException;

import net.geco.model.Factory;
import net.geco.model.Registry;
import net.geco.model.Stage;


/**
 * Hacking the AbstractImporter to import Or stage data
 * 
 * @author Simon Denier
 * @since Feb 9, 2009
 */
public class OrStageIO extends AbstractIO<Stage> {

	public static String sourceFilename() {
		return "Competition.csv"; //$NON-NLS-1$
	}
	
	private Stage stage;
	
	/**
	 * @param factory
	 * @param reader
	 * @param registry
	 */
	public OrStageIO(Factory factory, CsvReader reader) {
		super(factory, reader, null, null);
		if( this.reader!=null )
			this.reader.setCsvSep(";"); //$NON-NLS-1$
		if( this.writer!=null )
			this.writer.setCsvSep(";"); //$NON-NLS-1$
	}

	@Override
	public Stage importTData(String[] record) {
		// 94;"LL Mullagmmeen 12 Oct 2008";-1;1;36000000;true;true;true;true;"";"";
		// "F:\";"Mullaghmeen Results (12 Oct 08)";"F:\";"Mullaghmeen Splits";"[None]";"";"";"";"";
		// "12/10/2008";2;COM1;150;17;false;false;;0
		// Competition ID,Name,Type,Result Type,default start time,true,true,true,true,string,string,
		// result dir,result file,split dir,split file,SI port,csv dir,csv file,string,string,
		// subheading,spool split,spool port,Map DPI,GPS offset time,auto-enter,send server, server address, time format
		Stage stage = factory.createStage();
		stage.setName(record[1].replace("\"", "")); //$NON-NLS-1$ //$NON-NLS-2$
//		stage.setDate(record[]);
//		stage.setDefaultStartTime(record[]);
		this.stage = stage;
		return stage;
	}
	
	public Stage getStage() throws IOException {
		if( this.stage==null ) {
			importData();
		}
		return this.stage;
	}

	@Override
	public void register(Stage data, Registry registry) {
		// do nothing
	}

	@Override
	public String[] exportTData(Stage t) {
		return null;
	}
}
