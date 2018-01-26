/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.utils;

/*
 * XMLUtils.java
 * Project WorkflowEditor
 *
 * Created by benoit on Mar 1, 2004
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xerces.parsers.DOMParser;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.DOMBuilder;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Some static util methods for xml parsing.
 * 
 * @author benoit, sylvain
 */
public class XMLUtils {

	private static final Logger logger = Logger.getLogger(XMLUtils.class.getPackage().getName());

	// ==========================================================================
	// ============================= JDOM ================================
	// ==========================================================================

	public static org.jdom2.Document getJDOMDocument(File f) throws Exception {
		Document domDoc = parseXMLFile(f);
		return convertDOMToJDOM(domDoc);
	}

	public static org.jdom2.Document getJDOMDocument(InputStream is) throws Exception {
		Document domDoc = parseXML(is);
		return convertDOMToJDOM(domDoc);
	}

	public static org.jdom2.Document convertDOMToJDOM(Document domDoc) {
		DOMBuilder jDomBuilder = new DOMBuilder();
		org.jdom2.Document jdomDoc = jDomBuilder.build(domDoc);
		return jdomDoc;
	}

	public static boolean saveXMLFile(Document document, File aFile) {
		return saveXMLFile(convertDOMToJDOM(document), aFile);
	}

	public static boolean saveXMLFile(org.jdom2.Document document, File aFile) {
		try (FileOutputStream fos = new FileOutputStream(aFile)) {
			return saveXMLFile(document, fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean saveXMLFile(org.jdom2.Document document, OutputStream os) {
		try {
			Format prettyFormat = Format.getPrettyFormat();
			prettyFormat.setLineSeparator(LineSeparator.SYSTEM);
			XMLOutputter outputter = new XMLOutputter(prettyFormat);
			outputter.output(document, os);
			return true;
		} catch (Exception e) {
			// Warns about the exception
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Exception raised: " + e.getClass().getName() + ". See console for details.");
			}
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	// ==========================================================================
	// ======================= DOM > deprecated =============================
	// ==========================================================================

	public static Element elementFromFilePath(String xmlFilePath) throws Exception {
		return parseXMLFile(xmlFilePath).getDocumentElement();
	}

	public static Element elementFromFile(File xmlFile) throws Exception {
		return parseXMLFile(xmlFile).getDocumentElement();
	}

	public static Document parseXMLFile(String xmlFilePath) throws Exception {
		File f = new File(xmlFilePath);
		try (FileInputStream fis = new FileInputStream(f)) {
			Document result = parseXML(fis);
			return result;
		}
	}

	public static Document parseXMLFile(File f) throws Exception {
		try (FileInputStream fis = new FileInputStream(f)) {
			Document result = parseXML(fis);
			return result;
		}
	}

	/**
	 * @param inputStream
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	private static Document parseXML(InputStream inputStream) throws Exception, IOException {
		InputStreamReader reader = new InputStreamReader(inputStream, Charset.forName("utf8"));
		org.xml.sax.InputSource is = new org.xml.sax.InputSource(reader);
		Document d = parseXMLFile(is);
		inputStream.close();
		return d;
	}

	private static Document parseXMLFile(org.xml.sax.InputSource is) throws Exception {
		DOMParser parser = new DOMParser();
		parser.parse(is);

		Document d = parser.getDocument();
		parser.reset();
		return d;
	}

	public static Element elementFromString(String s) throws Exception {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(s.getBytes());
				InputStreamReader reader = new InputStreamReader(bis, Charset.forName("utf8"))) {
			org.xml.sax.InputSource is = new org.xml.sax.InputSource(reader);
			// org.xml.sax.InputSource is = new org.xml.sax.InputSource(bis);
			Element e = parseXMLFile(is).getDocumentElement();
			return e;
		}
	}

	public static String replaceAllSpecialChar(String s) {
		s = replaceStringByStringInString("&", "&amp;", s);
		s = replaceStringByStringInString("\"", "&quot;", s);
		s = replaceStringByStringInString(">", "&gt;", s);
		s = replaceStringByStringInString("<", "&lt;", s);
		return s;
	}

	public static String replaceStringByStringInString(String replacedString, String aNewString, String message) {
		// return replaceString(message,replacedString,aNewString);
		// DON'T WORK FOR (" ","",message)

		if (message == null || message.equals("")) {
			return "";
		}
		if (replacedString == null || replacedString.equals("")) {
			return message;
		}
		if (aNewString == null || aNewString.equals("")) {
			aNewString = "";
		}

		String newString = "";
		int replacedStringLength = replacedString.length();
		int indexOfTag = message.indexOf(replacedString);
		while (indexOfTag != -1) {
			newString = newString + message.substring(0, indexOfTag) + aNewString;
			message = message.substring(indexOfTag + replacedStringLength);
			indexOfTag = message.indexOf(replacedString);
		}
		return newString + message;
	}

	public static org.jdom2.Document readXMLFile(File f) throws JDOMException, IOException {
		try (FileInputStream fio = new FileInputStream(f)) {
			SAXBuilder parser = new SAXBuilder();
			org.jdom2.Document reply = parser.build(fio);
			return reply;
		}
	}

	public static org.jdom2.Element getElement(org.jdom2.Parent parent, String name) {
		Iterator<org.jdom2.Element> it = parent.getDescendants(new ElementFilter(name));
		if (it.hasNext()) {
			return it.next();
		}
		return null;
	}
}
