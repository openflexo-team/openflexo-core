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

package org.openflexo.drm;

import java.io.File;
import java.util.Vector;
import java.util.logging.Logger;

public class DocSubmissionReport extends DRMObject {

	private static final Logger logger = Logger.getLogger(DocSubmissionReport.class.getPackage().getName());

	private Vector<DocItemAction> _submissionActions;

	public DocSubmissionReport() {
		super();
		_submissionActions = new Vector<DocItemAction>();
	}

	public Vector getSubmissionActions() {
		return _submissionActions;
	}

	public void setSubmissionActions(Vector<DocItemAction> submissionActions) {
		_submissionActions = submissionActions;
	}

	public void addToSubmissionActions(DocItemAction action) {
		_submissionActions.add(action);
	}

	public void removeFromSubmissionActions(DocItemAction action) {
		_submissionActions.remove(action);
	}

	public int size() {
		return _submissionActions.size();
	}

	/*private static XMLMapping _dsrMapping;

	public static XMLMapping getDSRMapping() {
		if (_dsrMapping == null) {
			StringEncoder.getDefaultInstance()._addConverter(DocItemVersion.Version.converter);
			File dsrModelFile;
			dsrModelFile = new FileResource("Models/DSRModel.xml");
			if (!dsrModelFile.exists()) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.warning("File " + dsrModelFile.getAbsolutePath() + " doesn't exist. Maybe you have to check your paths ?");
				}
				return null;
			} else {
				try {
					_dsrMapping = new XMLMapping(dsrModelFile);
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
		return _dsrMapping;
	}
	*/
	public static DocSubmissionReport load(DocResourceCenter drc, File docSubmissionReportFile) {
		// TODO: reimplement this
		/*DocSubmissionReport report = null;
		StringEncoder.getDefaultInstance()._addConverter(ActionType.actionTypeConverter);
		try {
			FileInputStream in = new FileInputStream(docSubmissionReportFile);
			report = (DocSubmissionReport) XMLDecoder.decodeObjectWithMapping(in, getDSRMapping(), new DRMBuilder(drc));
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return report;*/
		return null;
	}

	public boolean save(File docSubmissionReportFile) {
		// TODO: reimplement this
		/*FileOutputStream out = null;
		try {
			out = new FileOutputStream(docSubmissionReportFile);
			XMLCoder.encodeObjectWithMapping(this, getDSRMapping(), out);
			out.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}*/
		return false;
	}

	public void clear() {
		getSubmissionActions().clear();
	}

	/**
	 * Overrides getIdentifier
	 * 
	 * @see org.openflexo.drm.DRMObject#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return "DocSubmissionReport";
	}
}
