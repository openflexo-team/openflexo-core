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
package org.openflexo.vpm.view;

import org.openflexo.components.widget.CommonFIB;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * This is the module view representing a standard FlexoConcept (an FlexoConcept which is not a VirtualModel, nor part of a
 * DiagramSpecification)<br>
 * 
 * @author sguerin
 * 
 */
public class StandardFlexoConceptView extends FlexoConceptView<FlexoConcept> {

	public StandardFlexoConceptView(FlexoConcept flexoConcept, FlexoController controller, FlexoPerspective perspective) {
		super(flexoConcept, CommonFIB.STANDARD_FLEXO_CONCEPT_VIEW_FIB_NAME, controller, perspective);
	}

}
