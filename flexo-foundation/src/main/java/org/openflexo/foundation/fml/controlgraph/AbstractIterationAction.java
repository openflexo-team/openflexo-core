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

package org.openflexo.foundation.fml.controlgraph;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.type.ConnieType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.FMLMigration;
import org.openflexo.foundation.fml.binding.IterationActionBindingModel;
import org.openflexo.foundation.fml.validation.TypeMustBeResolved;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Embedded;
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

@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractIterationAction.AbstractIterationActionImpl.class)
public interface AbstractIterationAction extends ControlStructureAction, FMLControlGraphOwner {

	@PropertyIdentifier(type = String.class)
	public static final String ITERATOR_NAME_KEY = "iteratorName";
	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String CONTROL_GRAPH_KEY = "controlGraph";
	@PropertyIdentifier(type = Type.class)
	public static final String DECLARED_TYPE_KEY = "declaredType";

	@Getter(value = ITERATOR_NAME_KEY)
	@XMLAttribute
	public String getIteratorName();

	@Setter(ITERATOR_NAME_KEY)
	public void setIteratorName(String iteratorName);

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

	/**
	 * Return type of item, which is the declared type if explicitely defined, or the analyzed type if type is not specified and this
	 * infered
	 * 
	 * @return
	 */
	public Type getItemType();

	/**
	 * Return infered item type (type of item on which we iterate)
	 * 
	 * @return
	 */
	public Type getAnalyzedType();

	/**
	 * Returns the control graph on which we iterate
	 * 
	 * @return
	 */
	@Getter(value = CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@CloningStrategy(StrategyType.CLONE)
	@XMLElement(context = "ControlGraph_")
	@Embedded
	public FMLControlGraph getControlGraph();

	@Setter(CONTROL_GRAPH_KEY)
	public void setControlGraph(FMLControlGraph aControlGraph);

	public static abstract class AbstractIterationActionImpl extends ControlStructureActionImpl implements AbstractIterationAction {

		@SuppressWarnings("unused")
		private static final Logger logger = Logger.getLogger(AbstractIterationAction.class.getPackage().getName());

		private String iteratorName = "item";

		@Override
		public String getIteratorName() {
			return iteratorName;
		}

		@Override
		public void setIteratorName(String iteratorName) {
			if (this.iteratorName == null || !this.iteratorName.equals(iteratorName)) {
				String oldValue = this.iteratorName;
				this.iteratorName = iteratorName;
				// rebuildInferedBindingModel();
				getPropertyChangeSupport().firePropertyChange(ITERATOR_NAME_KEY, oldValue, iteratorName);
			}
		}

		@Override
		public final Type getItemType() {
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

			if (getDeclaringCompilationUnit() != null && type instanceof ConnieType) {
				setDeclaredType(((ConnieType) type).translateTo(getDeclaringCompilationUnit().getTypingSpace()));
			}
			else {
				setDeclaredType(type);
			}
		}

		@Override
		protected IterationActionBindingModel makeInferedBindingModel() {
			return new IterationActionBindingModel(this);
		}

		@Override
		public void reduce() {
			if (getControlGraph() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getControlGraph()).reduce();
			}
		}

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			if (CONTROL_GRAPH_KEY.equals(ownerContext)) {
				return getControlGraph();
			}
			return null;
		}

		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {

			if (CONTROL_GRAPH_KEY.equals(ownerContext)) {
				setControlGraph(controlGraph);
			}
		}

		@Override
		public void setControlGraph(FMLControlGraph aControlGraph) {
			if (aControlGraph != null) {
				aControlGraph.setOwnerContext(CONTROL_GRAPH_KEY);
			}
			performSuperSetter(CONTROL_GRAPH_KEY, aControlGraph);
		}

		@Override
		public void setOwner(FMLControlGraphOwner owner) {
			super.setOwner(owner);
			if (getControlGraph() != null) {
				getControlGraph().getBindingModel().setBaseBindingModel(getBaseBindingModel(getControlGraph()));
			}
		}

		@Override
		public Type getInferedType() {
			if (getControlGraph() != null) {
				return getControlGraph().getInferedType();
			}
			return Void.class;
		}

		@Override
		public void accept(FMLControlGraphVisitor visitor) {
			super.accept(visitor);
			if (getControlGraph() != null) {
				getControlGraph().accept(visitor);
			}
		}

	}

	@DefineValidationRule
	public static class DeclaredTypeMustBeResolved extends TypeMustBeResolved<AbstractIterationAction> {
		public DeclaredTypeMustBeResolved() {
			super("declared_type_must_be_resolved", AbstractIterationAction.class);
		}

		@Override
		public Type getType(AbstractIterationAction declaration) {
			return declaration.getDeclaredType();
		}

	}

	@DefineValidationRule
	public static class TypeMustBeValid extends ValidationRule<TypeMustBeValid, AbstractIterationAction> {

		public TypeMustBeValid() {
			super(AbstractIterationAction.class, "declared_type_must_be_valid");
		}

		@Override
		public ValidationIssue<TypeMustBeValid, AbstractIterationAction> applyValidation(AbstractIterationAction iteration) {
			if (iteration.getDeclaredType() == null) {
				return new ValidationError<>(this, iteration, "type_must_be_declared");
			}
			if (TypeUtils.isVoid(iteration.getDeclaredType())) {
				return new ValidationError<>(this, iteration, "declared_type_cannot_be_void");
			}
			return null;
		}

	}

	@DefineValidationRule
	public static class DeclaredTypeShouldBeCompatibleWithAnalyzedType
			extends ValidationRule<DeclaredTypeShouldBeCompatibleWithAnalyzedType, AbstractIterationAction> {

		public DeclaredTypeShouldBeCompatibleWithAnalyzedType() {
			super(AbstractIterationAction.class, "declared_types_and_analyzed_types_must_be_compatible");
		}

		@Override
		public ValidationIssue<DeclaredTypeShouldBeCompatibleWithAnalyzedType, AbstractIterationAction> applyValidation(
				AbstractIterationAction iteration) {

			Type expected = iteration.getDeclaredType();
			Type analyzed = iteration.getAnalyzedType();

			if (expected != null && !TypeUtils.isTypeAssignableFrom(expected, analyzed, true)) {
				return new NotCompatibleTypesIssue(this, iteration, expected, analyzed);
			}

			return null;
		}

		public static class NotCompatibleTypesIssue
				extends ValidationError<DeclaredTypeShouldBeCompatibleWithAnalyzedType, AbstractIterationAction> {

			private Type expectedType;
			private Type analyzedType;

			public NotCompatibleTypesIssue(DeclaredTypeShouldBeCompatibleWithAnalyzedType rule, AbstractIterationAction anObject,
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

}
