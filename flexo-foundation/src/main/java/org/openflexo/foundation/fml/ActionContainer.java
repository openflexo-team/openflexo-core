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

package org.openflexo.foundation.fml;

import java.util.List;
import java.util.Vector;

import org.openflexo.connie.BindingModel;
import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
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

// TODO: this should disappear to be replaced by FMLControlGraph
@Deprecated
@ModelEntity(isAbstract = true)
@ImplementationClass(ActionContainer.ActionContainerImpl.class)
public interface ActionContainer extends FlexoBehaviourObject {

	@PropertyIdentifier(type = Vector.class)
	public static final String ACTIONS_KEY = "actions";

	@Getter(value = ACTIONS_KEY, cardinality = Cardinality.LIST, inverse = EditionAction.ACTION_CONTAINER_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	@XMLElement
	public List<EditionAction> getActions();

	@Setter(ACTIONS_KEY)
	public void setActions(List<EditionAction> actions);

	@Adder(ACTIONS_KEY)
	@PastingPoint
	public void addToActions(EditionAction aAction);

	@Remover(ACTIONS_KEY)
	public void removeFromActions(EditionAction aAction);

	@Override
	public FlexoBehaviour getFlexoBehaviour();

	@Override
	public BindingModel getBindingModel();

	// public BindingModel getInferedBindingModel();

	public int getIndex(EditionAction action);

	// public void insertActionAtIndex(EditionAction action, int index);

	public void actionFirst(EditionAction a);

	public void actionUp(EditionAction a);

	public void actionDown(EditionAction a);

	public void actionLast(EditionAction a);

	public <A extends TechnologySpecificAction<?, ?>> A createAction(Class<A> actionClass, ModelSlot<?> modelSlot);

	public EditionAction deleteAction(EditionAction anAction);

	public void variableAdded(AssignableAction action);

	public boolean isALastAction(EditionAction a);

	public boolean isAFirstAction(EditionAction a);

	// public ActionContainerBindingModel getControlGraphBindingModel();

	@Implementation
	public static abstract class ActionContainerImpl extends FlexoBehaviourObjectImpl implements ActionContainer {
		@Override
		public int getIndex(EditionAction action) {
			return getActions().indexOf(action);
		}

		/* 
		@Override
		public void insertActionAtIndex(EditionAction action, int index) {
			// action.setScheme(getEditionScheme());
			action.setActionContainer(this);
			getActions().add(index, action);
			getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
		}

		@Override
		public void actionFirst(EditionAction a) {
			getActions().remove(a);
			getActions().add(0, a);
			getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
		}

		@Override
		public void actionUp(EditionAction a) {
			int index = getActions().indexOf(a);
			if (index > 0) {
				getActions().remove(a);
				getActions().add(index - 1, a);
				getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
			}
		}

		@Override
		public void actionDown(EditionAction a) {
			int index = getActions().indexOf(a);
			if (index > -1) {
				getActions().remove(a);
				getActions().add(index + 1, a);
				getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
			}
		}

		@Override
		public void actionLast(EditionAction a) {
			getActions().remove(a);
			getActions().add(a);
			getPropertyChangeSupport().firePropertyChange(ACTIONS_KEY, null, getActions());
		}*/

		@Override
		public boolean isALastAction(EditionAction a) {
			boolean isLast = false;
			if (a != null) {
				if (getIndex(a) == -1) {
					for (EditionAction ea : getActions()) {
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
		public boolean isAFirstAction(EditionAction a) {
			boolean isFirst = false;
			if (a != null) {
				if (getIndex(a) == -1) {
					for (EditionAction ea : getActions()) {
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
