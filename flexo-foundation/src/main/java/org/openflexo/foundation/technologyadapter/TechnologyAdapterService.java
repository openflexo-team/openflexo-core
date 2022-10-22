/**
 * 
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.foundation.technologyadapter;

import java.util.List;
import java.util.Map;

import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.CustomTypeFactory;
import org.openflexo.connie.type.CustomTypeManager;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.editionaction.AbstractFetchRequest;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.ResourceRepository;
import org.openflexo.foundation.resource.ResourceRepositoryImpl;
import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;

/**
 * This service provides access to all technology adapters available in a given environment.
 * 
 * Please note that this service MUST use a {@link FlexoResourceCenterService}
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(DefaultTechnologyAdapterService.class)
public interface TechnologyAdapterService extends FlexoService, CustomTypeManager {
	public static final String TECHNOLOGY_ADAPTERS = "technologyAdapters";
	public static final String RESOURCE_CENTER_SERVICE = "flexoResourceCenterService";

	/**
	 * Load all available technology adapters
	 */
	// public void loadAvailableTechnologyAdapters();

	@Getter(value = TECHNOLOGY_ADAPTERS, cardinality = Cardinality.LIST, ignoreType = true)
	public List<TechnologyAdapter> getTechnologyAdapters();

	@Setter(TECHNOLOGY_ADAPTERS)
	public void setTechnologyAdapters(List<TechnologyAdapter> technologyAdapters);

	@Adder(TECHNOLOGY_ADAPTERS)
	public void addToTechnologyAdapters(TechnologyAdapter<?> technologyAdapters);

	@Remover(TECHNOLOGY_ADAPTERS)
	public void removeFromTechnologyAdapters(TechnologyAdapter<?> technologyAdapters);

	@Getter(value = RESOURCE_CENTER_SERVICE, ignoreType = true)
	public FlexoResourceCenterService getFlexoResourceCenterService();

	@Setter(RESOURCE_CENTER_SERVICE)
	public void setFlexoResourceCenterService(FlexoResourceCenterService flexoResourceCenterService);

	/**
	 * Return loaded technology adapter mapping supplied class<br>
	 * If adapter is not loaded, return null
	 * 
	 * @param technologyAdapterClass
	 * @return
	 */
	public <TA extends TechnologyAdapter<TA>> TA getTechnologyAdapter(Class<TA> technologyAdapterClass);

	/**
	 * Return the {@link TechnologyContextManager} for this technology shared by all {@link FlexoResourceCenter} declared in the scope of
	 * {@link FlexoResourceCenterService}
	 * 
	 * @return
	 */
	public <TA extends TechnologyAdapter<TA>> TechnologyContextManager<TA> getTechnologyContextManager(TA technologyAdapter);

	/**
	 * Return the list of all non-empty {@link ModelRepository} discovered in the scope of {@link FlexoServiceManager}, related to
	 * technology as supplied by {@link TechnologyAdapter} parameter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public List<ModelRepository<?, ?, ?, ?, ?, ?>> getAllModelRepositories(TechnologyAdapter<?> technologyAdapter);

	/**
	 * Return the list of all non-empty {@link MetaModelRepository} discovered in the scope of {@link FlexoServiceManager}, related to
	 * technology as supplied by {@link TechnologyAdapter} parameter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public List<MetaModelRepository<?, ?, ?, ?, ?>> getAllMetaModelRepositories(TechnologyAdapter<?> technologyAdapter);

	/**
	 * Return the list of all non-empty {@link ResourceRepositoryImpl} discovered in the scope of {@link FlexoServiceManager}, related to
	 * technology as supplied by {@link TechnologyAdapter} parameter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public List<ResourceRepository<?, ?>> getAllRepositories(TechnologyAdapter<?> technologyAdapter);

	/**
	 * Return the list of all global {@link ResourceRepositoryImpl} discovered in this {@link InformationSpace}, related to technology as
	 * supplied by {@link TechnologyAdapter} parameter.<br>
	 * One global repository for each {@link FlexoResourceCenter} is returned
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public <TA extends TechnologyAdapter<TA>> List<TechnologyAdapterResourceRepository<?, TA, ?, ?>> getGlobalRepositories(
			TA technologyAdapter);

	/**
	 * Return the list of all non-empty {@link ResourceRepositoryImpl} discovered in the scope of {@link FlexoServiceManager} which may give
	 * access to some instance of supplied resource data class, related to technology as supplied by {@link TechnologyAdapter} parameter
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	public <RD extends ResourceData<RD>> List<ResourceRepository<? extends FlexoResource<RD>, ?>> getAllRepositories(
			TechnologyAdapter<?> technologyAdapter, Class<RD> resourceDataClass);

	/**
	 * Return all {@link CustomType} factories defined for all known technologies
	 * 
	 * @return
	 */
	@Override
	public Map<Class<? extends CustomType>, CustomTypeFactory<?>> getCustomTypeFactories();

	/**
	 * Register CustomTypeFactory
	 * 
	 * @param typeClass
	 * @param factory
	 */
	public <T extends CustomType> void registerTypeClass(Class<T> typeClass, CustomTypeFactory<T> factory);

	/**
	 * Enable a {@link TechnologyAdapter}<br>
	 * All resources centers are notified to scan the resources that they may interpret
	 * 
	 * @param technologyAdapter
	 */
	public <TA extends TechnologyAdapter<TA>> FlexoTask activateTechnologyAdapter(TA technologyAdapter, boolean now);

	/**
	 * Disable a {@link TechnologyAdapter}<br>
	 * All resources centers are notified to free the resources that they are managing, if possible
	 * 
	 * @param technologyAdapter
	 */
	public <TA extends TechnologyAdapter<TA>> FlexoTask disactivateTechnologyAdapter(TA technologyAdapter);

	/**
	 * Return {@link TechnologyAdapter} where supplied modelSlotClass has been declared
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	public <MS extends ModelSlot<?>> TechnologyAdapter<?> getTechnologyAdapterForModelSlot(Class<MS> modelSlotClass);

	public <B extends FlexoBehaviour> TechnologyAdapter<?> getTechnologyAdapterForBehaviourType(Class<B> behaviourClass);

	/**
	 * Return the list of {@link FlexoRole} class available for supplied modelSlotClass
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	public <MS extends ModelSlot<?>> List<Class<? extends FlexoRole<?>>> getAvailableFlexoRoleTypes(Class<MS> modelSlotClass);

	/**
	 * Return the list of {@link TechnologySpecificAction} class available for supplied modelSlotClass
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	public <MS extends ModelSlot<?>> List<Class<? extends EditionAction>> getAvailableEditionActionTypes(Class<MS> modelSlotClass);

	/**
	 * Return the list of extra {@link FMLObject} class involved in supplied {@link ModelSlot} definition
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	public <MS extends ModelSlot<?>> List<Class<? extends FMLObject>> getAvailableFMLObjectTypes(Class<MS> modelSlotClass);

	/**
	 * Return the list of {@link AbstractFetchRequest} class available for supplied modelSlotClass
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	public <MS extends ModelSlot<?>> List<Class<? extends AbstractFetchRequest<?, ?, ?, ?>>> getAvailableAbstractFetchRequestActionTypes(
			Class<MS> modelSlotClass);

	/**
	 * Return the list of {@link FetchRequest} class available for supplied modelSlotClass
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	public <MS extends ModelSlot<?>> List<Class<? extends FetchRequest<?, ?, ?>>> getAvailableFetchRequestActionTypes(
			Class<MS> modelSlotClass);

	/**
	 * Return the list of {@link FlexoBehaviour} class available for supplied modelSlotClass
	 * 
	 * @param modelSlotClass
	 * @return
	 */
	public <MS extends ModelSlot<?>> List<Class<? extends FlexoBehaviour>> getAvailableFlexoBehaviourTypes(Class<MS> modelSlotClass);

	public <MS extends ModelSlot<?>> Class<? extends FlexoBehaviour> getFlexoBehaviour(Class<MS> modelSlotClass, String behaviourKeyword);

	public Class<? extends ModelSlot<?>> getModelSlotClass(Class<? extends FlexoRole<?>> roleClass);

	public <MS extends ModelSlot<?>> Class<? extends FlexoRole<?>> getFlexoRole(Class<MS> modelSlotClass, String roleKeyword);

	public <MS extends ModelSlot<?>> Class<? extends TechnologySpecificAction<?, ?>> getEditionAction(Class<MS> modelSlotClass,
			String editionActionKeyword);

	public <MS extends ModelSlot<?>> Class<? extends FMLObject> getFMLObject(Class<MS> modelSlotClass, String objectKeyword);

}
