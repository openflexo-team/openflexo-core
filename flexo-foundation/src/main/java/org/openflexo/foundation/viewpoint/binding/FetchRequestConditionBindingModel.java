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
import java.beans.PropertyChangeListener;

import org.openflexo.antar.binding.BindingModel;
import org.openflexo.foundation.viewpoint.editionaction.FetchRequestCondition;

/**
 * This is the {@link BindingModel} exposed by a {@link FetchRequestCondition}<br>
 * 
 * @author sylvain
 * 
 */
public class FetchRequestConditionBindingModel extends BindingModel implements PropertyChangeListener {

	private final FetchRequestConditionSelectedBindingVariable selectedBindingVariable;

	private final FetchRequestCondition fetchRequestCondition;

	public FetchRequestConditionBindingModel(FetchRequestCondition fetchRequestCondition) {
		super(fetchRequestCondition.getAction() != null ? fetchRequestCondition.getAction().getBindingModel() : null);

		this.fetchRequestCondition = fetchRequestCondition;
		selectedBindingVariable = new FetchRequestConditionSelectedBindingVariable(fetchRequestCondition);
		addToBindingVariables(selectedBindingVariable);

		if (fetchRequestCondition != null && fetchRequestCondition.getPropertyChangeSupport() != null) {
			fetchRequestCondition.getPropertyChangeSupport().addPropertyChangeListener(this);
		}

	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
		if (fetchRequestCondition != null && fetchRequestCondition.getPropertyChangeSupport() != null) {
			fetchRequestCondition.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		super.delete();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == fetchRequestCondition && evt.getPropertyName().equals(FetchRequestCondition.ACTION_KEY)) {
			setBaseBindingModel(fetchRequestCondition.getAction() != null ? fetchRequestCondition.getAction().getBindingModel() : null);
		}
	}

	public FetchRequestCondition getFetchRequestCondition() {
		return fetchRequestCondition;
	}

	public FetchRequestConditionSelectedBindingVariable getSelectedBindingVariable() {
		return selectedBindingVariable;
	}
}
