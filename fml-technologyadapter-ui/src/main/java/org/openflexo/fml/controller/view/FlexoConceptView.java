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
package org.openflexo.fml.controller.view;

import java.util.logging.Logger;

import org.openflexo.fib.view.container.FIBPanelView;
import org.openflexo.fib.view.container.FIBTabPanelView;
import org.openflexo.fib.view.widget.FIBBrowserWidget;
import org.openflexo.fib.view.widget.FIBTableWidget;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
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
public abstract class FlexoConceptView<EP extends FlexoConcept> extends FIBModuleView<EP> {

	private static final Logger logger = Logger.getLogger(FlexoConceptView.class.getPackage().getName());

	private final FlexoPerspective perspective;

	public FlexoConceptView(EP flexoConcept, Resource fibFile, FlexoController controller, FlexoPerspective perspective) {
		super(flexoConcept, controller, fibFile);
		this.perspective = perspective;
	}

	public FlexoConceptView(EP flexoConcept, String fibFileName, FlexoController controller, FlexoPerspective perspective) {
		super(flexoConcept, controller, ResourceLocator.locateResource(fibFileName));
		this.perspective = perspective;
	}

	@Override
	public FlexoPerspective getPerspective() {
		return perspective;
	}

	public void tryToSelect(FlexoConceptObject object) {

		FIBTableWidget flexoRoleTable = (FIBTableWidget) getFIBView("FlexoRoleTable");
		FIBTabPanelView mainTabPanel = (FIBTabPanelView) getFIBView("MainTabPanel");
		FIBTableWidget flexoBehaviourTable = (FIBTableWidget) getFIBView("FlexoBehaviourTable");
		FIBPanelView flexoBehaviourPanel = (FIBPanelView) getFIBView("FlexoBehaviourPanel");
		FIBTableWidget parametersTable = (FIBTableWidget) getFIBView("ParametersTable");
		FIBBrowserWidget editionActionBrowser = (FIBBrowserWidget) getFIBView("EditionActionBrowser");
		FIBTableWidget inspectorPropertyTable = (FIBTableWidget) getFIBView("InspectorPropertyTable");
		FIBTableWidget localizedTable = (FIBTableWidget) getFIBView("EntryTable");

		if (object instanceof FlexoRole) {
			if (flexoRoleTable != null) {
				flexoRoleTable.setSelected(object);
			}
		} else if (object instanceof FlexoBehaviour) {
			if (mainTabPanel != null) {
				mainTabPanel.setSelectedIndex(0);
			}
			if (flexoBehaviourTable != null) {
				flexoBehaviourTable.setSelected(object);
			}
		} else if (object instanceof FlexoBehaviourParameter) {
			if (mainTabPanel != null) {
				mainTabPanel.setSelectedIndex(0);
			}
			if (flexoBehaviourTable != null) {
				flexoBehaviourTable.setSelected(((FlexoBehaviourParameter) object).getFlexoBehaviour());
			}
			if (parametersTable != null) {
				parametersTable.setSelected(object);
			}
			// this is not a tab any more
			// editionSchemePanel.setSelectedIndex(0);
		} else if (object instanceof EditionAction) {
			if (mainTabPanel != null) {
				mainTabPanel.setSelectedIndex(0);
			}
			if (flexoBehaviourTable != null) {
				flexoBehaviourTable.setSelected(((EditionAction) object).getFlexoBehaviour());
			}
			// this is not a tab any more
			// editionSchemePanel.setSelectedIndex(1);
			if (editionActionBrowser != null) {
				editionActionBrowser.setSelected(object);
			}
		} else if (object instanceof FlexoConceptInspector) {
			if (mainTabPanel != null) {
				mainTabPanel.setSelectedIndex(1);
			}
		} else if (object instanceof InspectorEntry) {
			if (mainTabPanel != null) {
				mainTabPanel.setSelectedIndex(1);
			}
			if (inspectorPropertyTable != null) {
				inspectorPropertyTable.setSelected(object);
			}
		}
	}
}
