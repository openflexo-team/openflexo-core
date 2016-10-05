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
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.URIParameter;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.action.DeletionSchemeAction;
import org.openflexo.foundation.fml.rt.action.DeletionSchemeActionType;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gina.annotation.FIBPanel;
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
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;

/**
 * This action is used to explicitely delete a new {@link FlexoConceptInstance}<br>
 * This action overrides DeleteAction by proposing the choice of the DeletionScheme to use
 * 
 * @author sylvain
 * 
 * @param <M>
 * @param <MM>
 */

@FIBPanel("Fib/FML/DeleteFlexoConceptInstancePanel.fib")
@ModelEntity
@ImplementationClass(DeleteFlexoConceptInstance.DeleteFlexoConceptInstanceImpl.class)
@XMLElement
@FML("DeleteFlexoConceptInstance")
public interface DeleteFlexoConceptInstance
		extends DeleteAction<FlexoConceptInstance>, TechnologySpecificAction<FMLRTModelSlot<?, ?>, FlexoConceptInstance> {

	// @PropertyIdentifier(type = DataBinding.class)
	// public static final String VIRTUAL_MODEL_INSTANCE_KEY = "virtualModelInstance";
	@PropertyIdentifier(type = String.class)
	public static final String DELETION_SCHEME_URI_KEY = "deletionSchemeURI";
	@PropertyIdentifier(type = List.class)
	public static final String PARAMETERS_KEY = "parameters";

	/*@Getter(value = VIRTUAL_MODEL_INSTANCE_KEY)
	@XMLAttribute
	public DataBinding<AbstractVirtualModelInstance<?, ?>> getVirtualModelInstance();
	
	@Setter(VIRTUAL_MODEL_INSTANCE_KEY)
	public void setVirtualModelInstance(DataBinding<AbstractVirtualModelInstance<?, ?>> virtualModelInstance);*/

	@Getter(value = DELETION_SCHEME_URI_KEY)
	@XMLAttribute
	public String _getDeletionSchemeURI();

	@Setter(DELETION_SCHEME_URI_KEY)
	public void _setDeletionSchemeURI(String creationSchemeURI);

	public DeletionScheme getDeletionScheme();

	public void setDeletionScheme(DeletionScheme deletionScheme);

	@Getter(value = PARAMETERS_KEY, cardinality = Cardinality.LIST, inverse = DeleteFlexoConceptInstanceParameter.ACTION_KEY)
	@XMLElement
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	public List<DeleteFlexoConceptInstanceParameter> getParameters();

	@Setter(PARAMETERS_KEY)
	public void setParameters(List<DeleteFlexoConceptInstanceParameter> parameters);

	@Adder(PARAMETERS_KEY)
	public void addToParameters(DeleteFlexoConceptInstanceParameter aParameter);

	@Remover(PARAMETERS_KEY)
	public void removeFromParameters(DeleteFlexoConceptInstanceParameter aParameter);

	public FlexoConcept getFlexoConceptType();

	public static abstract class DeleteFlexoConceptInstanceImpl extends DeleteActionImpl<FlexoConceptInstance>
			implements DeleteFlexoConceptInstance {

		private static final Logger logger = Logger.getLogger(DeleteFlexoConceptInstance.class.getPackage().getName());

		// private FlexoConcept flexoConceptType;
		private DeletionScheme deletionScheme;
		private String _deletionSchemeURI;
		private boolean isUpdating = false;

		// I don't understand ? why this code is required by the compiler ?
		@Override
		public FlexoProperty getAssignedFlexoProperty() {
			// TODO Auto-generated method stub
			return super.getAssignedFlexoProperty();
		}

		/*public AbstractVirtualModelInstance<?, ?> getVirtualModelInstance(RunTimeEvaluationContext evaluationContext) {
			try {
				return getVirtualModelInstance().getBindingValue(evaluationContext);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}*/

		/*private DataBinding<AbstractVirtualModelInstance<?, ?>> virtualModelInstance;
		
		@Override
		public DataBinding<AbstractVirtualModelInstance<?, ?>> getVirtualModelInstance() {
			if (virtualModelInstance == null) {
				virtualModelInstance = new DataBinding<AbstractVirtualModelInstance<?, ?>>(this, AbstractVirtualModelInstance.class,
						DataBinding.BindingDefinitionType.GET);
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
			if (this.virtualModelInstance != aVirtualModelInstance) {
				this.getPropertyChangeSupport().firePropertyChange("virtualModelInstance", this.virtualModelInstance,
						aVirtualModelInstance);
				this.virtualModelInstance = aVirtualModelInstance;
			}
		}*/

		@Override
		public FlexoConcept getFlexoConceptType() {

			if (getObject().isSet() && getObject().isValid()) {
				Type type = getObject().getAnalyzedType();
				if (type instanceof FlexoConceptInstanceType) {
					return ((FlexoConceptInstanceType) type).getFlexoConcept();
				}
			}
			return null;
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			super.notifiedBindingChanged(dataBinding);
			if (dataBinding == getObject()) {
				getPropertyChangeSupport().firePropertyChange("flexoConceptType", null, getFlexoConceptType());
			}
		}

		@Override
		public String _getDeletionSchemeURI() {
			if (getDeletionScheme() != null) {
				return getDeletionScheme().getURI();
			}
			return _deletionSchemeURI;
		}

		@Override
		public void _setDeletionSchemeURI(String uri) {
			if (getFlexoConceptType() != null) {
				deletionScheme = (DeletionScheme) getFlexoConceptType().getFlexoBehaviourForURI(uri);
				if (deletionScheme == null) {
					logger.warning("Not able to find deletion Scheme : " + uri);
					_deletionSchemeURI = null;
				}
			}
			else {
				_deletionSchemeURI = uri;
			}
		}

		@Override
		public DeletionScheme getDeletionScheme() {
			if (deletionScheme == null && _deletionSchemeURI != null) {
				if (getFlexoConceptType() != null) {
					deletionScheme = (DeletionScheme) getFlexoConceptType().getFlexoBehaviourForURI(_deletionSchemeURI);
				}
				else if (getViewPointLibrary() != null) {
					deletionScheme = (DeletionScheme) getViewPointLibrary().getFlexoBehaviour(_deletionSchemeURI);
				}
			}
			else if (deletionScheme == null && getFlexoConceptType() != null) {
				deletionScheme = getFlexoConceptType().getDefaultDeletionScheme();
				if (deletionScheme != null) {
					_deletionSchemeURI = deletionScheme.getURI();
				}
			}

			return deletionScheme;
		}

		@Override
		public void setDeletionScheme(DeletionScheme deletionScheme) {
			this.deletionScheme = deletionScheme;
			if (deletionScheme != null) {
				_deletionSchemeURI = deletionScheme.getURI();
			}
		}

		// private Vector<AddFlexoConceptInstanceParameter> parameters = new Vector<AddFlexoConceptInstanceParameter>();

		@Override
		public List<DeleteFlexoConceptInstanceParameter> getParameters() {
			updateParameters();
			return (List<DeleteFlexoConceptInstanceParameter>) performSuperGetter(PARAMETERS_KEY);
		}

		public DeleteFlexoConceptInstanceParameter getParameter(FlexoBehaviourParameter p) {
			List<DeleteFlexoConceptInstanceParameter> pList = (List<DeleteFlexoConceptInstanceParameter>) performSuperGetter(
					PARAMETERS_KEY);
			for (DeleteFlexoConceptInstanceParameter deleteEPParam : pList) {
				if (deleteEPParam.getParam() == p) {
					return deleteEPParam;
				}
			}
			return null;
		}

		private void updateParameters() {
			if (!isUpdating) {
				List<DeleteFlexoConceptInstanceParameter> parametersToRemove = (List<DeleteFlexoConceptInstanceParameter>) performSuperGetter(
						PARAMETERS_KEY);
				if (getDeletionScheme() != null) {
					for (FlexoBehaviourParameter p : getDeletionScheme().getParameters()) {
						DeleteFlexoConceptInstanceParameter existingParam = getParameter(p);
						if (existingParam != null) {
							parametersToRemove.remove(existingParam);
						}
						else {
							isUpdating = true;
							addToParameters(getFMLModelFactory().newDeleteFlexoConceptInstanceParameter(p));
						}
					}
				}
				for (DeleteFlexoConceptInstanceParameter removeThis : parametersToRemove) {
					removeFromParameters(removeThis);
				}
				isUpdating = false;
			}
		}

		@Override
		public FlexoConceptInstance execute(RunTimeEvaluationContext evaluationContext) {
			logger.info("Perform performDeleteFlexoConceptInstance " + evaluationContext);
			AbstractVirtualModelInstance<?, ?> vmInstance = null; // getVirtualModelInstance(evaluationContext);

			// DeletionSchemeAction deletionSchemeAction = DeletionSchemeAction.actionType.makeNewEmbeddedAction(null, null, action);

			try {
				FlexoConceptInstance objectToDelete = getObject().getBindingValue(evaluationContext);
				// if VmInstance is null, use the one of the EPI
				if (objectToDelete != null) {
					vmInstance = objectToDelete.getVirtualModelInstance();

					logger.info("FlexoConceptInstance To Delete: " + objectToDelete);
					logger.info("VirtualModelInstance: " + vmInstance);
					logger.info("deletionScheme: " + deletionScheme);

					if (deletionScheme == null) {
						logger.warning("No deletion scheme !");
						return objectToDelete;
					}

					if (evaluationContext instanceof FlexoBehaviourAction) {

						DeletionSchemeActionType actionType = new DeletionSchemeActionType(deletionScheme, objectToDelete);
						DeletionSchemeAction deletionSchemeAction = actionType.makeNewEmbeddedAction(objectToDelete, null,
								(FlexoBehaviourAction<?, ?, ?>) evaluationContext);

						deletionSchemeAction.setFlexoConceptInstanceToDelete(objectToDelete);
						deletionSchemeAction.setVirtualModelInstance(vmInstance);
						// deletionSchemeAction.setDeletionScheme(getDeletionScheme());

						for (DeleteFlexoConceptInstanceParameter p : getParameters()) {
							FlexoBehaviourParameter param = p.getParam();
							Object value = p.evaluateParameterValue((FlexoBehaviourAction<?, ?, ?>) evaluationContext);
							logger.info("For parameter " + param + " value is " + value);
							if (value != null) {
								deletionSchemeAction.setParameterValue(p.getParam(),
										p.evaluateParameterValue((FlexoBehaviourAction<?, ?, ?>) evaluationContext));
							}
						}
						logger.info("Performing action");
						deletionSchemeAction.doAction();
						// Finally delete the FlexoConcept
						objectToDelete.delete();

						if (deletionSchemeAction.hasActionExecutionSucceeded()) {
							logger.info("Successfully performed performDeleteFlexoConcept " + evaluationContext);
							return deletionSchemeAction.getFlexoConceptInstance();
						}
					}
					else {
						logger.warning("Unexpected " + evaluationContext);
					}
				}

			} catch (TypeMismatchException e1) {
				e1.printStackTrace();
			} catch (NullReferenceException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			}
			return null;
		}

		@Override
		public TechnologyAdapter getModelSlotTechnologyAdapter() {
			return null;
		}

		@Override
		public ModelSlotInstance<FMLRTModelSlot<?, ?>, ?> getModelSlotInstance(RunTimeEvaluationContext evaluationContext) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<FMLRTModelSlot> getAvailableVirtualModelModelSlots() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <MS2 extends ModelSlot<?>> List<MS2> getAvailableModelSlots(Class<MS2> msType) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Type getAssignableType() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isIterable() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Type getIteratorType() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <MS2 extends ModelSlot<?>> List<MS2> getAvailableModelSlots() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@DefineValidationRule
	public static class DeleteFlexoConceptInstanceMustAddressADeletionScheme
			extends ValidationRule<DeleteFlexoConceptInstanceMustAddressADeletionScheme, DeleteFlexoConceptInstance> {
		public DeleteFlexoConceptInstanceMustAddressADeletionScheme() {
			super(DeleteFlexoConceptInstance.class, "delete_flexo_concept_action_must_address_a_valid_creation_scheme");
		}

		@Override
		public ValidationIssue<DeleteFlexoConceptInstanceMustAddressADeletionScheme, DeleteFlexoConceptInstance> applyValidation(
				DeleteFlexoConceptInstance action) {
			if (action.getDeletionScheme() == null) {
				if (action.getFlexoConceptType() == null) {
					return new ValidationError<DeleteFlexoConceptInstanceMustAddressADeletionScheme, DeleteFlexoConceptInstance>(this,
							action, "delete_flexo_concept_action_doesn't_define_any_flexo_concept");
				}
				else {
					return new ValidationError<DeleteFlexoConceptInstanceMustAddressADeletionScheme, DeleteFlexoConceptInstance>(this,
							action, "delete_flexo_concept_action_doesn't_define_any_deletion_scheme");
				}
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class DeleteFlexoConceptInstanceParametersMustBeValid
			extends ValidationRule<DeleteFlexoConceptInstanceParametersMustBeValid, DeleteFlexoConceptInstance> {

		public DeleteFlexoConceptInstanceParametersMustBeValid() {
			super(DeleteFlexoConceptInstance.class, "delete_flexo_concept_parameters_must_be_valid");
		}

		@Override
		public ValidationIssue<DeleteFlexoConceptInstanceParametersMustBeValid, DeleteFlexoConceptInstance> applyValidation(
				DeleteFlexoConceptInstance action) {
			if (action.getDeletionScheme() != null) {
				Vector<ValidationIssue<DeleteFlexoConceptInstanceParametersMustBeValid, DeleteFlexoConceptInstance>> issues = new Vector<ValidationIssue<DeleteFlexoConceptInstanceParametersMustBeValid, DeleteFlexoConceptInstance>>();
				for (DeleteFlexoConceptInstanceParameter p : action.getParameters()) {

					FlexoBehaviourParameter param = p.getParam();
					if (param.getIsRequired()) {
						if (p.getValue() == null || !p.getValue().isSet()) {
							DataBinding<String> uri = ((URIParameter) param).getBaseURI();
							if (param instanceof URIParameter && uri.isSet() && uri.isValid()) {
								// Special case, we will find a way to manage this
							}
							else {
								issues.add(new ValidationError<DeleteFlexoConceptInstanceParametersMustBeValid, DeleteFlexoConceptInstance>(
										this, action, "parameter_s_value_is_not_defined: " + param.getName()));
							}
						}
						else if (!p.getValue().isValid()) {
							DeleteFlexoConceptInstanceImpl.logger
									.info("Binding NOT valid: " + p.getValue() + " for " + p.getParam().getName() + " object="
											+ p.getAction() + ". Reason: " + p.getValue().invalidBindingReason());
							issues.add(new ValidationError<DeleteFlexoConceptInstanceParametersMustBeValid, DeleteFlexoConceptInstance>(
									this, action, "parameter_s_value_is_not_valid: " + param.getName()));
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
					return new CompoundIssue<DeleteFlexoConceptInstance.DeleteFlexoConceptInstanceParametersMustBeValid, DeleteFlexoConceptInstance>(
							action, issues);
				}
			}
			return null;
		}
	}

	/*@DefineValidationRule
	public static class VirtualModelInstanceBindingIsRequiredAndMustBeValid
			extends BindingIsRequiredAndMustBeValid<DeleteFlexoConceptInstance> {
		public VirtualModelInstanceBindingIsRequiredAndMustBeValid() {
			super("'virtual_model_instance'_binding_is_not_valid", DeleteFlexoConceptInstance.class);
		}
	
		@Override
		public DataBinding<AbstractVirtualModelInstance<?, ?>> getBinding(DeleteFlexoConceptInstance object) {
			return object.getVirtualModelInstance();
		}
	
	}*/

}
