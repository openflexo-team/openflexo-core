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

package org.openflexo.foundation.ontology.fml.editionaction;

import java.lang.reflect.InvocationTargetException;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.ontology.fml.binding.AbstractAssertionBindingModel;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;

@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractAssertion.AbstractAssertionImpl.class)
public abstract interface AbstractAssertion extends FlexoConceptObject {

	@PropertyIdentifier(type = AddIndividual.class)
	public static final String ACTION_KEY = "action";

	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONDITIONAL_KEY = "conditional";

	public AddIndividual<?, ?, ?, ?> getAction();

	public void setAction(AddIndividual<?, ?, ?, ?> action);

	@Getter(value = CONDITIONAL_KEY)
	@XMLAttribute
	public DataBinding<Boolean> getConditional();

	@Setter(CONDITIONAL_KEY)
	public void setConditional(DataBinding<Boolean> conditional);

	public boolean evaluateCondition(RunTimeEvaluationContext evaluationContext);

	@Override
	public AbstractAssertionBindingModel getBindingModel();

	public static abstract class AbstractAssertionImpl extends FlexoConceptObjectImpl implements AbstractAssertion {

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
		public FlexoConcept getFlexoConcept() {
			if (getAction() != null) {
				return getAction().getFlexoConcept();
			}
			return null;
		}

		@Override
		public boolean evaluateCondition(RunTimeEvaluationContext evaluationContext) {
			if (getConditional().isValid()) {
				try {
					return getConditional().getBindingValue(evaluationContext);
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

		/*@Override
		public FlexoConcept getFlexoConcept() {
			return getFlexoBehaviour() != null ? getFlexoBehaviour().getFlexoConcept() : null;
		}*/

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
				conditional = new DataBinding<>(this, Boolean.class, DataBinding.BindingDefinitionType.GET);
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
