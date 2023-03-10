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

package org.openflexo.foundation.fml.editionaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Primitive used to fire change for a property
 * 
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(NotifyPropertyChangedAction.NotifyPropertyChangedActionImpl.class)
@XMLElement
public interface NotifyPropertyChangedAction extends EditionAction {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String PROPERTY_NAME_KEY = "propertyName";
	@PropertyIdentifier(type = String.class)
	public static final String STATIC_PROPERTY_NAME_KEY = "staticPropertyName";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String OBJECT_KEY = "object";

	@Getter(value = STATIC_PROPERTY_NAME_KEY)
	@XMLAttribute
	@Deprecated
	public String getStaticPropertyName();

	@Setter(STATIC_PROPERTY_NAME_KEY)
	@Deprecated
	public void setStaticPropertyName(String propertyName);

	@Getter(value = PROPERTY_NAME_KEY)
	public DataBinding<String> getPropertyName();

	@Setter(PROPERTY_NAME_KEY)
	public void setPropertyName(DataBinding<String> propertyName);

	@Getter(value = OBJECT_KEY)
	@XMLAttribute
	public DataBinding<HasPropertyChangeSupport> getObject();

	@Setter(OBJECT_KEY)
	public void setObject(DataBinding<HasPropertyChangeSupport> object);

	public static abstract class NotifyPropertyChangedActionImpl extends EditionActionImpl implements NotifyPropertyChangedAction {

		private static final Logger logger = Logger.getLogger(NotifyPropertyChangedAction.class.getPackage().getName());

		private DataBinding<HasPropertyChangeSupport> object;
		private DataBinding<String> propertyName;

		@Override
		public DataBinding<String> getPropertyName() {
			if (propertyName == null) {
				propertyName = new DataBinding<>(this, String.class, BindingDefinitionType.GET);
				propertyName.setBindingName("propertyName");
			}
			return propertyName;
		}

		@Override
		public void setPropertyName(DataBinding<String> propertyName) {
			if (propertyName != null) {
				propertyName.setOwner(this);
				propertyName.setBindingName("propertyName");
				propertyName.setDeclaredType(String.class);
				propertyName.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.propertyName = propertyName;
			notifiedBindingChanged(propertyName);
		}

		@Override
		public DataBinding<HasPropertyChangeSupport> getObject() {
			if (object == null) {
				object = new DataBinding<>(this, HasPropertyChangeSupport.class, BindingDefinitionType.GET);
				object.setBindingName("object");
			}
			return object;
		}

		@Override
		public void setObject(DataBinding<HasPropertyChangeSupport> object) {
			if (object != null) {
				object.setOwner(this);
				object.setBindingName("object");
				object.setDeclaredType(HasPropertyChangeSupport.class);
				object.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.object = object;
			notifiedBindingChanged(object);
		}

		@Override
		public String getStringRepresentation() {
			return getHeaderContext() + getObject().toString() + ".firePropertyChange " + getPropertyName();
		}

		@Override
		public Object execute(RunTimeEvaluationContext evaluationContext) {
			HasPropertyChangeSupport hasPCSupport = null;
			String propertyName = null;
			try {
				propertyName = getPropertyName().getBindingValue(evaluationContext);
				if (getObject().isSet()) {
					hasPCSupport = getObject().getBindingValue(evaluationContext);
				}
				else {
					hasPCSupport = evaluationContext.getFlexoConceptInstance();
				}
			} catch (TypeMismatchException e1) {
				e1.printStackTrace();
			} catch (NullReferenceException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}

			System.out.println("FirePropertyChangeSupport for " + hasPCSupport + " propertyName=" + getPropertyName());

			if (hasPCSupport != null) {
				hasPCSupport.getPropertyChangeSupport().firePropertyChange(propertyName, false, true);
			}

			return null;
		}

		@Override
		public Type getInferedType() {
			return Void.class;
		}

	}

	@DefineValidationRule
	public static class PropertyNameBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<NotifyPropertyChangedAction> {
		public PropertyNameBindingIsRequiredAndMustBeValid() {
			super("'property_name'_binding_is_not_valid", NotifyPropertyChangedAction.class);
		}

		@Override
		public DataBinding<?> getBinding(NotifyPropertyChangedAction object) {
			return object.getPropertyName();
		}

	}

	@DefineValidationRule
	public static class ObjectBindingIsRequiredAndMustBeValid extends BindingMustBeValid<NotifyPropertyChangedAction> {
		public ObjectBindingIsRequiredAndMustBeValid() {
			super("'object'_binding_is_not_valid", NotifyPropertyChangedAction.class);
		}

		@Override
		public DataBinding<?> getBinding(NotifyPropertyChangedAction object) {
			return object.getObject();
		}

	}

}
