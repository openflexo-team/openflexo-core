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

package org.openflexo.foundation.ontology.fml.editionaction;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.ontology.BuiltInDataType;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyDataProperty;
import org.openflexo.foundation.ontology.fml.DataPropertyRole;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;

@ModelEntity(isAbstract = true)
@ImplementationClass(AddDataProperty.AddDataPropertyImpl.class)
public abstract interface AddDataProperty<MS extends TypeAwareModelSlot<M, ?>, M extends FlexoModel<M, ?> & TechnologyObject<?>, T extends IFlexoOntologyDataProperty<?>>
		extends AddConcept<MS, M, T> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String PROPERTY_NAME_KEY = "propertyName";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String DYNAMIC_DOMAIN_KEY = "dynamicDomain";
	@PropertyIdentifier(type = BuiltInDataType.class)
	public static final String DATA_TYPE_KEY = "dataType";

	@Getter(value = PROPERTY_NAME_KEY)
	@XMLAttribute
	@FMLAttribute(value = PROPERTY_NAME_KEY, required = true, description = "<html>Name of property to be created</html>")
	public DataBinding<String> getPropertyName();

	@Setter(PROPERTY_NAME_KEY)
	public void setPropertyName(DataBinding<String> propertyName);

	@Getter(value = DYNAMIC_DOMAIN_KEY)
	@XMLAttribute
	@FMLAttribute(value = DYNAMIC_DOMAIN_KEY, required = false, description = "<html>Domain of property to be created</html>")
	public DataBinding<IFlexoOntologyClass<?>> getDynamicDomain();

	@Setter(DYNAMIC_DOMAIN_KEY)
	public void setDynamicDomain(DataBinding<IFlexoOntologyClass<?>> dynamicDomain);

	@Getter(value = DATA_TYPE_KEY)
	@XMLAttribute
	public BuiltInDataType getDataType();

	@Setter(DATA_TYPE_KEY)
	public void setDataType(BuiltInDataType dataType);

	public static abstract class AddDataPropertyImpl<MS extends TypeAwareModelSlot<M, ?>, M extends FlexoModel<M, ?> & TechnologyObject<?>, T extends IFlexoOntologyDataProperty<?>>
			extends AddConceptImpl<MS, M, T> implements AddDataProperty<MS, M, T> {

		protected static final Logger logger = FlexoLogger.getLogger(AddDataProperty.class.getPackage().getName());

		private DataBinding<String> propertyName;
		private DataBinding<IFlexoOntologyClass<?>> dynamicDomain;

		public abstract Class<T> getOntologyDataPropertyClass();

		@Override
		public DataPropertyRole<T> getAssignedFlexoProperty() {
			return (DataPropertyRole<T>) super.getAssignedFlexoProperty();
		}

		@Override
		public DataBinding<String> getPropertyName() {
			if (propertyName == null) {
				propertyName = new DataBinding<>(this, String.class, DataBinding.BindingDefinitionType.GET);
				propertyName.setBindingName("propertyName");
			}
			return propertyName;
		}

		@Override
		public void setPropertyName(DataBinding<String> propertyName) {
			if (propertyName != null) {
				propertyName.setOwner(this);
				propertyName.setDeclaredType(String.class);
				propertyName.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				propertyName.setBindingName("propertyName");
			}
			this.propertyName = propertyName;
		}

		@Override
		public DataBinding<IFlexoOntologyClass<?>> getDynamicDomain() {
			if (dynamicDomain == null) {
				dynamicDomain = new DataBinding<>(this, IFlexoOntologyClass.class, DataBinding.BindingDefinitionType.GET);
				dynamicDomain.setBindingName("dynamicDomain");
			}
			return dynamicDomain;
		}

		@Override
		public void setDynamicDomain(DataBinding<IFlexoOntologyClass<?>> dynamicDomain) {
			if (dynamicDomain != null) {
				dynamicDomain.setOwner(this);
				dynamicDomain.setDeclaredType(IFlexoOntologyClass.class);
				dynamicDomain.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				dynamicDomain.setBindingName("dynamicDomain");
			}
			this.dynamicDomain = dynamicDomain;
		}

		@Override
		public Type getAssignableType() {
			// if (getSuperProperty() == null) {
			return IFlexoOntologyDataProperty.class;
			// }
			// return SubPropertyOfProperty.getSubPropertyOfProperty(getSuperProperty());
		}

	}

}
