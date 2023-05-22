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
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

@ModelEntity
@ImplementationClass(EventListener.EventListenerImpl.class)
@XMLElement
public interface EventListener extends AbstractActionScheme {

	@PropertyIdentifier(type = FlexoEvent.class)
	public static final String EVENT_TYPE_KEY = "eventType";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String LISTENED_VIRTUAL_MODEL_INSTANCE_KEY = "listenedVirtualModelInstance";
	@PropertyIdentifier(type = String.class)
	public static final String EVENT_TYPE_URI_KEY = "flexoEventTypeURI";

	@Getter(value = EVENT_TYPE_URI_KEY)
	@XMLAttribute
	@FMLMigration
	@Deprecated
	public String _getEventTypeURI();

	@Setter(EVENT_TYPE_URI_KEY)
	@FMLMigration
	@Deprecated
	public void _setEventTypeURI(String eventTypeURI);

	@Getter(value = EVENT_TYPE_KEY, ignoreType = true)
	public FlexoConceptInstanceType getEventType();

	@Setter(EVENT_TYPE_KEY)
	public void setEventType(FlexoConceptInstanceType eventType);

	@Getter(value = LISTENED_VIRTUAL_MODEL_INSTANCE_KEY)
	@XMLAttribute
	public DataBinding<VirtualModelInstance<?, ?>> getListenedVirtualModelInstance();

	@Setter(LISTENED_VIRTUAL_MODEL_INSTANCE_KEY)
	public void setListenedVirtualModelInstance(DataBinding<VirtualModelInstance<?, ?>> vmi);

	public VirtualModel getListenedVirtualModelType();

	public FlexoEvent getEvent();

	public static abstract class EventListenerImpl extends AbstractActionSchemeImpl implements EventListener {

		private String _eventTypeURI;
		private FlexoConceptInstanceType eventType;
		private FlexoEvent event;
		private DataBinding<VirtualModelInstance<?, ?>> listenedVirtualModelInstance;

		@Override
		public void finalizeDeserialization() {
			super.finalizeDeserialization();
			/*if (eventType == null && _eventTypeURI != null && getVirtualModelLibrary() != null) {
				eventType = (FlexoEvent) getVirtualModelLibrary().getFlexoConcept(_eventTypeURI, true);
			}*/
		}

		@Override
		public FlexoEvent getEvent() {
			if (getEventType() != null) {
				return (FlexoEvent) getEventType().getFlexoConcept();
			}
			return event;
		}

		@Override
		public FlexoConceptInstanceType getEventType() {
			if (eventType == null && event != null && _eventTypeURI != null && getVirtualModelLibrary() != null) {
				event = (FlexoEvent) getVirtualModelLibrary().getFlexoConcept(_eventTypeURI, false);
				if (event != null) {
					eventType = event.getInstanceType();
				}
			}
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
		public String _getEventTypeURI() {
			if (getEvent() != null) {
				return getEvent().getURI();
			}
			return _eventTypeURI;
		}

		@Override
		public void _setEventTypeURI(String uri) {
			_eventTypeURI = uri;
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
	}

	@DefineValidationRule
	public static class TypeMustBeResolved extends ValidationRule<TypeMustBeResolved, EventListener> {
		public TypeMustBeResolved() {
			super(EventListener.class, "event_type_must_be_resolved");
		}

		@Override
		public ValidationIssue<TypeMustBeResolved, EventListener> applyValidation(EventListener el) {

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
