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
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.IOFlexoException;
import org.openflexo.foundation.InconsistentDataException;
import org.openflexo.foundation.InvalidModelDefinitionException;
import org.openflexo.foundation.InvalidXMLException;
import org.openflexo.toolbox.FileUtils;

/**
 * Default implementation for a resource based on {@link Properties}
 * 
 * @author Sylvain
 * 
 */
public abstract class PropertiesResourceImpl<RD extends ResourceData<RD>> extends FlexoResourceImpl<RD> implements FlexoResource<RD> {

	private static final Logger logger = Logger.getLogger(PropertiesResourceImpl.class.getPackage().getName());

	/**
	 * Save the &quot;real&quot; resource data of this resource.
	 * 
	 * @throws SaveResourceException
	 */
	@Override
	public final void save() throws SaveResourceException {
		// progress.setProgress(getLocales().localizedForKey("saving") + " " + this.getName());
		if (!isLoaded()) {
			return;
		}
		if (!isDeleted()) {
			saveResourceData(true);
			resourceData.clearIsModified(false);
		}

	}

	/**
	 * Load the resource data of this resource.
	 * 
	 */
	@Override
	public RD loadResourceData() throws FlexoFileNotFoundException, IOFlexoException, InvalidXMLException, InconsistentDataException,
			InvalidModelDefinitionException {
		if (resourceData != null) {
			// already loaded
			return resourceData;
		}

		notifyResourceWillLoad();

		try {
			Properties properties = new Properties();
			properties.load(getInputStream());

			performLoad(properties);
			resourceData.setResource(this);
			resourceData.clearIsModified();
			return resourceData;

		} catch (IOException e) {
			e.printStackTrace();
			throw new IOFlexoException(e);
		} finally {
			notifyResourceLoaded();

		}

	}

	/**
	 * Should be implemented in sub-classes: do the effective loading from properties object
	 * 
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	protected abstract RD performLoad(Properties properties);

	// This should be removed from Pamela Resource class
	protected File getFile() {
		return (File) getIODelegate().getSerializationArtefact();
	}

	protected final void saveResourceData(boolean clearIsModified) throws SaveResourceException, SaveResourcePermissionDeniedException {
		// System.out.println("PamelaResourceImpl Saving " + getFile());
		if (!getIODelegate().hasWritePermission()) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning("Permission denied : " + getIODelegate().toString());
			}
			throw new SaveResourcePermissionDeniedException(getIODelegate());
		}
		if (resourceData != null) {
			performSave(clearIsModified);
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Succeeding to save Resource " + this + " : " + getFile().getName() + " with date "
						+ FileUtils.getDiskLastModifiedDate(getFile()));
			}
		}
		if (clearIsModified) {
			try {
				getResourceData().clearIsModified(false);
				// No need to reset the last memory update since it is valid
				notifyResourceSaved();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Should be implemented in sub-classes: do the effective saving
	 * 
	 * @param clearIsModified
	 * @throws SaveResourceException
	 */
	protected abstract void performSave(boolean clearIsModified) throws SaveResourceException;

}
