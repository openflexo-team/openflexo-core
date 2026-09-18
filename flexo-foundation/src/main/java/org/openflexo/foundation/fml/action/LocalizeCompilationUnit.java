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

package org.openflexo.foundation.fml.action;

import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.TechnologySpecificFlexoAction;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.LocalizedDictionaryResource;
import org.openflexo.foundation.fml.rm.LocalizedDictionaryResourceFactory;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.localization.Language;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.localization.LocalizedDelegate.LocalizedEntry;
import org.openflexo.rm.FileResourceImpl;

/**
 * "Localize...": give access to the localized dictionary of a compilation unit, creating it when it does not exist yet.
 *
 * <p>
 * A compilation unit has no dictionary until this action is invoked on it (or on its VirtualModel). Creating one:
 * <ul>
 * <li>creates the <code>Localized/</code> directory in the <code>Xxx.fml/</code> container, and the {@link LocalizedDictionaryResource}
 * contained in the compilation unit's resource;</li>
 * <li>fills it with the keys the compilation unit collected in memory so far - with their translations - and with the localizable keys it
 * declares;</li>
 * <li>writes it. Creating is the explicit purpose of the action, as for a user interface component; every later change follows the
 * ordinary modified/save cycle.</li>
 * </ul>
 * Opening the dictionary in the localization editor is the controller layer's business: the initializer does it, from
 * {@link #getDictionaryResource()}.
 *
 * @author sylvain
 */
public class LocalizeCompilationUnit extends FlexoAction<LocalizeCompilationUnit, FMLObject, FMLObject>
		implements TechnologySpecificFlexoAction<FMLTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(LocalizeCompilationUnit.class.getPackage().getName());

	public static FlexoActionFactory<LocalizeCompilationUnit, FMLObject, FMLObject> actionType = new FlexoActionFactory<LocalizeCompilationUnit, FMLObject, FMLObject>(
			"localize_compilation_unit", FlexoActionFactory.defaultGroup, FlexoActionFactory.NORMAL_ACTION_TYPE) {

		@Override
		public LocalizeCompilationUnit makeNewAction(FMLObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new LocalizeCompilationUnit(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			return compilationUnitResourceOf(object) != null;
		}

		/**
		 * An existing dictionary can always be opened (read-only when it comes from a jar). A new one can only be created in a file-based
		 * resource center.
		 */
		@Override
		public boolean isEnabledForSelection(FMLObject object, Vector<FMLObject> globalSelection) {
			CompilationUnitResource resource = compilationUnitResourceOf(object);
			return resource != null && (resource.getLocalizedDictionaryResource() != null || canHostANewDictionary(resource));
		}
	};

	static {
		FlexoObjectImpl.addActionForClass(LocalizeCompilationUnit.actionType, FMLCompilationUnit.class);
		FlexoObjectImpl.addActionForClass(LocalizeCompilationUnit.actionType, VirtualModel.class);
	}

	/**
	 * The compilation unit localized from supplied object: the object itself, or the one a VirtualModel is serialized in.
	 */
	public static FMLCompilationUnit compilationUnitOf(FMLObject object) {
		if (object instanceof FMLCompilationUnit) {
			return (FMLCompilationUnit) object;
		}
		if (object instanceof VirtualModel) {
			return ((VirtualModel) object).getCompilationUnit();
		}
		return null;
	}

	private static CompilationUnitResource compilationUnitResourceOf(FMLObject object) {
		FMLCompilationUnit compilationUnit = compilationUnitOf(object);
		return compilationUnit != null && compilationUnit.getResource() instanceof CompilationUnitResource
				? (CompilationUnitResource) compilationUnit.getResource()
				: null;
	}

	/**
	 * Only a file-based resource center can receive a new directory. The same criterion as the edit support of a stored dictionary.
	 */
	private static boolean canHostANewDictionary(CompilationUnitResource resource) {
		return resource.getIODelegate() != null && resource.getIODelegate().getSerializationArtefactAsResource() instanceof FileResourceImpl;
	}

	private LocalizedDictionaryResource dictionaryResource;
	private boolean created = false;

	private LocalizeCompilationUnit(FMLObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public Class<? extends FMLTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	public FMLCompilationUnit getCompilationUnit() {
		return compilationUnitOf(getFocusedObject());
	}

	/**
	 * The dictionary to open: the one that already existed, or the one this action created.
	 */
	public LocalizedDictionaryResource getDictionaryResource() {
		return dictionaryResource;
	}

	/**
	 * Whether this action created the dictionary, rather than finding an existing one.
	 */
	public boolean isCreated() {
		return created;
	}

	@Override
	protected void doAction(Object context) throws FlexoException {

		FMLCompilationUnit compilationUnit = getCompilationUnit();
		CompilationUnitResource compilationUnitResource = compilationUnitResourceOf(getFocusedObject());
		if (compilationUnitResource == null) {
			throw new FlexoException("No resource for " + compilationUnit);
		}

		dictionaryResource = compilationUnitResource.getLocalizedDictionaryResource();
		if (dictionaryResource != null) {
			// Nothing to do on the model: opening it is the initializer's job
			return;
		}

		// What the compilation unit collected in memory so far - read before the dictionary exists, since from then on the compilation unit
		// answers the dictionary instead
		LocalizedDelegate collected = compilationUnit.getLocalizedDictionary();

		dictionaryResource = makeDictionaryResource(compilationUnitResource);
		if (dictionaryResource.getCompilationUnitResource() != compilationUnitResource) {
			throw new FlexoException("Created " + dictionaryResource.getURI() + " but could not link it to " + compilationUnitResource.getURI());
		}

		LocalizedDelegate dictionary = dictionaryResource.getDictionary();
		if (collected != null && collected != dictionary) {
			for (LocalizedEntry entry : new ArrayList<>(collected.getEntries())) {
				dictionary.addEntry(entry.getKey());
				for (Language language : Language.availableValues()) {
					String value = collected.localizedForKeyAndLanguage(entry.getKey(), language, false);
					if (value != null) {
						dictionary.registerNewEntry(entry.getKey(), language, value);
					}
				}
			}
		}
		compilationUnit.searchNewLocalizedEntries();

		dictionaryResource.save();
		created = true;
		logger.info("Created " + dictionaryResource.getURI());

		compilationUnit.getPropertyChangeSupport().firePropertyChange("localizedDictionary", collected, dictionary);
	}

	@SuppressWarnings("unchecked")
	private <I> LocalizedDictionaryResource makeDictionaryResource(CompilationUnitResource compilationUnitResource) throws FlexoException {

		FlexoResourceCenter<I> resourceCenter = (FlexoResourceCenter<I>) compilationUnitResource.getResourceCenter();
		I container = resourceCenter.getContainer((I) compilationUnitResource.getIODelegate().getSerializationArtefact());
		I directory = resourceCenter.createDirectory(LocalizedDictionaryResourceFactory.DIRECTORY_NAME, container);

		LocalizedDictionaryResourceFactory factory = resourceCenter.getServiceManager().getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class).getResourceFactory(LocalizedDictionaryResourceFactory.class);

		try {
			return factory.makeResource(directory, resourceCenter, true);
		} catch (Exception e) {
			throw new FlexoException("Could not create the localized dictionary of " + compilationUnitResource.getURI(), e);
		}
	}
}
