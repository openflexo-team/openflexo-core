/**
 * 
 * Copyright (c) 2014, Openflexo
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

import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptNature;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelNature;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstanceNature;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceNature;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.view.ModuleView;

/**
 * A perspective specific to a technology, and providing specific view for objects having specific natures
 * 
 * Those natures are handled by a specific technology adapter
 * 
 * @author sylvain
 * 
 * @param <TA>
 */
public abstract class SpecificNaturePerspective<TA extends TechnologyAdapter<TA>> extends GenericPerspective {

	static final Logger logger = Logger.getLogger(SpecificNaturePerspective.class.getPackage().getName());

	private final TA technologyAdapter;

	private final VirtualModelNature virtualModelNature;
	private final FlexoConceptNature flexoConceptNature;
	private final VirtualModelInstanceNature virtualModelInstanceNature;
	private final FlexoConceptInstanceNature flexoConceptInstanceNature;

	public SpecificNaturePerspective(TA technologyAdapter, VirtualModelNature virtualModelNature, FlexoConceptNature flexoConceptNature,
			VirtualModelInstanceNature virtualModelInstanceNature, FlexoConceptInstanceNature flexoConceptInstanceNature,
			FlexoController controller) {
		super(controller);
		this.technologyAdapter = technologyAdapter;
		this.virtualModelNature = virtualModelNature;
		this.flexoConceptNature = flexoConceptNature;
		this.virtualModelInstanceNature = virtualModelInstanceNature;
		this.flexoConceptInstanceNature = flexoConceptInstanceNature;
	}

	/**
	 * Return the technology adapter handling specific natures
	 * 
	 * @return
	 */
	public TA getTechnologyAdapter() {
		return technologyAdapter;
	}

	/**
	 * Return the technology adapter controller handling specific natures
	 * 
	 * @return
	 */
	public TechnologyAdapterController<?> getTechnologyAdapterController() {
		TechnologyAdapterControllerService tacService = getController().getApplicationContext().getTechnologyAdapterControllerService();
		return tacService.getTechnologyAdapterController(getTechnologyAdapter());
	}

	public VirtualModelNature getVirtualModelNature() {
		return virtualModelNature;
	}

	public FlexoConceptNature getFlexoConceptNature() {
		return flexoConceptNature;
	}

	public VirtualModelInstanceNature getVirtualModelInstanceNature() {
		return virtualModelInstanceNature;
	}

	public FlexoConceptInstanceNature getFlexoConceptInstanceNature() {
		return flexoConceptInstanceNature;
	}

	/**
	 * Overrides getIcon
	 * 
	 * @see org.openflexo.view.controller.model.FlexoPerspective#getActiveIcon()
	 */
	@Override
	public ImageIcon getActiveIcon() {
		return getController().iconForObject(getTechnologyAdapter());
	}

	@Override
	public boolean isRepresentableInModuleView(FlexoObject object) {

		TechnologyAdapterControllerService tacService = getController().getApplicationContext().getTechnologyAdapterControllerService();
		for (TechnologyAdapterPluginController<?> plugin : tacService.getActivatedPlugins()) {
			if (plugin.isRepresentableInModuleView(object)) {
				return true;
			}
		}

		if (object instanceof VirtualModel && virtualModelNature != null && virtualModelNature.hasNature((VirtualModel) object)) {
			return true;
		}
		if (object instanceof FlexoConcept && flexoConceptNature != null && flexoConceptNature.hasNature((FlexoConcept) object)) {
			return true;
		}
		if (object instanceof FMLRTVirtualModelInstance && virtualModelInstanceNature != null
				&& virtualModelInstanceNature.hasNature((FMLRTVirtualModelInstance) object)) {
			return true;
		}
		if (object instanceof FlexoConceptInstance && flexoConceptInstanceNature != null
				&& flexoConceptInstanceNature.hasNature((FlexoConceptInstance) object)) {
			return true;
		}
		return super.isRepresentableInModuleView(object);
	}

	@Override
	public FlexoObject getRepresentableMasterObject(FlexoObject object) {
		TechnologyAdapterControllerService tacService = getController().getApplicationContext().getTechnologyAdapterControllerService();
		for (TechnologyAdapterPluginController<?> plugin : tacService.getActivatedPlugins()) {
			if (plugin.isRepresentableInModuleView(object)) {
				return plugin.getRepresentableMasterObject(object);
			}
		}
		if (object instanceof VirtualModel && virtualModelNature != null && virtualModelNature.hasNature((VirtualModel) object)) {
			return object;
		}
		if (object instanceof FlexoConcept && flexoConceptNature != null && flexoConceptNature.hasNature((FlexoConcept) object)) {
			return object;
		}
		if (object instanceof FMLRTVirtualModelInstance && virtualModelInstanceNature != null
				&& virtualModelInstanceNature.hasNature((FMLRTVirtualModelInstance) object)) {
			return object;
		}
		if (object instanceof FlexoConceptInstance && flexoConceptInstanceNature != null
				&& flexoConceptInstanceNature.hasNature((FlexoConceptInstance) object)) {
			return object;
		}
		return super.getRepresentableMasterObject(object);
	}

	@Override
	public final ModuleView<?> createModuleViewForMasterObject(FlexoObject object) {

		TechnologyAdapterControllerService tacService = getController().getApplicationContext().getTechnologyAdapterControllerService();
		for (TechnologyAdapterPluginController<?> plugin : tacService.getActivatedPlugins()) {
			if (plugin.handleObject(object)) {
				return plugin.createModuleViewForMasterObject(object, getController(), this);
			}
		}

		if (object instanceof FMLCompilationUnit) {
			return createModuleViewForCompilationUnit((FMLCompilationUnit) object);
		}
		if (object instanceof FlexoConcept) {
			return createModuleViewForFlexoConcept((FlexoConcept) object);
		}
		if (object instanceof FMLRTVirtualModelInstance) {
			return createModuleViewForVirtualModelInstance((FMLRTVirtualModelInstance) object);
		}
		if (object instanceof FlexoConceptInstance) {
			return createModuleViewForFlexoConceptInstance((FlexoConceptInstance) object);
		}
		return super.createModuleViewForMasterObject(object);
	}

	protected abstract ModuleView<FMLCompilationUnit> createModuleViewForCompilationUnit(FMLCompilationUnit compilationUnit);

	protected abstract ModuleView<FlexoConcept> createModuleViewForFlexoConcept(FlexoConcept flexoConcept);

	protected abstract ModuleView<? extends VirtualModelInstance<?, ?>> createModuleViewForVirtualModelInstance(
			FMLRTVirtualModelInstance vmi);

	protected abstract ModuleView<FlexoConceptInstance> createModuleViewForFlexoConceptInstance(FlexoConceptInstance flexoConceptInstance);

	@Override
	public void willShow() {
		super.willShow();
		if (!getTechnologyAdapter().isActivated()) {
			getTechnologyAdapter().getTechnologyAdapterService().activateTechnologyAdapter(getTechnologyAdapter(), false);
		}
	}

}
