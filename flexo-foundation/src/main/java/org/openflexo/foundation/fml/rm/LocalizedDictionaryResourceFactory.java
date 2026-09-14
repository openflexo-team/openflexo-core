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

import org.openflexo.foundation.fml.FMLLocalizedDelegate;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.TechnologySpecificFlexoResourceFactory;
import org.openflexo.pamela.exceptions.ModelDefinitionException;

/**
 * Registers the localized dictionary of a compilation unit - the <code>Localized/</code> directory of its <code>Xxx.fml/</code> container -
 * and links it into the {@link CompilationUnitResource} of that container.
 *
 * <p>
 * <b>This factory is never offered such a directory by the exploration of a resource center.</b> {@link FMLTechnologyAdapter#isIgnorable}
 * ignores every directory inside a <code>.fml</code> container, because what it holds is explored from the compilation unit that owns it.
 * Hence the two ways in:
 * <ul>
 * <li>discovery: {@link CompilationUnitResourceFactory} retrieves the dictionary while exploring the container of a compilation unit, where
 * it also finds the contained VirtualModels;</li>
 * <li>creation: the "Localize..." action makes the resource.</li>
 * </ul>
 * The same ignore rule is what keeps the resource center's directory watcher from registering a directory the platform has just created.
 *
 * @author sylvain
 */
public class LocalizedDictionaryResourceFactory
		extends TechnologySpecificFlexoResourceFactory<LocalizedDictionaryResource, FMLLocalizedDictionary, FMLTechnologyAdapter> {

	/**
	 * The name of the directory storing the dictionary, in the <code>Xxx.fml/</code> container. Kept from the former storage, which read
	 * that same directory without any resource: existing dictionaries need no migration.
	 */
	public static final String DIRECTORY_NAME = "Localized";

	public LocalizedDictionaryResourceFactory() throws ModelDefinitionException {
		super(LocalizedDictionaryResource.class);
	}

	@Override
	public FMLLocalizedDictionary makeEmptyResourceData(LocalizedDictionaryResource resource) {
		return FMLLocalizedDictionary.newInstance(new FMLLocalizedDelegate(resource));
	}

	@Override
	public <I> boolean isValidArtefact(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {
		if (!resourceCenter.isDirectory(serializationArtefact) || !DIRECTORY_NAME.equals(resourceCenter.retrieveName(serializationArtefact))) {
			return false;
		}
		I container = resourceCenter.getContainer(serializationArtefact);
		String containerName = container != null ? resourceCenter.retrieveName(container) : null;
		return containerName != null && containerName.endsWith(CompilationUnitResourceFactory.FML_SUFFIX);
	}

	/**
	 * Link the dictionary into the compilation unit of its container, when that one is already registered.
	 *
	 * <p>
	 * Silent when it is not: on discovery the caller holds the compilation unit and links the dictionary itself.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <I> LocalizedDictionaryResource registerResource(LocalizedDictionaryResource resource, FlexoResourceCenter<I> resourceCenter) {
		super.registerResource(resource, resourceCenter);

		if (resource.getContainer() == null) {
			I artefact = (I) resource.getIODelegate().getSerializationArtefact();
			CompilationUnitResource compilationUnitResource = CompilationUnitResourceFactory
					.getCompilationUnitResourceSerializedIn(resourceCenter.getContainer(artefact), resourceCenter);
			if (compilationUnitResource != null) {
				compilationUnitResource.addToContents(resource);
			}
		}

		return resource;
	}
}
