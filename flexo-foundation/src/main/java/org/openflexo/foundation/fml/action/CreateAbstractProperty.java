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

import java.lang.reflect.Type;
import java.security.InvalidParameterException;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.AbstractProperty;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoConceptStructuralFacet;

/**
 * Action allowing to create a {@link AbstractProperty}<br>
 * 
 * To be valid, such action:
 * <ul>
 * <li>must be configured with a {@link FlexoConceptObject} as focused object</li>
 * <li>must declare a valid property name</li>
 * <li>must declare a valid type</li>
 * <li>may declare a valid description</li>
 * </ul>
 */
public class CreateAbstractProperty extends AbstractCreateFlexoProperty<CreateAbstractProperty> {

	private static final Logger logger = Logger.getLogger(CreateAbstractProperty.class.getPackage().getName());

	public static FlexoActionFactory<CreateAbstractProperty, FlexoConceptObject, FMLObject> actionType = new FlexoActionFactory<CreateAbstractProperty, FlexoConceptObject, FMLObject>(
			"create_abstract_property", FlexoActionFactory.newPropertyMenu, FlexoActionFactory.defaultGroup,
			FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateAbstractProperty makeNewAction(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection,
				FlexoEditor editor) {
			return new CreateAbstractProperty(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConceptObject object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConceptObject object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateAbstractProperty.actionType, FlexoConcept.class);
		FlexoObjectImpl.addActionForClass(CreateAbstractProperty.actionType, FlexoConceptStructuralFacet.class);
	}

	private Type propertyType = Object.class;

	private AbstractProperty<?> newAbstractProperty;

	private CreateAbstractProperty(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	public Type getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(Type propertyType) {
		if ((propertyType == null && this.propertyType != null) || (propertyType != null && !propertyType.equals(this.propertyType))) {
			Type oldValue = this.propertyType;
			this.propertyType = propertyType;
			getPropertyChangeSupport().firePropertyChange("propertyType", oldValue, propertyType);
		}
	}

	@Override
	public AbstractProperty<?> getNewFlexoProperty() {
		return newAbstractProperty;
	}

	@Override
	protected String getDefaultPropertyName() {
		return "property";
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException, InvalidNameException {

		if (getPropertyType() != null) {
			FMLModelFactory factory = getFocusedObject().getFMLModelFactory();
			newAbstractProperty = factory.newAbstractProperty();

			if (newAbstractProperty != null) {

				newAbstractProperty.setPropertyName(getPropertyName());
				newAbstractProperty.setType(getPropertyType());

				finalizeDoAction(context);
			}
		}

		else {
			throw new InvalidParameterException("No property type defined");
		}

	}

}
