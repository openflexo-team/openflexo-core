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
package org.openflexo.foundation.viewpoint.binding;

import java.beans.PropertyChangeEvent;

import org.openflexo.antar.binding.BindingModel;
import org.openflexo.foundation.viewpoint.editionaction.AbstractAssertion;

/**
 * This is the {@link BindingModel} exposed by a {@link AbstractAssertion}<br>
 * 
 * @author sylvain
 * 
 */
public class AbstractAssertionBindingModel extends BindingModel {

	private final AbstractAssertion abstractAssertion;

	public AbstractAssertionBindingModel(AbstractAssertion abstractAssertion) {
		super(abstractAssertion.getAction() != null ? abstractAssertion.getAction().getBindingModel() : null);
		this.abstractAssertion = abstractAssertion;
		if (abstractAssertion != null && abstractAssertion.getPropertyChangeSupport() != null) {
			abstractAssertion.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
		if (abstractAssertion != null && abstractAssertion.getPropertyChangeSupport() != null) {
			abstractAssertion.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == abstractAssertion) {
			if (evt.getPropertyName().equals(AbstractAssertion.ACTION_KEY)) {
				setBaseBindingModel(abstractAssertion.getAction() != null ? abstractAssertion.getAction().getBindingModel() : null);
			}
		}
	}

	public AbstractAssertion getAbstractAssertion() {
		return abstractAssertion;
	}
}
