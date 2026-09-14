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

import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;

/**
 * The data of a {@link LocalizedDictionaryResource}: a holder around the localizer read from the <code>Localized/</code> directory.
 *
 * <p>
 * Needed for the same reason as {@link FMLFIBComponent}: a resource's data must be a {@link TechnologyObject}, hence a PAMELA
 * <code>FlexoObject</code>, which a {@link LocalizedDelegate} is not. The holder carries no state of its own and is never serialized - the
 * <code>.dict</code> files are written by the localizer - so one factory for the whole platform is enough.
 *
 * <p>
 * This is also the object the localization editor is opened on, and the one its modified status is carried by.
 *
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(FMLLocalizedDictionary.FMLLocalizedDictionaryImpl.class)
public interface FMLLocalizedDictionary extends TechnologyObject<FMLTechnologyAdapter>, ResourceData<FMLLocalizedDictionary> {

	public static final String DICTIONARY_KEY = "dictionary";

	/**
	 * The localizer this resource holds.
	 */
	@Getter(value = DICTIONARY_KEY, ignoreType = true)
	public LocalizedDelegate getDictionary();

	@Setter(DICTIONARY_KEY)
	public void setDictionary(LocalizedDelegate dictionary);

	@Override
	public LocalizedDictionaryResource getResource();

	/**
	 * The compilation unit this dictionary belongs to, loading it when required.
	 */
	public FMLCompilationUnit getCompilationUnit();

	/**
	 * Build a holder around supplied localizer.
	 */
	public static FMLLocalizedDictionary newInstance(LocalizedDelegate dictionary) {
		FMLLocalizedDictionary returned = HOLDER_FACTORY.newInstance(FMLLocalizedDictionary.class);
		returned.setDictionary(dictionary);
		return returned;
	}

	static final PamelaModelFactory HOLDER_FACTORY = makeHolderFactory();

	static PamelaModelFactory makeHolderFactory() {
		try {
			return new PamelaModelFactory(FMLLocalizedDictionary.class);
		} catch (ModelDefinitionException e) {
			throw new IllegalStateException("Cannot build the FMLLocalizedDictionary factory", e);
		}
	}

	public static abstract class FMLLocalizedDictionaryImpl extends FlexoObjectImpl implements FMLLocalizedDictionary {

		@Override
		public FMLTechnologyAdapter getTechnologyAdapter() {
			return getResource() != null ? getResource().getTechnologyAdapter() : null;
		}

		@Override
		public FMLCompilationUnit getCompilationUnit() {
			return getResource() != null ? getResource().getCompilationUnit() : null;
		}

		@Override
		public String toString() {
			FMLCompilationUnit compilationUnit = getResource() != null && getResource().getCompilationUnitResource() != null
					? getResource().getCompilationUnitResource().getLoadedCompilationUnit()
					: null;
			if (compilationUnit != null && compilationUnit.getVirtualModel() != null) {
				return compilationUnit.getVirtualModel().getName() + " (locales)";
			}
			return getResource() != null ? getResource().getName() : super.toString();
		}
	}
}
