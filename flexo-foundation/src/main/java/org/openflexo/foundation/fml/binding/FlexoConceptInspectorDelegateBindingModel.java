/**
 *
 * Copyright (c) 2014-2026, Openflexo
 *
 * This file is part of Flexo-foundation, a component of the software infrastructure
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

package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Type;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;

/**
 * This is the {@link BindingModel} in which the <code>derived</code> key of a {@link FlexoConcept}'s <code>@Inspector(derived=…)</code>
 * declaration is expressed.
 *
 * <p>
 * The expression reads the instance being inspected, exposed as the <code>instance</code> binding variable, on top of the
 * {@link BindingModel} of the concept itself - so <code>@Inspector(derived=myConceptB)</code> resolves <code>myConceptB</code> directly
 * as a role of the concept, exactly as <code>@Renderer(name)</code> resolves a property of it (see
 * {@link FlexoConceptRendererBindingModel}, the same shape for a different annotation).
 *
 * @author sylvain
 *
 */
public class FlexoConceptInspectorDelegateBindingModel extends BindingModel {

	private final FlexoConcept flexoConcept;

	public FlexoConceptInspectorDelegateBindingModel(FlexoConcept flexoConcept) {
		super(flexoConcept.getBindingModel());

		this.flexoConcept = flexoConcept;
		if (flexoConcept.getPropertyChangeSupport() != null) {
			flexoConcept.getPropertyChangeSupport().addPropertyChangeListener(this);
		}

		BindingVariable instanceBV = new BindingVariable(FlexoConcept.DERIVED_INSPECTOR_INSTANCE_PROPERTY,
				FlexoConceptInstanceType.getFlexoConceptInstanceType(flexoConcept)) {
			@Override
			public Type getType() {
				return FlexoConceptInstanceType.getFlexoConceptInstanceType(getFlexoConcept());
			}
		};
		instanceBV.setCacheable(false);
		addToBindingVariables(instanceBV);
	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
		if (flexoConcept != null && flexoConcept.getPropertyChangeSupport() != null) {
			flexoConcept.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == flexoConcept && FlexoConcept.PARENT_FLEXO_CONCEPTS_KEY.equals(evt.getPropertyName())) {
			setBaseBindingModel(flexoConcept.getBindingModel());
		}
	}

	public FlexoConcept getFlexoConcept() {
		return flexoConcept;
	}
}
