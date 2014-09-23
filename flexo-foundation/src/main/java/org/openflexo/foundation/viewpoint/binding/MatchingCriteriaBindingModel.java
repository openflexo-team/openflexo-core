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
import org.openflexo.foundation.viewpoint.editionaction.MatchingCriteria;

/**
 * This is the {@link BindingModel} exposed by a MatchingCriteria<br>
 * 
 * @author sylvain
 * 
 */
public class MatchingCriteriaBindingModel extends BindingModel {

	private final MatchingCriteria criteria;

	public MatchingCriteriaBindingModel(MatchingCriteria criteria) {
		super(criteria.getAction() != null ? criteria.getAction().getBindingModel() : null);
		this.criteria = criteria;
		if (criteria != null && criteria.getPropertyChangeSupport() != null) {
			criteria.getPropertyChangeSupport().addPropertyChangeListener(this);
		}
	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
		if (criteria != null && criteria.getPropertyChangeSupport() != null) {
			criteria.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == criteria) {
			if (evt.getPropertyName().equals(MatchingCriteria.ACTION_KEY)) {
				setBaseBindingModel(criteria.getAction() != null ? criteria.getAction().getBindingModel() : null);
			}
		}
	}

	public MatchingCriteria getMatchingCriteria() {
		return criteria;
	}
}
