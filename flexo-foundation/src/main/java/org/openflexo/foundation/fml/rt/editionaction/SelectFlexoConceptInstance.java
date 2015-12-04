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
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.fib.annotation.FIBPanel;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
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
 * Generic {@link FetchRequest} allowing to retrieve a selection of some {@link FlexoConceptInstance} matching some conditions and a given
 * {@link FlexoConcept}.<br>
 * 
 * @author sylvain
 * 
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
	public DataBinding<AbstractVirtualModelInstance<?, ?>> getVirtualModelInstance();

	@Setter(VIRTUAL_MODEL_INSTANCE_KEY)
	public void setVirtualModelInstance(DataBinding<AbstractVirtualModelInstance<?, ?>> virtualModelInstance);

	@Getter(value = FLEXO_CONCEPT_TYPE_URI_KEY)
	@XMLAttribute
	public String _getFlexoConceptTypeURI();

	@Setter(FLEXO_CONCEPT_TYPE_URI_KEY)
	public void _setFlexoConceptTypeURI(String flexoConceptTypeURI);

	public FlexoConcept getFlexoConceptType();

	public void setFlexoConceptType(FlexoConcept flexoConceptType);

	/**
	 * Return the {@link AbstractVirtualModel} beeing addressed by this action, according to the {@link #getVirtualModelInstance()} binding
	 * 
	 * @return
	 */
	public AbstractVirtualModel<?> getAddressedVirtualModel();

	public static abstract class SelectFlexoConceptInstanceImpl extends FetchRequestImpl<FMLRTModelSlot, FlexoConceptInstance> implements
			SelectFlexoConceptInstance {

		protected static final Logger logger = FlexoLogger.getLogger(SelectFlexoConceptInstance.class.getPackage().getName());

		private FlexoConcept flexoConceptType;
		private String flexoConceptTypeURI;

		public SelectFlexoConceptInstanceImpl() {
			super();
		}

		@Override
		public TechnologyAdapter getModelSlotTechnologyAdapter() {
			if (getServiceManager() != null) {
				return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
			}
			return super.getModelSlotTechnologyAdapter();
		}

		private DataBinding<AbstractVirtualModelInstance<?, ?>> virtualModelInstance;

		@Override
		public DataBinding<AbstractVirtualModelInstance<?, ?>> getVirtualModelInstance() {
			if (virtualModelInstance == null) {
				virtualModelInstance = new DataBinding<AbstractVirtualModelInstance<?, ?>>(this, AbstractVirtualModelInstance.class,
						DataBinding.BindingDefinitionType.GET);
				virtualModelInstance.setBindingName("virtualModelInstance");
			}
			return virtualModelInstance;
		}

		@Override
		public void setVirtualModelInstance(DataBinding<AbstractVirtualModelInstance<?, ?>> aVirtualModelInstance) {
			if (aVirtualModelInstance != null) {
				aVirtualModelInstance.setOwner(this);
				aVirtualModelInstance.setBindingName("virtualModelInstance");
				aVirtualModelInstance.setDeclaredType(AbstractVirtualModelInstance.class);
				aVirtualModelInstance.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.virtualModelInstance = aVirtualModelInstance;
			notifiedBindingChanged(virtualModelInstance);
		}

		@Override
		public String getParametersStringRepresentation() {
			String whereClauses = getWhereClausesFMLRepresentation(null);
			return "(type=" + (getFlexoConceptType() != null ? getFlexoConceptType().getName() : "null")
					+ (whereClauses != null ? "," + whereClauses : "") + ")";
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

			if (flexoConceptType == null && flexoConceptTypeURI != null && getAddressedVirtualModel() != null) {
				flexoConceptType = getAddressedVirtualModel().getFlexoConcept(flexoConceptTypeURI);
			}

			/*
			if (flexoConceptType == null && flexoConceptTypeURI != null && getOwningVirtualModel() != null) {
				flexoConceptType = getOwningVirtualModel().getFlexoConcept(flexoConceptTypeURI);
			}
			if (flexoConceptType == null && flexoConceptTypeURI != null && getFlexoConcept() instanceof VirtualModel) {
				flexoConceptType = ((VirtualModel) getFlexoConcept()).getFlexoConcept(flexoConceptTypeURI);
			}
			if (flexoConceptType == null && flexoConceptTypeURI != null && getModelSlot() instanceof FMLRTModelSlot) {
				if (getModelSlot().getAddressedVirtualModel() != null) {
					flexoConceptType = getModelSlot().getAddressedVirtualModel().getFlexoConcept(flexoConceptTypeURI);
				}
			}*/

			// System.out.println("return " + flexoConceptType);
			return flexoConceptType;
		}

		/**
		 * Return the {@link AbstractVirtualModel} beeing addressed by this action, according to the {@link #getVirtualModelInstance()}
		 * binding
		 * 
		 * @return
		 */
		@Override
		public AbstractVirtualModel<?> getAddressedVirtualModel() {
			if (getVirtualModelInstance() != null && getVirtualModelInstance().isSet() && getVirtualModelInstance().isValid()) {
				Type vmiType = getVirtualModelInstance().getAnalyzedType();
				if (vmiType instanceof VirtualModelInstanceType) {
					return ((VirtualModelInstanceType) vmiType).getVirtualModel();
				}
			}
			// I could not find VM, trying to "guess" (TODO: remove this hask ?)
			if (getFlexoConcept() instanceof AbstractVirtualModel) {
				return (AbstractVirtualModel<?>) getFlexoConcept();
			}
			if (getModelSlot() instanceof FMLRTModelSlot) {
				return getModelSlot().getAddressedVirtualModel();
			}
			return getOwningVirtualModel();
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

		public AbstractVirtualModelInstance<?, ?> getVirtualModelInstance(RunTimeEvaluationContext evaluationContext) {
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
				} else {
					logger.warning("Cannot find ModelSlotInstance for " + getModelSlot());
				}
				return ((FlexoBehaviourAction<?, ?, ?>) evaluationContext).getVirtualModelInstance();
			}
			return null;

		}

		@Override
		public List<FlexoConceptInstance> execute(RunTimeEvaluationContext evaluationContext) {
			AbstractVirtualModelInstance<?, ?> vmi = getVirtualModelInstance(evaluationContext);
			if (vmi != null) {
				// System.out.println("Returning " + vmi.getFlexoConceptInstances(getFlexoConceptType()));
				return filterWithConditions(vmi.getFlexoConceptInstances(getFlexoConceptType()), evaluationContext);
			} else {
				logger.warning(getStringRepresentation()
						+ " : Cannot find virtual model instance on which to apply SelectFlexoConceptInstance");
				logger.warning("getVirtualModelInstance()=" + getVirtualModelInstance());
				/*logger.warning("evaluationContext=" + evaluationContext);
				logger.warning("isSet=" + getVirtualModelInstance().isSet());
				logger.warning("isValid=" + getVirtualModelInstance().isValid());
				logger.warning("fci=" + evaluationContext.getFlexoConceptInstance());
				logger.warning("vmi=" + evaluationContext.getVirtualModelInstance());
				try {
					logger.warning("value=" + getVirtualModelInstance().getBindingValue(evaluationContext));
				} catch (TypeMismatchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullReferenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				logger.warning(getOwner().getFMLRepresentation());
				return null;
			}
		}
	}

	@DefineValidationRule
	public static class SelectFlexoConceptInstanceMustAddressAFlexoConceptType extends
			ValidationRule<SelectFlexoConceptInstanceMustAddressAFlexoConceptType, SelectFlexoConceptInstance> {
		public SelectFlexoConceptInstanceMustAddressAFlexoConceptType() {
			super(SelectFlexoConceptInstance.class, "select_flexo_concept_instance_action_must_address_a_valid_flexo_concept_type");
		}

		@Override
		public ValidationIssue<SelectFlexoConceptInstanceMustAddressAFlexoConceptType, SelectFlexoConceptInstance> applyValidation(
				SelectFlexoConceptInstance action) {
			if (action.getFlexoConceptType() == null) {
				return new ValidationError<SelectFlexoConceptInstanceMustAddressAFlexoConceptType, SelectFlexoConceptInstance>(this,
						action, "select_flexo_concept_instance_action_doesn't_define_any_flexo_concept_type");
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class VirtualModelInstanceBindingIsRequiredAndMustBeValid extends
			BindingIsRequiredAndMustBeValid<SelectFlexoConceptInstance> {
		public VirtualModelInstanceBindingIsRequiredAndMustBeValid() {
			super("'virtual_model_instance'_binding_is_not_valid", SelectFlexoConceptInstance.class);
		}

		@Override
		public DataBinding<AbstractVirtualModelInstance<?, ?>> getBinding(SelectFlexoConceptInstance object) {
			return object.getVirtualModelInstance();
		}

		@Override
		public ValidationIssue<BindingIsRequiredAndMustBeValid<SelectFlexoConceptInstance>, SelectFlexoConceptInstance> applyValidation(
				SelectFlexoConceptInstance object) {
			ValidationIssue<BindingIsRequiredAndMustBeValid<SelectFlexoConceptInstance>, SelectFlexoConceptInstance> returned = super
					.applyValidation(object);
			if (returned instanceof UndefinedRequiredBindingIssue) {
				((UndefinedRequiredBindingIssue) returned).addToFixProposals(new UseLocalVirtualModelInstance());
			} else {
				DataBinding<AbstractVirtualModelInstance<?, ?>> binding = getBinding(object);
				if (binding.getAnalyzedType() instanceof VirtualModelInstanceType && object.getFlexoConceptType() != null) {
					if (object.getFlexoConceptType().getVirtualModel() != ((VirtualModelInstanceType) binding.getAnalyzedType())
							.getVirtualModel()) {
						returned = new ValidationError(this, object, "incompatible_virtual_model_type");

						/*System.out.println(object.getRootOwner().getFMLRepresentation());
						System.out.println("FC=" + object.getRootOwner().getFlexoConcept());
						System.out.println("VM=" + object.getOwningVirtualModel());
						System.out.println("modelSlots=" + object.getOwningVirtualModel().getModelSlots(FMLRTModelSlot.class));*/

						// Attempt to find some solutions...

						for (FMLRTModelSlot ms : object.getOwningVirtualModel().getModelSlots(FMLRTModelSlot.class)) {
							// System.out.println("modelSlot " + ms + " vm=" + ms.getAddressedVirtualModel());
							if (object.getFlexoConceptType().getVirtualModel().isAssignableFrom(ms.getAddressedVirtualModel())) {
								((ValidationError) returned).addToFixProposals(new UseFMLRTModelSlot(ms));
							}
						}

						if (object.getRootOwner().getFlexoConcept() instanceof AbstractVirtualModel) {
							for (FMLRTModelSlot ms : ((AbstractVirtualModel<?>) object.getRootOwner().getFlexoConcept())
									.getModelSlots(FMLRTModelSlot.class)) {
								// System.out.println("modelSlot " + ms + " vm=" + ms.getAddressedVirtualModel());
								if (object.getFlexoConceptType().getVirtualModel().isAssignableFrom(ms.getAddressedVirtualModel())) {
									((ValidationError) returned).addToFixProposals(new UseFMLRTModelSlot(ms));
								}
							}
						}

					}
				}
			}
			return returned;
		}

		protected static class UseLocalVirtualModelInstance extends
				FixProposal<BindingIsRequiredAndMustBeValid<SelectFlexoConceptInstance>, SelectFlexoConceptInstance> {

			public UseLocalVirtualModelInstance() {
				super("sets_virtual_model_instance_to_'virtualModelInstance'_(local_virtual_model_instance)");
			}

			@Override
			protected void fixAction() {
				SelectFlexoConceptInstance action = getValidable();
				action.setVirtualModelInstance(new DataBinding<AbstractVirtualModelInstance<?, ?>>("virtualModelInstance"));
			}
		}

		protected static class UseFMLRTModelSlot extends
				FixProposal<BindingIsRequiredAndMustBeValid<SelectFlexoConceptInstance>, SelectFlexoConceptInstance> {

			private final FMLRTModelSlot modelSlot;

			public UseFMLRTModelSlot(FMLRTModelSlot modelSlot) {
				super("sets_virtual_model_instance_to_'" + modelSlot.getName() + "'");
				this.modelSlot = modelSlot;
			}

			@Override
			protected void fixAction() {
				SelectFlexoConceptInstance action = getValidable();
				action.setVirtualModelInstance(new DataBinding<AbstractVirtualModelInstance<?, ?>>(modelSlot.getName()));
			}
		}

	}

}
