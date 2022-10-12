/**
 * 
 * Copyright (c) 2015, Openflexo
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

import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLMigration;
import org.openflexo.foundation.fml.binding.ControlGraphBindingModel;
import org.openflexo.foundation.fml.binding.DeclarationActionBindingModel;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.Updater;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

@ModelEntity
@ImplementationClass(DeclarationAction.DeclarationActionImpl.class)
@XMLElement
public interface DeclarationAction<T> extends AbstractAssignationAction<T> {

	@PropertyIdentifier(type = String.class)
	public static final String VARIABLE_NAME_KEY = "variableName";
	@PropertyIdentifier(type = Type.class)
	public static final String DECLARED_TYPE_KEY = "declaredType";

	@Getter(value = VARIABLE_NAME_KEY)
	@XMLAttribute(xmlTag = "variable")
	public String getVariableName();

	@Setter(VARIABLE_NAME_KEY)
	public void setVariableName(String variableName);

	@FMLMigration("ignoreForEquality=true to be removed")
	@Getter(value = DECLARED_TYPE_KEY, isStringConvertable = true, ignoreForEquality = true)
	@XMLAttribute
	public Type getDeclaredType();

	@Setter(DECLARED_TYPE_KEY)
	public void setDeclaredType(Type type);

	/**
	 * We define an updater for DECLARED_TYPE property because we need to translate supplied Type to valid TypingSpace
	 * 
	 * @param type
	 */
	@Updater(DECLARED_TYPE_KEY)
	public void updateDeclaredType(Type type);

	public Type getAnalyzedType();

	public Type getType();

	public String getDeclarationTypeAsString();

	public String getFullQualifiedDeclarationTypeAsString();

	public static abstract class DeclarationActionImpl<T> extends AbstractAssignationActionImpl<T> implements DeclarationAction<T> {

		private static final Logger logger = Logger.getLogger(DeclarationAction.class.getPackage().getName());

		private ControlGraphBindingModel<?> inferedBindingModel = null;

		@Override
		public Type getDeclaredType() {
			Type returned = (Type) performSuperGetter(DECLARED_TYPE_KEY);
			if (returned == null) {
				return getAnalyzedType();
			}
			return returned;
		}

		/*@Override
		public void setVariableName(String variableName) {
			if (variableName.equals("model")) {
				setVariableName("_model");
			}
			else {
				performSuperSetter(VARIABLE_NAME_KEY, variableName);
			}
		}*/

		/*@Override
		public void setAssignableAction(AssignableAction<T> assignableAction) {
			performSuperSetter(ASSIGNABLE_ACTION_KEY, assignableAction);
			if (assignableAction instanceof AddVirtualModelInstance) {
				System.out.println("--------> Virer ce truc !!!!");
				Thread.dumpStack();
				ElementImportDeclaration importDeclaration = getFMLModelFactory().newElementImportDeclaration();
				importDeclaration.setResourceReference(
						new DataBinding<>('"' + ((AddVirtualModelInstance) assignableAction).getVirtualModelType().getURI() + '"'));
				getDeclaringCompilationUnit().addToElementImports(importDeclaration);
			}
		}*/

		@Override
		public T execute(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {
			T value = getAssignationValue(evaluationContext);
			evaluationContext.declareVariable(getVariableName(), value);
			return value;
		}

		@Override
		public ControlGraphBindingModel<?> getInferedBindingModel() {
			if (inferedBindingModel == null) {
				inferedBindingModel = makeInferedBindingModel();
			}
			return inferedBindingModel;
		}

		protected ControlGraphBindingModel<?> makeInferedBindingModel() {
			return new DeclarationActionBindingModel(this);
		}

		@Override
		public String getStringRepresentation() {
			if (getAssignableAction() != null) {
				return getHeaderContext() + getDeclarationTypeAsString() + " " + getVariableName() + " = "
						+ getAssignableAction().getStringRepresentation();
			}
			return getHeaderContext() + getDeclarationTypeAsString() + " " + getVariableName() + " = ???";
		}

		@Override
		public Type getAnalyzedType() {
			if (getAssignableAction() != null) {
				return getAssignableAction().getAssignableType();
			}
			return Object.class;
		}

		@Override
		public Type getType() {
			if (getDeclaredType() != null) {
				return getDeclaredType();
			}
			return getAnalyzedType();
		}

		/**
		 * We define an updater for DECLARED_TYPE property because we need to translate supplied Type to valid TypingSpace
		 * 
		 * This updater is called during updateWith() processing (generally applied during the FML parsing phases)
		 * 
		 * @param type
		 */
		@Override
		public void updateDeclaredType(Type type) {

			if (getDeclaringCompilationUnit() != null && type instanceof CustomType) {
				setDeclaredType(((CustomType) type).translateTo(getDeclaringCompilationUnit().getTypingSpace()));
			}
			else {
				setDeclaredType(type);
			}
		}

		@Override
		public String getDeclarationTypeAsString() {
			if (getAssignableAction() != null) {
				return TypeUtils.simpleRepresentation(getAssignableAction().getAssignableType());
			}
			return "null";
		}

		@Override
		public String getFullQualifiedDeclarationTypeAsString() {
			if (getAssignableAction() != null) {
				return TypeUtils.fullQualifiedRepresentation(getAssignableAction().getAssignableType());
			}
			return "null";
		}
	}

	@DefineValidationRule
	public static class DeclaredTypeShouldBeCompatibleWithAnalyzedType
			extends ValidationRule<DeclaredTypeShouldBeCompatibleWithAnalyzedType, DeclarationAction<?>> {

		public DeclaredTypeShouldBeCompatibleWithAnalyzedType() {
			super(DeclarationAction.class, "declared_types_and_analyzed_types_must_be_compatible");
		}

		@Override
		public ValidationIssue<DeclaredTypeShouldBeCompatibleWithAnalyzedType, DeclarationAction<?>> applyValidation(
				DeclarationAction<?> declaration) {

			Type expected = declaration.getDeclaredType();
			Type analyzed = declaration.getAssignableType();
			if (!TypeUtils.isTypeAssignableFrom(expected, analyzed, true)) {
				return new NotCompatibleTypesIssue(this, declaration, expected, analyzed);
			}

			return null;
		}

		public static class NotCompatibleTypesIssue
				extends ValidationError<DeclaredTypeShouldBeCompatibleWithAnalyzedType, DeclarationAction<?>> {

			private Type expectedType;
			private Type analyzedType;

			public NotCompatibleTypesIssue(DeclaredTypeShouldBeCompatibleWithAnalyzedType rule, DeclarationAction<?> anObject,
					Type expected, Type analyzed) {
				super(rule, anObject, "types_are_not_compatible_in_declaration_:_($expectedType)_is_not_assignable_from_($analyzedType)");
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

	// @DefineValidationRule
	// TODO: check variable name and validity
	// TODO: check type compatibility
}
