/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.view.controller;

import java.util.Collection;
import java.util.List;

import org.openflexo.connie.type.CustomType;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoService;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.gina.controller.CustomTypeEditor;
import org.openflexo.gina.controller.CustomTypeEditorProvider;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * This service provides access to all technology adapters controllers available in a given environment.
 * 
 * Please note that this service MUST use a {@link FlexoResourceCenterService}
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(DefaultTechnologyAdapterControllerService.class)
public interface TechnologyAdapterControllerService extends FlexoService, CustomTypeEditorProvider {

	/**
	 * Load all available technology adapters controllers
	 */
	// public void loadAvailableTechnologyAdapterControllers();

	/**
	 * Return loaded technology adapter controller mapping supplied class<br>
	 * If adapter is not loaded, return null
	 * 
	 * @param technologyAdapterClass
	 * @return
	 */
	public <TAC extends TechnologyAdapterController<TA>, TA extends TechnologyAdapter<TA>> TAC getTechnologyAdapterController(
			Class<TAC> technologyAdapterControllerClass);

	/**
	 * Return loaded technology adapter controller mapping supplied technology adapter<br>
	 * If adapter is not loaded, return null
	 * 
	 * @param technologyAdapterClass
	 * @return
	 */
	public <TAC extends TechnologyAdapterController<TA>, TA extends TechnologyAdapter<TA>> TAC getTechnologyAdapterController(
			TA technologyAdapter);

	/**
	 * Return boolean indicating if this TechnologyAdapter controller service support ModuleView rendering for supplied technology object
	 * 
	 * @param object
	 * @return
	 */
	//public <TA extends TechnologyAdapter<TA>> boolean hasModuleViewForObject(TechnologyObject<TA> object, FlexoController controller,
	//		FlexoPerspective perspective);

	/**
	 * Return a newly created ModuleView for supplied technology object, if this TechnologyAdapter controller service support ModuleView
	 * rendering
	 * 
	 * @param object
	 * @return
	 */
	//public <TA extends TechnologyAdapter<TA>> ModuleView<?> createModuleViewForObject(TechnologyObject<TA> object,
	//		FlexoController controller, FlexoPerspective perspective);

	/**
	 * Return a boolean indicating if this {@link FlexoPerspective} handles supplied object by defining a {@link ModuleView} in which 
	 * supplied object is representable either as a master object or as an object representable in related view
	 * 
	 * @param object
	 * @return
	 */
	public abstract <TA extends TechnologyAdapter<TA>> boolean isRepresentableInModuleView(TechnologyObject<TA> object);
	
	/**
	 * Return {@link FlexoObject} for which this perspective defines a {@link ModuleView} where supplied object is also representable
	 * 
	 * @param object
	 * @return
	 */
	public abstract <TA extends TechnologyAdapter<TA>> FlexoObject getRepresentableMasterObject(TechnologyObject<TA> object);
	
	/**
	 * Return a newly created {@link ModuleView} for supplied technology object, when supported
	 * rendering
	 * 
	 * @param object
	 * @return
	 */
	public abstract <TA extends TechnologyAdapter<TA>> ModuleView<?> createModuleViewForMasterObject(TechnologyObject<TA> object, FlexoController controller,
			FlexoPerspective perspective);


	
	public Collection<TechnologyAdapterController<?>> getLoadedAdapterControllers();

	/**
	 * Enable a {@link TechnologyAdapter}<br>
	 * The {@link FlexoResourceCenter} should scan the resources that it may interpret
	 * 
	 * @param technologyAdapter
	 */
	public void activateTechnology(TechnologyAdapter<?> technologyAdapter);

	/**
	 * Disable a {@link TechnologyAdapter}<br>
	 * The {@link FlexoResourceCenter} is notified to free the resources that it is managing, if possible
	 * 
	 * @param technologyAdapter
	 */
	public void disactivateTechnology(TechnologyAdapter<?> technologyAdapter);

	/**
	 * Return editor for supplied custom type
	 * 
	 * @param typeClass
	 * @return
	 */
	@Override
	public <T extends CustomType> CustomTypeEditor<T> getCustomTypeEditor(Class<T> typeClass);

	/**
	 * Return singleton instance of supplied Plugin class
	 * 
	 * @param <P>
	 * @param pluginClass
	 * @return
	 */
	public <P extends TechnologyAdapterPluginController<?>> P getPlugin(Class<P> pluginClass);

	/**
	 * Return the list of all activated {@link TechnologyAdapterPluginController}
	 * 
	 * @return
	 */
	public List<TechnologyAdapterPluginController<?>> getActivatedPlugins();

}
