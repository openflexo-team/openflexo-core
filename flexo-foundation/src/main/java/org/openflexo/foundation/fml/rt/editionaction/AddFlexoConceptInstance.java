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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.fib.annotation.FIBPanel;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.URIParameter;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreationSchemeAction;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.validation.CompoundIssue;
import org.openflexo.model.validation.FixProposal;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;

/**
 * This action is used to explicitely instanciate a new {@link FlexoConceptInstance} in a given {@link VirtualModelInstance} with some
 * parameters
 * 
 * @author sylvain
 * 
 * @param <M>
 * @param <MM>
 */

@FIBPanel("Fib/FML/AddFlexoConceptInstancePanel.fib")
@ModelEntity
@ImplementationClass(AddFlexoConceptInstance.AddFlexoConceptInstanceImpl.class)
@XMLElement
@FML("AddFlexoConceptInstance")
public interface AddFlexoConceptInstance extends FMLRTAction<FlexoConceptInstance> {

	@PropertyIdentifier(type = String.class)
	public static final String CREATION_SCHEME_URI_KEY = "creationSchemeURI";
	@PropertyIdentifier(type = List.class)
	public static final String PARAMETERS_KEY = "parameters";

	@Override
	@Getter(value = VIRTUAL_MODEL_INSTANCE_KEY)
	@XMLAttribute
	public DataBinding<VirtualModelInstance> getVirtualModelInstance();

	@Override
	@Setter(VIRTUAL_MODEL_INSTANCE_KEY)
	public void setVirtualModelInstance(DataBinding<VirtualModelInstance> virtualModelInstance);

	@Getter(value = CREATION_SCHEME_URI_KEY)
	@XMLAttribute
	public String _getCreationSchemeURI();

	@Setter(CREATION_SCHEME_URI_KEY)
	public void _setCreationSchemeURI(String creationSchemeURI);

	public CreationScheme getCreationScheme();

	public void setCreationScheme(CreationScheme creationScheme);

