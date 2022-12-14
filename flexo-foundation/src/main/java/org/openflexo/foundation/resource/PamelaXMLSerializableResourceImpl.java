/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Parent;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.openflexo.foundation.PamelaResourceModelFactory;
import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.toolbox.FlexoVersion;

/**
 * Default implementation for {@link PamelaResource} (a resource where underlying model is managed by PAMELA framework)
 * 
 * @param <RD>
 *            the type of the resource data reference by this resource
 * @author Sylvain
 * 
 */
public abstract class PamelaXMLSerializableResourceImpl<RD extends ResourceData<RD> & AccessibleProxyObject, F extends PamelaModelFactory & PamelaResourceModelFactory>
		extends PamelaResourceImpl<RD, F> implements PamelaXMLSerializableResource<RD, F> {

	private static final Logger logger = Logger.getLogger(PamelaXMLSerializableResourceImpl.class.getPackage().getName());

	@Override
	protected RD performLoad() throws IOException, Exception {
		// Retrieve the data from an input stream given by the FlexoIOStream
		// delegate of the resource
		InputStream inputStream = getFlexoIOStreamDelegate().getInputStream();
		try {
			return (RD) getFactory().deserialize(inputStream);
		} finally {
			inputStream.close();
		}
	}

	@Override
	protected void performSave(boolean clearIsModified) throws SaveResourceException {
		File temporaryFile = null;
		FileWritingLock lock = getFlexoIOStreamDelegate().willWriteOnDisk();

		if (getFlexoIOStreamDelegate() != null && getFlexoIOStreamDelegate().getSaveToSourceResource()
				&& getFlexoIOStreamDelegate().getSourceResource() != null) {
			logger.info("Saving SOURCE resource " + this + " : " + getFlexoIOStreamDelegate().getSourceResource().getFile() + " version="
					+ getModelVersion());
		}
		else {
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Saving resource " + this + " : " + getFile() + " version=" + getModelVersion());
			}
		}
		try {
			/*
			 * File dir = getFile().getParentFile(); willWrite(dir); if
			 * (!dir.exists()) { dir.mkdirs(); } willWrite(getFile());
			 */
			// Make local copy
			makeLocalCopy();
			// Using temporary file

			temporaryFile = File.createTempFile("temp", ".xml", getFile().getParentFile());
			if (logger.isLoggable(Level.FINE)) {
				logger.finer("Creating temp file " + temporaryFile.getAbsolutePath());
			}
			performXMLSerialization(/* handler, */temporaryFile);
			if (logger.isLoggable(Level.FINE)) {
				logger.finer("Renaming temp file " + temporaryFile.getAbsolutePath() + " to " + getFile().getAbsolutePath());
			}
			// Renaming temporary file is done in post serialization
			postXMLSerialization(temporaryFile, lock, clearIsModified);
		} catch (IOException e) {
			e.printStackTrace();
			if (temporaryFile != null) {
				temporaryFile.delete();
			}
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Failed to save resource " + this + " with model version " + getModelVersion());
			}
			getFlexoIOStreamDelegate().hasWrittenOnDisk(lock);
			throw new SaveResourceException(getIODelegate(), e);
		} /*
			* finally { hasWritten(getFile());
			* hasWritten(getFile().getParentFile()); }
			*/
	}

	/**
	 * @param version
	 * @param temporaryFile
	 * @param lock
	 * @param clearIsModified
	 * @throws IOException
	 */
	private void postXMLSerialization(File temporaryFile, FileWritingLock lock, boolean clearIsModified) throws IOException {
		if (getFlexoIOStreamDelegate() != null && getFlexoIOStreamDelegate().getSaveToSourceResource()
				&& getFlexoIOStreamDelegate().getSourceResource() != null) {
			FileUtils.rename(temporaryFile, getFlexoIOStreamDelegate().getSourceResource().getFile());
		}
		else {
			FileUtils.rename(temporaryFile, getFile());
		}
		getFlexoIOStreamDelegate().hasWrittenOnDisk(lock);
		if (clearIsModified) {
			notifyResourceStatusChanged();
		}
	}

	/**
	 * 
	 * @param temporaryFile
	 * @throws IOException
	 */
	private void performXMLSerialization(File temporaryFile) throws IOException {
		try (FileOutputStream out = new FileOutputStream(temporaryFile)) {
			getFactory().serialize(resourceData, out);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	/**
	 * Manually converts resource file from version v1 to version v2. This methods only warns and does nothing, and must be overriden in
	 * subclasses !
	 * 
	 * @param v1
	 * @param v2
	 * @return boolean indicating if conversion was sucessfull
	 */
	protected boolean convertResourceFileFromVersionToVersion(FlexoVersion v1, FlexoVersion v2) {
		if (logger.isLoggable(Level.WARNING)) {
			logger.warning("Unable to find converter for resource " + this + " from version " + v1 + " to version " + v2);
		}
		return false;
	}

	/**
	 * Read an XML input stream from File and return the parsed Document
	 */
	public static Document readXMLFile(File f) throws JDOMException, IOException {
		try (FileInputStream fio = new FileInputStream(f)) {
			return readXMLInputStream(fio);
		}
	}

	/**
	 * Read an XML input stream and return the parsed Document
	 */
	public static Document readXMLInputStream(InputStream inputStream) throws JDOMException, IOException {
		SAXBuilder parser = new SAXBuilder();
		Document reply = parser.build(inputStream);
		return reply;
	}

	public static Element getElement(Parent parent, String name) {
		Iterator<Element> it = parent.getDescendants(new ElementFilter(name));
		if (it.hasNext()) {
			return it.next();
		}
		return null;
	}

}
