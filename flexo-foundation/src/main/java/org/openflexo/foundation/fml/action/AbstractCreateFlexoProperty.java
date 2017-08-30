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

package org.openflexo.foundation.fml.action;

import java.security.InvalidParameterException;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.toolbox.StringUtils;

/**
 * Abstract implementation for an action creating a {@link FlexoProperty}
 * 
 * @author sylvain
 *
 * @param <A>
 */
public abstract class AbstractCreateFlexoProperty<A extends AbstractCreateFlexoProperty<A>>
		extends FlexoAction<A, FlexoConceptObject, FMLObject>implements Bindable {

	private static final Logger logger = Logger.getLogger(AbstractCreateFlexoProperty.class.getPackage().getName());

	private String propertyName;
	private String description;

	private DataBinding<?> defaultValue;
	private DataBinding<?> container;

	AbstractCreateFlexoProperty(FlexoActionFactory<A, FlexoConceptObject, FMLObject> actionType, FlexoConceptObject focusedObject,
			Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	public FlexoConcept getFlexoConcept() {
		if (getFocusedObject() != null) {
			return getFocusedObject().getFlexoConcept();
		}
		return null;
	}

	protected abstract String getDefaultPropertyName();

	public String getPropertyName() {
		if (StringUtils.isEmpty(propertyName)) {
			return getFlexoConcept().getAvailablePropertyName(getDefaultPropertyName());
		}
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		if ((propertyName == null && getPropertyName() != null) || (propertyName != null && !propertyName.equals(getPropertyName()))) {
			String oldValue = getPropertyName();
			this.propertyName = propertyName;
			getPropertyChangeSupport().firePropertyChange("propertyName", oldValue, propertyName);
		}
	}

	public abstract FlexoProperty<?> getNewFlexoProperty();

	@Override
	public boolean isValid() {
		if (StringUtils.isEmpty(getPropertyName())) {
			return false;
		}
		else if (getFlexoConcept().getDeclaredProperty(getPropertyName()) != null) {
			return false;
		}
		return true;
	}

	public DataBinding<?> getContainer() {
		if (container == null) {
			container = new DataBinding<Object>(this, Object.class, BindingDefinitionType.GET);
			container.setBindingName("container");
		}
		return container;
	}

	public void setContainer(DataBinding<?> container) {
		if (container != null) {
			container.setOwner(this);
			container.setBindingName("container");
			container.setDeclaredType(Object.class);
			container.setBindingDefinitionType(BindingDefinitionType.GET);
		}
		this.container = container;
	}

	public DataBinding<?> getDefaultValue() {
		if (defaultValue == null) {
			defaultValue = new DataBinding<Object>(this, Object.class, BindingDefinitionType.GET);
			defaultValue.setBindingName("defaultValue");
		}
		return defaultValue;
	}

	public void setDefaultValue(DataBinding<?> defaultValue) {
		if (defaultValue != null) {
			defaultValue.setOwner(this);
			defaultValue.setBindingName("container");
			defaultValue.setDeclaredType(Object.class);
			defaultValue.setBindingDefinitionType(BindingDefinitionType.GET);
		}
		this.defaultValue = defaultValue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if ((description == null && this.description != null) || (description != null && !description.equals(this.description))) {
			String oldValue = this.description;
			this.description = description;
			getPropertyChangeSupport().firePropertyChange("description", oldValue, description);
		}
	}

	protected void finalizeDoAction(Object context) throws NotImplementedException, InvalidParameterException {
		if (getFlexoConcept() != null && getNewFlexoProperty() != null) {
			getNewFlexoProperty().setDescription(getDescription());
			getFlexoConcept().addToFlexoProperties(getNewFlexoProperty());
		}
		else {
			throw new InvalidParameterException("Could not create property");
		}
	}

	@Override
	public BindingModel getBindingModel() {
		if (getFlexoConcept() != null) {
			return getFlexoConcept().getBindingModel();
		}
		return null;
	}

	@Override
	public BindingFactory getBindingFactory() {
		if (getFlexoConcept() != null) {
			return getFlexoConcept().getBindingFactory();
		}
		return null;
	}

	@Override
	public void notifiedBindingChanged(DataBinding<?> dataBinding) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
		// TODO Auto-generated method stub

	}

}
