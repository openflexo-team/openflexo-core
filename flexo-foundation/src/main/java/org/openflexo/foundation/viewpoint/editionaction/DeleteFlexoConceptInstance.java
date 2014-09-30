/*
 * (c) Copyright 2013 -  Openflexo
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
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.antar.binding.DataBinding;
import org.openflexo.antar.expr.NullReferenceException;
import org.openflexo.antar.expr.TypeMismatchException;
import org.openflexo.foundation.validation.CompoundIssue;
import org.openflexo.foundation.validation.ValidationError;
import org.openflexo.foundation.validation.ValidationIssue;
import org.openflexo.foundation.validation.ValidationRule;
import org.openflexo.foundation.validation.annotations.DefineValidationRule;
import org.openflexo.foundation.view.FlexoConceptInstance;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.view.action.DeletionSchemeAction;
import org.openflexo.foundation.view.action.FlexoBehaviourAction;
import org.openflexo.foundation.viewpoint.DeletionScheme;
import org.openflexo.foundation.viewpoint.FlexoBehaviourParameter;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.URIParameter;
import org.openflexo.foundation.viewpoint.VirtualModelModelSlot;
import org.openflexo.foundation.viewpoint.annotations.FIBPanel;
import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
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

/**
 * This action is used to explicitely instanciate a new {@link FlexoConceptInstance} in a given {@link VirtualModelInstance} with some
 * parameters
 * 
 * @author sylvain
 * 
 * @param <M>
 * @param <MM>
 */

