package org.openflexo.foundation.fml.validation;

/**
 * 
 * Copyright (c) 2014-2025, Openflexo
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
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.pamela.validation.FixProposal;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * A {@link ValidationRule} checking that binding must be valid
 * 
 * @param <C>
 */
public abstract class BindingMustBeValid<C extends FMLObject> extends ValidationRule<BindingMustBeValid<C>, C> {

	private static final Logger logger = Logger.getLogger(BindingMustBeValid.class.getPackage().getName());

	public BindingMustBeValid(String ruleName, Class<C> clazz) {
		super(clazz, ruleName);
	}

	public abstract DataBinding<?> getBinding(C object);

	@Override
	public ValidationIssue<BindingMustBeValid<C>, C> applyValidation(C object) {
		if (getBinding(object) != null && getBinding(object).isSet()) {
			// We force revalidate the binding to be sure that the binding is valid
			if (!getBinding(object).revalidate()) {
				logger.info("Binding NOT valid: " + getBinding(object) + " for " + object.getStringRepresentation() + ". Reason: "
						+ getBinding(object).invalidBindingReason());
				BindingMustBeValid.DeleteBinding<C> deleteBinding = new BindingMustBeValid.DeleteBinding<>(this);
				// return new ValidationError<BindingMustBeValid<C>, C>(this, object, BindingMustBeValid.this.getRuleName(), "Binding: "
				// + getBinding(object) + " reason: " + getBinding(object).invalidBindingReason(), deleteBinding);
				return new BindingMustBeValid.InvalidBindingIssue<>(this, object, deleteBinding);
			}
		}
		return null;
	}

	public static class InvalidBindingIssue<C extends FMLObject> extends ValidationError<BindingMustBeValid<C>, C> {

		@SafeVarargs
		public InvalidBindingIssue(BindingMustBeValid<C> rule, C anObject, FixProposal<BindingMustBeValid<C>, C>... fixProposals) {
			// super(rule, anObject, "binding_'($binding.bindingName)'_is_not_valid: ($binding)", fixProposals);
			super(rule, anObject, "invalid_value_'($binding.bindingName)': ($reason)", fixProposals);
		}

		public DataBinding<?> getBinding() {
			return getCause().getBinding(getValidable());
		}

		public String getReason() {
			return getBinding().invalidBindingReason();
		}

		@Override
		public String getDetailedInformations() {
			return "($reason)";
		}
	}

	protected static class DeleteBinding<C extends FMLObject> extends FixProposal<BindingMustBeValid<C>, C> {

		private final BindingMustBeValid<C> rule;

		public DeleteBinding(BindingMustBeValid<C> rule) {
			super("delete_this_binding");
			this.rule = rule;
		}

		@Override
		protected void fixAction() {
			rule.getBinding(getValidable()).reset();
		}

	}
}
