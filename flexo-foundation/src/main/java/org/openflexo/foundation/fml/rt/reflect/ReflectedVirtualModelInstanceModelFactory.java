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

package org.openflexo.foundation.fml.rt.reflect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.AbstractCreationScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.annotations.DeclareActorReferences;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstanceModelFactory;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.FlexoEnumValueConverter;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.utils.FlexoObjectReferenceConverter;
import org.openflexo.pamela.PamelaMetaModelLibrary;
import org.openflexo.pamela.converter.DataBindingConverter;
import org.openflexo.pamela.converter.FlexoVersionConverter;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.EditingContext;
import org.openflexo.pamela.factory.PamelaModelFactory;

/**
 * {@link PamelaModelFactory} used to handle {@link ReflectedVirtualModelInstance} models<br>
 * 
 * @author sylvain
 * 
 */
public abstract class ReflectedVirtualModelInstanceModelFactory<R extends TechnologyAdapterResource<RD, TA>, RD extends ResourceData<RD> & TechnologyObject<TA>, TA extends TechnologyAdapter<TA>, S>
		extends PamelaModelFactory implements AbstractVirtualModelInstanceModelFactory {

	/**
	 * This is the resource beeing reflected (viewed) as a VirtualModelInstance
	 */
	private R reflectedResource;

	private final FlexoServiceManager serviceManager;

	public ReflectedVirtualModelInstanceModelFactory(R reflectedResource, Class<? extends VirtualModelInstance<?, ?>> baseVMIClass,
			EditingContext editingContext, TechnologyAdapterService taService) throws ModelDefinitionException {

		super(PamelaMetaModelLibrary.retrieveMetaModel(appendGRClasses(allClassesForModelContext(baseVMIClass, taService))));

		serviceManager = taService.getServiceManager();
		setEditingContext(editingContext);
		addConverter(new DataBindingConverter());
		addConverter(new FlexoVersionConverter());
		addConverter(new FlexoObjectReferenceConverter(taService.getServiceManager().getResourceManager()));
		addConverter(new FlexoEnumValueConverter());

		this.reflectedResource = reflectedResource;
	}

	public R getReflectedResource() {
		return reflectedResource;
	}

	@Override
	public FlexoServiceManager getServiceManager() {
		return serviceManager;
	}

	/**
	 * Iterate on all defined {@link TechnologyAdapter} to extract classes to expose being involved in technology adapter as VirtualModel
	 * parts, and return a newly created PamelaMetaModel dedicated to {@link VirtualModel} manipulations
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

	private static Class<?>[] appendGRClasses(final Collection<Class<?>> classes) {
		final Set<Class<?>> returned = new HashSet<>(classes);
		return returned.toArray(new Class<?>[returned.size()]);
	}

	public ReflectedFlexoConceptInstance<S> makeNewFlexoConceptInstance(FlexoConcept concept, S supportObject,
			FlexoConceptInstance container, VirtualModelInstance<?, ?> ownerVirtualModelInstance,
			RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {
		return makeNewFlexoConceptInstance(concept, supportObject, container, ownerVirtualModelInstance, null, evaluationContext);
	}

	public abstract ReflectedFlexoConceptInstance<S> makeNewFlexoConceptInstance(FlexoConcept concept, S supportObject,
			FlexoConceptInstance container, VirtualModelInstance<?, ?> ownerVirtualModelInstance, AbstractCreationScheme creationScheme,
			RunTimeEvaluationContext evaluationContext) throws FMLExecutionException;

}
