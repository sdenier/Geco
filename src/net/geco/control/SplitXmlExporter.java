/**
 * Copyright (c) 2011 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package net.geco.control;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.geco.basics.TimeManager;
import net.geco.control.ResultBuilder.SplitTime;
import net.geco.model.RankedRunner;
import net.geco.model.Result;
import net.geco.model.Runner;
import net.geco.model.RunnerRaceData;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Simon Denier
 * @since Nov 11, 2011
 *
 */
public class SplitXmlExporter extends Control {

	private ResultBuilder resultBuilder;

	private Document document;

	private boolean includeSplits;

	public SplitXmlExporter(GecoControl gecoControl) {
		super(gecoControl);
		resultBuilder = getService(ResultBuilder.class);
	}

	public Document generateXMLResult(List<Result> results, String filepath, boolean withSplits)
			throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		includeSplits = withSplits;
		Element root = documentPreamble();
		
		for (Result result : results) {
			generateCategoryResult(root, result);
		}
		
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "IOFdata.dtd"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		transformer.transform(new DOMSource(document), new StreamResult(new File(filepath)));
		
		return document;
	}

	protected Element documentPreamble() {
		Element root = createChildElement(document, "ResultList"); //$NON-NLS-1$
		root.setAttribute("status", "complete"); //$NON-NLS-1$ //$NON-NLS-2$

		Element version = createChildElement(root, "IOFVersion"); //$NON-NLS-1$
		version.setAttribute("version", "2.0.3"); //$NON-NLS-1$ //$NON-NLS-2$
		return root;
	}
	
	protected void generateCategoryResult(Element root, Result result) {
		Element classResult = createChildElement(root, "ClassResult"); //$NON-NLS-1$
		createContentChildElement(classResult, "ClassShortName", result.getIdentifier()); //$NON-NLS-1$
		for (RankedRunner runner : result.getRanking()) {
			generateRunnerResult(classResult, runner.getRunnerData(), runner.getRank());
		}
		for (RunnerRaceData runnerData : result.getNRRunners()) {
			generateRunnerResult(classResult, runnerData, 0);
		}
	}
	
	protected void generateRunnerResult(Element classResult, RunnerRaceData runnerData, int rank) {
		Element personResult = createChildElement(classResult, "PersonResult"); //$NON-NLS-1$
		Runner runner = runnerData.getRunner();
		Element person = createChildElement(personResult, "Person"); //$NON-NLS-1$
		createContentChildElement(person, "PersonId",  //$NON-NLS-1$
						runner.getArchiveId()!=null ? runner.getArchiveId().toString() : ""); //$NON-NLS-1$
		Element personName = createChildElement(person, "PersonName"); //$NON-NLS-1$
		createContentChildElement(personName, "Family", runner.getLastname()); //$NON-NLS-1$
		createContentChildElement(personName, "Given", runner.getFirstname()); //$NON-NLS-1$
//		personResult > clubid?
		
		Element result = createChildElement(personResult, "Result"); //$NON-NLS-1$
		createContentChildElement(result, "StartTime", //$NON-NLS-1$
									TimeManager.time(runnerData.getOfficialStarttime())); //$NON-NLS-1$
		createContentChildElement(result, "FinishTime", //$NON-NLS-1$
									TimeManager.time(runnerData.getFinishtime())); //$NON-NLS-1$
		createContentChildElement(result, "Time", runnerData.getResult().formatRacetime()); //$NON-NLS-1$
		if( rank > 0 ) {
			createContentChildElement(result, "ResultPosition", Integer.toString(rank)); //$NON-NLS-1$
		}
//		result > coursevariationid?
		Element status = createChildElement(result, "CompetitorStatus"); //$NON-NLS-1$
		status.setAttribute("value", runnerData.getIofStatus()); //$NON-NLS-1$

		if( includeSplits ) {
			SplitTime[] splits = resultBuilder.buildNormalSplits(runnerData, null);
			for (int i = 0; i < splits.length; i++) {
				SplitTime split = splits[i];
				if( split.trace!=null && split.trace.isOK() ) {
					Element splitTime = createChildElement(result, "SplitTime"); //$NON-NLS-1$
					splitTime.setAttribute("sequence", Integer.toString(i + 1)); //$NON-NLS-1$
					createContentChildElement(splitTime, "ControlCode", split.trace.getCode()); //$NON-NLS-1$
					createContentChildElement(splitTime, "Time", TimeManager.time(split.time));	//$NON-NLS-1$
				}
			}			
		}
	}
	
	private Element createChildElement(Node parent, String tagName) {
		return (Element) parent.appendChild(document.createElement(tagName));
	}

	private Element createContentChildElement(Node parent, String tagName, String content) {
		Element element = createChildElement(parent, tagName);
		element.setTextContent(content);
		return element;
	}
	
}
