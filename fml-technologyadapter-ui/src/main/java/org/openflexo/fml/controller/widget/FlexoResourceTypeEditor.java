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

package org.openflexo.fml.controller.widget;

import java.util.ArrayList;
import java.util.List;

import org.openflexo.ApplicationContext;
import org.openflexo.components.widget.DefaultCustomTypeEditorImpl;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.resource.FlexoResourceType;
import org.openflexo.foundation.resource.ITechnologySpecificFlexoResourceFactory;
import org.openflexo.gina.ApplicationFIBLibrary.ApplicationFIBLibraryImpl;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.view.GinaViewFactory;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.FlexoFIBController;

/**
 * An editor to edit a {@link FlexoConceptInstanceType}
 * 
 * @author sylvain
 * 
 */
@FIBPanel("Fib/CustomType/FlexoResourceTypeEditor.fib")
public class FlexoResourceTypeEditor extends DefaultCustomTypeEditorImpl<FlexoResourceType> {

	private List<FlexoConcept> matchingValues = new ArrayList<>();

	private ITechnologySpecificFlexoResourceFactory<?, ?, ?> selectedResourceFactory = null;

	public FlexoResourceTypeEditor(FlexoServiceManager serviceManager) {
		super(serviceManager);
	}

	@Override
	public String getPresentationName() {
		return "Resource";
	}

	@Override
	public Class<FlexoResourceType> getCustomType() {
		return FlexoResourceType.class;
	}

	public ITechnologySpecificFlexoResourceFactory<?, ?, ?> getSelectedResourceFactory() {
		return selectedResourceFactory;
	}

	public void setSelectedResourceFactory(ITechnologySpecificFlexoResourceFactory<?, ?, ?> selectedResourceFactory) {

		System.out.println("selected: " + selectedResourceFactory);
		System.out.println("resource: " + selectedResourceFactory.getResourceClass());
		if ((selectedResourceFactory == null && this.selectedResourceFactory != null)
				|| (selectedResourceFactory != null && !selectedResourceFactory.equals(this.selectedResourceFactory))) {
			ITechnologySpecificFlexoResourceFactory<?, ?, ?> oldValue = this.selectedResourceFactory;
			this.selectedResourceFactory = selectedResourceFactory;
			getPropertyChangeSupport().firePropertyChange("selectedResourceFactory", oldValue, selectedResourceFactory);
			// System.out.println("class:" + selectedResourceFactory.getResourceClass());
		}
	}

	@Override
	public FlexoResourceType getEditedType() {
		return FlexoResourceType.getFlexoResourceType(selectedResourceFactory);
	}

	public List<FlexoConcept> getMatchingValues() {
		return matchingValues;
	}

	public boolean isFiltered() {
		return false;
	}

	public Object getRootObject() {
		return getServiceManager().getTechnologyAdapterService();
	}

	@Override
	public FlexoFIBController makeFIBController() {
		FIBComponent component = ApplicationFIBLibraryImpl.instance().retrieveFIBComponent(getFIBComponentResource());
		FlexoController controller = ((ApplicationContext) getServiceManager()).getModuleLoader().getActiveModule().getController();

		return new ResourceTypeSelectorFIBController(component, this, controller);
	}

	public static class ResourceTypeSelectorFIBController extends SelectorFIBController {
		public ResourceTypeSelectorFIBController(FIBComponent component, GinaViewFactory<?> viewFactory) {
			super(component, viewFactory);
		}

		public ResourceTypeSelectorFIBController(FIBComponent component, DefaultCustomTypeEditorImpl editor, FlexoController controller) {
			super(component, editor, controller);
		}

		@Override
		public void doubleClick(Object object) {
			super.doubleClick(object);
		}
	}

}
