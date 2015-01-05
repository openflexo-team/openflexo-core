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

import org.openflexo.antar.binding.BindingModel;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;

/**
 * This is the {@link BindingModel} exposed by a {@link FetchRequest}<br>
 * 
 * @author sylvain
 * 
 */
public class FetchRequestBindingModel extends EditionActionBindingModel {

	public FetchRequestBindingModel(FetchRequest<?, ?> fetchRequest) {
		super(fetchRequest);
		if (fetchRequest.getEmbeddingIteration() != null) {
			setBaseBindingModel(fetchRequest.getEmbeddingIteration().getBindingModel());
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == getEditionAction()) {
			if (evt.getPropertyName().equals(EditionAction.ACTION_CONTAINER_KEY) || evt.getPropertyName().equals("embeddingIteration")) {
				if (getEditionAction().getEmbeddingIteration() != null) {
					setBaseBindingModel(getEditionAction().getEmbeddingIteration().getBindingModel());
				} else {
					// Already done in superclass
					// setBaseBindingModel(getEditionAction().getOwner() != null ? getEditionAction().getOwner().getBaseBindingModel(
					// getEditionAction()) : null);
				}
			}
		}
	}

	@Override
	public FetchRequest<?, ?> getEditionAction() {
		return (FetchRequest<?, ?>) super.getEditionAction();
	}

}
