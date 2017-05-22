/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.foundation.fml.rt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.BindingValueChangeListener;
import org.openflexo.foundation.fml.EventListener;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoEvent;
import org.openflexo.foundation.fml.rt.action.ActionSchemeAction;
import org.openflexo.foundation.fml.rt.action.ActionSchemeActionType;

/**
 * Base implementation for {@link FMLRunTimeEngine}
 * 
 * Manages a list of {@link AbstractVirtualModelInstance} beeing monitored and executed by current engine instance
 * 
 * 
 * @author sylvain
 * 
 */
public abstract class DefaultFMLRunTimeEngine implements FMLRunTimeEngine, PropertyChangeListener {

	// Stores all AbstractVirtualModelInstance managed by this FMLRunTimeEngine
	private List<AbstractVirtualModelInstance<?, ?>> virtualModelInstances;

	// Stores all EventInstanceListener for this Run-time engine
	private Set<EventInstanceListener> eventListeners;

	// Stores all EventInstanceListener relatively to listened VirtualModelInstance
	private Map<AbstractVirtualModelInstance<?, ?>, Set<EventInstanceListener>> listeningInstances;

	public DefaultFMLRunTimeEngine() {
		virtualModelInstances = new ArrayList<>();
		listeningInstances = new HashMap<>();
		eventListeners = new HashSet<>();
	}

	@Override
	public void addToExecutionContext(AbstractVirtualModelInstance<?, ?> vmi, RunTimeEvaluationContext evaluationContext) {
		System.out.println("************ Adding to ExecutionContext: " + vmi);
		virtualModelInstances.add(vmi);
		if (vmi.getVirtualModel() != null) {
			for (EventListener el : vmi.getVirtualModel().getFlexoBehaviours(EventListener.class)) {
				startEventListening(vmi, el, evaluationContext);
			}
			for (FlexoConcept concept : vmi.getVirtualModel().getFlexoConcepts()) {
				if (requireEventListening(concept)) {
					for (FlexoConceptInstance fci : vmi.getFlexoConceptInstances(concept)) {
						for (EventListener el : concept.getFlexoBehaviours(EventListener.class)) {
							startEventListening(fci, el, evaluationContext);
						}
					}
				}
			}
		}
		vmi.getPropertyChangeSupport().addPropertyChangeListener(this);
	}

