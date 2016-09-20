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

package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewType;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.View;

/**
 * This is the {@link BindingModel} exposed by a ViewPoint<br>
 * 
 * Allows reflexive access to the {@link ViewPoint} itself<br>
 * If a {@link ViewPoint} defines some statically-defined VirtualModelInstance, those instances are reflected here<br>
 * 
 * Note that default {@link RunTimeEvaluationContext} corresponding to this {@link BindingModel} is a {@link View}
 * 
 * 
 * @author sylvain
 * 
 */
public class ViewPointBindingModel extends VirtualModelBindingModel implements PropertyChangeListener {

	// private final ViewPoint viewPoint;

	public static final String REFLEXIVE_ACCESS_PROPERTY = "viewPoint";
	public static final String VIEW_PROPERTY = "view";
	// TODO : to be renamed ResourceCenter
	public static final String RC_PROPERTY = "resourceCenter";
	@Deprecated
	public static final String PROJECT_PROPERTY = "project";

	private final BindingVariable reflexiveAccessBindingVariable;
	private final BindingVariable viewBindingVariable;
	private final BindingVariable projectBindingVariable;

	/**
	 * Build a new {@link BindingModel} dedicated to a ViewPoint
	 * 
	 * @param viewPoint
	 */
	public ViewPointBindingModel(ViewPoint viewPoint) {
		super(viewPoint);
		// this.viewPoint = viewPoint;
		/*if (viewPoint != null && viewPoint.getPropertyChangeSupport() != null) {
			viewPoint.getPropertyChangeSupport().addPropertyChangeListener(this);
		}*/
		viewBindingVariable = new BindingVariable(VIEW_PROPERTY, viewPoint != null ? ViewType.getViewType(viewPoint) : View.class);
		addToBindingVariables(viewBindingVariable);

		reflexiveAccessBindingVariable = new BindingVariable(REFLEXIVE_ACCESS_PROPERTY, ViewPoint.class);
		addToBindingVariables(reflexiveAccessBindingVariable);

		projectBindingVariable = new BindingVariable(PROJECT_PROPERTY, FlexoProject.class);
		addToBindingVariables(projectBindingVariable);
	}

	public ViewPoint getViewPoint() {
		return (ViewPoint) getVirtualModel();
	}

	public BindingVariable getViewBindingVariable() {
		return viewBindingVariable;
	}

	/**
	 * Return the reflexive access {@link BindingVariable}<br>
	 * (Allows reflexive access to the {@link ViewPoint} itself)
	 * 
	 * @return
	 */
	@Override
	public BindingVariable getReflexiveAccessBindingVariable() {
		return reflexiveAccessBindingVariable;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

	}

	@Override
	protected ViewType getVirtualModelInstanceType() {
		return ViewType.getViewType(getViewPoint());
	}

	/**
	 * Delete this {@link BindingModel}
	 */
	/*@Override
	public void delete() {
		super.delete();
		if (viewPoint != null && viewPoint.getPropertyChangeSupport() != null) {
			viewPoint.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
	}*/
}
