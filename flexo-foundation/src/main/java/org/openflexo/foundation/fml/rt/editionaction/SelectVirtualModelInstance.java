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

package org.openflexo.foundation.fml.rt.editionaction;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.validation.FixProposal;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;

/**
 * Generic {@link FetchRequest} allowing to retrieve a selection of some {@link VirtualModelInstance} matching some conditions and a given
 * {@link VirtualModel}.<br>
 * 
 * @author sylvain
 * 
 */
@FIBPanel("Fib/FML/SelectVirtualModelInstancePanel.fib")
@ModelEntity
@ImplementationClass(SelectVirtualModelInstance.SelectVirtualModelInstanceImpl.class)
@XMLElement
@FML("SelectVirtualModelInstance")
public interface SelectVirtualModelInstance extends FetchRequest<FMLRTModelSlot, VirtualModelInstance> {

	@PropertyIdentifier(type = String.class)
	public static final String VIRTUAL_MODEL_TYPE_URI_KEY = "virtualModelTypeURI";

	@PropertyIdentifier(type = DataBinding.class)
	public static final String VIEW_KEY = "view";

	@Getter(value = VIEW_KEY)
	@XMLAttribute
	public DataBinding<View> getView();

	@Setter(VIEW_KEY)
	public void setView(DataBinding<View> view);

	@Getter(value = VIRTUAL_MODEL_TYPE_URI_KEY)
	@XMLAttribute
	public String _getVirtualModelTypeURI();

	@Setter(VIRTUAL_MODEL_TYPE_URI_KEY)
	public void _setVirtualModelTypeURI(String virtualModelTypeURI);

	public VirtualModelResource getVirtualModelType();

	public void setVirtualModelType(VirtualModelResource virtualModelType);

