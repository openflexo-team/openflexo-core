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

package org.openflexo.foundation.fml;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.connie.type.UnresolvedType;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.Updater;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * A java property which type is any type of Java language
 * 
 * Take care that this property is transient: value cannot be serialized at run-time
 * 
 * @author sylvain
 *
 * @param <T>
 */
// TODO: rename to JavaProperty
@ModelEntity
@ImplementationClass(JavaRole.JavaRoleImpl.class)
@XMLElement(xmlTag = "JavaRole")
@FML("JavaRole")
public interface JavaRole<T> extends BasicProperty<T> {

	@Override
	@Getter(value = TYPE_KEY, isStringConvertable = true)
	@XMLAttribute
	public Type getType();

	@Override
	@Setter(TYPE_KEY)
	public void setType(Type type);

	/**
	 * We define an updater for TYPE property because we need to translate supplied Type to valid TypingSpace
	 * 
	 * @param type
	 */
	@Override
	@Updater(TYPE_KEY)
	public void updateType(Type type);

	public static abstract class JavaRoleImpl<T> extends BasicPropertyImpl<T> implements JavaRole<T> {

		protected static final Logger logger = FlexoLogger.getLogger(JavaRole.class.getPackage().getName());

		private Type type;

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void setType(Type type) {
			if (requireChange(getType(), type)) {
				Type oldValue = this.type;
				this.type = type;
				notifyChange(TYPE_KEY, oldValue, type);
				notifyResultingTypeChanged();
			}
		}

		@Override
		public ActorReference<T> makeActorReference(T object, FlexoConceptInstance fci) {
			// Never serialized
			return null;
		}

		@Override
		public void handleRequiredImports(FMLCompilationUnit compilationUnit) {
			super.handleRequiredImports(compilationUnit);
			if (compilationUnit != null) {
				Class<?> rawType = TypeUtils.getRawType(getType());
				if (!TypeUtils.isPrimitive(rawType)) {
					compilationUnit.ensureJavaImport(rawType);
				}
			}
		}

		/*@Override
		public void handleTypeDeclarationInImports() {
		
			if (getDeclaringCompilationUnit() == null) {
				return;
			}
		
			Class<?> rawType = TypeUtils.getRawType(getType());
		
			if (!TypeUtils.isPrimitive(rawType)) {
		
				boolean typeWasFound = false;
				for (JavaImportDeclaration importDeclaration : getDeclaringCompilationUnit().getJavaImports()) {
					if (importDeclaration.getFullQualifiedClassName().equals(rawType.getName())) {
						typeWasFound = true;
						break;
					}
				}
				if (!typeWasFound) {
					// Adding import
					JavaImportDeclaration newJavaImportDeclaration = getDeclaringCompilationUnit().getFMLModelFactory()
							.newJavaImportDeclaration();
					newJavaImportDeclaration.setFullQualifiedClassName(rawType.getName());
					getDeclaringCompilationUnit().addToJavaImports(newJavaImportDeclaration);
				}
			}
		
		}*/

	}

	@DefineValidationRule
	public static class TypeMustBeResolved extends ValidationRule<TypeMustBeResolved, JavaRole> {
		public TypeMustBeResolved() {
			super(JavaRole.class, "assigned_type_must_be_compatible");
		}

		@Override
		public ValidationIssue<TypeMustBeResolved, JavaRole> applyValidation(JavaRole role) {

			if (role.getType() instanceof UnresolvedType) {
				return new ValidationError<>(this, role, "unresolved_type_($validable.type)");
			}
			return null;
		}

	}

}
