package org.openflexo.ve.controller;

import java.util.logging.Logger;

import org.openflexo.fib.model.FIBComponent;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fmlrt.VirtualModelInstance;
import org.openflexo.foundation.fmlrt.action.SynchronizationSchemeAction;
import org.openflexo.foundation.fmlrt.action.SynchronizationSchemeActionType;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * Represents the controller used in VE (ViewEditor)<br>
 * Extends FlexoFIBController by supporting features relative to VE module
 * 
 * 
 * @author sylvain
 */
public class VEFIBController extends FlexoFIBController {

	protected static final Logger logger = FlexoLogger.getLogger(VEFIBController.class.getPackage().getName());

	public VEFIBController(FIBComponent component) {
		super(component);
	}

	public VEFIBController(FIBComponent component, FlexoController controller) {
		super(component, controller);
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

}
