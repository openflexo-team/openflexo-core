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
package org.openflexo.foundation.viewpoint.editionaction;

import java.lang.reflect.InvocationTargetException;

import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.view.action.FlexoBehaviourAction;
import org.openflexo.foundation.viewpoint.FlexoBehaviour;
import org.openflexo.foundation.viewpoint.FlexoBehaviourObject;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.binding.AbstractAssertionBindingModel;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;

@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractAssertion.AbstractAssertionImpl.class)
public abstract interface AbstractAssertion extends FlexoBehaviourObject {

	@PropertyIdentifier(type = AddIndividual.class)
	public static final String ACTION_KEY = "action";

	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONDITIONAL_KEY = "conditional";

	public AddIndividual<?, ?> getAction();

	public void setAction(AddIndividual<?, ?> action);

	@Getter(value = CONDITIONAL_KEY)
	@XMLAttribute
	public DataBinding<Boolean> getConditional();

	@Setter(CONDITIONAL_KEY)
	public void setConditional(DataBinding<Boolean> conditional);

	public boolean evaluateCondition(FlexoBehaviourAction action);

	@Override
	public AbstractAssertionBindingModel getBindingModel();

	public static abstract class AbstractAssertionImpl extends FlexoBehaviourObjectImpl implements AbstractAssertion {

		// private AddIndividual<?, ?> _action;
		private DataBinding<Boolean> conditional;
		private AbstractAssertionBindingModel bindingModel;

		public AbstractAssertionImpl() {
			super();
		}

		@Override
		public String getURI() {
			return null;
		}

		/*@Override
		public void setAction(AddIndividual<?, ?> action) {
			_action = action;
		}

		@Override
		public AddIndividual<?, ?> getAction() {
			return _action;
		}*/

		@Override
		public FlexoBehaviour getFlexoBehaviour() {
			if (getAction() != null) {
				return getAction().getFlexoBehaviour();
			}
			return null;
		}

		@Override
		public VirtualModel getVirtualModel() {
			if (getAction() != null) {
				return getAction().getVirtualModel();
			}
			return null;
		}

		@Override
		public boolean evaluateCondition(FlexoBehaviourAction action) {
			if (getConditional().isValid()) {
				try {
					return getConditional().getBindingValue(action);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			return true;
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			return getFlexoBehaviour() != null ? getFlexoBehaviour().getFlexoConcept() : null;
		}

		@Override
		public AbstractAssertionBindingModel getBindingModel() {
			if (bindingModel == null) {
				bindingModel = new AbstractAssertionBindingModel(this);
			}
			return bindingModel;
		}

		@Override
		public DataBinding<Boolean> getConditional() {
			if (conditional == null) {
				conditional = new DataBinding<Boolean>(this, Boolean.class, DataBinding.BindingDefinitionType.GET);
				conditional.setBindingName("conditional");
			}
			return conditional;
		}

		@Override
		public void setConditional(DataBinding<Boolean> conditional) {
			if (conditional != null) {
				conditional.setOwner(this);
				conditional.setDeclaredType(Boolean.class);
				conditional.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				conditional.setBindingName("conditional");
			}
			this.conditional = conditional;
		}

	}
}
