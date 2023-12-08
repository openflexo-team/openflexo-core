/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Connie-core, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeSupport;
import java.lang.reflect.Type;

import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.binding.Property;
import org.openflexo.connie.binding.SimplePathElement;
import org.openflexo.connie.type.ProxyType;
import org.openflexo.connie.type.TypeUtils;

/**
 * Default implementation for a simple path element in a binding path, represented by a simple get/set access through a property
 * 
 * @author sylvain
 * 
 */
public abstract class FMLSimplePathElementImpl<P extends Property> extends AbstractFMLPathElementImpl implements SimplePathElement<P> {

	private P property;
	private Type type;

	public FMLSimplePathElementImpl() {
		super();
	}

	/*public FMLSimplePathElementImpl(IBindingPathElement parent, String propertyName, Type type) {
		super(parent, propertyName);
		setType(type);
	}*/

	@Override
	public String getPropertyName() {
		if (getProperty() != null) {
			return getProperty().getName();
		}
		return getParsed();
	}

	@Override
	public void setPropertyName(String propertyName) {
		setParsed(propertyName);
	}

	@Override
	public P getProperty() {
		return property;
	}

	@Override
	public void setProperty(P property) {
		if ((property == null && this.property != null) || (property != null && !property.equals(this.property))) {
			P oldValue = this.property;
			this.property = property;
			if (property != null) {
				setParsed(property.getName());
			}
			getPropertyChangeSupport().firePropertyChange("property", oldValue, property);
			getPropertyChangeSupport().firePropertyChange(NAME_PROPERTY, oldValue != null ? oldValue.getName() : null,
					property != null ? property.getName() : null);
			if (property != null) {
				setType(property.getType());
			}
			else {
				setType(Object.class);
			}
		}
	}

	/**
	 * Return accessed type for this {@link IBindingPathElement}<br>
	 * If this is a {@link ProxyType} return referenced type
	 * 
	 * @return
	 */
	@Override
	public Type getActualType() {
		if (getType() instanceof ProxyType) {
			return ((ProxyType) getType()).getReferencedType();
		}
		return getType();
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public void setType(Type type) {
		if ((type == null && this.type != null) || (type != null && !type.equals(this.type))) {
			Type oldValue = this.type;
			this.type = type;
			getPropertyChangeSupport().firePropertyChange(TYPE_PROPERTY, oldValue, type);
		}
	}

	@Override
	public boolean isSettable() {
		return true;
	}

	/**
	 * Return a flag indicating if this BindingPathElement supports computation with 'null' value as entry (target)<br>
	 * 
	 * @return false in this case
	 */
	@Override
	public boolean supportsNullValues() {
		return false;
	}

	@Override
	public String getSerializationRepresentation() {
		return getPropertyName();
	}

	/*@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getParent() == null) ? 0 : getParent().hashCode());
		result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
		return result;
	}*/

	/*@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimplePathElement other = (SimplePathElement) obj;
		if (getParent() == null) {
			if (other.getParent() != null)
				return false;
		}
		else if (!getParent().equals(other.getParent()))
			return false;
		if (propertyName == null) {
			if (other.propertyName != null)
				return false;
		}
		else if (!propertyName.equals(other.propertyName))
			return false;
		return true;
	}*/

	@Override
	public boolean isNotifyingBindingPathChanged() {
		return false;
	}

	public String getBindingPathAsString() {
		if (getParent() instanceof FMLSimplePathElementImpl) {
			return ((FMLSimplePathElementImpl) getParent()).getBindingPathAsString() + "." + getLabel();
		}
		if (getParent() instanceof BindingVariable) {
			return ((BindingVariable) getParent()).getVariableName() + "." + getLabel();
		}
		return getLabel();
	}

	/**
	 * Return boolean indicating if this {@link BindingPathElement} is notification-safe (all modifications of data are notified using
	 * {@link PropertyChangeSupport} scheme)<br>
	 * 
	 * When tagged as unsafe, disable caching while evaluating related {@link DataBinding}.
	 * 
	 * Otherwise return true
	 * 
	 * @return
	 */
	@Override
	public boolean isNotificationSafe() {
		return true;
	}

	@Override
	public BindingPathCheck checkBindingPathIsValid(IBindingPathElement parentElement, Type parentType) {

		BindingPathCheck check = super.checkBindingPathIsValid(parentElement, parentType);

		check.returnedType = TypeUtils.makeInstantiatedType(getType(), parentType);
		check.valid = true;
		return check;
	}

	@Override
	public boolean requiresContext() {
		return true;
	}
}
