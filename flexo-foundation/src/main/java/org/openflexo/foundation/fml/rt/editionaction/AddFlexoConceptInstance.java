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

import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;

/**
 * This action is used to explicitely instanciate a new {@link FlexoConceptInstance} in a given {@link VirtualModelInstance} with some
 * parameters
 * 
 * @author sylvain
 * 
 * @param <M>
 * @param <MM>
 */

@ModelEntity
@ImplementationClass(AddFlexoConceptInstance.AddFlexoConceptInstanceImpl.class)
@XMLElement
@FML("AddFlexoConceptInstance")
public interface AddFlexoConceptInstance<VMI extends VirtualModelInstance<VMI, ?>>
		extends AbstractAddFlexoConceptInstance<FlexoConceptInstance, VMI> {

	public static abstract class AddFlexoConceptInstanceImpl<VMI extends VirtualModelInstance<VMI, ?>>
			extends AbstractAddFlexoConceptInstanceImpl<FlexoConceptInstance, VMI> implements AddFlexoConceptInstance<VMI> {

		static final Logger logger = Logger.getLogger(AddFlexoConceptInstance.class.getPackage().getName());

		@Override
		public FlexoConceptInstance execute(RunTimeEvaluationContext evaluationContext) {
			return super.execute(evaluationContext);
		}

		@Override
		public Class<VMI> getVirtualModelInstanceClass() {
			return (Class) VirtualModelInstance.class;
		}

		@Override
		protected FlexoConceptInstance makeNewFlexoConceptInstance(RunTimeEvaluationContext evaluationContext) {
			FlexoConceptInstance container = null;
			VMI vmi = getVirtualModelInstance(evaluationContext);

			if (getFlexoConceptType().getContainerFlexoConcept() != null) {
				container = getContainer(evaluationContext);
				if (container == null) {
					logger.warning("null container while creating new concept " + getFlexoConceptType());
					return null;
				}
			}

			return vmi.makeNewFlexoConceptInstance(getFlexoConceptType(), container);
		}
	}

	@DefineValidationRule
	public static class AddFlexoConceptInstanceMustAddressACreationScheme
			extends ValidationRule<AddFlexoConceptInstanceMustAddressACreationScheme, AddFlexoConceptInstance> {
		public AddFlexoConceptInstanceMustAddressACreationScheme() {
			super(AddFlexoConceptInstance.class, "add_flexo_concept_action_must_address_a_valid_creation_scheme");
		}

		@Override
		public ValidationIssue<AddFlexoConceptInstanceMustAddressACreationScheme, AddFlexoConceptInstance> applyValidation(
				AddFlexoConceptInstance action) {
			if (action.getCreationScheme() == null) {
				if (action.getFlexoConceptType() == null) {
					return new ValidationError<AddFlexoConceptInstanceMustAddressACreationScheme, AddFlexoConceptInstance>(this, action,
							"add_flexo_concept_action_doesn't_define_any_flexo_concept");
				}
				else {
					return new ValidationError<AddFlexoConceptInstanceMustAddressACreationScheme, AddFlexoConceptInstance>(this, action,
							"add_flexo_concept_action_doesn't_define_any_creation_scheme");
				}
			}
			return null;
		}
	}

}
