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

package org.openflexo.foundation.fml.editionaction;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.expr.BindingPath;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.validation.BindingIsRequiredAndMustBeValid;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * The FML assignation statement {@code target = value;}.
 * <p>
 * The target ({@link #getAssignation()}) is a binding which can be written: a property of the enclosing concept, a local variable, or a
 * property of another object ({@code other.label}). The value is computed by the right-hand side ({@link #getAssignableAction()}), an
 * expression or an FML action such as a fetch request. Execution returns the assigned value.
 * <p>
 * Only the {@code =} operator is supported: in a statement such as {@code n += 2;}, the operator is ignored (known defect
 * {@code CORE-D-7}). A collection cannot be assigned to a role of multiple cardinality: add the items one by one.
 * <p>
 * Example (excerpt of {@code FML/Library.fml} in the {@code flexo-test-resources} test resource center):
 *
 * <pre>
 * create(required String label, int capacity=10) {
 *     label = parameters.label;
 *     capacity = parameters.capacity;
 * }
 * </pre>
 *
 * @param <T>
 *            type of the assigned value
 */
@ModelEntity
@ImplementationClass(AssignationAction.AssignationActionImpl.class)
@XMLElement
// TODO: manage assignment operator
public interface AssignationAction<T> extends AbstractAssignationAction<T> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String ASSIGNATION_KEY = "assignation";

	@Getter(value = ASSIGNATION_KEY)
	@XMLAttribute(xmlTag = "assign")
	public DataBinding<? super T> getAssignation();

	@Setter(ASSIGNATION_KEY)
	public void setAssignation(DataBinding<? super T> assignation);

	/**
	 * Return the property of the enclosing concept assigned by this action, when the target is the bare name of such a property
	 *
	 * @return the assigned property; null when the target is not a bare property name (a local variable, {@code this.label},
	 *         {@code other.label}...)
	 */
	@Override
	public FlexoProperty<T> getAssignedFlexoProperty();

	public static abstract class AssignationActionImpl<T> extends AbstractAssignationActionImpl<T> implements AssignationAction<T> {

		private static final Logger logger = Logger.getLogger(AssignationAction.class.getPackage().getName());

		private DataBinding<? super T> assignation;

		@Override
		public DataBinding<? super T> getAssignation() {
			if (assignation == null) {
				assignation = new DataBinding<Object>(this, Object.class, DataBinding.BindingDefinitionType.GET_SET);
				assignation.setBindingName("assignation");
				assignation.setMandatory(true);

			}
			return assignation;
		}

		@Override
		public void setAssignation(DataBinding<? super T> assignation) {
			if (assignation != null) {
				this.assignation = assignation;
				this.assignation.setOwner(this);
				this.assignation.setBindingName("assignation");
				this.assignation.setMandatory(true);
				this.assignation.setDeclaredType(Object.class);
				this.assignation.setBindingDefinitionType(BindingDefinitionType.GET_SET);
			}
			notifiedBindingChanged(this.assignation);
		}

		@Override
		public FlexoProperty<T> getAssignedFlexoProperty() {
			if (getFlexoConcept() == null) {
				return null;
			}
			if (assignation != null && assignation.isValid() && assignation.isBindingPath()) {
				BindingPath bindingPath = (BindingPath) assignation.getExpression();
				if (bindingPath.isValid() && bindingPath.getBindingPath().size() == 0) {
					return (FlexoProperty<T>) getFlexoConcept().getAccessibleProperty(bindingPath.getVariableName());
				}
			}
			return null;
		}

		@Override
		public T execute(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {
			T value = getAssignationValue(evaluationContext);
			try {
				getAssignation().setBindingValue(value, evaluationContext);
			} catch (Exception e) {
				logger.warning("Unexpected assignation issue, " + getAssignation() + " value=" + value + " exception: " + e);
				e.printStackTrace();
				throw new FMLExecutionException(e);
			}
			return value;
		}

		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + (getAssignation() != null ? getAssignation().toString() : "") + " = "
					+ (getAssignableAction() != null ? getAssignableAction().getStringRepresentation() : "<no_assignable_action>");
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getAssignation().rebuild();
		}

	}

	@DefineValidationRule
	public static class AssignationBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<AssignationAction> {
		public AssignationBindingIsRequiredAndMustBeValid() {
			super("'assignation'_binding_is_required_and_must_be_valid", AssignationAction.class);
		}

		@Override
		public DataBinding<Object> getBinding(AssignationAction object) {
			return object.getAssignation();
		}

	}

	@DefineValidationRule
	public static class AssignedTypeMustBeCompatible extends ValidationRule<AssignedTypeMustBeCompatible, AssignationAction<?>> {
		public AssignedTypeMustBeCompatible() {
			super(AssignationAction.class, "assigned_type_must_be_compatible");
		}

		@Override
		public ValidationIssue<AssignedTypeMustBeCompatible, AssignationAction<?>> applyValidation(AssignationAction<?> assignation) {

			Type expected = assignation.getAssignation().getAnalyzedType();
			Type analyzed = assignation.getAssignableType();

			if (!TypeUtils.isTypeAssignableFrom(expected, analyzed, true)) {
				return new NotCompatibleTypesIssue(this, assignation, expected, analyzed);
			}

			return null;
		}

		public static class NotCompatibleTypesIssue extends ValidationError<AssignedTypeMustBeCompatible, AssignationAction<?>> {

			private Type expectedType;
			private Type analyzedType;

			public NotCompatibleTypesIssue(AssignedTypeMustBeCompatible rule, AssignationAction<?> anObject, Type expected, Type analyzed) {
				super(rule, anObject, "types_are_not_compatible_in_assignation_:_($expectedType)_is_not_assignable_from_($analyzedType)");
				this.analyzedType = analyzed;
				this.expectedType = expected;
			}

			public String getExpectedType() {
				return TypeUtils.simpleRepresentation(expectedType);
			}

			public String getAnalyzedType() {
				return TypeUtils.simpleRepresentation(analyzedType);
			}

		}

	}

}
