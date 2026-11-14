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
import org.openflexo.foundation.fml.FMLNamedReference;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourArgument;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.DeletionSchemeAction;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.pamela.annotations.Adder;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Getter.Cardinality;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Remover;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.Updater;
import org.openflexo.pamela.validation.CompoundIssue;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * An action deleting a {@link FlexoConceptInstance} with a chosen deletion scheme ({@link #getDeletionScheme()}, by default the default
 * deletion scheme of its concept), executed with the values of {@link #getParameters()}. When the instance is a
 * {@link FMLRTVirtualModelInstance}, its resource is deleted too.
 * <p>
 * This action is not produced by the FML parser: the FML statement {@code delete expression;} is a {@link DeleteAction}.
 *
 * @author sylvain
 *
 * @param <VMI>
 *            type of the VirtualModelInstance holding the deleted instance
 * @deprecated use the FML deletion statement {@code delete expression;} ({@link DeleteAction})
 */
@ModelEntity
@ImplementationClass(DeleteFlexoConceptInstance.DeleteFlexoConceptInstanceImpl.class)
@FML("DeleteFlexoConceptInstance")
@Deprecated
public interface DeleteFlexoConceptInstance<VMI extends VirtualModelInstance<VMI, ?>> extends DeleteAction<FlexoConceptInstance> {

	@PropertyIdentifier(type = FMLNamedReference.class)
	public static final String DELETION_SCHEME_REFERENCE_KEY = "deletionSchemeReference";
	@PropertyIdentifier(type = List.class)
	public static final String PARAMETERS_KEY = "parameters";

	/**
	 * The deletion scheme executed on the deleted instance, as a reference resolved in the concept of the deleted object (see
	 * {@link #getDeletionScheme()})
	 *
	 * @return the reference to the deletion scheme, null for the default deletion scheme of the concept
	 */
	@Getter(value = DELETION_SCHEME_REFERENCE_KEY, ignoreType = true)
	public FMLNamedReference<DeletionScheme> getDeletionSchemeReference();

	@Setter(DELETION_SCHEME_REFERENCE_KEY)
	public void setDeletionSchemeReference(FMLNamedReference<DeletionScheme> deletionSchemeReference);

	/**
	 * Keeps the name only of a reference coming from another compilation unit (merge with {@code updateWith()})
	 *
	 * @param deletionSchemeReference
	 *            the reference held by the action of the other compilation unit
	 */
	@Updater(DELETION_SCHEME_REFERENCE_KEY)
	public void updateDeletionSchemeReference(FMLNamedReference<DeletionScheme> deletionSchemeReference);

	/**
	 * The deletion scheme executed on the deleted instance: the one of {@link #getDeletionSchemeReference()}, otherwise the default
	 * deletion scheme of the concept of the deleted object
	 *
	 * @return the deletion scheme
	 */
	public DeletionScheme getDeletionScheme();

	public void setDeletionScheme(DeletionScheme deletionScheme);

	@Getter(value = PARAMETERS_KEY, cardinality = Cardinality.LIST, inverse = DeleteFlexoConceptInstanceParameter.ACTION_KEY)
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

	public static abstract class DeleteFlexoConceptInstanceImpl<VMI extends VirtualModelInstance<VMI, ?>>
			extends DeleteActionImpl<FlexoConceptInstance> implements DeleteFlexoConceptInstance<VMI> {

		private static final Logger logger = Logger.getLogger(DeleteFlexoConceptInstance.class.getPackage().getName());

		private FMLNamedReference<DeletionScheme> deletionSchemeReference;
		private boolean isUpdating = false;

		@Override
		public FlexoConcept getFlexoConceptType() {

			if (getObject() != null && getObject().isSet() && getObject().isValid()) {
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
		public FMLNamedReference<DeletionScheme> getDeletionSchemeReference() {
			return deletionSchemeReference;
		}

		@Override
		public void setDeletionSchemeReference(FMLNamedReference<DeletionScheme> deletionSchemeReference) {
			FMLNamedReference<DeletionScheme> oldValue = this.deletionSchemeReference;
			this.deletionSchemeReference = deletionSchemeReference;
			getPropertyChangeSupport().firePropertyChange(DELETION_SCHEME_REFERENCE_KEY, oldValue, deletionSchemeReference);
		}

		@Override
		public void updateDeletionSchemeReference(FMLNamedReference<DeletionScheme> deletionSchemeReference) {
			setDeletionSchemeReference(deletionSchemeReference != null ? deletionSchemeReference.unresolved() : null);
		}

		@Override
		public DeletionScheme getDeletionScheme() {
			FlexoConcept concept = getFlexoConceptType();
			if (deletionSchemeReference != null) {
				return deletionSchemeReference.resolve(concept != null ? name -> {
					FlexoBehaviour behaviour = concept.getFlexoBehaviour(name);
					return behaviour instanceof DeletionScheme ? (DeletionScheme) behaviour : null;
				} : null);
			}
			return concept != null ? concept.getDefaultDeletionScheme() : null;
		}

		@Override
		public void setDeletionScheme(DeletionScheme deletionScheme) {
			setDeletionSchemeReference(FMLNamedReference.to(deletionScheme));
		}

		@Override
		public List<DeleteFlexoConceptInstanceParameter> getParameters() {
			updateParameters();
			return (List<DeleteFlexoConceptInstanceParameter>) performSuperGetter(PARAMETERS_KEY);
		}

		public DeleteFlexoConceptInstanceParameter getParameter(FlexoBehaviourArgument p) {
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
					for (FlexoBehaviourArgument p : getDeletionScheme().getParameters()) {
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
		public FlexoConceptInstance execute(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {

			try {
				FlexoConceptInstance objectToDelete = getObject().getBindingValue(evaluationContext);
				// if VmInstance is null, use the one of the EPI
				if (objectToDelete != null) {

					if (getDeletionScheme() == null) {
						logger.warning("No deletion scheme !");
						return objectToDelete;
					}

					if (evaluationContext instanceof FlexoBehaviourAction) {

						FMLRTVirtualModelInstanceResource resourceToDelete = null;

						if (objectToDelete instanceof FMLRTVirtualModelInstance) {
							resourceToDelete = (FMLRTVirtualModelInstanceResource) ((VirtualModelInstance<?, ?>) objectToDelete)
									.getResource();
						}

						DeletionSchemeAction deletionSchemeAction = new DeletionSchemeAction(getDeletionScheme(), objectToDelete, null,
								(FlexoBehaviourAction<?, ?, ?>) evaluationContext);

						for (DeleteFlexoConceptInstanceParameter p : getParameters()) {
							FlexoBehaviourArgument param = p.getParam();
							Object value = p.evaluateParameterValue((FlexoBehaviourAction<?, ?, ?>) evaluationContext);
							logger.info("For parameter " + param + " value is " + value);
							if (value != null) {
								deletionSchemeAction.setParameterValue(p.getParam(),
										p.evaluateParameterValue((FlexoBehaviourAction<?, ?, ?>) evaluationContext));
							}
						}
						logger.info("Performing action");
						deletionSchemeAction.doAction();
						if (deletionSchemeAction.hasActionExecutionFailed()) {
							// The deletion scheme failed: the instance is not deleted, and the failure reaches the caller (CORE-D-47)
							throw new FMLExecutionException("Deletion scheme failed for " + objectToDelete,
									deletionSchemeAction.getThrownException());
						}
						// Finally delete the FlexoConcept
						objectToDelete.delete();

						if (resourceToDelete != null) {
							System.out.println("Cool, on supprime la resource: " + resourceToDelete);
							resourceToDelete.delete();
						}

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
				throw new FMLExecutionException(e1.getCause());
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public Type getAssignableType() {
			return FlexoConceptInstance.class;
		}

		@Override
		public String getStringRepresentation() {
			if (getDeletionScheme() != null) {
				return getObject().toString() + "." + getDeletionScheme().getName() + "()";
			}
			else {
				return super.getStringRepresentation();
			}
		}
	}

	@DefineValidationRule
	public static class DeleteFlexoConceptInstanceMustAddressADeletionScheme
			extends ValidationRule<DeleteFlexoConceptInstanceMustAddressADeletionScheme, DeleteFlexoConceptInstance<?>> {
		public DeleteFlexoConceptInstanceMustAddressADeletionScheme() {
			super(DeleteFlexoConceptInstance.class, "delete_flexo_concept_action_must_address_a_valid_creation_scheme");
		}

		@Override
		public ValidationIssue<DeleteFlexoConceptInstanceMustAddressADeletionScheme, DeleteFlexoConceptInstance<?>> applyValidation(
				DeleteFlexoConceptInstance<?> action) {
			if (action.getDeletionScheme() == null) {
				if (action.getFlexoConceptType() == null) {
					return new ValidationError<>(this, action, "delete_flexo_concept_action_doesn't_define_any_flexo_concept");
				}
				return new ValidationError<>(this, action, "delete_flexo_concept_action_doesn't_define_any_deletion_scheme");
			}
			return null;
		}
	}

	@DefineValidationRule
	public static class DeleteFlexoConceptInstanceParametersMustBeValid
			extends ValidationRule<DeleteFlexoConceptInstanceParametersMustBeValid, DeleteFlexoConceptInstance<?>> {

		public DeleteFlexoConceptInstanceParametersMustBeValid() {
			super(DeleteFlexoConceptInstance.class, "delete_flexo_concept_parameters_must_be_valid");
		}

		@Override
		public ValidationIssue<DeleteFlexoConceptInstanceParametersMustBeValid, DeleteFlexoConceptInstance<?>> applyValidation(
				DeleteFlexoConceptInstance<?> action) {
			if (action.getDeletionScheme() != null) {
				Vector<ValidationIssue<DeleteFlexoConceptInstanceParametersMustBeValid, DeleteFlexoConceptInstance<?>>> issues = new Vector<>();
				for (DeleteFlexoConceptInstanceParameter p : action.getParameters()) {

					FlexoBehaviourArgument param = p.getParam();
					if (param.getIsRequired()) {
						if (p.getValue() == null || !p.getValue().isSet()) {
							issues.add(new ValidationError<>(this, action, "parameter_s_value_is_not_defined: " + param.getName()));
						}
						else if (!p.getValue().isValid()) {
							DeleteFlexoConceptInstanceImpl.logger
									.info("Binding NOT valid: " + p.getValue() + " for " + p.getParam().getName() + " object="
											+ p.getAction() + ". Reason: " + p.getValue().invalidBindingReason());
							issues.add(new ValidationError<>(this, action, "parameter_s_value_is_not_valid: " + param.getName()));
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
					return new CompoundIssue<>(action, issues);
				}
			}
			return null;
		}
	}

}
