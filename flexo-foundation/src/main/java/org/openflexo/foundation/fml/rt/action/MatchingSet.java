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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.editionaction.FinalizeMatching;
import org.openflexo.foundation.fml.rt.editionaction.InitiateMatching;
import org.openflexo.foundation.fml.rt.editionaction.MatchCondition;
import org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance;

/**
 * At run-time, an instance of matching set is used to manage a set of {@link FlexoConceptInstance} beeing matched<br>
 * 
 * This object is the one we work with when using {@link InitiateMatching}, {@link MatchFlexoConceptInstance} and
 * {@link FinalizeMatching}<br>
 * 
 * We maintain first an initial list of {@link FlexoConceptInstance} where matching will be performed (see {@link #getAllInstances()}).
 * 
 * @author sylvain
 * 
 * @see InitiateMatching
 * @see FinalizeMatching
 * @see MatchFlexoConceptInstance
 *
 */
public class MatchingSet {

	private FlexoConceptInstance container = null;
	private FlexoConcept flexoConceptType;
	private List<FlexoConceptInstance> allInstances;
	private List<FlexoConceptInstance> unmatchedInstances;

	/**
	 * Constructor used during InitiateMatching
	 * 
	 * @param initiateMatchingRequest
	 * @param evaluationContext
	 */
	public MatchingSet(InitiateMatching initiateMatchingRequest, RunTimeEvaluationContext evaluationContext) {

		flexoConceptType = initiateMatchingRequest.getFlexoConceptType();

		try {
			container = initiateMatchingRequest.getContainer().getBindingValue(evaluationContext);
			if (container instanceof VirtualModelInstance) {
				if (flexoConceptType != null) {
					allInstances = ((AbstractVirtualModelInstance<?, ?>) container).getFlexoConceptInstances(flexoConceptType);
				}
				else {
					allInstances = ((AbstractVirtualModelInstance<?, ?>) container).getAllRootFlexoConceptInstances();
				}
			}
			else {
				if (flexoConceptType != null) {
					List<FlexoConceptInstance> allEmbedded = container.getEmbeddedFlexoConceptInstances();
					allInstances = new ArrayList<>();
					for (FlexoConceptInstance fci : allEmbedded) {
						if (flexoConceptType.isAssignableFrom(fci.getFlexoConcept())) {
							allInstances.add(fci);
						}
					}
				}
				else {
					allInstances = container.getEmbeddedFlexoConceptInstances();
				}
			}

			// System.out.println("Before filtering: " + allInstances);

			if (initiateMatchingRequest.getConditions().size() > 0) {

				List<FlexoConceptInstance> filteredMatchedInstances = new ArrayList<>();
				for (final FlexoConceptInstance proposedFCI : allInstances) {
					boolean takeIt = true;
					for (MatchCondition condition : initiateMatchingRequest.getConditions()) {
						if (!condition.evaluateCondition(proposedFCI, evaluationContext)) {
							takeIt = false;
							// System.out.println("I dismiss " + proposedFetchResult + " because of " + condition.getCondition() + " valid="
							// + condition.getCondition().isValid());
							break;
						}
					}
					if (takeIt) {
						filteredMatchedInstances.add(proposedFCI);
						// System.out.println("I take " + proposedFetchResult);
					}
					else {
					}
				}
				allInstances = filteredMatchedInstances;

				// System.out.println("After filtering: " + allInstances);

			}

		} catch (TypeMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullReferenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		unmatchedInstances = new ArrayList<>(allInstances);
	}

	/**
	 * Constructor for implicit MatchingSet computation<br>
	 * Type is not known and all types belonging to this virtual model will be returned
	 * 
	 * @param matchRequest
	 * @param evaluationContext
	 */
	public MatchingSet(MatchFlexoConceptInstance matchRequest, RunTimeEvaluationContext evaluationContext) {
		// In this case, we don't know the type, otherwise some instances might be missed
		this.flexoConceptType = null;

		try {
			container = matchRequest.getReceiver().getBindingValue(evaluationContext);
			// System.out.println("container=" + container);
			if (container != null) {
				/*if (flexoConceptType != null) {
					allInstances = ((AbstractVirtualModelInstance<?, ?>) container).getFlexoConceptInstances(flexoConceptType);
				}
				else {*/
				// allInstances = ((AbstractVirtualModelInstance<?, ?>) container).getAllRootFlexoConceptInstances();
				allInstances = ((AbstractVirtualModelInstance<?, ?>) container).getFlexoConceptInstances();
				// }
				// System.out.println("Toutes les instances: " + allInstances);
			}
		} catch (TypeMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullReferenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		unmatchedInstances = new ArrayList<>(allInstances);
		// System.out.println("Et unmatched=" + unmatchedInstances);
	}

	/**
	 * Called by {@link MatchFlexoConceptInstance} when a FCI has been matched
	 * 
	 * @param criterias
	 * @return
	 */
	public FlexoConceptInstance matchFlexoConceptInstance(Hashtable<FlexoProperty<?>, Object> criterias) {
		// System.out.println("MATCH fci on " + allInstances + " for " + flexoConceptType + " with " + criterias);
		for (FlexoConceptInstance fci : unmatchedInstances) {
			boolean allCriteriasMatching = true;
			for (FlexoProperty<?> pr : criterias.keySet()) {
				if (!FlexoObjectImpl.areSameValue(fci.getFlexoPropertyValue(pr), criterias.get(pr))) {
					allCriteriasMatching = false;
				}
			}
			if (allCriteriasMatching) {
				return fci;
			}
		}
		return null;
	}

	/**
	 * Return initial list of {@link FlexoConceptInstance} where matching will be performed
	 * 
	 * @return
	 */
	public List<FlexoConceptInstance> getAllInstances() {
		return allInstances;
	}

	/**
	 * Return initial list of {@link FlexoConceptInstance} which have not been matched
	 * 
	 * @return
	 */
	public List<FlexoConceptInstance> getUnmatchedInstances() {
		return unmatchedInstances;
	}

	/**
	 * Called by {@link MatchFlexoConceptInstance} when a FCI has been found<br>
	 * Instance is removed from unmatched instances
	 * 
	 * @param matchingFlexoConceptInstance
	 *            isntance beeing found
	 * @return
	 */
	public void foundMatchingFlexoConceptInstance(FlexoConceptInstance matchingFlexoConceptInstance) {
		unmatchedInstances.remove(matchingFlexoConceptInstance);
		return;
	}

	/**
	 * Called by {@link MatchFlexoConceptInstance} when a FCI has been created
	 * 
	 * @param matchingFlexoConceptInstance
	 *            instance beeing found
	 */
	public void newFlexoConceptInstance(FlexoConceptInstance newFlexoConceptInstance) {
		// System.out.println("NEW FCI : " + newFlexoConceptInstance);
	}

	/**
	 * Called by {@link MatchFlexoConceptInstance} when a FCI has been finalized (generally deleted)
	 * 
	 * @param flexoConceptInstance
	 *            instance beeing finalized (generally deleted)
	 */
	public void finalizeFlexoConceptInstance(FlexoConceptInstance flexoConceptInstance) {
		// System.out.println("Finalize FCI : " + newFlexoConceptInstance);
		unmatchedInstances.remove(flexoConceptInstance);
	}

}
