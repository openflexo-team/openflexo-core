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

import java.util.logging.Logger;

import org.openflexo.connie.BindingModel;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * 
 * 
 * @author sylvain
 *
 */
@ModelEntity
@XMLElement
@ImplementationClass(JavaImportDeclaration.JavaImportDeclarationImpl.class)
public interface JavaImportDeclaration extends FMLPrettyPrintable {

	@PropertyIdentifier(type = FMLCompilationUnit.class)
	public static final String COMPILATION_UNIT_KEY = "compilationUnit";
	@PropertyIdentifier(type = String.class)
	public static final String FULL_QUALIFIED_CLASS_NAME_KEY = "fullQualifiedClassName";
	@PropertyIdentifier(type = String.class)
	public static final String ABBREV_KEY = "abbrev";

	/**
	 * Return simple class name represented by the full qualified denotation of this import
	 * 
	 * @return
	 */
	public String getClassName();

	@Getter(value = FULL_QUALIFIED_CLASS_NAME_KEY)
	@XMLAttribute
	public String getFullQualifiedClassName();

	@Setter(FULL_QUALIFIED_CLASS_NAME_KEY)
	public void setFullQualifiedClassName(String fullQualifiedClassName);

	@Getter(value = ABBREV_KEY)
	@XMLAttribute
	public String getAbbrev();

	@Setter(ABBREV_KEY)
	public void setAbbrev(String abbrev);

	@Getter(value = COMPILATION_UNIT_KEY, inverse = FMLCompilationUnit.JAVA_IMPORTS_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public FMLCompilationUnit getCompilationUnit();

	@Setter(COMPILATION_UNIT_KEY)
	public void setCompilationUnit(FMLCompilationUnit compilationUnit);

	public Class<?> getJavaClass();

	public static abstract class JavaImportDeclarationImpl extends FMLObjectImpl implements JavaImportDeclaration {

		private static final Logger logger = Logger.getLogger(JavaImportDeclarationImpl.class.getPackage().getName());

		private Class<?> javaClass;
		private boolean lookupPerformed = false;;

		@Override
		public Class<?> getJavaClass() {
			if (javaClass != null) {
				return javaClass;
			}
			if (!lookupPerformed) {
				try {
					javaClass = Class.forName(getFullQualifiedClassName());
				} catch (ClassNotFoundException e) {
					logger.warning("Cannot find " + getFullQualifiedClassName());
				}
				lookupPerformed = true;
			}
			return javaClass;
		}

		@Override
		public FMLCompilationUnit getResourceData() {
			return getCompilationUnit();
		}

		@Override
		public String getClassName() {
			if (getAbbrev() != null) {
				return getAbbrev();
			}
			if (getFullQualifiedClassName() == null) {
				return null;
			}
			return getFullQualifiedClassName().substring(getFullQualifiedClassName().lastIndexOf(".") + 1);
		}

		@Override
		public String toString() {
			return "JavaImportDeclaration(" + getFullQualifiedClassName() + ")";
		}

		@Override
		public void setAbbrev(String abbrev) {
			performSuperSetter(ABBREV_KEY, abbrev);
			getPropertyChangeSupport().firePropertyChange("className", null, getClassName());
		}

		@Override
		public void setFullQualifiedClassName(String fullQualifiedClassName) {
			performSuperSetter(FULL_QUALIFIED_CLASS_NAME_KEY, fullQualifiedClassName);
			getPropertyChangeSupport().firePropertyChange("className", null, getClassName());
		}

		@Override
		public BindingModel getBindingModel() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
