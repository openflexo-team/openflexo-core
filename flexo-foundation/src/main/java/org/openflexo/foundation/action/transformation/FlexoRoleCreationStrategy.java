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

import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;

/**
 * This abstract class is the base class for a element replacing transformation<br>
 * The goal of this transformation is to create a new {@link FlexoRole} with some charracteristics of focused object.
 * 
 * @author sylvain
 *
 */
public abstract class FlexoRoleCreationStrategy<A extends AbstractDeclareInFlexoConcept<A, T1, T2>, R extends FlexoRole<T1>, T1 extends TechnologyObject<?>, T2 extends TechnologyObject<?>>
		extends TransformationStrategy<A> {

	private static final String NO_TRANSFORMATION_ACTION_DEFINED = "no_transformation_action_defined";
	private static final String NO_FLEXO_CONCEPT_DEFINED = "no_flexo_concept_defined";
	private static final String NO_ROLE_NAME_DEFINED = "no_role_name_defined";
	private static final String DUPLICATED_ROLE_NAME = "duplicated_role_name";

	private String newRoleName;

	public FlexoRoleCreationStrategy(A transformationAction) {
		super(transformationAction);
	}

	/**
	 * Return type of role
	 * 
	 * @return
	 */
	public abstract Class<R> getRoleType();

	/**
	 * Return name of role to be created
	 * 
	 * @return
	 */
	public String getNewRoleName() {
		return newRoleName;
	}

	/**
	 * Sets name of role to be created
	 * 
	 * @param newRoleName
	 */
	public void setNewRoleName(String newRoleName) {
		if (!newRoleName.equals(getNewRoleName())) {
			String oldValue = getNewRoleName();
			this.newRoleName = newRoleName;
			getPropertyChangeSupport().firePropertyChange("newRoleName", oldValue, newRoleName);
		}
	}

	@Override
	public boolean isValid() {
		if (getTransformationAction() == null) {
			setIssueMessage(FlexoLocalization.localizedForKey(NO_TRANSFORMATION_ACTION_DEFINED), IssueMessageType.ERROR);
			return false;
		}
		if (getTransformationAction().getFlexoConcept() == null) {
			setIssueMessage(FlexoLocalization.localizedForKey(NO_FLEXO_CONCEPT_DEFINED), IssueMessageType.ERROR);
			return false;
		}
		if (StringUtils.isEmpty(getNewRoleName())) {
			setIssueMessage(FlexoLocalization.localizedForKey(NO_ROLE_NAME_DEFINED), IssueMessageType.ERROR);
			return false;
		}
		if (getTransformationAction().getFlexoConcept().getAccessibleProperty(getNewRoleName()) != null) {
			setIssueMessage(FlexoLocalization.localizedForKey(DUPLICATED_ROLE_NAME), IssueMessageType.ERROR);
			return false;
		}
		return true;
	}

	/**
	 * Called to execute transformation beeing encoded by this {@link TransformationStrategy}, asserting that current strategy is valid<br>
	 * We will create a new {@link FlexoRole} with some charracteristics of focused object.
	 * 
	 * @see #isValid()
	 * @return role beeing created
	 */
	@Override
	public final R performStrategy() {
		newFlexoRole = createNewFlexoRole();
		return newFlexoRole;
	}

	/**
	 * Internally used create a new {@link FlexoRole} with some characteristics of focused object.
	 * 
	 * @return role beeing created
	 */
	protected abstract R createNewFlexoRole();

	private R newFlexoRole;

	public R getNewFlexoRole() {
		return newFlexoRole;
	}
}
