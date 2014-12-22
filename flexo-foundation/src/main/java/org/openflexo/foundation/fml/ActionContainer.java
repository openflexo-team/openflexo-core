/*
 * (c) Copyright 2010-2011 AgileBirds
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
package org.openflexo.foundation.fml;

import java.util.List;
import java.util.Vector;

import org.openflexo.antar.binding.BindingModel;
import org.openflexo.foundation.fml.binding.ActionContainerBindingModel;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.Implementation;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PastingPoint;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity(isAbstract = true)
@ImplementationClass(ActionContainer.ActionContainerImpl.class)
public interface ActionContainer extends FlexoBehaviourObject {

	@PropertyIdentifier(type = Vector.class)
	public static final String ACTIONS_KEY = "actions";

	@Getter(value = ACTIONS_KEY, cardinality = Cardinality.LIST, inverse = EditionAction.ACTION_CONTAINER_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	@XMLElement
	public List<EditionAction<?, ?>> getActions();

	@Setter(ACTIONS_KEY)
	public void setActions(List<EditionAction<?, ?>> actions);

	@Adder(ACTIONS_KEY)
	@PastingPoint
	public void addToActions(EditionAction<?, ?> aAction);

	@Remover(ACTIONS_KEY)
	public void removeFromActions(EditionAction<?, ?> aAction);

	@Override
	public FlexoBehaviour getFlexoBehaviour();

	@Override
	public BindingModel getBindingModel();

	// public BindingModel getInferedBindingModel();

	public int getIndex(EditionAction<?, ?> action);

	public void insertActionAtIndex(EditionAction<?, ?> action, int index);

	public void actionFirst(EditionAction<?, ?> a);

	public void actionUp(EditionAction<?, ?> a);

	public void actionDown(EditionAction<?, ?> a);

	public void actionLast(EditionAction<?, ?> a);

	public <A extends EditionAction<?, ?>> A createAction(Class<A> actionClass, ModelSlot<?> modelSlot);

	public EditionAction<?, ?> deleteAction(EditionAction<?, ?> anAction);

	public void variableAdded(AssignableAction action);

	public boolean isALastAction(EditionAction<?, ?> a);

	public boolean isAFirstAction(EditionAction<?, ?> a);

	public ActionContainerBindingModel getControlGraphBindingModel();

	@Implementation
	public static abstract class ActionContainerImpl extends FlexoBehaviourObjectImpl implements ActionContainer {
		@Override
		public int getIndex(EditionAction<?, ?> action) {
			return getActions().indexOf(action);
		}

		/* 
		@Override
		public void insertActionAtIndex(EditionAction<?, ?> action, int index) {
			// action.setScheme(getEditionScheme());
			action.setActionContainer(this);
			getActions().add(index, action);
			getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
		}

		@Override
		public void actionFirst(EditionAction<?, ?> a) {
			getActions().remove(a);
			getActions().add(0, a);
			getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
		}

		@Override
		public void actionUp(EditionAction<?, ?> a) {
			int index = getActions().indexOf(a);
			if (index > 0) {
				getActions().remove(a);
				getActions().add(index - 1, a);
				getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
			}
		}

		@Override
		public void actionDown(EditionAction<?, ?> a) {
			int index = getActions().indexOf(a);
			if (index > -1) {
				getActions().remove(a);
				getActions().add(index + 1, a);
				getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
			}
		}

		@Override
		public void actionLast(EditionAction<?, ?> a) {
			getActions().remove(a);
			getActions().add(a);
			getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
		}*/

		@Override
		public boolean isALastAction(EditionAction<?, ?> a) {
			boolean isLast = false;
			if (a != null) {
				if (getIndex(a) == -1) {
					for (EditionAction<?, ?> ea : getActions()) {
						if (ea instanceof ActionContainer) {
							isLast = ((ActionContainer) ea).isALastAction(a);
						}
					}
				} else if (getActions().size() < getIndex(a)) {
					isLast = false;
				}
			}
			return isLast;
		}

		@Override
		public boolean isAFirstAction(EditionAction<?, ?> a) {
			boolean isFirst = false;
			if (a != null) {
				if (getIndex(a) == -1) {
					for (EditionAction<?, ?> ea : getActions()) {
						if (ea instanceof ActionContainer) {
							isFirst = ((ActionContainer) ea).isAFirstAction(a);
						}
					}
				} else if (getIndex(a) == 0) {
					isFirst = true;
				}
			}
			return isFirst;
		}

	}
}