@FIBPanel("Fib/VPM/DeleteFlexoConceptInstancePanel.fib")
@ModelEntity
@ImplementationClass(DeleteFlexoConceptInstance.DeleteFlexoConceptInstanceImpl.class)
@XMLElement
public interface DeleteFlexoConceptInstance extends DeleteAction<VirtualModelModelSlot, FlexoConceptInstance> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String VIRTUAL_MODEL_INSTANCE_KEY = "virtualModelInstance";
	@PropertyIdentifier(type = String.class)
	public static final String DELETION_SCHEME_URI_KEY = "deletionSchemeURI";
	@PropertyIdentifier(type = List.class)
	public static final String PARAMETERS_KEY = "parameters";

	@Getter(value = VIRTUAL_MODEL_INSTANCE_KEY)
	@XMLAttribute
	public DataBinding<VirtualModelInstance> getVirtualModelInstance();

	@Setter(VIRTUAL_MODEL_INSTANCE_KEY)
	public void setVirtualModelInstance(DataBinding<VirtualModelInstance> virtualModelInstance);

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

	public void setFlexoConceptType(FlexoConcept flexoConceptType);

	public abstract static class DeleteFlexoConceptInstanceImpl extends DeleteActionImpl<VirtualModelModelSlot, FlexoConceptInstance>
			implements DeleteFlexoConceptInstance {

		private static final Logger logger = Logger.getLogger(DeleteFlexoConceptInstance.class.getPackage().getName());

		private FlexoConcept flexoConceptType;
		private DeletionScheme deletionScheme;
		private String _deletionSchemeURI;
		private boolean isUpdating = false;

		public VirtualModelInstance getVirtualModelInstance(FlexoBehaviourAction<?, ?, ?> action) {
			try {
				return getVirtualModelInstance().getBindingValue(action);
			} catch (TypeMismatchException e) {
				e.printStackTrace();
			} catch (NullReferenceException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}

		private DataBinding<VirtualModelInstance> virtualModelInstance;

		@Override
		public DataBinding<VirtualModelInstance> getVirtualModelInstance() {
			if (virtualModelInstance == null) {
				virtualModelInstance = new DataBinding<VirtualModelInstance>(this, VirtualModelInstance.class,
						DataBinding.BindingDefinitionType.GET);
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
			if (this.virtualModelInstance != aVirtualModelInstance) {
				this.getPropertyChangeSupport()
						.firePropertyChange("virtualModelInstance", this.virtualModelInstance, aVirtualModelInstance);
				this.virtualModelInstance = aVirtualModelInstance;
			}
		}

		@Override
		public FlexoConcept getFlexoConceptType() {
			if (flexoConceptType == null && deletionScheme != null) {
				flexoConceptType = deletionScheme.getFlexoConcept();
			}
			return flexoConceptType;
		}

		@Override
		public void setFlexoConceptType(FlexoConcept flexoConceptType) {
			if (this.flexoConceptType != flexoConceptType) {
				this.getPropertyChangeSupport().firePropertyChange("flexoConceptType", this.flexoConceptType, flexoConceptType);
				this.flexoConceptType = flexoConceptType;
			}

			if (getDeletionScheme() != null && getDeletionScheme().getFlexoConcept() != flexoConceptType) {
				setDeletionScheme(null);
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
					if (deletionScheme != null)
						setFlexoConceptType(deletionScheme.getFlexoConcept());
				}
			}
			else if (deletionScheme == null && getFlexoConceptType() != null) {
				deletionScheme = getFlexoConceptType().getDefaultDeletionScheme();
				_deletionSchemeURI = deletionScheme.getURI();
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
			List<DeleteFlexoConceptInstanceParameter> pList = (List<DeleteFlexoConceptInstanceParameter>) performSuperGetter(PARAMETERS_KEY);
			for (DeleteFlexoConceptInstanceParameter deleteEPParam : pList) {
				if (deleteEPParam.getParam() == p) {
					return deleteEPParam;
				}
			}
			return null;
		}

		private void updateParameters() {
			if (!isUpdating) {
				List<DeleteFlexoConceptInstanceParameter> parametersToRemove = (List<DeleteFlexoConceptInstanceParameter>) performSuperGetter(PARAMETERS_KEY);
				if (getDeletionScheme() != null) {
					for (FlexoBehaviourParameter p : getDeletionScheme().getParameters()) {
						DeleteFlexoConceptInstanceParameter existingParam = getParameter(p);
						if (existingParam != null) {
							parametersToRemove.remove(existingParam);
						}
						else {
							isUpdating = true;
							addToParameters(getVirtualModelFactory().newDeleteFlexoConceptInstanceParameter(p));
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
		public FlexoConceptInstance performAction(FlexoBehaviourAction action) {
			logger.info("Perform performDeleteFlexoConceptInstance " + action);
			VirtualModelInstance vmInstance = getVirtualModelInstance(action);

			DeletionSchemeAction deletionSchemeAction = DeletionSchemeAction.actionType.makeNewEmbeddedAction(null, null, action);

			try {
				FlexoConceptInstance objectToDelete = (FlexoConceptInstance) getObject().getBindingValue(action);
				// if VmInstance is null, use the one of the EPI
				if (objectToDelete != null) {
					vmInstance = objectToDelete.getVirtualModelInstance();

					logger.info("FlexoConceptInstance To Delete: " + objectToDelete);
					logger.info("VirtualModelInstance: " + vmInstance);
					deletionSchemeAction.setFlexoConceptInstanceToDelete(objectToDelete);
					deletionSchemeAction.setVirtualModelInstance(vmInstance);
					deletionSchemeAction.setDeletionScheme(getDeletionScheme());

					for (DeleteFlexoConceptInstanceParameter p : getParameters()) {
						FlexoBehaviourParameter param = p.getParam();
						Object value = p.evaluateParameterValue(action);
						logger.info("For parameter " + param + " value is " + value);
						if (value != null) {
							deletionSchemeAction.setParameterValue(p.getParam(), p.evaluateParameterValue(action));
						}
					}
					logger.info("Performing action");
					deletionSchemeAction.doAction();
					// Finally delete the FlexoConcept
					objectToDelete.delete();
				}

			} catch (TypeMismatchException e1) {
				e1.printStackTrace();
			} catch (NullReferenceException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			}
			if (deletionSchemeAction.hasActionExecutionSucceeded()) {
				logger.info("Successfully performed performDeleteFlexoConcept " + action);
				return deletionSchemeAction.getFlexoConceptInstance();
			}
			return null;
		}

	}

	@DefineValidationRule
	public static class DeleteFlexoConceptInstanceMustAddressADeletionScheme extends
			ValidationRule<DeleteFlexoConceptInstanceMustAddressADeletionScheme, DeleteFlexoConceptInstance> {
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
	public static class DeleteFlexoConceptInstanceParametersMustBeValid extends
			ValidationRule<DeleteFlexoConceptInstanceParametersMustBeValid, DeleteFlexoConceptInstance> {

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
							DeleteFlexoConceptInstanceImpl.logger.info("Binding NOT valid: " + p.getValue() + " for "
									+ p.getParam().getName() + " object=" + p.getAction() + ". Reason: "
									+ p.getValue().invalidBindingReason());
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

	@DefineValidationRule
	public static class VirtualModelInstanceBindingIsRequiredAndMustBeValid extends
			BindingIsRequiredAndMustBeValid<DeleteFlexoConceptInstance> {
		public VirtualModelInstanceBindingIsRequiredAndMustBeValid() {
			super("'virtual_model_instance'_binding_is_not_valid", DeleteFlexoConceptInstance.class);
		}

		@Override
		public DataBinding<VirtualModelInstance> getBinding(DeleteFlexoConceptInstance object) {
			return object.getVirtualModelInstance();
		}

	}

}