	public static abstract class SelectVirtualModelInstanceImpl extends FetchRequestImpl<FMLRTModelSlot, VirtualModelInstance>
			implements SelectVirtualModelInstance {

		protected static final Logger logger = FlexoLogger.getLogger(SelectVirtualModelInstance.class.getPackage().getName());

		private VirtualModelResource virtualModelType;
		private String virtualModelTypeURI;

		public SelectVirtualModelInstanceImpl() {
			super();
		}

		@Override
		public TechnologyAdapter getModelSlotTechnologyAdapter() {
			if (getServiceManager() != null) {
				return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
			}
			return super.getModelSlotTechnologyAdapter();
		}

		private DataBinding<View> view;

		@Override
		public DataBinding<View> getView() {
			if (view == null) {
				view = new DataBinding<View>(this, View.class, DataBinding.BindingDefinitionType.GET);
				view.setBindingName("view");
			}
			return view;
		}

		@Override
		public void setView(DataBinding<View> aView) {
			if (aView != null) {
				aView.setOwner(this);
				aView.setBindingName("view");
				aView.setDeclaredType(View.class);
				aView.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.view = aView;
			notifiedBindingChanged(view);
		}

		@Override
		public String getParametersStringRepresentation() {
			String whereClauses = getWhereClausesFMLRepresentation(null);
			return "(type=" + (getVirtualModelType() != null ? getVirtualModelType().getName() : "null")
					+ (whereClauses != null ? "," + whereClauses : "") + ")";
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append(getTechnologyAdapterIdentifier() + "::" + getImplementedInterface().getSimpleName()
					+ (getView() != null ? " from " + getView() : "") + " as "
					+ (getVirtualModelType() != null ? getVirtualModelType().getName() : "No Type Specified")
					+ (getConditions().size() > 0 ? " " + getWhereClausesFMLRepresentation(context) : ""), context);
			return out.toString();
		}

		@Override
		public VirtualModelInstanceType getFetchedType() {
			try {
				return VirtualModelInstanceType
						.getVirtualModelInstanceType(getVirtualModelType() != null ? getVirtualModelType().getResourceData(null) : null);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ResourceLoadingCancelledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FlexoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public String _getVirtualModelTypeURI() {
			if (virtualModelType != null) {
				return virtualModelType.getURI();
			}
			return virtualModelTypeURI;
		}

		@Override
		public void _setVirtualModelTypeURI(String virtualModelTypeURI) {
			this.virtualModelTypeURI = virtualModelTypeURI;
		}

		@Override
		public VirtualModelResource getVirtualModelType() {
			if (virtualModelType == null && virtualModelTypeURI != null && getViewPoint() != null) {
				virtualModelType = ((ViewPointResource) getViewPoint().getResource()).getVirtualModelResource(virtualModelTypeURI);
			}
			return virtualModelType;
		}

		@Override
		public void setVirtualModelType(VirtualModelResource virtualModelType) {
			if (virtualModelType != this.virtualModelType) {
				VirtualModelResource oldValue = this.virtualModelType;
				this.virtualModelType = virtualModelType;
				getPropertyChangeSupport().firePropertyChange("virtualModelType", oldValue, virtualModelType);
			}
		}

		public View getView(RunTimeEvaluationContext evaluationContext) {
			if (getView() != null && getView().isSet() && getView().isValid()) {
				try {
					return getView().getBindingValue(evaluationContext);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			return null;

		}

		@Override
		public List<VirtualModelInstance> execute(RunTimeEvaluationContext evaluationContext) {
			View view = getView(evaluationContext);
			if (view != null) {
				try {
					return filterWithConditions(view.getVirtualModelInstancesForVirtualModel(getVirtualModelType().getResourceData(null)),
							evaluationContext);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ResourceLoadingCancelledException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FlexoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
			else {
				logger.warning(getStringRepresentation() + " : Cannot find view on which to apply SelectVirtualModelInstance");
				logger.warning("Additional info: getView()=" + getView());
				return null;
			}
		}
	}

	@DefineValidationRule
	public static class SelectVirtualModelInstanceMustAddressAFlexoConceptType
			extends ValidationRule<SelectVirtualModelInstanceMustAddressAFlexoConceptType, SelectVirtualModelInstance> {
		public SelectVirtualModelInstanceMustAddressAFlexoConceptType() {
			super(SelectVirtualModelInstance.class, "select_virtual_model_instance_action_must_address_a_valid_virtual_model_type");
		}

		@Override
		public ValidationIssue<SelectVirtualModelInstanceMustAddressAFlexoConceptType, SelectVirtualModelInstance> applyValidation(
				SelectVirtualModelInstance action) {
			if (action.getVirtualModelType() == null) {
				return new ValidationError<SelectVirtualModelInstanceMustAddressAFlexoConceptType, SelectVirtualModelInstance>(this, action,
						"select_virtual_model_instance_action_doesn't_define_any_virtual_model_type");
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class ViewBindingIsRequiredAndMustBeValid extends BindingIsRequiredAndMustBeValid<SelectVirtualModelInstance> {
		public ViewBindingIsRequiredAndMustBeValid() {
			super("'view'_binding_is_not_valid", SelectVirtualModelInstance.class);
		}

		@Override
		public DataBinding<View> getBinding(SelectVirtualModelInstance object) {
			return object.getView();
		}

		@Override
		public ValidationIssue<BindingIsRequiredAndMustBeValid<SelectVirtualModelInstance>, SelectVirtualModelInstance> applyValidation(
				SelectVirtualModelInstance object) {
			ValidationIssue<BindingIsRequiredAndMustBeValid<SelectVirtualModelInstance>, SelectVirtualModelInstance> returned = super.applyValidation(
					object);
			if (returned instanceof UndefinedRequiredBindingIssue) {
				((UndefinedRequiredBindingIssue) returned).addToFixProposals(new UseDefaultView());
			}
			return returned;
		}

		protected static class UseDefaultView
				extends FixProposal<BindingIsRequiredAndMustBeValid<SelectVirtualModelInstance>, SelectVirtualModelInstance> {

			public UseDefaultView() {
				super("sets_view_to_'view'");
			}

			@Override
			protected void fixAction() {
				SelectVirtualModelInstance action = getValidable();
				action.setView(new DataBinding<View>("view"));
			}
		}

	}

}
