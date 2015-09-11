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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.fib.annotation.FIBPanel;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
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
import org.openflexo.model.validation.ValidationIssue;

/**
 * Generic {@link FetchRequest} allowing to retrieve a selection of some {@link FlexoConceptInstance} matching some conditions and a given
 * {@link FlexoConcept}.<br>
 * 
 * @author sylvain
 * 
 * @param <M>
 * @param <MM>
 * @param <T>
 */
@FIBPanel("Fib/FML/SelectFlexoConceptInstancePanel.fib")
@ModelEntity
@ImplementationClass(SelectFlexoConceptInstance.SelectFlexoConceptInstanceImpl.class)
@XMLElement
@FML("SelectFlexoConceptInstance")
public interface SelectFlexoConceptInstance extends FetchRequest<FMLRTModelSlot, FlexoConceptInstance> {

	@PropertyIdentifier(type = String.class)
	public static final String FLEXO_CONCEPT_TYPE_URI_KEY = "flexoConceptTypeURI";

	@PropertyIdentifier(type = DataBinding.class)
	public static final String VIRTUAL_MODEL_INSTANCE_KEY = "virtualModelInstance";

	@Getter(value = VIRTUAL_MODEL_INSTANCE_KEY)
	@XMLAttribute
	public DataBinding<VirtualModelInstance> getVirtualModelInstance();

	@Setter(VIRTUAL_MODEL_INSTANCE_KEY)
	public void setVirtualModelInstance(DataBinding<VirtualModelInstance> virtualModelInstance);

	@Getter(value = FLEXO_CONCEPT_TYPE_URI_KEY)
	@XMLAttribute
	public String _getFlexoConceptTypeURI();

	@Setter(FLEXO_CONCEPT_TYPE_URI_KEY)
	public void _setFlexoConceptTypeURI(String flexoConceptTypeURI);

	public FlexoConcept getFlexoConceptType();

	public void setFlexoConceptType(FlexoConcept flexoConceptType);

