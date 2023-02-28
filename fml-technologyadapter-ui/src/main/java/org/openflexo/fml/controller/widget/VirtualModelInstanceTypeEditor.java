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

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.gina.annotation.FIBPanel;

/**
 * An editor to edit a {@link VirtualModelInstanceType}
 * 
 * @author sylvain
 * 
 */
@FIBPanel("Fib/CustomType/VirtualModelInstanceTypeEditor.fib")
public class VirtualModelInstanceTypeEditor extends FMLCustomTypeEditor<VirtualModelInstanceType> {

	private CompilationUnitResource selectedVirtualModel = null;

	public VirtualModelInstanceTypeEditor(FlexoServiceManager serviceManager) {
		super(serviceManager);
	}

	@Override
	public String getPresentationName() {
		return AbstractFMLTypingSpace.MODEL_INSTANCE;
	}

	@Override
	public Class<VirtualModelInstanceType> getCustomType() {
		return VirtualModelInstanceType.class;
	}

	public CompilationUnitResource getSelectedVirtualModel() {
		return selectedVirtualModel;
	}

	public void setSelectedVirtualModel(CompilationUnitResource selectedVirtualModel) {
		if ((selectedVirtualModel == null && this.selectedVirtualModel != null)
				|| (selectedVirtualModel != null && !selectedVirtualModel.equals(this.selectedVirtualModel))) {
			CompilationUnitResource oldValue = this.selectedVirtualModel;
			this.selectedVirtualModel = selectedVirtualModel;
			getPropertyChangeSupport().firePropertyChange("selectedVirtualModel", oldValue, selectedVirtualModel);
			System.out.println("on selectionne " + selectedVirtualModel);
		}
	}

	@Override
	public VirtualModelInstanceType getEditedType() {
		if (getSelectedVirtualModel() != null) {
			return VirtualModelInstanceType.getVirtualModelInstanceType(getSelectedVirtualModel().getCompilationUnit().getVirtualModel());
		}
		return VirtualModelInstanceType.UNDEFINED_VIRTUAL_MODEL_INSTANCE_TYPE;
	}
}
