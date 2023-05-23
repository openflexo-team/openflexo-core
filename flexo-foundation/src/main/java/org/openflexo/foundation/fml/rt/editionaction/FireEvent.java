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

package org.openflexo.foundation.fml.rt.editionaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoEvent;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoEventInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;

/**
 * Primitive used to fire a new {@link FlexoEvent}.<br>
 * Life-cycle of event is somewhat different from {@link FlexoConcept} instance, since it's life is restricted to the propagation of the
 * event
 * 
 * @author sylvain
 */
@ModelEntity
@ImplementationClass(FireEvent.FireEventImpl.class)
public interface FireEvent extends EditionAction {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String EVENT_INSTANCE_KEY = "eventInstance";

	@Getter(value = EVENT_INSTANCE_KEY)
	public DataBinding<FlexoEventInstance> getEventInstance();

	@Setter(EVENT_INSTANCE_KEY)
	public void setEventInstance(DataBinding<FlexoEventInstance> eventInstance);

	public static abstract class FireEventImpl extends EditionActionImpl implements FireEvent {

		private static final Logger logger = Logger.getLogger(FireEvent.class.getPackage().getName());

		private DataBinding<FlexoEventInstance> eventInstance;

		@Override
		public DataBinding<FlexoEventInstance> getEventInstance() {
			if (eventInstance == null) {
				eventInstance = new DataBinding<>(this, FlexoEventInstance.class, BindingDefinitionType.GET);
				eventInstance.setBindingName(EVENT_INSTANCE_KEY);
			}
			return eventInstance;
		}

		@Override
		public void setEventInstance(DataBinding<FlexoEventInstance> eventInstance) {
			if (eventInstance != null) {
				eventInstance.setOwner(this);
				eventInstance.setBindingName(EVENT_INSTANCE_KEY);
				eventInstance.setDeclaredType(FlexoEventInstance.class);
				eventInstance.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.eventInstance = eventInstance;
		}

		@Override
		public Object execute(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {
			FlexoEventInstance eventInstance = null;
			try {
				eventInstance = getEventInstance().getBindingValue(evaluationContext);
			} catch (TypeMismatchException e1) {
				e1.printStackTrace();
			} catch (NullReferenceException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				throw new FMLExecutionException(e1.getCause());
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}

			VirtualModelInstance<?, ?> vmi = evaluationContext.getVirtualModelInstance();

			// VMI vmi = getVirtualModelInstance(evaluationContext);
			// FlexoEventInstance returned = (FlexoEventInstance) super.execute(evaluationContext);

			// And we fire the new event to the listening FMLRunTimeEngine(s)
			if (vmi != null) {
				vmi.getPropertyChangeSupport().firePropertyChange(FMLRTVirtualModelInstance.EVENT_FIRED, null, eventInstance);
			}

			return null;
		}

		@Override
		public Type getInferedType() {
			return Void.class;
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getEventInstance().rebuild();
		}
	}

	@DefineValidationRule
	public static class EventInstanceBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<FireEvent> {
		public EventInstanceBindingIsRequiredAndMustBeValid() {
			super("'event_instance'_binding_is_not_valid", FireEvent.class);
		}

		@Override
		public DataBinding<?> getBinding(FireEvent object) {
			return object.getEventInstance();
		}

	}

}
