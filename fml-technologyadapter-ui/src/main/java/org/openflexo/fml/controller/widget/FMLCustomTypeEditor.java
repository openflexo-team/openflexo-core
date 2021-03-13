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

import org.openflexo.components.widget.DefaultCustomTypeEditorImpl;
import org.openflexo.connie.type.CustomType;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.gina.model.widget.FIBCustom.FIBCustomComponent.CustomComponentParameter;

/**
 * An editor to edit a {@link FlexoConceptInstanceType}
 * 
 * @author sylvain
 * 
 */
public abstract class FMLCustomTypeEditor<T extends CustomType> extends DefaultCustomTypeEditorImpl<T> {

	private List<FlexoConcept> matchingValues = new ArrayList<>();

	public FMLCustomTypeEditor(FlexoServiceManager serviceManager) {
		super(serviceManager);
	}

	public List<FlexoConcept> getMatchingValues() {
		return matchingValues;
	}

	public boolean isFiltered() {
		return false;
	}

	public VirtualModelLibrary getViewPointLibrary() {
		if (getServiceManager() != null) {
			return getServiceManager().getVirtualModelLibrary();
		}
		return null;
	}

	private VirtualModel virtualModel;

	public VirtualModel getVirtualModel() {
		return virtualModel;
	}

	@CustomComponentParameter(name = "virtualModel", type = CustomComponentParameter.Type.OPTIONAL)
	public void setVirtualModel(VirtualModel virtualModel) {

		if (this.virtualModel != virtualModel) {
			FlexoObject oldRoot = getRootObject();
			this.virtualModel = virtualModel;
			getPropertyChangeSupport().firePropertyChange("rootObject", oldRoot, getRootObject());
		}
	}

	public FlexoObject getRootObject() {
		if (getVirtualModel() != null) {
			return getVirtualModel();
		}
		else {
			return getViewPointLibrary();
		}
	}

}
