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

package org.openflexo.foundation.fml;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.FMLLocalizedDictionary;
import org.openflexo.foundation.fml.rm.LocalizedDictionaryResource;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.localization.LocalizedDelegateImpl;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.Resource;

/**
 * The localizer of a {@link FMLCompilationUnit}, in one of two shapes.
 *
 * <p>
 * <b>Stored</b>: the data of a {@link LocalizedDictionaryResource}, read from the <code>Xxx.fml/Localized/</code> directory. It follows the
 * cycle of any resource: a change marks the resource modified, and nothing is written until that resource is saved - through the IO
 * delegate's protocol, so that the resource center does not discover the files it writes as a stranger's.
 *
 * <p>
 * <b>In memory</b>: what a compilation unit uses as long as it has no dictionary. It reads nothing and writes nothing, but it still
 * REGISTERS the keys it is asked for. That is its whole point: {@link LocalizedDelegateImpl} registers a missing key in the first localizer
 * of the chain, so without this one the key would land in the dictionary of the container VirtualModel, or in the platform's own. These
 * keys are what "Localize..." starts from when it creates the dictionary.
 *
 * <p>
 * In both shapes the parent is looked up at each use rather than fixed at construction, so that a dictionary created later for the
 * container VirtualModel is seen by the dictionaries of the VirtualModels it contains.
 *
 * @author sylvain
 */
public class FMLLocalizedDelegate extends LocalizedDelegateImpl {

	private static final Logger logger = Logger.getLogger(FMLLocalizedDelegate.class.getPackage().getName());

	private final Supplier<FMLCompilationUnit> compilationUnit;
	private final LocalizedDictionaryResource resource;

	/**
	 * A localizer held in memory, for a compilation unit that has no dictionary.
	 */
	public FMLLocalizedDelegate(Supplier<FMLCompilationUnit> compilationUnit) {
		super((LocalizedDelegate) null);
		this.compilationUnit = compilationUnit;
		this.resource = null;
		installLocalizationRetriever();
	}

	/**
	 * The data of supplied resource, read from its directory.
	 *
	 * <p>
	 * The compilation unit is reached WITHOUT loading it: a lookup may run while that compilation unit is itself being loaded, and must not
	 * trigger a second load. A dictionary opened before its compilation unit simply has no parent until the latter is loaded.
	 */
	public FMLLocalizedDelegate(LocalizedDictionaryResource resource) {
		super(directoryOf(resource), null, false, directoryOf(resource) instanceof FileResourceImpl);
		this.compilationUnit = () -> {
			CompilationUnitResource compilationUnitResource = resource.getCompilationUnitResource();
			return compilationUnitResource != null ? compilationUnitResource.getLoadedCompilationUnit() : null;
		};
		this.resource = resource;
		installLocalizationRetriever();
	}

	private static Resource directoryOf(LocalizedDictionaryResource resource) {
		return resource.getIODelegate() != null ? resource.getIODelegate().getSerializationArtefactAsResource() : null;
	}

	/**
	 * What the "search" action of the localization editor runs.
	 */
	private void installLocalizationRetriever() {
		setLocalizationRetriever(() -> {
			FMLCompilationUnit cu = compilationUnit.get();
			if (cu != null) {
				cu.searchNewLocalizedEntries();
			}
		});
	}

	/**
	 * The resource this localizer is the data of, or null when it is held in memory.
	 */
	public LocalizedDictionaryResource getResource() {
		return resource;
	}

	@Override
	public LocalizedDelegate getParent() {
		FMLCompilationUnit cu = compilationUnit.get();
		return cu != null ? cu.getParentLocales() : null;
	}

	/**
	 * The files are written when the resource is saved, never as a side effect of reading them.
	 */
	@Override
	protected boolean createsMissingDictionaryFiles() {
		return false;
	}

	@Override
	protected void dictionariesChanged() {
		FMLLocalizedDictionary data = resource != null ? resource.getLoadedResourceData() : null;
		if (data != null) {
			data.setIsModified();
		}
	}

	/**
	 * An explicit request - the localization editor has one - goes through the resource, which writes within the IO delegate's protocol.
	 */
	@Override
	public void save() {
		saveResource();
	}

	@Override
	public void saveAllDictionaries() {
		saveResource();
	}

	private void saveResource() {
		if (resource == null) {
			// Held in memory: there is nowhere to write
			return;
		}
		try {
			resource.save();
		} catch (SaveResourceException e) {
			logger.log(Level.WARNING, "Could not save " + resource.getURI(), e);
		}
	}

	/**
	 * The actual write. Only the resource calls it, from within the IO delegate's protocol.
	 */
	public void writeDictionaries() {
		super.saveAllDictionaries();
	}

	@Override
	public String toString() {
		if (resource != null) {
			return super.toString();
		}
		FMLCompilationUnit cu = compilationUnit.get();
		return "Locales of " + (cu != null && cu.getResource() != null ? cu.getResource().getURI() : cu) + " (not stored)";
	}
}
