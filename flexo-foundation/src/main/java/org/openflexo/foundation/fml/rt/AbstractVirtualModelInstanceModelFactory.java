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

package org.openflexo.foundation.fml.rt;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.foundation.DefaultPamelaResourceModelFactory;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.annotations.DeclareActorReferences;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.utils.FlexoObjectReferenceConverter;
import org.openflexo.pamela.converter.DataBindingConverter;
import org.openflexo.pamela.converter.FlexoVersionConverter;
import org.openflexo.pamela.converter.RelativePathResourceConverter;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.EditingContext;
import org.openflexo.pamela.factory.ModelFactory;

/**
 * {@link ModelFactory} used to handle {@link VirtualModelInstance} models<br>
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractVirtualModelInstanceModelFactory<R extends AbstractVirtualModelInstanceResource<?, ?>>
		extends DefaultPamelaResourceModelFactory<R> {

	private RelativePathResourceConverter relativePathResourceConverter;

	public AbstractVirtualModelInstanceModelFactory(R virtualModelInstanceResource,
			Class<? extends VirtualModelInstance<?, ?>> baseVMIClass, EditingContext editingContext, TechnologyAdapterService taService)
			throws ModelDefinitionException {

		super(virtualModelInstanceResource, allClassesForModelContext(baseVMIClass, taService));
		setEditingContext(editingContext);
		addConverter(new DataBindingConverter());
		addConverter(new FlexoVersionConverter());
		addConverter(new FlexoObjectReferenceConverter(taService.getServiceManager().getResourceManager()));
		addConverter(new FlexoEnumValueConverter());

		addConverter(relativePathResourceConverter = new RelativePathResourceConverter(null));
		if (virtualModelInstanceResource != null && virtualModelInstanceResource.getIODelegate() != null
				&& virtualModelInstanceResource.getIODelegate().getSerializationArtefactAsResource() != null) {
			relativePathResourceConverter
					.setContainerResource(virtualModelInstanceResource.getIODelegate().getSerializationArtefactAsResource().getContainer());
		}

	}

	/**
	 * Iterate on all defined {@link TechnologyAdapter} to extract classes to expose being involved in technology adapter as VirtualModel
	 * parts, and return a newly created ModelContext dedicated to {@link VirtualModel} manipulations
	 * 
	 * @param taService
	 * @return
	 * @throws ModelDefinitionException
	 */
	public static List<Class<?>> allClassesForModelContext(Class<? extends VirtualModelInstance<?, ?>> baseVMIClass,
			TechnologyAdapterService taService) throws ModelDefinitionException {
		List<Class<?>> classes = new ArrayList<>();
		classes.add(baseVMIClass);
		if (taService != null) {
			for (TechnologyAdapter<?> ta : taService.getTechnologyAdapters()) {
				for (Class<?> modelSlotClass : ta.getAvailableModelSlotTypes()) {
					classes.add(modelSlotClass);
					DeclareActorReferences arDeclarations = modelSlotClass.getAnnotation(DeclareActorReferences.class);
					if (arDeclarations != null) {
						for (Class<? extends ActorReference> arClass : arDeclarations.value()) {
							classes.add(arClass);
						}
					}
				}
			}
		}

		return classes;
	}
}
