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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.BindingValue;
import org.openflexo.foundation.fml.rt.TypeAwareModelSlotInstance;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.foundation.technologyadapter.URIUtilities;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.toolbox.StringUtils;

@ModelEntity
@ImplementationClass(URIParameter.URIParameterImpl.class)
@XMLElement
public interface URIParameter extends InnerModelSlotParameter<TypeAwareModelSlot<?, ?>> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String BASE_URI_KEY = "baseURI";

	@Getter(value = BASE_URI_KEY)
	@XMLAttribute(xmlTag = "base")
	public DataBinding<String> getBaseURI();

	@Setter(BASE_URI_KEY)
	public void setBaseURI(DataBinding<String> baseURI);

	public List<FlexoBehaviourParameter> getDependancies();

	public static abstract class URIParameterImpl extends InnerModelSlotParameterImpl<TypeAwareModelSlot<?, ?>> implements URIParameter {

		private DataBinding<String> baseURI;

		public URIParameterImpl() {
			super();
		}

		@Override
		public TypeAwareModelSlot getModelSlot() {
			TypeAwareModelSlot<?, ?> returned = super.getModelSlot();
			if (returned != null) {
				return returned;
			}
			else {
				if (getFlexoBehaviour() != null && getFlexoBehaviour().getOwningVirtualModel() != null) {
					if (getFlexoBehaviour().getOwningVirtualModel().getModelSlots(TypeAwareModelSlot.class).size() > 0) {
						return getFlexoBehaviour().getOwningVirtualModel().getModelSlots(TypeAwareModelSlot.class).get(0);
					}
				}
			}
			return null;
		}

		@Override
		public DataBinding<String> getBaseURI() {
			if (baseURI == null) {
				baseURI = new DataBinding<String>(this, String.class, BindingDefinitionType.GET);
				baseURI.setBindingName("baseURI");
			}
			return baseURI;
		}

		@Override
		public void setBaseURI(DataBinding<String> baseURI) {
			if (baseURI != null) {
				baseURI.setOwner(this);
				baseURI.setBindingName("baseURI");
				baseURI.setDeclaredType(String.class);
				baseURI.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.baseURI = baseURI;
		}

		@Override
		public Type getType() {
			return String.class;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public List<TypeAwareModelSlot> getAccessibleModelSlots() {
			return getOwningVirtualModel().getModelSlots(TypeAwareModelSlot.class);
		}

		@Override
		public WidgetType getWidget() {
			return WidgetType.URI;
		}

		@Override
		public boolean getIsRequired() {
			return true;
		}

		@Override
		public boolean isValid(FlexoBehaviourAction action, Object value) {
			if (!(value instanceof String)) {
				return false;
			}

			String proposedURI = (String) value;

			if (StringUtils.isEmpty(proposedURI)) {
				return false;
			}
			if (proposalIsNotUnique(action, proposedURI)) {
				// declared_uri_must_be_unique_please_choose_an_other_uri
				return false;
			}
			else if (proposalIsWellFormed(action, proposedURI) == false) {
				// declared_uri_is_not_well_formed_please_choose_an_other_uri
				return false;
			}

			return true;
		}

		/*private String getActionOntologyURI(FlexoBehaviourAction<?, ?, ?> action) {
			return action.getProject().getURI();
		}*/

		private FlexoModel<?, ?> getFlexoModel(FlexoBehaviourAction<?, ?, ?> action) {
			TypeAwareModelSlotInstance msInstance = (TypeAwareModelSlotInstance) action.getVirtualModelInstance()
					.getModelSlotInstance(getModelSlot());
			return msInstance.getModel();
		}

		private boolean proposalIsNotUnique(FlexoBehaviourAction<?, ?, ?> action, String uriProposal) {
			return URIUtilities.isDuplicatedURI(getFlexoModel(action), uriProposal);
			// return action.getProject().isDuplicatedURI(getActionOntologyURI(action), uriProposal);
		}

		private boolean proposalIsWellFormed(FlexoBehaviourAction<?, ?, ?> action, String uriProposal) {
			return URIUtilities.testValidURI(getFlexoModel(action), uriProposal);
			// return action.getProject().testValidURI(getActionOntologyURI(action), uriProposal);
		}

		@Override
		public Object getDefaultValue(FlexoBehaviourAction<?, ?, ?> action) {
			if (getBaseURI().isValid()) {
				String baseProposal = null;
				try {
					baseProposal = getBaseURI().getBindingValue(action);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				if (baseProposal == null) {
					return null;
				}
				TypeAwareModelSlot modelSlot = getModelSlot();

				return modelSlot.generateUniqueURIName(
						(TypeAwareModelSlotInstance<?, ?, ?>) action.getVirtualModelInstance().getModelSlotInstance(modelSlot),
						baseProposal);

				/*baseProposal = JavaUtils.getClassName(baseProposal);
				String proposal = baseProposal;
				Integer i = null;
				while (proposalIsNotUnique(action, proposal)) {
					if (i == null) {
						i = 1;
					} else {
						i++;
					}
					proposal = baseProposal + i;
				}
				System.out.println("Generate URI " + proposal);
				return proposal;*/
			}
			return null;
		}

		@Override
		public List<FlexoBehaviourParameter> getDependancies() {
			if (getBaseURI().isSet() && getBaseURI().isValid()) {
				List<FlexoBehaviourParameter> returned = new ArrayList<FlexoBehaviourParameter>();
				for (BindingValue bv : getBaseURI().getExpression().getAllBindingValues()) {
					FlexoBehaviourParameter p = getBehaviour().getParameter(bv.getVariableName());
					if (p != null) {
						returned.add(p);
					}
				}
				return returned;
			}
			else {
				return null;
			}
		}

	}

	@DefineValidationRule
	public static class BaseURIBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<URIParameter> {
		public BaseURIBindingIsRequiredAndMustBeValid() {
			super("'base_uri'_binding_is_required", URIParameter.class);
		}

		@Override
		public DataBinding<String> getBinding(URIParameter object) {
			return object.getBaseURI();
		}

	}

}
