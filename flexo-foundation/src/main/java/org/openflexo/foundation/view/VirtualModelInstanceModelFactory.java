/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.view;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.foundation.DefaultPamelaResourceModelFactory;
import org.openflexo.foundation.resource.FileFlexoIODelegate;
import org.openflexo.foundation.technologyadapter.DeclareActorReference;
import org.openflexo.foundation.technologyadapter.DeclareActorReferences;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.view.rm.VirtualModelInstanceResource;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.model.converter.DataBindingConverter;
import org.openflexo.model.converter.FlexoVersionConverter;
import org.openflexo.model.converter.RelativePathResourceConverter;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.EditingContext;
import org.openflexo.model.factory.ModelFactory;

/**
 * {@link ModelFactory} used to handle VirtualModelInstance models<br>
 * Only one instance of this class should be used in a session
 * 
 * @author sylvain
 * 
 */
public class VirtualModelInstanceModelFactory extends DefaultPamelaResourceModelFactory<VirtualModelInstanceResource> {

	/*public VirtualModelInstanceModelFactory() throws ModelDefinitionException {
		super(ModelContextLibrary.getModelContext(VirtualModelInstance.class));
		addConverter(new DataBindingConverter());
		addConverter(new FlexoVersionConverter());
	}*/

	public VirtualModelInstanceModelFactory(VirtualModelInstanceResource virtualModelInstanceResource, EditingContext editingContext,
			TechnologyAdapterService taService) throws ModelDefinitionException {
		super(virtualModelInstanceResource, allClassesForModelContext(taService));
		setEditingContext(editingContext);
		addConverter(new DataBindingConverter());
		addConverter(new FlexoVersionConverter());
		if (virtualModelInstanceResource != null) {
			if(virtualModelInstanceResource.getFlexoIODelegate() instanceof FileFlexoIODelegate){
				FileFlexoIODelegate delegate = (FileFlexoIODelegate) virtualModelInstanceResource.getFlexoIODelegate();
				addConverter(new RelativePathResourceConverter(delegate.getFile().getParent()));
			}
			addConverter(virtualModelInstanceResource.getProject().getObjectReferenceConverter());
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
	private static List<Class<?>> allClassesForModelContext(TechnologyAdapterService taService) throws ModelDefinitionException {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(VirtualModelInstance.class);
		if (taService != null) {
			for (TechnologyAdapter ta : taService.getTechnologyAdapters()) {
				for (Class<?> modelSlotClass : ta.getAvailableModelSlotTypes()) {
					classes.add(modelSlotClass);
					DeclareActorReferences arDeclarations = modelSlotClass.getAnnotation(DeclareActorReferences.class);
					if (arDeclarations != null) {
						for (DeclareActorReference arDeclaration : arDeclarations.value()) {
							classes.add(arDeclaration.actorReferenceClass());
						}
					}
				}
			}
		}

		return classes;
	}
}
