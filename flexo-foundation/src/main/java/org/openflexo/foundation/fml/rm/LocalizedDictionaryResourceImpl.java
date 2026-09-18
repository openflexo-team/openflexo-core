/**
 *
 * Copyright (c) 2014-2026, Openflexo
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

package org.openflexo.foundation.fml.rm;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLLocalizedDelegate;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.resource.FileWritingLock;
import org.openflexo.foundation.resource.FlexoResourceImpl;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.resource.StreamIODelegate;
import org.openflexo.localization.LocalizedDelegate;

/**
 * Default implementation of {@link LocalizedDictionaryResource}.
 *
 * @author sylvain
 */
public abstract class LocalizedDictionaryResourceImpl extends FlexoResourceImpl<FMLLocalizedDictionary>
		implements LocalizedDictionaryResource {

	private static final Logger logger = Logger.getLogger(LocalizedDictionaryResourceImpl.class.getPackage().getName());

	@Override
	public FMLTechnologyAdapter getTechnologyAdapter() {
		if (getServiceManager() != null) {
			return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		}
		return null;
	}

	@Override
	public Class<FMLLocalizedDictionary> getResourceDataClass() {
		return FMLLocalizedDictionary.class;
	}

	/**
	 * Notify that this resource now holds data, whichever way it got it: loaded from disk, or created with empty contents by
	 * <code>FlexoResourceFactory.createEmptyContents()</code>, which notifies nothing itself. Same reason as in
	 * {@link FIBComponentResourceImpl#setResourceData(FMLFIBComponent)}.
	 */
	@Override
	public void setResourceData(FMLLocalizedDictionary resourceData) {
		super.setResourceData(resourceData);
		if (resourceData != null) {
			notifyResourceLoaded();
		}
	}

	@Override
	public void notifyResourceLoaded() {
		super.notifyResourceLoaded();
		getPropertyChangeSupport().firePropertyChange("dictionary", null,
				getLoadedResourceData() != null ? getLoadedResourceData().getDictionary() : null);
	}

	@Override
	public FMLLocalizedDictionary getLocalizedDictionary() {
		try {
			return getResourceData();
		} catch (FlexoException | ResourceLoadingCancelledException | java.io.FileNotFoundException e) {
			logger.log(Level.WARNING, "Could not load localized dictionary " + getURI(), e);
			return null;
		}
	}

	@Override
	public LocalizedDelegate getDictionary() {
		FMLLocalizedDictionary dictionary = getLocalizedDictionary();
		return dictionary != null ? dictionary.getDictionary() : null;
	}

	@Override
	public CompilationUnitResource getCompilationUnitResource() {
		return getContainer() instanceof CompilationUnitResource ? (CompilationUnitResource) getContainer() : null;
	}

	@Override
	public FMLCompilationUnit getCompilationUnit() {
		CompilationUnitResource compilationUnitResource = getCompilationUnitResource();
		return compilationUnitResource != null ? compilationUnitResource.getCompilationUnit() : null;
	}

	@Override
	public FMLLocalizedDictionary loadResourceData() throws FlexoException {

		if (getIODelegate() == null || getIODelegate().getSerializationArtefactAsResource() == null) {
			throw new FlexoException("No serialization artefact for " + getURI());
		}

		FMLLocalizedDictionary returned = FMLLocalizedDictionary.newInstance(new FMLLocalizedDelegate(this));
		returned.setResource(this);
		setResourceData(returned);

		return returned;
	}

	@Override
	public void save() throws SaveResourceException {

		FMLLocalizedDictionary data = getLoadedResourceData();
		if (data == null || !(data.getDictionary() instanceof FMLLocalizedDelegate) || getIODelegate() == null) {
			return;
		}

		// The write MUST be bracketed by the IO delegate's protocol, which tells the resource center that these files are being
		// written BY the platform: see FIBComponentResourceImpl.save(). hasWrittenOnDisk() is called whatever willWriteOnDisk()
		// answered - it currently always answers a null lock, and hasWrittenOnDisk() handles that case.
		StreamIODelegate<?> streamIODelegate = getIODelegate() instanceof StreamIODelegate ? (StreamIODelegate<?>) getIODelegate() : null;
		FileWritingLock lock = streamIODelegate != null ? streamIODelegate.willWriteOnDisk() : null;

		try {
			((FMLLocalizedDelegate) data.getDictionary()).writeDictionaries();
		} finally {
			if (streamIODelegate != null) {
				streamIODelegate.hasWrittenOnDisk(lock);
			}
		}

		data.clearIsModified();
		notifyResourceSaved();
	}
}
