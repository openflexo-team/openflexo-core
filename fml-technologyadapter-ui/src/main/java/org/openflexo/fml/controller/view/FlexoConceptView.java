/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.view;

import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.openflexo.fml.controller.FMLTechnologyAdapterController;
import org.openflexo.fml.controller.widget.FIBCompilationUnitBrowser;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.editionaction.AbstractAssignationAction;
import org.openflexo.foundation.fml.editionaction.FetchRequestCondition;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
import org.openflexo.gina.swing.view.widget.JFIBBrowserWidget;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.FIBModuleView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * This is the module view representing an FlexoConcept<br>
 * Because an FlexoConcept can be of multiple forms, this class is abstract and must be subclassed with a specific FIB
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public abstract class FlexoConceptView extends FIBModuleView<FlexoConcept> {

	private static final Logger logger = Logger.getLogger(FlexoConceptView.class.getPackage().getName());

	private final FlexoPerspective perspective;

	public FlexoConceptView(FlexoConcept flexoConcept, Resource fibFile, FlexoController controller, FlexoPerspective perspective) {
		super(flexoConcept, controller, fibFile, controller.getTechnologyAdapter(FMLTechnologyAdapter.class).getLocales());
		this.perspective = perspective;

		// Fixed CORE-101 FlexoConceptView does not display FlexoConcept at creation
		// SGU: I don't like this design, but i don't see other solutions unless getting deeply in the code: not enough time yet
		getFIBView().getController().objectAddedToSelection(flexoConcept);

	}

	public FlexoConceptView(FlexoConcept flexoConcept, String fibFileName, FlexoController controller, FlexoPerspective perspective,
			LocalizedDelegate locales) {
		super(flexoConcept, controller, ResourceLocator.locateResource(fibFileName), locales);
		this.perspective = perspective;
	}

	@Override
	public FlexoPerspective getPerspective() {
		return perspective;
	}

	@Override
	public void fireObjectSelected(FlexoObject object) {

		if (object == getRepresentedObject()) {
			getFIBView().getController().objectAddedToSelection(object);
		}
		else {

			if (object instanceof InspectorEntry) {
				object = ((InspectorEntry) object).getInspector();
			}
			if (object instanceof FetchRequestCondition) {
				object = ((FetchRequestCondition) object).getAction();
			}
			if (object instanceof FMLControlGraph && ((FMLControlGraph) object).getOwner() instanceof AbstractAssignationAction
					&& ((AbstractAssignationAction<?>) ((FMLControlGraph) object).getOwner()).getAssignableAction() == object) {
				// Special case for actions that are beeing represented by a single BrowserCell
				super.fireObjectSelected(((FMLControlGraph) object).getOwner());
			}
			else if (object instanceof FMLControlGraph && ((FMLControlGraph) object).getOwner() instanceof IterationAction
					&& ((IterationAction) ((FMLControlGraph) object).getOwner()).getIterationAction() == object) {
				// Special case for actions that are beeing represented by a single BrowserCell
				super.fireObjectSelected(((FMLControlGraph) object).getOwner());
			}
			else {
				super.fireObjectSelected(object);
			}
		}
	}

	@Override
	public void fireObjectDeselected(FlexoObject object) {
		// System.out.println("Object deselected: " + object);
		super.fireObjectDeselected(object);
	}

	public FIBCompilationUnitBrowser getCompilationUnitBrowser() {
		FMLTechnologyAdapterController technologyAdapterController = (FMLTechnologyAdapterController) getFlexoController()
				.getTechnologyAdapterController(FMLTechnologyAdapter.class);
		return technologyAdapterController.getCompilationUnitBrowser();
	}

	@Override
	public void willShow() {
		super.willShow();
		getCompilationUnitBrowser().setCompilationUnit(getRepresentedObject().getDeclaringCompilationUnit());
		getPerspective().setBottomLeftView(getCompilationUnitBrowser());
		SwingUtilities.invokeLater(() -> {
			if (getFIBView("flexoConceptBrowser") instanceof JFIBBrowserWidget) {
				JFIBBrowserWidget<FMLObject> browser = (JFIBBrowserWidget<FMLObject>) getFIBView("flexoConceptBrowser");
				browser.performExpand(getRepresentedObject().getStructuralFacet());
				browser.performExpand(getRepresentedObject().getBehaviouralFacet());
				browser.performExpand(getRepresentedObject().getInnerConceptsFacet());
			}
		});
	}

	@Override
	public void willHide() {
		super.willHide();
		getPerspective().setBottomLeftView(null);
	}

}
