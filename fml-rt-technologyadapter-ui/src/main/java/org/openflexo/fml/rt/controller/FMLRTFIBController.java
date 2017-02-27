/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-rt-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.rt.controller;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.SynchronizationSchemeAction;
import org.openflexo.foundation.fml.rt.action.SynchronizationSchemeActionType;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * Represents the controller used in FML@runtime technology adapter <br>
 * Extends FlexoFIBController by supporting features relative to FML@runtime technology adapter
 * 
 * 
 * @author sylvain
 */
public class FMLRTFIBController extends FlexoFIBController {

	protected static final Logger logger = FlexoLogger.getLogger(FMLRTFIBController.class.getPackage().getName());

	public FMLRTFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
		super(component, viewFactory);
	}

	public FMLRTFIBController(FIBComponent component, GinaViewFactory<?> viewFactory, FlexoController controller) {
		super(component, viewFactory, controller);
	}

	public VirtualModelInstance synchronizeVirtualModelInstance(VirtualModelInstance virtualModelInstance) {
		VirtualModel vm = virtualModelInstance.getVirtualModel();
		if (vm.hasSynchronizationScheme()) {
			SynchronizationScheme ss = vm.getSynchronizationScheme();
			SynchronizationSchemeActionType actionType = new SynchronizationSchemeActionType(ss, virtualModelInstance);
			SynchronizationSchemeAction action = actionType.makeNewAction(virtualModelInstance, null, getEditor());
			action.doAction();
			return virtualModelInstance;
		}
		logger.warning("No synchronization scheme defined for " + virtualModelInstance.getVirtualModel());
		return null;
	}

	public FIBComponent inspectorForFlexoConceptInstance(FlexoConceptInstance fci) {
		/*if (action == null) {
			return null;
		}
		if (action instanceof TechnologySpecificAction && ((TechnologySpecificAction<?, ?>) action).getModelSlot() != null) {
			TechnologyAdapter technologyAdapter = ((TechnologySpecificAction<?, ?>) action).getModelSlot().getModelSlotTechnologyAdapter();
			if (technologyAdapter != null) {
				TechnologyAdapterController<?> taController = getFlexoController().getTechnologyAdapterController(technologyAdapter);
				return taController.getFIBPanelForObject(action);
			}
			else
				// No specific TechnologyAdapter, lookup in generic libraries
				return getFIBPanelForObject(action);
		}
		else {
			// No specific TechnologyAdapter, lookup in generic libraries
			return getFIBPanelForObject(action);
		}*/
		System.out.println("Tiens faudrait trouver l'inspecteur de " + fci);
		if (getFlexoController() != null && getFlexoController().getModuleInspectorController() != null && fci != null) {
			System.out.println("moduleInspectorController=" + getFlexoController().getModuleInspectorController());
			return getFlexoController().getModuleInspectorController().getFIBInspectorPanel(fci.getFlexoConcept());
		}
		return null;

	}

}