	@Override
	public void removeFromExecutionContext(AbstractVirtualModelInstance<?, ?> vmi, RunTimeEvaluationContext evaluationContext) {
		System.out.println("************ Removing from ExecutionContext: " + vmi);
		virtualModelInstances.remove(vmi);
		if (vmi.getVirtualModel() != null) {
			for (EventListener el : vmi.getVirtualModel().getFlexoBehaviours(EventListener.class)) {
				stopEventListening(vmi, el, evaluationContext);
			}
			for (FlexoConcept concept : vmi.getVirtualModel().getFlexoConcepts()) {
				if (requireEventListening(concept)) {
					for (FlexoConceptInstance fci : vmi.getFlexoConceptInstances(concept)) {
						for (EventListener el : concept.getFlexoBehaviours(EventListener.class)) {
							stopEventListening(fci, el, evaluationContext);
						}
					}
				}
			}
		}
		vmi.getPropertyChangeSupport().removePropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof AbstractVirtualModelInstance && virtualModelInstances.contains(evt.getSource())) {
			if (evt.getPropertyName().equals(AbstractVirtualModelInstance.EVENT_FIRED)) {
				System.out.println("Ouaip je recois l'event " + evt.getNewValue());
				receivedEvent((FlexoEventInstance) evt.getNewValue());
			}
			else if (evt.getPropertyName().equals(AbstractVirtualModelInstance.FLEXO_CONCEPT_INSTANCES_KEY)) {
				System.out.println(">>>>>>>>>> hop, je recois un nouvel event " + evt);
				if (evt.getNewValue() instanceof FlexoConceptInstance) {
					FlexoConceptInstance newValue = (FlexoConceptInstance) evt.getNewValue();
					if (requireEventListening(newValue.getFlexoConcept())) {
						for (EventListener el : newValue.getFlexoConcept().getFlexoBehaviours(EventListener.class)) {
							startEventListening(newValue, el, (AbstractVirtualModelInstance) evt.getSource());
						}
					}
				}
				if (evt.getOldValue() instanceof FlexoConceptInstance) {
					FlexoConceptInstance oldValue = (FlexoConceptInstance) evt.getOldValue();
					if (requireEventListening(oldValue.getFlexoConcept())) {
						for (EventListener el : oldValue.getFlexoConcept().getFlexoBehaviours(EventListener.class)) {
							stopEventListening(oldValue, el, (AbstractVirtualModelInstance) evt.getSource());
						}
					}
				}
			}
		}
	}

	private boolean requireEventListening(FlexoConcept concept) {
		return concept.getFlexoBehaviours(EventListener.class).size() > 0;
	}

	public class EventInstanceListener {

		private FlexoConceptInstance instanceBeeingListening;
		private EventListener listener;
		private AbstractVirtualModelInstance<?, ?> listenedVMI;
		private RunTimeEvaluationContext evaluationContext;
		private BindingValueChangeListener<AbstractVirtualModelInstance<?, ?>> bvChangeListener;

		public EventInstanceListener(FlexoConceptInstance instanceBeeingListening, EventListener listener,
				RunTimeEvaluationContext evaluationContext) {
			super();
			this.instanceBeeingListening = instanceBeeingListening;
			this.listener = listener;
			this.evaluationContext = evaluationContext;
		}

		public FlexoConceptInstance getInstanceBeeingListening() {
			return instanceBeeingListening;
		}

		public EventListener getListener() {
			return listener;
		}

		private void listenVMIValueChange() {
			if (bvChangeListener != null) {
				bvChangeListener.stopObserving();
				bvChangeListener.delete();
			}

			DataBinding<AbstractVirtualModelInstance<?, ?>> vmiBinding = listener.getListenedVirtualModelInstance();

			if (vmiBinding != null && vmiBinding.isValid()) {
				bvChangeListener = new BindingValueChangeListener<AbstractVirtualModelInstance<?, ?>>(vmiBinding, evaluationContext, true) {
					@Override
					public void bindingValueChanged(Object source, AbstractVirtualModelInstance<?, ?> newValue) {
						System.out.println(" **** bindingValueChanged() detected for vmi=" + listener.getListenedVirtualModelInstance()
								+ " with newValue=" + newValue + " source=" + source);
						listenTo(newValue);
					}
				};
			}
		}

		private void stopListenVMIValueChange() {
			if (bvChangeListener != null) {
				bvChangeListener.stopObserving();
				bvChangeListener.delete();
				bvChangeListener = null;
			}
		}

		private void listenTo(AbstractVirtualModelInstance<?, ?> vmi) {
			if (vmi != listenedVMI) {
				if (listenedVMI != null) {
					Set<EventInstanceListener> l = listeningInstances.get(listenedVMI);
					if (l != null) {
						l.remove(this);
					}
				}
				if (vmi != null) {
					Set<EventInstanceListener> l = listeningInstances.get(vmi);
					if (l == null) {
						l = new HashSet<>();
						listeningInstances.put(vmi, l);
					}
					if (!l.contains(this)) {
						l.add(this);
					}
					System.out.println("%%%%% OK j'ai rajoute " + this + " pour " + vmi);
				}
				listenedVMI = vmi;
			}
		}

		public void delete() {
			stopListenVMIValueChange();
			if (listenedVMI != null) {
				Set<EventInstanceListener> l = listeningInstances.get(listenedVMI);
				if (l != null) {
					l.remove(this);
				}
			}
		}

	}

	private void startEventListening(FlexoConceptInstance fci, EventListener eventListener, RunTimeEvaluationContext evaluationContext) {
		System.out.println("START listening " + fci + " for listener " + eventListener);

		EventInstanceListener evtListener = new EventInstanceListener(fci, eventListener, evaluationContext);
		eventListeners.add(evtListener);
		evtListener.listenVMIValueChange();
	}

	private void stopEventListening(FlexoConceptInstance fci, EventListener eventListener, RunTimeEvaluationContext evaluationContext) {
		System.out.println("STOP listening " + fci + " for listener " + eventListener);
		EventInstanceListener eventInstanceListener = getEventInstanceListener(fci, eventListener);
		if (eventInstanceListener != null) {
			eventListeners.remove(eventInstanceListener);
			eventInstanceListener.delete();
		}
	}

	// TODO: perf issue : please optimize this
	private EventInstanceListener getEventInstanceListener(FlexoConceptInstance fci, EventListener eventListener) {
		for (EventInstanceListener l : eventListeners) {
			if (l.getInstanceBeeingListening() == fci && l.getListener() == eventListener) {
				return l;
			}
		}
		return null;
	}

	private void receivedEvent(FlexoEventInstance event) {
		if (event.getFlexoConcept() instanceof FlexoEvent) {
			System.out.println("Bon a qui je distribue cet event ???? " + event);
			System.out.println("VMI=" + event.getSourceVirtualModelInstance());
			for (AbstractVirtualModelInstance<?, ?> vmi : listeningInstances.keySet()) {
				System.out.println(" > " + vmi + " -> " + listeningInstances.get(vmi));
			}

			Set<EventInstanceListener> listeners = listeningInstances.get(event.getSourceVirtualModelInstance());
			if (listeners != null) {
				for (EventInstanceListener l : listeners) {
					if (l.getListener().getEventType().isAssignableFrom(event.getFlexoEvent())) {
						System.out.println("Donc ok, je declenche " + l.getListener() + " pour " + l.getInstanceBeeingListening());
						ActionSchemeActionType actionType = new ActionSchemeActionType(l.getListener(), l.getInstanceBeeingListening());
						ActionSchemeAction action = actionType.makeNewAction(l.getInstanceBeeingListening(), null,
								l.getInstanceBeeingListening().getEditor());
						action.doAction();
					}
				}
			}
		}
	}

}
