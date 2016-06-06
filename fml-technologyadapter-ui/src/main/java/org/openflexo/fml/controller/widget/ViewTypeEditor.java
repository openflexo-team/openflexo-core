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
import org.openflexo.foundation.fml.ViewType;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.gina.annotation.FIBPanel;

/**
 * An editor to edit a {@link ViewType}
 * 
 * @author sylvain
 * 
 */
@FIBPanel("Fib/CustomType/ViewTypeEditor.fib")
public class ViewTypeEditor extends AbstractFlexoConceptInstanceTypeEditor<ViewType> {

	private ViewPointResource selectedViewPoint = null;

	public ViewTypeEditor(FlexoServiceManager serviceManager) {
		super(serviceManager);
	}

	@Override
	public String getPresentationName() {
		return "ViewPoint inst.";
	}

	@Override
	public Class<ViewType> getCustomType() {
		return ViewType.class;
	}

	public ViewPointResource getSelectedViewPoint() {
		return selectedViewPoint;
	}

	public void setSelectedViewPoint(ViewPointResource selectedViewPoint) {
		if ((selectedViewPoint == null && this.selectedViewPoint != null)
				|| (selectedViewPoint != null && !selectedViewPoint.equals(this.selectedViewPoint))) {
			ViewPointResource oldValue = this.selectedViewPoint;
			this.selectedViewPoint = selectedViewPoint;
			getPropertyChangeSupport().firePropertyChange("selectedViewPoint", oldValue, selectedViewPoint);
		}
	}

	@Override
	public ViewType getEditedType() {
		if (getSelectedViewPoint() != null) {
			return ViewType.getViewType(getSelectedViewPoint().getViewPoint());
		}
		return ViewType.UNDEFINED_VIEW_TYPE;
	}
}
