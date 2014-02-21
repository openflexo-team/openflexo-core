/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.view.action;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.view.FlexoConceptInstance;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.view.VirtualModelInstanceObject;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.EditionScheme;
import org.openflexo.foundation.viewpoint.PatternRole;
import org.openflexo.foundation.viewpoint.SynchronizationScheme;
import org.openflexo.foundation.viewpoint.binding.PatternRoleBindingVariable;

public class SynchronizationSchemeAction extends
		EditionSchemeAction<SynchronizationSchemeAction, SynchronizationScheme, VirtualModelInstance> {

	private static final Logger logger = Logger.getLogger(SynchronizationSchemeAction.class.getPackage().getName());

	private final SynchronizationSchemeActionType actionType;

	public SynchronizationSchemeAction(SynchronizationSchemeActionType actionType, VirtualModelInstance focusedObject,
			Vector<VirtualModelInstanceObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		this.actionType = actionType;
	}

	public SynchronizationScheme getSynchronizationScheme() {
		if (actionType != null) {
			return actionType.getSynchronizationScheme();
		}
		return null;
	}

	/**
	 * Return the {@link FlexoConceptInstance} on which this {@link EditionScheme} is applied.<br>
	 * Note that here, the returned {@link FlexoConceptInstance} is the {@link VirtualModelInstance} which is to be synchronized
	 * 
	 * @return
	 */
	@Override
	public VirtualModelInstance getFlexoConceptInstance() {
		if (actionType != null) {
			return (VirtualModelInstance) actionType.getFlexoConceptInstance();
		}
		return null;
	}

	@Override
	public SynchronizationScheme getEditionScheme() {
		return getSynchronizationScheme();
	}

	@Override
	protected void doAction(Object context) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Perform action " + actionType);
		}

		if (getSynchronizationScheme() != null && getSynchronizationScheme().evaluateCondition(actionType.getFlexoConceptInstance())) {
			applyEditionActions();
		}
	}

	@Override
	protected void applyEditionActions() {
		beginSynchronization();
		super.applyEditionActions();
		endSynchronization();
	}

	/**
	 * Return {@link VirtualModelInstance} in which synchronized {@link VirtualModelInstance} does exist
	 */
	@Override
	public VirtualModelInstance retrieveVirtualModelInstance() {
		/*if (getFlexoConceptInstance() instanceof VirtualModelInstance) {
			return (VirtualModelInstance) getFlexoConceptInstance();
		}*/
		if (getFlexoConceptInstance() != null) {
			return getFlexoConceptInstance().getVirtualModelInstance();
		}
		/*if (getFocusedObject() instanceof DiagramElement<?>) {
			return ((DiagramElement<?>) getFocusedObject()).getDiagram();
		}*/
		return null;
	}

	private List<FlexoConceptInstance> episToBeRemoved;

	public void beginSynchronization() {
		System.out.println("BEGIN synchronization on " + getVirtualModelInstance());
		episToBeRemoved = new ArrayList<FlexoConceptInstance>();
		episToBeRemoved.addAll(getVirtualModelInstance().getAllEPInstances());
	}

	public void endSynchronization() {
		System.out.println("END synchronization on " + getVirtualModelInstance());
		for (FlexoConceptInstance epi : episToBeRemoved) {
			System.out.println("Deleting " + epi);
			epi.delete();
		}
	}

	public FlexoConceptInstance matchFlexoConceptInstance(FlexoConcept flexoConceptType, Hashtable<PatternRole, Object> criterias) {
		System.out.println("MATCH epi on " + getVirtualModelInstance() + " for " + flexoConceptType + " with " + criterias);
		for (FlexoConceptInstance epi : getVirtualModelInstance().getEPInstances(flexoConceptType)) {
			boolean allCriteriasMatching = true;
			for (PatternRole pr : criterias.keySet()) {
				if (!FlexoObjectImpl.areSameValue(epi.getPatternActor(pr), criterias.get(pr))) {
					allCriteriasMatching = false;
				}
			}
			if (allCriteriasMatching) {
				return epi;
			}
		}
		return null;
	}

	public void foundMatchingFlexoConceptInstance(FlexoConceptInstance matchingFlexoConceptInstance) {
		System.out.println("FOUND matching : " + matchingFlexoConceptInstance);
		episToBeRemoved.remove(matchingFlexoConceptInstance);
	}

	public void newFlexoConceptInstance(FlexoConceptInstance newFlexoConceptInstance) {
		System.out.println("NEW EPI : " + newFlexoConceptInstance);

	}

	@Override
	public Object getValue(BindingVariable variable) {
		if (variable instanceof PatternRoleBindingVariable) {
			return getFlexoConceptInstance().getPatternActor(((PatternRoleBindingVariable) variable).getPatternRole());
		} else if (variable.getVariableName().equals(EditionScheme.THIS)) {
			return getFlexoConceptInstance();
		}
		return super.getValue(variable);
	}

	@Override
	public void setValue(Object value, BindingVariable variable) {
		if (variable instanceof PatternRoleBindingVariable) {
			getFlexoConceptInstance().setPatternActor(value, ((PatternRoleBindingVariable) variable).getPatternRole());
			return;
		}
		super.setValue(value, variable);
	}

}
