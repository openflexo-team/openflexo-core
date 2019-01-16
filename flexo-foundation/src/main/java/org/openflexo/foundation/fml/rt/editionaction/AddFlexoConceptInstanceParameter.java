/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.fml.rt.editionaction;

import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.ValidationIssue;

/**
 * A parameter used to instanciate a new {@link FlexoConceptInstance} or a new {@link VirtualModelInstance}
 * 
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(AddFlexoConceptInstanceParameter.AddFlexoConceptInstanceParameterImpl.class)
@XMLElement
public interface AddFlexoConceptInstanceParameter extends BehaviourParameter<AbstractAddFlexoConceptInstance<?, ?>> {

	public static abstract class AddFlexoConceptInstanceParameterImpl extends BehaviourParameterImpl<AbstractAddFlexoConceptInstance<?, ?>>
			implements AddFlexoConceptInstanceParameter {

		static final Logger logger = Logger.getLogger(AddFlexoConceptInstanceParameter.class.getPackage().getName());

		@Override
		public FlexoBehaviour getAccessedBehaviour() {
			return getOwner().getCreationScheme();
		}
	}

	@DefineValidationRule
	public static class ValueBindingMustBeValid extends BindingIsRequiredAndMustBeValid<AddFlexoConceptInstanceParameter> {
		public ValueBindingMustBeValid() {
			super("'value'_binding_is_required_and_must_be_valid", AddFlexoConceptInstanceParameter.class);
		}

		@Override
		public DataBinding<?> getBinding(AddFlexoConceptInstanceParameter object) {
			return object.getValue();
		}

		@Override
		public ValidationIssue<BindingIsRequiredAndMustBeValid<AddFlexoConceptInstanceParameter>, AddFlexoConceptInstanceParameter> applyValidation(
				AddFlexoConceptInstanceParameter object) {
			// Should return an issue only if parameter is required

			DataBinding<?> b = getBinding(object);
			if (b == null || !b.isSet()) {
				return new UndefinedRequiredBindingIssue<>(this, object);
			}

			if (object.getParam() != null && object.getParam().getIsRequired()) {
				return super.applyValidation(object);
			}
			return null;
		}

	}

}
