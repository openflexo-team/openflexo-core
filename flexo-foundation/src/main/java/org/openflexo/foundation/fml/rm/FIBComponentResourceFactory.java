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

import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.TechnologySpecificFlexoResourceFactory;
import org.openflexo.pamela.exceptions.ModelDefinitionException;

/**
 * Registers the GINA components (<code>.fib</code> / <code>.inspector</code>) of a resource center, and links each component stored in a
 * <code>Xxx.fml/</code> container back into the {@link CompilationUnitResource} of that container.
 *
 * <p>
 * That back-link is the whole point, and it is the symmetric of what
 * {@link org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResourceFactory} does for a <code>.fml.rt</code> stored the same way:
 * without it the component is registered with a null container, sits in a repository folder merely NAMED after the VirtualModel, and
 * appears nowhere under it.
 *
 * <p>
 * Unlike the VMI case, which uses a dedicated <code>containedVMI</code> property, a component goes into the generic
 * {@link FlexoResource#getContents()} list: the browsers already display it, and a component has no meaning outside the VirtualModel that
 * drives it, so being deleted with it is the wanted behaviour.
 *
 * @author sylvain
 */
public class FIBComponentResourceFactory
		extends TechnologySpecificFlexoResourceFactory<FIBComponentResource, FMLFIBComponent, FMLTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(FIBComponentResourceFactory.class.getPackage().getName());

	public static final String COMPONENT_SUFFIX = ".fib";
	public static final String INSPECTOR_SUFFIX = ".inspector";

	public FIBComponentResourceFactory() throws ModelDefinitionException {
		super(FIBComponentResource.class);
	}

	@Override
	public FMLFIBComponent makeEmptyResourceData(FIBComponentResource resource) {
		// A component is authored in the FIB editor, never created empty from here
		return null;
	}

	@Override
	public <I> boolean isValidArtefact(I serializationArtefact, FlexoResourceCenter<I> resourceCenter) {
		String name = resourceCenter.retrieveName(serializationArtefact);
		return name != null && (name.endsWith(COMPONENT_SUFFIX) || name.endsWith(INSPECTOR_SUFFIX));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <I> FIBComponentResource registerResource(FIBComponentResource resource, FlexoResourceCenter<I> resourceCenter) {
		super.registerResource(resource, resourceCenter);

		// We lookup here components stored inside a .fml directory: those are the user interfaces the enclosing
		// VirtualModel drives, and they belong to its contents.
		// Deliberately NOT through RepositoryFolder: containers of serialization artefacts are the one primitive that
		// behaves identically over a file-based and a jar-based resource center.
		I artefact = (I) resource.getIODelegate().getSerializationArtefact();
		I container = resourceCenter.getContainer(artefact);

		if (container == null || !endsWithFMLSuffix(resourceCenter.retrieveName(container))) {
			return resource;
		}

		CompilationUnitResource compilationUnitResource = compilationUnitIn(container, resourceCenter);
		if (compilationUnitResource != null) {
			compilationUnitResource.addToContents(resource);
		}
		else {
			logger.warning("Found " + resource.getURI() + " in a " + CompilationUnitResourceFactory.FML_SUFFIX
					+ " directory holding no compilation unit");
		}

		return resource;
	}

	private static boolean endsWithFMLSuffix(String name) {
		return name != null && name.endsWith(CompilationUnitResourceFactory.FML_SUFFIX);
	}

	/**
	 * The compilation unit serialized in supplied directory, or null when there is none.
	 */
	@SuppressWarnings("unchecked")
	private <I> CompilationUnitResource compilationUnitIn(I directory, FlexoResourceCenter<I> resourceCenter) {

		FMLTechnologyAdapter fmlTA = getTechnologyAdapter(resourceCenter.getServiceManager());
		if (fmlTA == null) {
			return null;
		}

		for (CompilationUnitResource compilationUnitResource : fmlTA.getVirtualModelRepository(resourceCenter).getAllResources()) {
			I serializationArtefact = (I) compilationUnitResource.getIODelegate().getSerializationArtefact();
			if (directory.equals(resourceCenter.getContainer(serializationArtefact))) {
				return compilationUnitResource;
			}
		}
		return null;
	}
}
