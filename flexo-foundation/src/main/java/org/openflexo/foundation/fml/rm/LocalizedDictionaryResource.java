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

import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * The localized dictionary of a {@link FMLCompilationUnit}: the <code>Xxx.fml/Localized/</code> directory and the one
 * <code>&lt;Language&gt;.dict</code> file per language it holds.
 *
 * <p>
 * A first-class resource contained in the {@link CompilationUnitResource}, exactly as the <code>.fib</code> / <code>.inspector</code>
 * components of that container are (see {@link FIBComponentResource}): it shows up under its VirtualModel in the browsers, is opened in the
 * localization editor, and follows the ordinary modified/save cycle.
 *
 * <p>
 * A compilation unit does not necessarily have one: it is created on demand, by the "Localize..." action. Until then the compilation unit
 * localizes in memory - see {@link org.openflexo.foundation.fml.FMLLocalizedDelegate}.
 *
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(LocalizedDictionaryResourceImpl.class)
public interface LocalizedDictionaryResource extends TechnologyAdapterResource<FMLLocalizedDictionary, FMLTechnologyAdapter> {

	/**
	 * The dictionary this resource holds, loading the resource when required, or null when it cannot be loaded.
	 */
	public FMLLocalizedDictionary getLocalizedDictionary();

	/**
	 * The localizer of the dictionary this resource holds, loading the resource when required, or null when it cannot be loaded.
	 */
	public LocalizedDelegate getDictionary();

	/**
	 * The resource of the compilation unit this dictionary belongs to, or null when this resource is not (yet) linked to one.
	 */
	public CompilationUnitResource getCompilationUnitResource();

	/**
	 * The compilation unit this dictionary belongs to, loading it when required.
	 */
	public FMLCompilationUnit getCompilationUnit();
}
