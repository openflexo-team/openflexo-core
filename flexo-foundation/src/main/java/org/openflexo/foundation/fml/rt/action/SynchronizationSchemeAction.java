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

package org.openflexo.foundation.fml.rt.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstanceObject;

public class SynchronizationSchemeAction extends
		FlexoBehaviourAction<SynchronizationSchemeAction, SynchronizationScheme, AbstractVirtualModelInstance<?, ?>> {

	private static final Logger logger = Logger.getLogger(SynchronizationSchemeAction.class.getPackage().getName());

	private final SynchronizationSchemeActionType actionType;

	public SynchronizationSchemeAction(SynchronizationSchemeActionType actionType, AbstractVirtualModelInstance<?, ?> focusedObject,
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
	 * Return the {@link FlexoConceptInstance} on which this {@link FlexoBehaviour} is applied.<br>
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
	public SynchronizationScheme getFlexoBehaviour() {
		return getSynchronizationScheme();
	}

	@Override
	protected void doAction(Object context) throws FlexoException {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Perform action " + actionType);
		}

		if (getSynchronizationScheme() != null && getSynchronizationScheme().evaluateCondition(actionType.getFlexoConceptInstance())) {

			// System.out.println("Executing code: ");
			// System.out.println(getSynchronizationScheme().getFMLRepresentation());
			executeControlGraph();
		}
	}

	@Override
	protected void executeControlGraph() throws FlexoException {
		beginSynchronization();
		super.executeControlGraph();
		endSynchronization();
	}

	/**
	 * Return {@link VirtualModelInstance} in which synchronized {@link VirtualModelInstance} does exist
	 */
	@Override
	public AbstractVirtualModelInstance<?, ?> retrieveVirtualModelInstance() {
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
		// System.out.println("BEGIN synchronization on " + getVirtualModelInstance());
		episToBeRemoved = new ArrayList<FlexoConceptInstance>();
		episToBeRemoved.addAll(getFocusedObject().getFlexoConceptInstances());
	}

	public void endSynchronization() {
		// System.out.println("END synchronization on " + getVirtualModelInstance());
		for (FlexoConceptInstance epi : episToBeRemoved) {
			epi.delete();
			getVirtualModelInstance().removeFromFlexoConceptInstances(epi);
		}
	}

	@Override
	public void foundMatchingFlexoConceptInstance(FlexoConceptInstance matchingFlexoConceptInstance) {
		episToBeRemoved.remove(matchingFlexoConceptInstance);
	}

	@Override
	public Object getValue(BindingVariable variable) {
		return super.getValue(variable);
	}

}
