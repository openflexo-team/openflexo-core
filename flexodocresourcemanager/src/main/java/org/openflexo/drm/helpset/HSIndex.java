/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexodocresourcemanager, a component of the software infrastructure 
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

package org.openflexo.drm.helpset;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Logger;

import org.jdom2.DocType;
import org.openflexo.drm.DocItem;
import org.openflexo.drm.DocResourceCenter;
import org.openflexo.foundation.KVCFlexoObject;
import org.openflexo.localization.Language;

public class HSIndex extends KVCFlexoObject {

	private static final Logger logger = Logger.getLogger(HSIndex.class.getPackage().getName());

	private final DocResourceCenter _drc;
	private final Language _language;
	private Vector<HSIndexEntry> _rootEntries;
	public String version = "1.0";
	private final File _indexFile;

	private final HelpSetConfiguration configuration;

	public HSIndex(DocResourceCenter drc, Language language, File indexFile, HelpSetConfiguration config) {
		_drc = drc;
		_language = language;
		_indexFile = indexFile;
		configuration = config;
		getRootEntries();
	}

	public Vector<HSIndexEntry> getRootEntries() {
		if (_rootEntries == null) {
			_rootEntries = new Vector<HSIndexEntry>();
			for (DocItem next : _drc.getAllItems()) {
				if (next.isIncluded(configuration)) {
					if (next.getInheritanceParentItem() == null && !next.getIsEmbedded() && next.isPublished()) {
						logger.fine("Generate index entry for " + next);
						_rootEntries.add(new HSIndexEntry(next));
					}
				} else {
					logger.fine("Ignoring " + next);
				}
			}
		}
		return _rootEntries;
	}

	public class HSIndexEntry extends KVCFlexoObject {
		public DocItem docItem;
		public Vector entryChilds;
		public String text;

		public HSIndexEntry(DocItem docItem) {
			this.docItem = docItem;
			text = docItem.getTitle(_language);
			if (text == null) {
				text = docItem.getIdentifier();
			}

			entryChilds = new Vector();
			for (Enumeration en = docItem.getInheritanceChildItems().elements(); en.hasMoreElements();) {
				DocItem next = (DocItem) en.nextElement();
				if (next.isPublished()) {
					entryChilds.add(new HSIndexEntry(next));
				}
			}

		}
	}

	protected void generate() {
		/*	try {
				FileOutputStream out = new FileOutputStream(_indexFile);
				XMLCoder.encodeObjectWithMapping(this, getIndexMapping(), out, getIndexDocType());
				out.flush();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}*/
	}

	/*private static XMLMapping _indexMapping;

	public static XMLMapping getIndexMapping() {
		if (_indexMapping == null) {
			File hsIndexModelFile;
			hsIndexModelFile = new FileResource("Models/HSIndexModel.xml");
			if (!hsIndexModelFile.exists()) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.warning("File " + hsIndexModelFile.getAbsolutePath() + " doesn't exist. Maybe you have to check your paths ?");
				}
				return null;
			} else {
				try {
					_indexMapping = new XMLMapping(hsIndexModelFile);
				} catch (InvalidModelException e) {
					// Warns about the exception
					if (logger.isLoggable(Level.WARNING)) {
						logger.warning("Exception raised: " + e.getClass().getName() + ". See console for details.");
					}
					e.printStackTrace();
				} catch (IOException e) {
					// Warns about the exception
					if (logger.isLoggable(Level.WARNING)) {
						logger.warning("Exception raised: " + e.getClass().getName() + ". See console for details.");
					}
					e.printStackTrace();
				} catch (SAXException e) {
					// Warns about the exception
					if (logger.isLoggable(Level.WARNING)) {
						logger.warning("Exception raised: " + e.getClass().getName() + ". See console for details.");
					}
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// Warns about the exception
					if (logger.isLoggable(Level.WARNING)) {
						logger.warning("Exception raised: " + e.getClass().getName() + ". See console for details.");
					}
					e.printStackTrace();
				}
			}
		}
		return _indexMapping;
	}*/

	public static DocType getIndexDocType() {
		return new DocType("index", "-//Sun Microsystems Inc.//DTD JavaHelp Index Version 1.0//EN",
				"http://java.sun.com/products/javahelp/index_1_0.dtd");
	}

}
