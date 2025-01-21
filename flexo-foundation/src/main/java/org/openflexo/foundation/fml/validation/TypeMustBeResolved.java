package org.openflexo.foundation.fml.validation;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
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

import org.openflexo.connie.type.ConnieType;
import org.openflexo.connie.type.LoadedClassesInfo;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.connie.type.UnresolvedType;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.pamela.validation.FixProposal;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * A {@link ValidationRule} checking that a type must be resolved
 * 
 * @param <C>
 */
public abstract class TypeMustBeResolved<C extends FMLObject> extends ValidationRule<TypeMustBeResolved<C>, C> {

	private static final Logger logger = Logger.getLogger(TypeMustBeResolved.class.getPackage().getName());

	public TypeMustBeResolved(String ruleName, Class<C> clazz) {
		super(clazz, ruleName);
	}

	public abstract Type getType(C object);

	@Override
	public ValidationIssue<TypeMustBeResolved<C>, C> applyValidation(C object) {

		if (getType(object) instanceof UnresolvedType) {
			return new UnresolvedTypeIssue<>(this, object);
		}
		if (getType(object) instanceof ConnieType) {
			if (!((ConnieType) getType(object)).isResolved()) {
				return new UnresolvedTypeIssue<>(this, object);
			}
		}

		return null;
	}

	public static class UnresolvedTypeIssue<C extends FMLObject> extends ValidationError<TypeMustBeResolved<C>, C> {

		public UnresolvedTypeIssue(TypeMustBeResolved<C> rule, C anObject) {
			super(rule, anObject, "unresolved_type_($typeAsString)");

			findPotentialResolution(rule.getType(anObject), rule);

		}

		private LoadedClassesInfo getLoadedClassesInfo() {
			return LoadedClassesInfo.getInstance();
		}

		private void findPotentialResolution(Type type, TypeMustBeResolved<C> rule) {
			if (type instanceof UnresolvedType) {
				UnresolvedType unresolvedType = (UnresolvedType) type;
				List<Class<?>> foundClasses = getLoadedClassesInfo().search(unresolvedType.getUnresolvedTypeName());
				for (Class<?> c : foundClasses) {
					getLoadedClassesInfo().registerClass(c);
					if (c.getSimpleName().equals(unresolvedType.getUnresolvedTypeName())) {
						ImportClass<C> fixProposal = new ImportClass<>(rule, c);
						addToFixProposals(fixProposal);
					}
				}
			}
			else if (type instanceof ParameterizedType) {
				ParameterizedType t = (ParameterizedType) type;
				findPotentialResolution(t.getRawType(), rule);
				for (Type argType : t.getActualTypeArguments()) {
					findPotentialResolution(argType, rule);
				}
			}
			else if (type instanceof WildcardType) {
				WildcardType t = (WildcardType) type;
				for (Type argType : t.getUpperBounds()) {
					findPotentialResolution(argType, rule);
				}
				for (Type argType : t.getLowerBounds()) {
					findPotentialResolution(argType, rule);
				}
			}
		}

		public Type getType() {
			return getCause().getType(getValidable());
		}

		public String getTypeAsString() {
			return TypeUtils.simpleRepresentation(getType());
		}

	}

	protected static class ImportClass<C extends FMLObject> extends FixProposal<TypeMustBeResolved<C>, C> {

		private final TypeMustBeResolved<C> rule;
		private Class<?> classToImport;

		public ImportClass(TypeMustBeResolved<C> rule, Class<?> classToImport) {
			super("import_class_($classToImport.name)");
			this.rule = rule;
			this.classToImport = classToImport;
		}

		public Class<?> getClassToImport() {
			return classToImport;
		}

		@Override
		protected void fixAction() {
			getValidable().getDeclaringCompilationUnit().ensureJavaImport(classToImport);
			getValidable().getDeclaringCompilationUnit().setModified(true);
		}

	}
}
