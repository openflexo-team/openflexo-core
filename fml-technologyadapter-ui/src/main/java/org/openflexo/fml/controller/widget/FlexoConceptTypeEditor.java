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
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptType;
import org.openflexo.gina.annotation.FIBPanel;

/**
 * An editor to edit a {@link FlexoConceptType}
 * 
 * @author sylvain
 * 
 */
@FIBPanel("Fib/CustomType/FlexoConceptTypeEditor.fib")
public class FlexoConceptTypeEditor extends FMLCustomTypeEditor<FlexoConceptType> {

	private FlexoConcept selectedFlexoConcept = null;

	public FlexoConceptTypeEditor(FlexoServiceManager serviceManager) {
		super(serviceManager);
	}

	@Override
	public String getPresentationName() {
		return "FML concept";
	}

	@Override
	public Class<FlexoConceptType> getCustomType() {
		return FlexoConceptType.class;
	}

	public FlexoConcept getSelectedFlexoConcept() {
		return selectedFlexoConcept;
	}

	public void setSelectedFlexoConcept(FlexoConcept selectedFlexoConcept) {
		if ((selectedFlexoConcept == null && this.selectedFlexoConcept != null)
				|| (selectedFlexoConcept != null && !selectedFlexoConcept.equals(this.selectedFlexoConcept))) {
			FlexoConcept oldValue = this.selectedFlexoConcept;
			this.selectedFlexoConcept = selectedFlexoConcept;
			getPropertyChangeSupport().firePropertyChange("selectedFlexoConcept", oldValue, selectedFlexoConcept);
		}
	}

	@Override
	public FlexoConceptType getEditedType() {
		return FlexoConceptType.retrieveFlexoConceptType(getSelectedFlexoConcept());
	}
}
