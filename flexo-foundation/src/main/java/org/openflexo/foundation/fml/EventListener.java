/**
 * 
 * Copyright (c) 2014, Openflexo
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

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.binding.EventListenerBindingModel;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.EventListenerActionFactory;
import org.openflexo.foundation.fml.validation.BindingIsRequiredAndMustBeValid;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.Updater;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * A behaviour executed each time an event is received, declared in FML with {@code listen EventType from expression { ... }}.
 * <p>
 * The expression designates the listened {@link VirtualModelInstance} (see {@link #getListenedVirtualModelInstance()}): the behaviour is
 * executed for each event of the listened type ({@link #getEventType()}), or of a sub-type, fired from that instance. In the body of the
 * behaviour, the received event is available as {@code evt}.
 * <p>
 * Listening is started by the {@link org.openflexo.foundation.fml.rt.FMLRunTimeEngine} when the listening instance is registered in its
 * execution context. See {@link FlexoEvent} for an example.
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(EventListener.EventListenerImpl.class)
@XMLElement
public interface EventListener extends AbstractActionScheme {

	@PropertyIdentifier(type = FlexoEvent.class)
	public static final String EVENT_TYPE_KEY = "eventType";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String LISTENED_VIRTUAL_MODEL_INSTANCE_KEY = "listenedVirtualModelInstance";
	@Getter(value = EVENT_TYPE_KEY, ignoreType = true)
	public FlexoConceptInstanceType getEventType();

	@Setter(EVENT_TYPE_KEY)
	public void setEventType(FlexoConceptInstanceType eventType);

	/**
	 * Translates the supplied type into the typing space of this listener, when it comes from another compilation unit (the one the FML
	 * editor has just parsed, merged with {@code updateWith()})
	 *
	 * @param eventType
	 *            the listened event type, possibly expressed in another typing space
	 */
	@Updater(EVENT_TYPE_KEY)
	public void updateEventType(FlexoConceptInstanceType eventType);

	@Getter(value = LISTENED_VIRTUAL_MODEL_INSTANCE_KEY)
	@XMLAttribute
	public DataBinding<VirtualModelInstance<?, ?>> getListenedVirtualModelInstance();

	@Setter(LISTENED_VIRTUAL_MODEL_INSTANCE_KEY)
	public void setListenedVirtualModelInstance(DataBinding<VirtualModelInstance<?, ?>> vmi);

	public VirtualModel getListenedVirtualModelType();

	public FlexoEvent getEvent();

	public static abstract class EventListenerImpl extends AbstractActionSchemeImpl implements EventListener {

		private FlexoConceptInstanceType eventType;
		private DataBinding<VirtualModelInstance<?, ?>> listenedVirtualModelInstance;

		@Override
		public FlexoEvent getEvent() {
			if (getEventType() != null && getEventType().getFlexoConcept() instanceof FlexoEvent) {
				return (FlexoEvent) getEventType().getFlexoConcept();
			}
			return null;
		}

		@Override
		public FlexoConceptInstanceType getEventType() {
			return eventType;
		}

		@Override
		public void setEventType(FlexoConceptInstanceType eventType) {
			/*if ((eventType == null && this.eventType != null) || (eventType != null && !eventType.equals(this.eventType))) {
				String oldSignature = getSignature();
				FlexoEvent oldValue = this.eventType;
				this.eventType = eventType;
				getPropertyChangeSupport().firePropertyChange("eventType", oldValue, eventType);
				updateSignature(oldSignature);
				// notifyResultingTypeChanged();
			}*/
			this.eventType = eventType;
		}

		@Override
		public void updateEventType(FlexoConceptInstanceType eventType) {
			if (eventType != null && getDeclaringCompilationUnit() != null) {
				setEventType(eventType.translateTo(getDeclaringCompilationUnit().getTypingSpace()));
			}
			else {
				setEventType(eventType);
			}
		}

		@Override
		public DataBinding<VirtualModelInstance<?, ?>> getListenedVirtualModelInstance() {
			if (listenedVirtualModelInstance == null) {
				listenedVirtualModelInstance = new DataBinding<>(this, FMLRTVirtualModelInstance.class, BindingDefinitionType.GET);
				listenedVirtualModelInstance.setBindingName("listenedVirtualModelInstance");
			}
			return listenedVirtualModelInstance;
		}

		@Override
		public void setListenedVirtualModelInstance(DataBinding<VirtualModelInstance<?, ?>> constraint) {
			if (constraint != null) {
				constraint.setOwner(this);
				constraint.setBindingName("listenedVirtualModelInstance");
				constraint.setDeclaredType(FMLRTVirtualModelInstance.class);
				constraint.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.listenedVirtualModelInstance = constraint;
		}

		@Override
		public VirtualModel getListenedVirtualModelType() {
			if (getListenedVirtualModelInstance().isSet() && getListenedVirtualModelInstance().isValid()) {
				Type type = getListenedVirtualModelInstance().getAnalyzedType();
				if (type instanceof VirtualModelInstanceType) {
					return ((VirtualModelInstanceType) type).getVirtualModel();
				}
			}
			return null;
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			super.notifiedBindingChanged(dataBinding);
			if (dataBinding == getListenedVirtualModelInstance()) {
				getPropertyChangeSupport().firePropertyChange("listenedVirtualModelType", null, getListenedVirtualModelType());
			}
		}

		/**
		 * Return the FlexoBehaviour's specific {@link BindingModel}
		 */
		@Override
		protected EventListenerBindingModel makeBindingModel() {
			return new EventListenerBindingModel(this);
		}

		@Override
		protected String getParameterListAsString(boolean fullyQualified) {
			/*if (getEventType() != null) {
				return getEventType().getName();
			}
			return "FlexoEvent";*/
			return "event";
		}

		@Override
		public EventListenerActionFactory getActionFactory(FlexoConceptInstance fci) {
			return new EventListenerActionFactory(this, fci);
		}

		@Override
		public String getDisplayRepresentation() {
			return "listen " + TypeUtils.simpleRepresentation(getEventType()) + " from " + getListenedVirtualModelInstance();
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getListenedVirtualModelInstance().rebuild();
		}

	}

	@DefineValidationRule
	public static class EventTypeMustBeResolved extends ValidationRule<EventTypeMustBeResolved, EventListener> {
		public EventTypeMustBeResolved() {
			super(EventListener.class, "event_type_must_be_resolved");
		}

		@Override
		public ValidationIssue<EventTypeMustBeResolved, EventListener> applyValidation(EventListener el) {

			if (el.getEventType() == null) {
				return new ValidationError<>(this, el, "unresolved_type" /* "unresolved_type_($validable.type)"*/);
			}
			return null;
		}

	}

	@DefineValidationRule
	public static class ListenedVirtualModelInstanceBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<EventListener> {
		public ListenedVirtualModelInstanceBindingIsRequiredAndMustBeValid() {
			super("'listened_virtual_model_instance'_binding_is_not_valid", EventListener.class);
		}

		@Override
		public DataBinding<?> getBinding(EventListener object) {
			return object.getListenedVirtualModelInstance();
		}

	}

}
