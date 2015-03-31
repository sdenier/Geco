/**
 * Copyright (c) 2015 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.golive;

import static spark.Spark.get;
import static spark.SparkBase.stop;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import net.geco.control.results.JsonExporter;
import net.geco.framework.IGecoApp;
import net.geco.model.Pool;
import net.geco.model.Result;
import net.geco.model.ResultType;
import net.geco.model.iojson.JSONSerializer;
import net.geco.model.iojson.JacksonSerializer;



/**
 * @author Simon Denier
 * @since 26 mars 2015
 *
 */
public class SparkServer {

	public SparkServer(IGecoApp geco) {
		get("/json/lastresults", (request, response) -> {
			Pool[] pools = geco.registry().getCourses().toArray(new Pool[0]);
			List<Result> results = geco.resultBuilder().buildResults(pools, ResultType.CourseResult);;
			Writer writer = new StringWriter();
			JSONSerializer json = new JacksonSerializer(writer , true);
			new JsonExporter(geco.stage()).exportResultsToJson(results, json );
			response.type("application/json");
			return writer.toString();
		});
	}
	
	public void shutdown() {
		stop();
	}
	
}
