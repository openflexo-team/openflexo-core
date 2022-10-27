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
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.binding.BindingPathChangeListener;
import org.openflexo.foundation.fml.EventListener;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.rt.action.EventListenerAction;
import org.openflexo.foundation.fml.rt.action.EventListenerActionFactory;

/**
 * Base implementation for {@link FMLRunTimeEngine}
 * 
 * Manages a list of {@link FMLRTVirtualModelInstance} beeing monitored and executed by current engine instance
 * 
 * 
 * @author sylvain
 * 
 */
public abstract class DefaultFMLRunTimeEngine implements FMLRunTimeEngine, PropertyChangeListener {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DefaultFMLRunTimeEngine.class.getPackage().getName());

	// Stores all FMLRTVirtualModelInstance managed by this FMLRunTimeEngine
	private List<VirtualModelInstance<?, ?>> virtualModelInstances;

	// Stores all EventInstanceListener for this Run-time engine
	private Set<EventInstanceListener> eventListeners;

	// Stores all EventInstanceListener relatively to listened FMLRTVirtualModelInstance
	protected Map<VirtualModelInstance<?, ?>, Set<EventInstanceListener>> listeningInstances;

	public DefaultFMLRunTimeEngine() {
		virtualModelInstances = new ArrayList<>();
		listeningInstances = new HashMap<>();
		eventListeners = new HashSet<>();
	}

	@Override
	public void addToExecutionContext(VirtualModelInstance<?, ?> vmi, RunTimeEvaluationContext evaluationContext) {
		// System.out.println("************ Adding to ExecutionContext: " + (vmi != null ? vmi.getURI() : "null"));
		virtualModelInstances.add(vmi);
		if (vmi.getVirtualModel() != null) {
			for (EventListener el : vmi.getVirtualModel().getAccessibleFlexoBehaviours(EventListener.class, false)) {
				startEventListening(vmi, el, evaluationContext);
			}
			for (FlexoConcept concept : vmi.getVirtualModel().getFlexoConcepts()) {
				if (requireEventListening(concept)) {
					for (FlexoConceptInstance fci : vmi.getFlexoConceptInstances(concept)) {
						for (EventListener el : concept.getAccessibleFlexoBehaviours(EventListener.class, false)) {
							startEventListening(fci, el, fci);
						}
					}
				}
			}
		}
		vmi.getPropertyChangeSupport().addPropertyChangeListener(this);
	}

	@Override
	public void removeFromExecutionContext(VirtualModelInstance<?, ?> vmi, RunTimeEvaluationContext evaluationContext) {
		// System.out.println("************ Removing from ExecutionContext: " + (vmi != null ? vmi.getURI() : "null"));
		virtualModelInstances.remove(vmi);
		if (vmi.getVirtualModel() != null) {
			for (EventListener el : vmi.getVirtualModel().getAccessibleFlexoBehaviours(EventListener.class, false)) {
				stopEventListening(vmi, el, evaluationContext);
			}
			for (FlexoConcept concept : vmi.getVirtualModel().getFlexoConcepts()) {
				if (requireEventListening(concept)) {
					for (FlexoConceptInstance fci : vmi.getFlexoConceptInstances(concept)) {
						for (EventListener el : concept.getAccessibleFlexoBehaviours(EventListener.class, false)) {
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
		if (evt.getSource() instanceof FMLRTVirtualModelInstance && virtualModelInstances.contains(evt.getSource())) {
			if (evt.getPropertyName().equals(FMLRTVirtualModelInstance.EVENT_FIRED)) {
				// System.out.println("Receiving EVENT_FIRED " + evt.getNewValue());
				receivedEvent((FlexoEventInstance) evt.getNewValue());
			}
			else if (evt.getPropertyName().equals(FMLRTVirtualModelInstance.FINALIZE_FLEXO_CONCEPT_INSTANCE_ADDING_KEY)
					&& evt.getNewValue() instanceof FlexoConceptInstance) {
				FlexoConceptInstance newValue = (FlexoConceptInstance) evt.getNewValue();
				if (requireEventListening(newValue.getFlexoConcept())) {
					for (EventListener el : newValue.getFlexoConcept().getAccessibleFlexoBehaviours(EventListener.class, false)) {
						startEventListening(newValue, el, /*(FMLRTVirtualModelInstance) evt.getSource()*/newValue);
					}
				}
			}
			if (evt.getPropertyName().equals(FMLRTVirtualModelInstance.FLEXO_CONCEPT_INSTANCES_KEY)
					&& evt.getOldValue() instanceof FlexoConceptInstance) {
				FlexoConceptInstance oldValue = (FlexoConceptInstance) evt.getOldValue();
				if (requireEventListening(oldValue.getFlexoConcept())) {
					for (EventListener el : oldValue.getFlexoConcept().getAccessibleFlexoBehaviours(EventListener.class, false)) {
						stopEventListening(oldValue, el, (FMLRTVirtualModelInstance) evt.getSource());
					}
				}
			}
		}

	}

	private static boolean requireEventListening(FlexoConcept concept) {
		if (concept == null) {
			return false;
		}
		return concept.getAccessibleFlexoBehaviours(EventListener.class, false).size() > 0;
	}

	public class EventInstanceListener {

		private FlexoConceptInstance instanceBeeingListening;
		private EventListener listener;
		private VirtualModelInstance<?, ?> listenedVMI;
		private RunTimeEvaluationContext evaluationContext;
		private BindingPathChangeListener<VirtualModelInstance<?, ?>> bvChangeListener;

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

			DataBinding<VirtualModelInstance<?, ?>> vmiBinding = listener.getListenedVirtualModelInstance();

			if (vmiBinding != null && vmiBinding.isValid()) {
				bvChangeListener = new BindingPathChangeListener<VirtualModelInstance<?, ?>>(vmiBinding, evaluationContext, true) {
					@Override
					public void bindingValueChanged(Object source, VirtualModelInstance<?, ?> newValue) {
						// System.out.println(" **** bindingValueChanged() detected for fci=" + instanceBeeingListening + "vmi="
						// + listener.getListenedVirtualModelInstance() + " with newValue=" + newValue + " source=" + source);
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

		private void listenTo(VirtualModelInstance<?, ?> vmi) {

			if (vmi == null) {
				logger.warning(
						"Listening to a null VMI: " + listener.getListenedVirtualModelInstance() + " from " + instanceBeeingListening);
			}

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
						// System.out.println("%%%%% Added " + this + " for " + vmi);
					}
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
		// System.out.println("START listening " + fci + " for listener " + eventListener);
		EventInstanceListener evtListener = new EventInstanceListener(fci, eventListener, evaluationContext);
		eventListeners.add(evtListener);
		evtListener.listenVMIValueChange();
		// System.out.println("Listened VMI=" + evtListener.listenedVMI);
	}

	private void stopEventListening(FlexoConceptInstance fci, EventListener eventListener, RunTimeEvaluationContext evaluationContext) {
		// System.out.println("STOP listening " + fci + " for listener " + eventListener);
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

	protected void fireEventListener(FlexoConceptInstance fci, EventListener eventListener, FlexoEventInstance event) {
		// System.out.println("-----------> fireEventListener " + eventListener + " for " + fci);
		// System.out.println("executing:\n" + eventListener.getFMLRepresentation());
		EventListenerActionFactory actionType = new EventListenerActionFactory(eventListener, fci);
		EventListenerAction action = actionType.makeNewAction(fci, null, fci.getEditor());
		action.setEventInstance(event);
		action.doAction();
	}

}
