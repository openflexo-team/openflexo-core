/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexodiagram, a component of the software infrastructure 
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

package org.openflexo.foundation.action.transformation;

import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.toolbox.StringUtils;

/**
 * This abstract class is the base class for a {@link FlexoConcept} creation strategy, as it is exposed in the
 * {@link AbstractDeclareInFlexoConcept} class
 * 
 * @author sylvain
 *
 */
public abstract class FlexoConceptCreationStrategy<A extends AbstractDeclareInFlexoConcept<A, ?, ?>> extends TransformationStrategy<A> {

	private static final String NO_FLEXO_NAME_DEFINED = "no_flexo_name_defined";

	private String flexoConceptName;

	private FlexoConcept newFlexoConcept;

	public FlexoConceptCreationStrategy(A transformationAction) {
		super(transformationAction);
	}

	public String getFlexoConceptName() {
		return flexoConceptName;
	}

	public void setFlexoConceptName(String flexoConceptName) {
		if ((flexoConceptName == null && this.flexoConceptName != null)
				|| (flexoConceptName != null && !flexoConceptName.equals(this.flexoConceptName))) {
			String oldValue = this.flexoConceptName;
			this.flexoConceptName = flexoConceptName;
			getPropertyChangeSupport().firePropertyChange("flexoConceptName", oldValue, flexoConceptName);
		}
	}

	@Override
	public boolean isValid() {
		if (StringUtils.isEmpty(getFlexoConceptName())) {
			setIssueMessage(getLocales().localizedForKey(NO_FLEXO_NAME_DEFINED), IssueMessageType.ERROR);
			return false;
		}
		return true;
	}

	/**
	 * Called to execute transformation beeing encoded by this {@link TransformationStrategy}, asserting that current strategy is valid<br>
	 * We will create a new {@link FlexoConcept} with some characteristics of focused object.
	 * 
	 * @see #isValid()
	 * @return {@link FlexoConcept} which has been created
	 */
	@Override
	public FlexoConcept performStrategy() {
		// Create new flexo concept
		newFlexoConcept = getTransformationAction().getFactory().newFlexoConcept();
		newFlexoConcept.setName(getFlexoConceptName());

		// And add the newly created flexo concept
		getTransformationAction().getVirtualModel().addToFlexoConcepts(newFlexoConcept);

		return newFlexoConcept;

	}

	/**
	 * Return the new {@link FlexoConcept} beeing created
	 * 
	 * @return
	 */
	public FlexoConcept getNewFlexoConcept() {
		return newFlexoConcept;
	}

}
