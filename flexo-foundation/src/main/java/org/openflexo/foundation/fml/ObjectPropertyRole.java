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

import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyObjectProperty;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;

@ModelEntity(isAbstract = true)
@ImplementationClass(ObjectPropertyRole.ObjectPropertyRoleImpl.class)
public abstract interface ObjectPropertyRole<P extends IFlexoOntologyObjectProperty> extends PropertyRole<P> {

	@PropertyIdentifier(type = String.class)
	public static final String RANGE_URI_KEY = "rangeURI";

	@Getter(value = RANGE_URI_KEY)
	@XMLAttribute(xmlTag = "range")
	public String _getRangeURI();

	@Setter(RANGE_URI_KEY)
	public void _setRangeURI(String rangeURI);

	public static abstract class ObjectPropertyRoleImpl<P extends IFlexoOntologyObjectProperty> extends PropertyRoleImpl<P>
			implements ObjectPropertyRole<P> {

		private String rangeURI;

		public ObjectPropertyRoleImpl() {
			super();
		}

		@Override
		public Type getType() {
			if (getParentProperty() == null) {
				return IFlexoOntologyObjectProperty.class;
			}
			return super.getType();
		}

		@Override
		public String getTypeDescription() {
			if (getParentProperty() != null) {
				return getParentProperty().getName();
			}
			return "";
		}

		@Override
		public IFlexoOntologyObjectProperty getParentProperty() {
			return (IFlexoOntologyObjectProperty) super.getParentProperty();
		}

		public void setParentProperty(IFlexoOntologyObjectProperty ontologyProperty) {
			super.setParentProperty(ontologyProperty);
		}

		@Override
		public String _getRangeURI() {
			return rangeURI;
		}

		@Override
		public void _setRangeURI(String domainURI) {
			this.rangeURI = domainURI;
		}

		public IFlexoOntologyClass getRange() {
			return getOwningVirtualModel().getOntologyClass(_getRangeURI());
		}

		public void setRange(IFlexoOntologyClass c) {
			_setRangeURI(c != null ? c.getURI() : null);
		}

	}
}
