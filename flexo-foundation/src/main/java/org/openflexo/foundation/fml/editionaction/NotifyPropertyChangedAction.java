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
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
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

	@PropertyIdentifier(type = String.class)
	public static final String PROPERTY_NAME_KEY = "propertyName";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String OBJECT_KEY = "object";

	@Getter(value = PROPERTY_NAME_KEY)
	@XMLAttribute
	public String getPropertyName();

	@Setter(PROPERTY_NAME_KEY)
	public void setPropertyName(String propertyName);

	@Getter(value = OBJECT_KEY)
	@XMLAttribute
	public DataBinding<HasPropertyChangeSupport> getObject();

	@Setter(OBJECT_KEY)
	public void setObject(DataBinding<HasPropertyChangeSupport> object);

	public static abstract class NotifyPropertyChangedActionImpl extends EditionActionImpl implements NotifyPropertyChangedAction {

		private static final Logger logger = Logger.getLogger(NotifyPropertyChangedAction.class.getPackage().getName());

		private DataBinding<HasPropertyChangeSupport> object;

		@Override
		public DataBinding<HasPropertyChangeSupport> getObject() {
			if (object == null) {
				object = new DataBinding<HasPropertyChangeSupport>(this, HasPropertyChangeSupport.class, BindingDefinitionType.GET);
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
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append(getObject().toString() + ".firePropertyChange " + getPropertyName(), context);
			return out.toString();
		}

		@Override
		public Object execute(RunTimeEvaluationContext evaluationContext) {
			HasPropertyChangeSupport hasPCSupport = null;
			try {
				hasPCSupport = getObject().getBindingValue(evaluationContext);
			} catch (TypeMismatchException e1) {
				e1.printStackTrace();
			} catch (NullReferenceException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			}

			// System.out.println("FirePropertyChangeSupport for " + hasPCSupport + " propertyName=" + getPropertyName());

			if (hasPCSupport != null) {
				hasPCSupport.getPropertyChangeSupport().firePropertyChange(getPropertyName(), false, true);
			}

			return null;
		}

		@Override
		public Type getInferedType() {
			return Void.class;
		}

	}

	@DefineValidationRule
	public static class ObjectBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<NotifyPropertyChangedAction> {
		public ObjectBindingIsRequiredAndMustBeValid() {
			super("'object'_binding_is_not_valid", NotifyPropertyChangedAction.class);
		}

		@Override
		public DataBinding<?> getBinding(NotifyPropertyChangedAction object) {
			return object.getObject();
		}

	}

}
