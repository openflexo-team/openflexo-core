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
package org.openflexo.foundation.fml.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.pamela.validation.FixProposal;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * A {@link ValidationRule} checking that binding is required and must be valid
 * 
 * @param <C>
 */
public abstract class BindingIsRequiredAndMustBeValid<C extends FMLObject> extends ValidationRule<BindingIsRequiredAndMustBeValid<C>, C> {

	private static final Logger logger = Logger.getLogger(BindingIsRequiredAndMustBeValid.class.getPackage().getName());

	public BindingIsRequiredAndMustBeValid(String ruleName, Class<C> clazz) {
		super(clazz, ruleName);
	}

	public abstract DataBinding<?> getBinding(C object);

	@Override
	public ValidationIssue<BindingIsRequiredAndMustBeValid<C>, C> applyValidation(C object) {
		DataBinding<?> b = getBinding(object);
		if (b == null || !b.isSet()) {
			return new BindingIsRequiredAndMustBeValid.UndefinedRequiredBindingIssue<>(this, object);
		}
		// We force revalidate the binding to be sure that the binding is valid
		else if (!b.revalidate()) {
			logger.info(getClass().getName() + ": Binding NOT valid: " + b + " for " + object.getStringRepresentation() + ". Reason: "
					+ b.invalidBindingReason());
			// Thread.dumpStack();

			BindingIsRequiredAndMustBeValid.InvalidRequiredBindingIssue<C> returned = new BindingIsRequiredAndMustBeValid.InvalidRequiredBindingIssue<>(
					this, object);

			List<BindingIsRequiredAndMustBeValid.UseProposedBinding<C>> proposals = findProposals(b, object);
			for (BindingIsRequiredAndMustBeValid.UseProposedBinding<C> proposedBinding : proposals) {
				returned.addToFixProposals(proposedBinding);
			}

			return returned;
			// return new ValidationError<BindingIsRequiredAndMustBeValid<C>, C>(this, object,
			// BindingIsRequiredAndMustBeValid.this.getRuleName(), "Binding: " + getBinding(object) + " reason: "
			// + getBinding(object).invalidBindingReason());
		}
		return null;
	}

	public List<BindingIsRequiredAndMustBeValid.UseProposedBinding<C>> findProposals(DataBinding<?> b, C object) {
		List<BindingIsRequiredAndMustBeValid.UseProposedBinding<C>> returned = new ArrayList<>();
		if (object instanceof FlexoConceptObject) {
			String proposal = b.toString();
			if (((FlexoConceptObject) object).getFlexoConcept() instanceof VirtualModel) {
				logger.info("Not valid for VirtualModel " + ((FlexoConceptObject) object).getFlexoConcept() + " " + b);
				proposal = proposal.replace("virtualModelInstance.virtualModelDefinition", "this.virtualModel");
				proposal = proposal.replace("virtualModelInstance", "this");
			}
			else {
				logger.info("Not valid for Concept " + ((FlexoConceptObject) object).getFlexoConcept() + " " + b);
				proposal = proposal.replace("virtualModelInstance", "container");
				proposal = proposal.replace("flexoConceptInstance", "this");
			}
			if (!proposal.equals(b.toString())) {
				logger.info("DataBinding validation: providing proposal " + proposal + " instead of " + b.toString());
				returned.add(new BindingIsRequiredAndMustBeValid.UseProposedBinding<>(b, proposal));
			}
			else {
				// FMLObjectImpl.logger
				// .info("DataBinding validation: cannot find any proposal " + proposal + " instead of " + b.toString());
			}
		}
		return returned;
	}

	protected static class UseProposedBinding<C extends FMLObject> extends FixProposal<BindingIsRequiredAndMustBeValid<C>, C> {

		private DataBinding<?> binding;
		private String proposedValue;

		public UseProposedBinding(DataBinding<?> binding, String proposedValue) {
			super("sets_value_to_($proposedValue)");
			this.binding = binding;
			this.proposedValue = proposedValue;
		}

		public DataBinding<?> getBinding() {
			return binding;
		}

		public String getProposedValue() {
			return proposedValue;
		}

		@Override
		protected void fixAction() {
			binding.setUnparsedBinding(proposedValue);
			// binding.markedAsToBeReanalized();
		}
	}

	public static class UndefinedRequiredBindingIssue<C extends FMLObject> extends ValidationError<BindingIsRequiredAndMustBeValid<C>, C> {

		@SafeVarargs
		public UndefinedRequiredBindingIssue(BindingIsRequiredAndMustBeValid<C> rule, C anObject,
				FixProposal<BindingIsRequiredAndMustBeValid<C>, C>... fixProposals) {
			super(rule, anObject, "binding_'($binding.bindingName)'_is_required_but_was_not_set", fixProposals);
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

	public static class InvalidRequiredBindingIssue<C extends FMLObject> extends ValidationError<BindingIsRequiredAndMustBeValid<C>, C> {

		@SafeVarargs
		public InvalidRequiredBindingIssue(BindingIsRequiredAndMustBeValid<C> rule, C anObject,
				FixProposal<BindingIsRequiredAndMustBeValid<C>, C>... fixProposals) {
			// super(rule, anObject, "binding_'($binding.bindingName)'_is_required_but_value_is_invalid: ($binding)", fixProposals);
			super(rule, anObject, "($reason)", fixProposals);

			/*System.out.println("InvalidRequiredBindingIssue:");
			System.out.println("object: " + anObject);
			System.out.println("binding=" + rule.getBinding(anObject));
			System.out.println("bindable=" + rule.getBinding(anObject).getOwner());
			System.out.println("binding model=" + rule.getBinding(anObject).getOwner().getBindingModel());
			System.out.println("reason=" + rule.getBinding(anObject).invalidBindingReason());
			Thread.dumpStack();*/
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

}
