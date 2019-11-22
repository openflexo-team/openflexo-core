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

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
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
@ImplementationClass(ElementImportDeclaration.ElementImportDeclarationImpl.class)
public interface ElementImportDeclaration extends FMLPrettyPrintable {

	@PropertyIdentifier(type = FMLCompilationUnit.class)
	public static final String COMPILATION_UNIT_KEY = "compilationUnit";
	@PropertyIdentifier(type = String.class)
	public static final String RESOURCE_REFERENCE_KEY = "resourceReference";
	@PropertyIdentifier(type = String.class)
	public static final String OBJECT_REFERENCE_KEY = "objectReference";
	@PropertyIdentifier(type = String.class)
	public static final String ABBREV_KEY = "abbrev";

	@Getter(value = RESOURCE_REFERENCE_KEY)
	@XMLAttribute
	public DataBinding<?> getResourceReference();

	@Setter(RESOURCE_REFERENCE_KEY)
	public void setResourceReference(DataBinding<?> resourceReference);

	@Getter(value = OBJECT_REFERENCE_KEY)
	@XMLAttribute
	public DataBinding<?> getObjectReference();

	@Setter(OBJECT_REFERENCE_KEY)
	public void setObjectReference(DataBinding<?> objectReference);

	@Getter(value = COMPILATION_UNIT_KEY, inverse = FMLCompilationUnit.JAVA_IMPORTS_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public FMLCompilationUnit getCompilationUnit();

	@Setter(COMPILATION_UNIT_KEY)
	public void setCompilationUnit(FMLCompilationUnit compilationUnit);

	@Getter(value = ABBREV_KEY)
	@XMLAttribute
	public String getAbbrev();

	@Setter(ABBREV_KEY)
	public void setAbbrev(String abbrev);

	public static abstract class ElementImportDeclarationImpl extends FMLObjectImpl implements ElementImportDeclaration {

		@Override
		public FMLCompilationUnit getResourceData() {
			return getCompilationUnit();
		}

		@Override
		public String toString() {
			return "ElementImportDeclaration(" + getResourceReference() + (getObjectReference() != null ? getObjectReference() : "") + ")";
		}

		@Override
		public String getURI() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			return "<not_implemented:" + getStringRepresentation() + ">";
		}

		@Override
		public BindingModel getBindingModel() {
			// TODO Auto-generated method stub
			return null;
		}

		private DataBinding<?> resourceReference;
		private DataBinding<?> objectReference;

		@Override
		public DataBinding<?> getResourceReference() {
			if (resourceReference == null) {
				resourceReference = new DataBinding<Object>(this, Object.class, DataBinding.BindingDefinitionType.GET);
				resourceReference.setBindingName("resourceReference");
				resourceReference.setMandatory(true);

			}
			return resourceReference;
		}

		@Override
		public void setResourceReference(DataBinding<?> resourceReference) {
			if (resourceReference != null) {
				this.resourceReference = new DataBinding<Object>(resourceReference.toString(), this, Object.class,
						DataBinding.BindingDefinitionType.GET);
				this.resourceReference.setBindingName("resourceReference");
				this.resourceReference.setMandatory(true);
			}
			notifiedBindingChanged(resourceReference);
		}

		@Override
		public DataBinding<?> getObjectReference() {
			if (objectReference == null) {
				objectReference = new DataBinding<Object>(this, Object.class, DataBinding.BindingDefinitionType.GET);
				objectReference.setBindingName("objectReference");
				objectReference.setMandatory(true);

			}
			return objectReference;
		}

		@Override
		public void setObjectReference(DataBinding<?> objectReference) {
			if (objectReference != null) {
				this.objectReference = new DataBinding<Object>(objectReference.toString(), this, Object.class,
						DataBinding.BindingDefinitionType.GET);
				this.objectReference.setBindingName("objectReference");
				this.objectReference.setMandatory(true);
			}
			notifiedBindingChanged(objectReference);
		}

	}
}
