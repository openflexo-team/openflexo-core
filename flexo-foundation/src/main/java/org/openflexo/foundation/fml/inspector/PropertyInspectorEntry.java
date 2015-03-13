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

package org.openflexo.foundation.fml.inspector;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyStructuralProperty;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

/**
 * Represents an inspector entry for an ontology property
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(PropertyInspectorEntry.PropertyInspectorEntryImpl.class)
@XMLElement(xmlTag = "Property")
public interface PropertyInspectorEntry extends InspectorEntry {

	@PropertyIdentifier(type = String.class)
	public static final String PARENT_PROPERTY_URI_KEY = "parentPropertyURI";
	@PropertyIdentifier(type = String.class)
	public static final String DOMAIN_URI_KEY = "domainURI";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String DOMAIN_VALUE_KEY = "domainValue";

	@Getter(value = PARENT_PROPERTY_URI_KEY)
	@XMLAttribute(xmlTag = "parentProperty")
	public String _getParentPropertyURI();

	@Setter(PARENT_PROPERTY_URI_KEY)
	public void _setParentPropertyURI(String parentPropertyURI);

	@Getter(value = DOMAIN_URI_KEY)
	@XMLAttribute(xmlTag = "domain")
	public String _getDomainURI();

	@Setter(DOMAIN_URI_KEY)
	public void _setDomainURI(String domainURI);

	@Getter(value = DOMAIN_VALUE_KEY)
	@XMLAttribute
	public DataBinding<IFlexoOntologyClass> getDomainValue();

	@Setter(DOMAIN_VALUE_KEY)
	public void setDomainValue(DataBinding<IFlexoOntologyClass> domainValue);

	public IFlexoOntologyStructuralProperty getParentProperty();

	public void setParentProperty(IFlexoOntologyStructuralProperty ontologyProperty);

	public IFlexoOntologyClass getDomain();

	public void setDomain(IFlexoOntologyClass c);

	public boolean getIsDynamicDomainValue();

	public void setIsDynamicDomainValue(boolean isDynamic);

	public static abstract class PropertyInspectorEntryImpl extends InspectorEntryImpl implements PropertyInspectorEntry {

		private String parentPropertyURI;
		private String domainURI;
		private boolean isDynamicDomainValueSet = false;
		private DataBinding<IFlexoOntologyClass> domainValue;

		public PropertyInspectorEntryImpl() {
			super();
		}

		@Override
		public Class getDefaultDataClass() {
			return IFlexoOntologyStructuralProperty.class;
		}

		@Override
		public String getWidgetName() {
			return "PropertySelector";
		}

		@Override
		public String _getParentPropertyURI() {
			return parentPropertyURI;
		}

		@Override
		public void _setParentPropertyURI(String parentPropertyURI) {
			this.parentPropertyURI = parentPropertyURI;
		}

		@Override
		public IFlexoOntologyStructuralProperty getParentProperty() {
			if (getOwningVirtualModel() != null) {
				return getOwningVirtualModel().getOntologyProperty(_getParentPropertyURI());
			}
			return null;
		}

		@Override
		public void setParentProperty(IFlexoOntologyStructuralProperty ontologyProperty) {
			parentPropertyURI = ontologyProperty != null ? ontologyProperty.getURI() : null;
		}

		@Override
		public String _getDomainURI() {
			return domainURI;
		}

		@Override
		public void _setDomainURI(String domainURI) {
			this.domainURI = domainURI;
		}

		@Override
		public IFlexoOntologyClass getDomain() {
			return getOwningVirtualModel().getOntologyClass(_getDomainURI());
		}

		@Override
		public void setDomain(IFlexoOntologyClass c) {
			_setDomainURI(c != null ? c.getURI() : null);
		}

		@Override
		public DataBinding<IFlexoOntologyClass> getDomainValue() {
			if (domainValue == null) {
				domainValue = new DataBinding<IFlexoOntologyClass>(this, IFlexoOntologyClass.class, BindingDefinitionType.GET);
				domainValue.setBindingName("domainValue");
			}
			return domainValue;
		}

		@Override
		public void setDomainValue(DataBinding<IFlexoOntologyClass> domainValue) {
			if (domainValue != null) {
				domainValue.setOwner(this);
				domainValue.setBindingName("domainValue");
				domainValue.setDeclaredType(IFlexoOntologyClass.class);
				domainValue.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.domainValue = domainValue;
		}

		@Override
		public boolean getIsDynamicDomainValue() {
			return getDomainValue().isSet() || isDynamicDomainValueSet;
		}

		@Override
		public void setIsDynamicDomainValue(boolean isDynamic) {
			if (isDynamic) {
				isDynamicDomainValueSet = true;
			} else {
				domainValue = null;
				isDynamicDomainValueSet = false;
			}
		}

	}
}