	public static abstract class SelectFlexoConceptInstanceImpl extends FetchRequestImpl<FMLRTModelSlot, FlexoConceptInstance>
			implements SelectFlexoConceptInstance {

		protected static final Logger logger = FlexoLogger.getLogger(SelectFlexoConceptInstance.class.getPackage().getName());

		private FlexoConcept flexoConceptType;
		private String flexoConceptTypeURI;

		public SelectFlexoConceptInstanceImpl() {
			super();
		}

		private DataBinding<VirtualModelInstance> virtualModelInstance;

		@Override
		public DataBinding<VirtualModelInstance> getVirtualModelInstance() {
			if (virtualModelInstance == null) {
				virtualModelInstance = new DataBinding<VirtualModelInstance>(this, VirtualModelInstance.class,
						DataBinding.BindingDefinitionType.GET);
				virtualModelInstance.setBindingName("virtualModelInstance");
			}
			return virtualModelInstance;
		}

		@Override
		public void setVirtualModelInstance(DataBinding<VirtualModelInstance> aVirtualModelInstance) {
			if (aVirtualModelInstance != null) {
				aVirtualModelInstance.setOwner(this);
				aVirtualModelInstance.setBindingName("virtualModelInstance");
				aVirtualModelInstance.setDeclaredType(VirtualModelInstance.class);
				aVirtualModelInstance.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.virtualModelInstance = aVirtualModelInstance;
			notifiedBindingChanged(virtualModelInstance);
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			System.out.println("J'ai bien detecte que ca a change");
			System.out.println("ResourceData=" + getResourceData());
			super.notifiedBindingChanged(dataBinding);
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append(getTechnologyAdapterIdentifier() + "::" + getImplementedInterface().getSimpleName()
					+ (getVirtualModelInstance() != null ? " from " + getVirtualModelInstance() : "") + " as "
					+ (getFlexoConceptType() != null ? getFlexoConceptType().getName() : "No Type Specified")
					+ (getConditions().size() > 0 ? " " + getWhereClausesFMLRepresentation(context) : ""), context);
			return out.toString();
		}

		@Override
		public FlexoConceptInstanceType getFetchedType() {
			return FlexoConceptInstanceType.getFlexoConceptInstanceType(getFlexoConceptType());
		}

		@Override
		public String _getFlexoConceptTypeURI() {
			if (flexoConceptType != null) {
				return flexoConceptType.getURI();
			}
			return flexoConceptTypeURI;
		}

		@Override
		public void _setFlexoConceptTypeURI(String flexoConceptURI) {
			this.flexoConceptTypeURI = flexoConceptURI;
		}

		// private boolean isUpdatingBindingModels = false;

		@Override
		public FlexoConcept getFlexoConceptType() {
			// System.out.println("getFlexoConceptType() for " + flexoConceptTypeURI);
			// System.out.println("vm=" + getVirtualModel());
			// System.out.println("ep=" + getFlexoConcept());
			// System.out.println("ms=" + getModelSlot());
			// if (getModelSlot() instanceof FMLRTModelSlot) {
			// System.out.println("ms.vm=" + ((FMLRTModelSlot) getModelSlot()).getAddressedVirtualModel());
			// }
			if (flexoConceptType == null && flexoConceptTypeURI != null && getOwningVirtualModel() != null) {
				flexoConceptType = getOwningVirtualModel().getFlexoConcept(flexoConceptTypeURI);
				/*if (!isUpdatingBindingModels) {
					isUpdatingBindingModels = true;
					for (FlexoBehaviour s : getFlexoConcept().getEditionSchemes()) {
						s.updateBindingModels();
					}
					isUpdatingBindingModels = false;
				}*/
			}
			if (flexoConceptType == null && flexoConceptTypeURI != null && getFlexoConcept() instanceof VirtualModel) {
				flexoConceptType = ((VirtualModel) getFlexoConcept()).getFlexoConcept(flexoConceptTypeURI);
			}
			if (flexoConceptType == null && flexoConceptTypeURI != null && getModelSlot() instanceof FMLRTModelSlot) {
				if (getModelSlot().getAddressedVirtualModel() != null) {
					flexoConceptType = getModelSlot().getAddressedVirtualModel().getFlexoConcept(flexoConceptTypeURI);
				}
			}
			// System.out.println("return " + flexoConceptType);
			return flexoConceptType;
		}

		@Override
		public void setFlexoConceptType(FlexoConcept flexoConceptType) {
			if (flexoConceptType != this.flexoConceptType) {
				FlexoConcept oldValue = this.flexoConceptType;
				this.flexoConceptType = flexoConceptType;
				/*for (FlexoBehaviour s : getFlexoConcept().getFlexoBehaviours()) {
					s.updateBindingModels();
				}*/
				getPropertyChangeSupport().firePropertyChange("flexoConceptType", oldValue, oldValue);
			}
		}

		@Override
		public String getStringRepresentation() {
			return getImplementedInterface().getSimpleName()
					+ (getFlexoConceptType() != null ? " : " + getFlexoConceptType().getName() : "")
					/*+ (StringUtils.isNotEmpty(getAssignation().toString()) ? " (" + getAssignation().toString() + ")" : "")*/;
		}

		public VirtualModelInstance getVirtualModelInstance(RunTimeEvaluationContext evaluationContext) {
			if (getVirtualModelInstance() != null && getVirtualModelInstance().isSet() && getVirtualModelInstance().isValid()) {
				try {
					return getVirtualModelInstance().getBindingValue(evaluationContext);
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			if (getModelSlot() instanceof FMLRTModelSlot && evaluationContext instanceof FlexoBehaviourAction) {
				ModelSlotInstance modelSlotInstance = ((FlexoBehaviourAction<?, ?, ?>) evaluationContext).getVirtualModelInstance()
						.getModelSlotInstance(getModelSlot());
				if (modelSlotInstance != null) {
					// System.out.println("modelSlotInstance=" + modelSlotInstance + " model=" + modelSlotInstance.getModel());
					return (VirtualModelInstance) modelSlotInstance.getAccessedResourceData();
				}
				else {
					logger.warning("Cannot find ModelSlotInstance for " + getModelSlot());
				}
				return ((FlexoBehaviourAction<?, ?, ?>) evaluationContext).getVirtualModelInstance();
			}
			return null;

		}

		@Override
		public List<FlexoConceptInstance> execute(RunTimeEvaluationContext evaluationContext) {
			VirtualModelInstance vmi = getVirtualModelInstance(evaluationContext);
			if (vmi != null) {
				System.out.println("Returning " + vmi.getFlexoConceptInstances(getFlexoConceptType()));
				return filterWithConditions(vmi.getFlexoConceptInstances(getFlexoConceptType()), evaluationContext);
			}
			else {
				logger.warning(
						getStringRepresentation() + " : Cannot find virtual model instance on which to apply SelectFlexoConceptInstance");
				logger.warning("Additional info: getVirtualModelInstance()=" + getVirtualModelInstance());
				return null;
			}
		}
	}

	@DefineValidationRule
	public static class VirtualModelInstanceBindingIsRequiredAndMustBeValid
			extends BindingIsRequiredAndMustBeValid<SelectFlexoConceptInstance> {
		public VirtualModelInstanceBindingIsRequiredAndMustBeValid() {
			super("'virtual_model_instance'_binding_is_not_valid", SelectFlexoConceptInstance.class);
		}

		@Override
		public DataBinding<VirtualModelInstance> getBinding(SelectFlexoConceptInstance object) {
			return object.getVirtualModelInstance();
		}

		@Override
		public ValidationIssue<BindingIsRequiredAndMustBeValid<SelectFlexoConceptInstance>, SelectFlexoConceptInstance> applyValidation(
				SelectFlexoConceptInstance object) {
			ValidationIssue<BindingIsRequiredAndMustBeValid<SelectFlexoConceptInstance>, SelectFlexoConceptInstance> returned = super.applyValidation(
					object);
			if (returned instanceof UndefinedRequiredBindingIssue) {
				((UndefinedRequiredBindingIssue) returned).addToFixProposals(new UseLocalVirtualModelInstance());
			}
			return returned;
		}

		protected static class UseLocalVirtualModelInstance
				extends FixProposal<BindingIsRequiredAndMustBeValid<SelectFlexoConceptInstance>, SelectFlexoConceptInstance> {

			public UseLocalVirtualModelInstance() {
				super("sets_virtual_model_instance_to_'virtualModelInstance'_(local_virtual_model_instance)");
			}

			@Override
			protected void fixAction() {
				SelectFlexoConceptInstance action = getValidable();
				action.setVirtualModelInstance(new DataBinding<VirtualModelInstance>("virtualModelInstance"));
			}
		}

	}

}
