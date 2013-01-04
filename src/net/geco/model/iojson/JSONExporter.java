/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.model.iojson;

import java.util.Date;






/**
 * @author Simon Denier
 * @since Jan 4, 2013
 *
 */
public interface JSONExporter {

	public JSONExporter key(String key);

	public JSONExporter value(String value);

	public JSONExporter value(Date value);
	
	public JSONExporter value(long value);
	
	public JSONExporter value(double value);
	
	public JSONExporter value(boolean value);

	public JSONExporter startObject();

	public JSONExporter startObjectField(String key);

	public JSONExporter endObject();

	public JSONExporter startArray();

	public JSONExporter startArrayField(String key);

	public JSONExporter endArray();

	public JSONExporter field(String key, String value);

	public JSONExporter field(String key, Date date);

	public JSONExporter field(String key, long value);

	public JSONExporter field(String key, double value);

	public JSONExporter field(String key, boolean value);

	public JSONExporter optField(String key, Object nullableValue);

	public JSONExporter optField(String key, boolean flag);

	public JSONExporter id(String key, Object object);

	public JSONExporter ref(String key, Object object);

	public JSONExporter optRef(String key, Object nullableObject);

	public JSONExporter idMax(String key);

}
