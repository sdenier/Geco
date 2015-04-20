/**
 * Copyright (c) 2013 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.geco.basics.GecoResources;
import net.geco.control.context.ContextList;
import net.geco.control.context.CourseContext;
import net.geco.control.context.GenericContext;
import net.geco.control.context.RunnerContext;
import net.geco.control.context.StageContext;
import net.geco.model.Course;
import net.geco.model.Messages;
import net.geco.model.RunnerRaceData;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

/**
 * @author Simon Denier
 * @since Aug 19, 2013
 *
 */
public class StartlistExporter extends Control {

	public StartlistExporter(GecoControl gecoControl) {
		super(StartlistExporter.class, gecoControl);
	}

	public void exportTo(String filename, String fileFormat) throws IOException {
		Template template = getTemplate(fileFormat);
		Writer writer = GecoResources.getSafeWriterFor(filename + "." + fileFormat); //$NON-NLS-1$
		exportStartlists(template, writer);
		writer.close();
	}

	protected Template getTemplate(String fileFormat) throws IOException {
		String templateName = "/resources/formats/startlists." + fileFormat + ".mustache"; //$NON-NLS-1$ //$NON-NLS-2$
		Reader templateReader = GecoResources.getResourceReader(templateName);
		Template template = Mustache.compiler().defaultValue("N/A").compile(templateReader); //$NON-NLS-1$
		templateReader.close();
		return template;
	}

	protected void exportStartlists(Template template, Writer out) {
		template.execute(buildDataContext(), out);
	}
	
	protected GenericContext buildDataContext() {
		StageContext stageCtx = new StageContext(stage().getName());
		ContextList startlistsCtx = stageCtx.createContextList("geco_StartlistsCollection"); //$NON-NLS-1$
		for(String courseName: registry().getSortedCourseNames()) {
			Course course = registry().findCourse(courseName);
			List<RunnerRaceData> runners = registry().getRunnerDataFromCourse(course);
			Collections.sort(runners, new Comparator<RunnerRaceData>() {
				public int compare(RunnerRaceData o1, RunnerRaceData o2) {
					return (int) (o1.getOfficialStarttime().getTime() - o2.getOfficialStarttime().getTime());
				}
			});

			if( ! runners.isEmpty() ) {
				CourseContext courseCtx = new CourseContext(course, runners.size());
				startlistsCtx.add(courseCtx);
				ContextList runnersCtx = courseCtx.createContextList("geco_Runners"); //$NON-NLS-1$
				for (RunnerRaceData runner : runners) {
					runnersCtx.add(RunnerContext.createRegisteredRunner(runner));
				}
			}
		}
		mergeI18nProperties(stageCtx);
		return stageCtx;
	}

	protected void mergeI18nProperties(GenericContext stageCtx) {
		stageCtx.put("i18n_NameHeader", Messages.getString("ResultBuilder.NameHeader")); //$NON-NLS-1$ //$NON-NLS-2$
		stageCtx.put("i18n_ClubHeader", Messages.getString("ResultBuilder.ClubHeader")); //$NON-NLS-1$ //$NON-NLS-2$
		stageCtx.put("i18n_CategoryHeader", Messages.getString("ResultBuilder.CategoryHeader")); //$NON-NLS-1$ //$NON-NLS-2$
		stageCtx.put("i18n_LastUpdateLabel", Messages.getString("ResultExporter.LastUpdateLabel")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	
	
}
