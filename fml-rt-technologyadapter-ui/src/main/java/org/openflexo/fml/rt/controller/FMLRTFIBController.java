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

import org.openflexo.components.validation.RevalidationTask;
import org.openflexo.fml.rt.controller.validation.FixIssueDialog;
import org.openflexo.fml.rt.controller.validation.IssueFixing;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FMLRTValidationReport;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreateFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.SynchronizationSchemeAction;
import org.openflexo.foundation.fml.rt.action.SynchronizationSchemeActionFactory;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.validation.ProblemIssue;
import org.openflexo.pamela.validation.Validable;
import org.openflexo.pamela.validation.ValidationIssue;
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

	public VirtualModelInstance<?, ?> synchronizeVirtualModelInstance(VirtualModelInstance<?, ?> virtualModelInstance) {
		VirtualModel vm = virtualModelInstance.getVirtualModel();
		if (vm.hasSynchronizationScheme()) {
			SynchronizationScheme ss = vm.getSynchronizationScheme();
			SynchronizationSchemeActionFactory actionType = new SynchronizationSchemeActionFactory(ss, virtualModelInstance);
			SynchronizationSchemeAction action = actionType.makeNewAction(virtualModelInstance, null, getEditor());
			action.doAction();
			return virtualModelInstance;
		}
		logger.warning("No synchronization scheme defined for " + virtualModelInstance.getVirtualModel());
		return null;
	}

	public FIBComponent inspectorForFlexoConceptInstance(FlexoConceptInstance fci) {
		if (getFlexoController() != null && getFlexoController().getModuleInspectorController() != null && fci != null
				&& fci.getInspectedObject() != null && fci.getFlexoConcept() != null) {
			return getFlexoController().getModuleInspectorController().getFIBInspectorPanel(fci.getInspectedObject().getFlexoConcept());
		}
		return null;
	}

	public FlexoConceptInstance createFlexoConceptInstance(FlexoConceptInstance container) {
		System.out.println("create FCI, container=" + container);
		CreateFlexoConceptInstance createFCI = CreateFlexoConceptInstance.actionType.makeNewAction(container, null, getEditor());
		createFCI.doAction();
		return createFCI.getNewFlexoConceptInstance();
	}

	public void deleteFlexoConceptInstance(FlexoConceptInstance conceptInstance) {
		System.out.println("delete FCI, container=" + conceptInstance);
		conceptInstance.delete();
	}

	public void deleteActorReference(FlexoConceptInstance fci, ActorReference<?> actorReference) {
		System.out.println("deleteActorReference with " + fci + " et " + actorReference);
		if (fci != null) {
			fci.removeFromActors(actorReference);
		}
	}

	private boolean showErrorsWarnings = true;

	public boolean showErrorsWarnings() {
		return showErrorsWarnings;
	}

	public void setShowErrorsWarnings(boolean showErrorsWarnings) {
		// System.out.println("setShowErrorsWarnings with " + showErrorsWarnings);
		if (this.showErrorsWarnings != showErrorsWarnings) {
			this.showErrorsWarnings = showErrorsWarnings;
			getPropertyChangeSupport().firePropertyChange("showErrorsWarnings", !showErrorsWarnings, showErrorsWarnings);
		}
	}

	public void showIssue(ValidationIssue<?, ?> issue) {
		if (issue != null) {
			Validable objectToSelect = issue.getValidable();
			// getFlexoController().selectAndFocusObject((FlexoObject) objectToSelect);
			if (objectToSelect instanceof FlexoObject) {
				getFlexoController().getSelectionManager().setSelectedObject((FlexoObject) objectToSelect);
			}
		}
	}

	public void fixIssue(ValidationIssue<?, ?> issue) {
		if (issue instanceof ProblemIssue) {
			VirtualModelInstance vmiToRevalidate = null;
			if (issue.getValidationReport().getRootObject() instanceof VirtualModelInstance) {
				vmiToRevalidate = (VirtualModelInstance) issue.getValidationReport().getRootObject();
			}
			IssueFixing<?, ?> fixing = new IssueFixing<>((ProblemIssue<?, ?>) issue, getFlexoController());
			FixIssueDialog dialog = new FixIssueDialog(fixing, getFlexoController());
			dialog.showDialog();
			if (dialog.getStatus() == Status.VALIDATED) {
				fixing.fix();
				if (vmiToRevalidate != null) {
					revalidate(vmiToRevalidate);
				}
			}
			else if (dialog.getStatus() == Status.NO) {
				fixing.ignore();
			}
		}
	}

	public void revalidate(VirtualModelInstance virtualModelInstance) {
		if (getServiceManager() != null) {
			FMLRTTechnologyAdapterController tac = getServiceManager().getTechnologyAdapterControllerService()
					.getTechnologyAdapterController(FMLRTTechnologyAdapterController.class);
			FMLRTValidationReport virtualModelInstanceReport = (FMLRTValidationReport) tac.getValidationReport(virtualModelInstance, true);
			RevalidationTask validationTask = new RevalidationTask(virtualModelInstanceReport);
			getServiceManager().getTaskManager().scheduleExecution(validationTask);
		}
	}

}
