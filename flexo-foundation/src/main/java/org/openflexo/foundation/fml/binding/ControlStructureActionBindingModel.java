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
import org.openflexo.foundation.fml.controlgraph.ControlStructureAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;

/**
 * This is the {@link BindingModel} exposed by a {@link EditionAction}<br>
 * This {@link BindingModel} is based on ActionContainer's {@link BindingModel}
 * 
 * @author sylvain
 * 
 */
public class ControlStructureActionBindingModel<CG extends ControlStructureAction> extends ControlGraphBindingModel<CG> {

	public ControlStructureActionBindingModel(CG editionAction) {
		super(editionAction);
	}

	/**
	 * Delete this {@link BindingModel}
	 */
	@Override
	public void delete() {
		super.delete();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getSource() == getEditionAction()) {
			if (evt.getPropertyName().equals(EditionAction.ACTION_CONTAINER_KEY)) {
				setBaseBindingModel(getEditionAction().getOwner() != null ? getEditionAction().getOwner().getBaseBindingModel(
						getEditionAction()) : null);
			}
		}
	}

	public CG getEditionAction() {
		return getControlGraph();
	}

}
