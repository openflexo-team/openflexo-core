/*
  * (c) Copyright 2014-2015 Openflexo
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
package org.openflexo.foundation.fml.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openflexo.antar.binding.BindingEvaluationContext;
import org.openflexo.antar.binding.BindingModel;
import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fmlrt.View;

/**
 * This is the {@link BindingModel} exposed by a ViewPoint<br>
 * 
 * Allows reflexive access to the {@link ViewPoint} itself<br>
 * If a {@link ViewPoint} defines some statically-defined VirtualModelInstance, those instances are reflected here<br>
 * 
 * Note that default {@link BindingEvaluationContext} corresponding to this {@link BindingModel} is a {@link View}
 * 
 * 
 * @author sylvain
 * 
 */
public class ViewPointBindingModel extends BindingModel implements PropertyChangeListener {

	private final ViewPoint viewPoint;

	private final BindingVariable reflexiveAccessBindingVariable;

	public static final String REFLEXIVE_ACCESS_PROPERTY = "viewPointDefinition";

	/**
	 * Build a new {@link BindingModel} dedicated to a ViewPoint
	 * 
	 * @param viewPoint
	 */
	public ViewPointBindingModel(ViewPoint viewPoint) {
		super();
		this.viewPoint = viewPoint;
		if (viewPoint != null && viewPoint.getPropertyChangeSupport() != null) {
			viewPoint.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
		reflexiveAccessBindingVariable = new BindingVariable(REFLEXIVE_ACCESS_PROPERTY, ViewPoint.class);
		addToBindingVariables(reflexiveAccessBindingVariable);
	}

	public ViewPoint getViewPoint() {
		return viewPoint;
	}

	/**
	 * Return the reflexive access {@link BindingVariable}<br>
	 * (Allows reflexive access to the {@link ViewPoint} itself)
	 * 
	 * @return
	 */
	public BindingVariable getReflexiveAccessBindingVariable() {
		return reflexiveAccessBindingVariable;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
		super.delete();
		if (viewPoint != null && viewPoint.getPropertyChangeSupport() != null) {
			viewPoint.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
	}
}