	@Getter(value = PARAMETERS_KEY, cardinality = Cardinality.LIST, inverse = AddFlexoConceptInstanceParameter.ACTION_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<AddFlexoConceptInstanceParameter> getParameters();

	@Setter(PARAMETERS_KEY)
	public void setParameters(List<AddFlexoConceptInstanceParameter> parameters);

	@Adder(PARAMETERS_KEY)
	public void addToParameters(AddFlexoConceptInstanceParameter aParameter);

	@Remover(PARAMETERS_KEY)
	public void removeFromParameters(AddFlexoConceptInstanceParameter aParameter);

	public FlexoConcept getFlexoConceptType();

	public void setFlexoConceptType(FlexoConcept flexoConceptType);

	public static abstract class AddFlexoConceptInstanceImpl extends FMLRTActionImpl<FlexoConceptInstance>
			implements AddFlexoConceptInstance {

		static final Logger logger = Logger.getLogger(AddFlexoConceptInstance.class.getPackage().getName());

		private FlexoConcept flexoConceptType;
		private CreationScheme creationScheme;
		private String _creationSchemeURI;
		private Vector<AddFlexoConceptInstanceParameter> parameters = new Vector<AddFlexoConceptInstanceParameter>();

		public VirtualModelInstance getVirtualModelInstance(RunTimeEvaluationContext evaluationContext) {
			try {
				// System.out.println("getVirtualModelInstance() with " + getVirtualModelInstance());
				// System.out.println("Valid=" + getVirtualModelInstance().isValid() + " " +
				// getVirtualModelInstance().invalidBindingReason());
				// System.out.println("returned: " + getVirtualModelInstance().getBindingValue(action));
				return getVirtualModelInstance().getBindingValue(evaluationContext);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public FlexoConcept getFlexoConceptType() {
			if (getCreationScheme() != null) {
				return getCreationScheme().getFlexoConcept();
			}
			return flexoConceptType;
		}

		@Override
		public void setFlexoConceptType(FlexoConcept flexoConceptType) {
			this.flexoConceptType = flexoConceptType;
			if (getCreationScheme() != null && getCreationScheme().getFlexoConcept() != flexoConceptType) {
				setCreationScheme(null);
			}
		}

		@Override
		public String _getCreationSchemeURI() {
			if (getCreationScheme() != null) {
				return getCreationScheme().getURI();
			}
			return _creationSchemeURI;
		}

		@Override
		public void _setCreationSchemeURI(String uri) {
			if (getViewPointLibrary() != null) {
				creationScheme = (CreationScheme) getViewPointLibrary().getFlexoBehaviour(uri);
			}
			_creationSchemeURI = uri;
		}

		@Override
		public CreationScheme getCreationScheme() {
			if (creationScheme == null && _creationSchemeURI != null && getViewPointLibrary() != null) {
				creationScheme = (CreationScheme) getViewPointLibrary().getFlexoBehaviour(_creationSchemeURI);
			}
			if (creationScheme == null && getAssignedFlexoProperty() instanceof FlexoConceptInstanceRole) {
				creationScheme = ((FlexoConceptInstanceRole) getAssignedFlexoProperty()).getCreationScheme();
			}
			return creationScheme;
		}

		@Override
		public void setCreationScheme(CreationScheme creationScheme) {
			this.creationScheme = creationScheme;
			if (creationScheme != null) {
				_creationSchemeURI = creationScheme.getURI();
			}
		}

		// private Vector<AddFlexoConceptInstanceParameter> parameters = new Vector<AddFlexoConceptInstanceParameter>();

		@Override
		public List<AddFlexoConceptInstanceParameter> getParameters() {
			// Comment this because of an infinite loop with updateParameters() method
			updateParameters();
			return parameters;
		}

		public void setParameters(Vector<AddFlexoConceptInstanceParameter> parameters) {
			this.parameters = parameters;
		}

		@Override
		public void addToParameters(AddFlexoConceptInstanceParameter parameter) {
			parameter.setAction(this);
			parameters.add(parameter);
		}

		@Override
		public void removeFromParameters(AddFlexoConceptInstanceParameter parameter) {
			parameter.setAction(null);
			parameters.remove(parameter);
		}

		public AddFlexoConceptInstanceParameter getParameter(FlexoBehaviourParameter p) {
			for (AddFlexoConceptInstanceParameter addEPParam : parameters) {
				if (addEPParam.getParam() == p) {
					return addEPParam;
				}
			}
			return null;
		}

		private void updateParameters() {
			List<AddFlexoConceptInstanceParameter> parametersToRemove = new ArrayList<AddFlexoConceptInstanceParameter>(parameters);
			if (getCreationScheme() != null) {
				for (FlexoBehaviourParameter p : getCreationScheme().getParameters()) {
					AddFlexoConceptInstanceParameter existingParam = getParameter(p);
					if (existingParam != null) {
						parametersToRemove.remove(existingParam);
					}
					else {
						if (getFMLModelFactory() != null) {
							addToParameters(getFMLModelFactory().newAddFlexoConceptInstanceParameter(p));
						}
					}
				}
			}
			for (AddFlexoConceptInstanceParameter removeThis : parametersToRemove) {
				removeFromParameters(removeThis);
			}
		}

		@Override
		public FlexoConceptInstance execute(RunTimeEvaluationContext evaluationContext) {
			logger.info("Perform performAddFlexoConceptInstance " + evaluationContext);
			VirtualModelInstance vmInstance = getVirtualModelInstance(evaluationContext);
			logger.info("VirtualModelInstance: " + vmInstance);
			if (vmInstance == null) {
				logger.warning("null VirtualModelInstance");
				return null;
			}
			if (evaluationContext instanceof FlexoBehaviourAction) {
				CreationSchemeAction creationSchemeAction = CreationSchemeAction.actionType.makeNewEmbeddedAction(vmInstance, null,
						(FlexoBehaviourAction<?, ?, ?>) evaluationContext);
				creationSchemeAction.setVirtualModelInstance(vmInstance);
				creationSchemeAction.setCreationScheme(getCreationScheme());
				for (AddFlexoConceptInstanceParameter p : getParameters()) {
					FlexoBehaviourParameter param = p.getParam();
					Object value = p.evaluateParameterValue((FlexoBehaviourAction<?, ?, ?>) evaluationContext);
					logger.info("For parameter " + param + " value is " + value);
					if (value != null) {
						creationSchemeAction.setParameterValue(p.getParam(),
								p.evaluateParameterValue((FlexoBehaviourAction<?, ?, ?>) evaluationContext));
					}
				}
				creationSchemeAction.doAction();
				if (creationSchemeAction.hasActionExecutionSucceeded()) {
					logger.info("Successfully performed performAddFlexoConcept " + evaluationContext);
					return creationSchemeAction.getFlexoConceptInstance();
				}
			}
			else {
				logger.warning("Unexpected: " + evaluationContext);
			}
			return null;
		}

		/*@Override
		public Type getAssignableType() {
			return FlexoConceptInstanceType.getFlexoConceptInstanceType(getFlexoConceptType());
		}*/

		@Override
		public Type getAssignableType() {
			if (getViewPoint() != null) {
				return FlexoConceptInstanceType.getFlexoConceptInstanceType(getFlexoConceptType());
			}
			else {
				return FlexoConceptInstanceType.UNDEFINED_FLEXO_CONCEPT_INSTANCE_TYPE;
			}
			// NPE Protection
			/*ViewPoint vp = this.getViewPoint();
			if (vp != null) {
				return vp.getInstanceType(getFlexoConceptType());
			} else {
				logger.warning("Adding FlexoConcept Instance in a null ViewPoint !");
				return null;
			}*/
		}

	}

	@DefineValidationRule
	public static class AddFlexoConceptInstanceMustAddressACreationScheme
			extends ValidationRule<AddFlexoConceptInstanceMustAddressACreationScheme, AddFlexoConceptInstance> {
		public AddFlexoConceptInstanceMustAddressACreationScheme() {
			super(AddFlexoConceptInstance.class, "add_flexo_concept_action_must_address_a_valid_creation_scheme");
		}

		@Override
		public ValidationIssue<AddFlexoConceptInstanceMustAddressACreationScheme, AddFlexoConceptInstance> applyValidation(
				AddFlexoConceptInstance action) {
			if (action.getCreationScheme() == null) {
				if (action.getFlexoConceptType() == null) {
					return new ValidationError<AddFlexoConceptInstanceMustAddressACreationScheme, AddFlexoConceptInstance>(this, action,
							"add_flexo_concept_action_doesn't_define_any_flexo_concept");
				}
				else {
					return new ValidationError<AddFlexoConceptInstanceMustAddressACreationScheme, AddFlexoConceptInstance>(this, action,
							"add_flexo_concept_action_doesn't_define_any_creation_scheme");
				}
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class AddFlexoConceptInstanceParametersMustBeValid
			extends ValidationRule<AddFlexoConceptInstanceParametersMustBeValid, AddFlexoConceptInstance> {

		public AddFlexoConceptInstanceParametersMustBeValid() {
			super(AddFlexoConceptInstance.class, "add_flexo_concept_parameters_must_be_valid");
		}

		@Override
		public ValidationIssue<AddFlexoConceptInstanceParametersMustBeValid, AddFlexoConceptInstance> applyValidation(
				AddFlexoConceptInstance action) {
			if (action.getCreationScheme() != null) {
				Vector<ValidationIssue<AddFlexoConceptInstanceParametersMustBeValid, AddFlexoConceptInstance>> issues = new Vector<ValidationIssue<AddFlexoConceptInstanceParametersMustBeValid, AddFlexoConceptInstance>>();
				for (AddFlexoConceptInstanceParameter p : action.getParameters()) {

					FlexoBehaviourParameter param = p.getParam();
					if (param.getIsRequired()) {
						if (p.getValue() == null || !p.getValue().isSet()) {
							DataBinding<String> uri = ((URIParameter) param).getBaseURI();
							if (param instanceof URIParameter && uri.isSet() && uri.isValid()) {
								// Special case, we will find a way to manage this
							}
							else {
								issues.add(new ValidationError(this, action, "parameter_s_value_is_not_defined: " + param.getName()));
							}
						}
						else if (!p.getValue().isValid()) {
							AddFlexoConceptInstanceImpl.logger
									.info("Binding NOT valid: " + p.getValue() + " for " + p.getName() + " object="
											+ p.getAction().getStringRepresentation() + ". Reason: " + p.getValue().invalidBindingReason());
							issues.add(new ValidationError(this, action, "parameter_s_value_is_not_valid: " + param.getName()));
						}
					}
				}
				if (issues.size() == 0) {
					return null;
				}
				else if (issues.size() == 1) {
					return issues.firstElement();
				}
				else {
					return new CompoundIssue<AddFlexoConceptInstanceParametersMustBeValid, AddFlexoConceptInstance>(action, issues);
				}
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class VirtualModelInstanceBindingIsRequiredAndMustBeValid
			extends BindingIsRequiredAndMustBeValid<AddFlexoConceptInstance> {
		public VirtualModelInstanceBindingIsRequiredAndMustBeValid() {
			super("'virtual_model_instance'_binding_is_not_valid", AddFlexoConceptInstance.class);
		}

		@Override
		public DataBinding<VirtualModelInstance> getBinding(AddFlexoConceptInstance object) {
			return object.getVirtualModelInstance();
		}

		@Override
		public ValidationIssue<BindingIsRequiredAndMustBeValid<AddFlexoConceptInstance>, AddFlexoConceptInstance> applyValidation(
				AddFlexoConceptInstance object) {
			ValidationIssue<BindingIsRequiredAndMustBeValid<AddFlexoConceptInstance>, AddFlexoConceptInstance> returned = super.applyValidation(
					object);
			if (returned instanceof UndefinedRequiredBindingIssue) {
				((UndefinedRequiredBindingIssue) returned).addToFixProposals(new UseLocalVirtualModelInstance());
			}
			else {
				DataBinding<VirtualModelInstance> binding = getBinding(object);
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

		protected static class UseLocalVirtualModelInstance
				extends FixProposal<BindingIsRequiredAndMustBeValid<AddFlexoConceptInstance>, AddFlexoConceptInstance> {

			public UseLocalVirtualModelInstance() {
				super("sets_virtual_model_instance_to_'virtualModelInstance'_(local_virtual_model_instance)");
			}

			@Override
			protected void fixAction() {
				AddFlexoConceptInstance action = getValidable();
				action.setVirtualModelInstance(new DataBinding<VirtualModelInstance>("virtualModelInstance"));
			}
		}

		protected static class UseFMLRTModelSlot
				extends FixProposal<BindingIsRequiredAndMustBeValid<AddFlexoConceptInstance>, AddFlexoConceptInstance> {

			private final FMLRTModelSlot modelSlot;

			public UseFMLRTModelSlot(FMLRTModelSlot modelSlot) {
				super("sets_virtual_model_instance_to_'" + modelSlot.getName() + "'");
				this.modelSlot = modelSlot;
			}

			@Override
			protected void fixAction() {
				AddFlexoConceptInstance action = getValidable();
				action.setVirtualModelInstance(new DataBinding<VirtualModelInstance>(modelSlot.getName()));
			}
		}

	}

}
