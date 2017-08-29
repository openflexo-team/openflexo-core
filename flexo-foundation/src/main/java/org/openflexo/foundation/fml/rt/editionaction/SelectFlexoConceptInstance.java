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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.expr.BinaryOperatorExpression;
import org.openflexo.connie.expr.BooleanBinaryOperator;
import org.openflexo.connie.expr.Expression;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.binding.FetchRequestConditionSelectedBindingVariable;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.fml.editionaction.FetchRequestCondition;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
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
@ModelEntity
@ImplementationClass(SelectFlexoConceptInstance.SelectFlexoConceptInstanceImpl.class)
@XMLElement
@FML("SelectFlexoConceptInstance")
public interface SelectFlexoConceptInstance<VMI extends VirtualModelInstance<VMI, ?>>
		extends FetchRequest<FMLRTModelSlot<VMI, ?>, VMI, FlexoConceptInstance> {

	@PropertyIdentifier(type = String.class)
	public static final String FLEXO_CONCEPT_TYPE_URI_KEY = "flexoConceptTypeURI";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";

	@Getter(value = CONTAINER_KEY)
	@XMLAttribute
	public DataBinding<FlexoConceptInstance> getContainer();

	@Setter(CONTAINER_KEY)
	public void setContainer(DataBinding<FlexoConceptInstance> container);

	@Getter(value = FLEXO_CONCEPT_TYPE_URI_KEY)
	@XMLAttribute
	public String _getFlexoConceptTypeURI();

	@Setter(FLEXO_CONCEPT_TYPE_URI_KEY)
	public void _setFlexoConceptTypeURI(String flexoConceptTypeURI);

	public FlexoConcept getFlexoConceptType();

	public void setFlexoConceptType(FlexoConcept flexoConceptType);

	/**
	 * Return the {@link VirtualModel} beeing addressed by this action, according to the {@link #getVirtualModelInstance()} binding
	 * 
	 * @return
	 */
	public VirtualModel getAddressedVirtualModel();

	public static abstract class SelectFlexoConceptInstanceImpl<VMI extends VirtualModelInstance<VMI, ?>>
			extends FetchRequestImpl<FMLRTModelSlot<VMI, ?>, VMI, FlexoConceptInstance> implements SelectFlexoConceptInstance<VMI> {

		protected static final Logger logger = FlexoLogger.getLogger(SelectFlexoConceptInstance.class.getPackage().getName());

		private FlexoConcept flexoConceptType;
		private String flexoConceptTypeURI;

		@Override
		public TechnologyAdapter getModelSlotTechnologyAdapter() {
			if (getServiceManager() != null) {
				return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
			}
			return super.getModelSlotTechnologyAdapter();
		}

		private DataBinding<FlexoConceptInstance> container;

		@Override
		public DataBinding<FlexoConceptInstance> getContainer() {
			if (container == null) {
				container = new DataBinding<FlexoConceptInstance>(this, FlexoConceptInstance.class, BindingDefinitionType.GET);
				container.setBindingName("container");
			}
			return container;
		}

		@Override
		public void setContainer(DataBinding<FlexoConceptInstance> container) {
			if (container != null) {
				container.setOwner(this);
				container.setBindingName("container");
				container.setDeclaredType(FlexoConceptInstance.class);
				container.setBindingDefinitionType(BindingDefinitionType.GET);
			}
			this.container = container;
		}

		@Override
		public String getParametersStringRepresentation() {
			String whereClauses = getWhereClausesFMLRepresentation(null);
			return "(type=" + (getFlexoConceptType() != null ? getFlexoConceptType().getName() : "null")
					+ (whereClauses != null ? "," + whereClauses : "") + ")";
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			return getReceiver().toString() + "." + getImplementedInterface().getSimpleName() + "("
					+ (getFlexoConceptType() != null ? "type=" + getFlexoConceptType().getName() : "type=?")
					+ (getConditions().size() > 0 ? ",where=" + getWhereClausesFMLRepresentation(context) : "") + ")";
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

		private boolean isComputingFlexoConceptType = false;

		@Override
		public FlexoConcept getFlexoConceptType() {

			if (!isComputingFlexoConceptType && flexoConceptType == null && flexoConceptTypeURI != null) {
				isComputingFlexoConceptType = true;
				if (getAddressedVirtualModel() != null) {
					flexoConceptType = getAddressedVirtualModel().getFlexoConcept(flexoConceptTypeURI);
				}
				isComputingFlexoConceptType = false;
			}

			return flexoConceptType;
		}

		private boolean isAnalyzingContainer = false;

		/**
		 * Return the {@link VirtualModel} beeing addressed by this action, according to the {@link #getVirtualModelInstance()} binding
		 * 
		 * @return
		 */
		@Override
		public VirtualModel getAddressedVirtualModel() {
			if (getReceiver() != null && getReceiver().isSet()) {
				if (isAnalyzingContainer) {
					return null;
				}
				else {
					if (getReceiver().isValid()) {
						isAnalyzingContainer = true;
						Type vmiType = getReceiver().getAnalyzedType();
						isAnalyzingContainer = false;
						if (vmiType instanceof VirtualModelInstanceType) {
							return ((VirtualModelInstanceType) vmiType).getVirtualModel();
						}
					}
				}
			}
			// I could not find VM, trying to "guess" (TODO: remove this hack ?)
			if (getFlexoConcept() instanceof VirtualModel) {
				return (VirtualModel) getFlexoConcept();
			}
			if (getInferedModelSlot() instanceof FMLRTModelSlot) {
				return getInferedModelSlot().getAccessedVirtualModel();
			}
			return getOwningVirtualModel();
		}

		@Override
		public void setFlexoConceptType(FlexoConcept flexoConceptType) {
			if (flexoConceptType != this.flexoConceptType) {
				FlexoConcept oldValue = this.flexoConceptType;
				this.flexoConceptType = flexoConceptType;
				getPropertyChangeSupport().firePropertyChange("flexoConceptType", oldValue, oldValue);
			}
		}

		public VMI getVirtualModelInstance(RunTimeEvaluationContext evaluationContext) {
			if (getReceiver() != null && getReceiver().isSet() && getReceiver().isValid()) {
				try {
					return getReceiver().getBindingValue(evaluationContext);
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

		public FlexoConceptInstance getContainer(RunTimeEvaluationContext evaluationContext) {
			if (getContainer() != null && getContainer().isSet() && getContainer().isValid()) {
				try {
					return getContainer().getBindingValue(evaluationContext);
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

		private List<FlexoConceptInstance> getIndexedMatchingList(FetchRequestCondition indexableCondition, VirtualModelInstance<?, ?> vmi,
				RunTimeEvaluationContext evaluationContext)
				throws TypeMismatchException, NullReferenceException, InvocationTargetException {
			Expression indexableTerm = getIndexableTerm(indexableCondition);
			Expression oppositeTerm = getOppositeTerm(indexableCondition);

			// System.out.println("indexable term = " + indexableTerm);
			// System.out.println("opposite term = " + oppositeTerm);

			DataBinding<?> indexableTermBinding = new DataBinding<>(indexableTerm.toString(), indexableCondition, Object.class,
					BindingDefinitionType.GET);
			indexableTermBinding.setBindingName("indexableTerm");

			DataBinding<?> valueBinding = new DataBinding<>(oppositeTerm.toString(), indexableCondition, Object.class,
					BindingDefinitionType.GET);
			valueBinding.setBindingName("expectedValue");

			Object expectedValue = valueBinding.getBindingValue(evaluationContext);
			// System.out.println("Searching" + indexableTerm + " = " + expectedValue);

			Map<Object, List<FlexoConceptInstance>> index = vmi.getIndex(getFlexoConceptType().getInstanceType(), indexableTermBinding);

			if (index != null) {
				List<FlexoConceptInstance> returned = index.get(expectedValue);
				if (returned != null) {
					return returned;
				}
				else {
					return Collections.emptyList();
				}
			}

			return Collections.emptyList();
		}

		@Override
		public List<FlexoConceptInstance> execute(RunTimeEvaluationContext evaluationContext) {

			VirtualModelInstance<?, ?> vmi = getVirtualModelInstance(evaluationContext);
			FlexoConceptInstance container = getContainer(evaluationContext);

			if (container == null) {
				container = vmi;
			}

			if (vmi != null) {

				// System.out.println("SELECT FCI " + getFlexoConceptType().getName() + " from " + vmi + " container=" + container);

				if (isIndexable(container)) {
					List<FlexoConceptInstance> returned;
					try {
						// Compute returned as result of filter for first condition to apply
						returned = getIndexedMatchingList(getConditions().get(0), vmi, evaluationContext);

						// More than one condition, we need to merge multiple filters
						for (int i = 1; i < getConditions().size(); i++) {
							List<FlexoConceptInstance> filtered = getIndexedMatchingList(getConditions().get(i), vmi, evaluationContext);
							Iterator<FlexoConceptInstance> it = returned.iterator();
							while (it.hasNext()) {
								FlexoConceptInstance fci = it.next();
								if (!filtered.contains(fci)) {
									// fci is not in the filtered list, we discard it
									it.remove();
								}
							}
						}

						return returned;
					} catch (TypeMismatchException e) {
						e.printStackTrace();
					} catch (NullReferenceException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}

				// Otherwise, we do it brute force !!!

				List<FlexoConceptInstance> fciList = null;
				if (container instanceof FMLRTVirtualModelInstance) {
					fciList = ((VirtualModelInstance<?, ?>) container).getFlexoConceptInstances(getFlexoConceptType());
				}
				else {
					fciList = container.getEmbeddedFlexoConceptInstances(getFlexoConceptType());
				}
				// System.out.println("Unfiltered FCI list for " + getFlexoConceptType() + " : " + fciList);
				return filterWithConditions(fciList, evaluationContext);
			}
			else {
				logger.warning(
						getStringRepresentation() + " : Cannot find virtual model instance on which to apply SelectFlexoConceptInstance");
				logger.warning("getReceiver()=" + getReceiver());
				/*logger.warning("evaluationContext=" + evaluationContext);
				logger.warning("isSet=" + getVirtualModelInstance().isSet());
				logger.warning("isValid=" + getVirtualModelInstance().isValid());
				logger.warning("fci=" + evaluationContext.getFlexoConceptInstance());
				logger.warning("vmi=" + evaluationContext.getVirtualModelInstance());
				try {
					logger.warning("value=" + getVirtualModelInstance().getBindingValue(evaluationContext));
				} catch (TypeMismatchException e) {
					e.printStackTrace();
				} catch (NullReferenceException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}*/
				logger.warning(getOwner().getFMLRepresentation());
				return null;
			}
		}

		public boolean isIndexable(FlexoConceptInstance container) {
			if (container instanceof FMLRTVirtualModelInstance && getConditions().size() > 0) {
				for (FetchRequestCondition condition : getConditions()) {
					if (!isIndexableCondition(condition)) {
						return false;
					}
				}
				return true;
			}
			return false;

		}

		private boolean isIndexableCondition(FetchRequestCondition condition) {
			return getIndexableTerm(condition) != null;
		}

		private Expression getIndexableTerm(FetchRequestCondition condition) {
			if (condition.getCondition() != null && condition.getCondition().getExpression() instanceof BinaryOperatorExpression) {
				BinaryOperatorExpression binaryExpression = (BinaryOperatorExpression) condition.getCondition().getExpression();
				Expression leftTerm = binaryExpression.getLeftArgument();
				boolean leftTermUsesSelectedBindingVariable = expressionUsesSelectedBindingVariable(leftTerm);
				Expression rightTerm = binaryExpression.getRightArgument();
				boolean rightTermUsesSelectedBindingVariable = expressionUsesSelectedBindingVariable(rightTerm);
				if (binaryExpression.getOperator() == BooleanBinaryOperator.EQUALS) {
					if (leftTermUsesSelectedBindingVariable) {
						if (rightTermUsesSelectedBindingVariable) {
							return null;
						}
						else {
							return leftTerm;
						}
					}
					else {
						if (rightTermUsesSelectedBindingVariable) {
							return rightTerm;
						}
						else {
							return null;
						}
					}
				}
			}
			return null;
		}

		private Expression getOppositeTerm(FetchRequestCondition condition) {
			if (condition.getCondition() != null && condition.getCondition().getExpression() instanceof BinaryOperatorExpression) {
				BinaryOperatorExpression binaryExpression = (BinaryOperatorExpression) condition.getCondition().getExpression();
				Expression leftTerm = binaryExpression.getLeftArgument();
				boolean leftTermUsesSelectedBindingVariable = expressionUsesSelectedBindingVariable(leftTerm);
				Expression rightTerm = binaryExpression.getRightArgument();
				boolean rightTermUsesSelectedBindingVariable = expressionUsesSelectedBindingVariable(rightTerm);
				if (binaryExpression.getOperator() == BooleanBinaryOperator.EQUALS) {
					if (leftTermUsesSelectedBindingVariable) {
						if (rightTermUsesSelectedBindingVariable) {
							return null;
						}
						else {
							return rightTerm;
						}
					}
					else {
						if (rightTermUsesSelectedBindingVariable) {
							return leftTerm;
						}
						else {
							return null;
						}
					}
				}
			}
			return null;
		}

		private boolean expressionUsesSelectedBindingVariable(Expression exp) {
			List<BindingVariable> allBVs = exp.getAllBindingVariables();
			for (BindingVariable v : allBVs) {
				if (v instanceof FetchRequestConditionSelectedBindingVariable) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void notifiedBindingChanged(DataBinding<?> dataBinding) {
			super.notifiedBindingChanged(dataBinding);
			if (dataBinding == getReceiver()) {
				getPropertyChangeSupport().firePropertyChange("addressedVirtualModel", null, getAddressedVirtualModel());
			}
		}
	}

	@DefineValidationRule
	@SuppressWarnings({ "rawtypes" })
	public static class SelectFlexoConceptInstanceMustAddressAFlexoConceptType
			extends ValidationRule<SelectFlexoConceptInstanceMustAddressAFlexoConceptType, SelectFlexoConceptInstance> {
		public SelectFlexoConceptInstanceMustAddressAFlexoConceptType() {
			super(SelectFlexoConceptInstance.class, "select_flexo_concept_instance_action_must_address_a_valid_flexo_concept_type");
		}

		@Override
		public ValidationIssue<SelectFlexoConceptInstanceMustAddressAFlexoConceptType, SelectFlexoConceptInstance> applyValidation(
				SelectFlexoConceptInstance action) {
			if (action.getFlexoConceptType() == null) {
				return new ValidationError<SelectFlexoConceptInstanceMustAddressAFlexoConceptType, SelectFlexoConceptInstance>(this, action,
						"select_flexo_concept_instance_action_doesn't_define_any_flexo_concept_type");
			}
			return null;
		}
	}

	@DefineValidationRule
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static class VirtualModelInstanceBindingIsRequiredAndMustBeValid
			extends BindingIsRequiredAndMustBeValid<SelectFlexoConceptInstance> {
		public VirtualModelInstanceBindingIsRequiredAndMustBeValid() {
			super("'receiver'_binding_is_not_valid", SelectFlexoConceptInstance.class);
		}

		@Override
		public DataBinding<VirtualModelInstance<?, ?>> getBinding(SelectFlexoConceptInstance object) {
			return object.getReceiver();
		}

		@Override
		public ValidationIssue<BindingIsRequiredAndMustBeValid<SelectFlexoConceptInstance>, SelectFlexoConceptInstance> applyValidation(
				SelectFlexoConceptInstance object) {
			ValidationIssue<BindingIsRequiredAndMustBeValid<SelectFlexoConceptInstance>, SelectFlexoConceptInstance> returned = super.applyValidation(
					object);
			if (returned instanceof UndefinedRequiredBindingIssue) {
				((UndefinedRequiredBindingIssue) returned).addToFixProposals(new UseLocalVirtualModelInstance());
			}
			else {
				DataBinding<VirtualModelInstance<?, ?>> binding = getBinding(object);
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
							if (object.getFlexoConceptType().getVirtualModel().isAssignableFrom(ms.getAccessedVirtualModel())) {
								((ValidationError) returned).addToFixProposals(new UseFMLRTModelSlot(ms));
							}
						}

						if (object.getRootOwner().getFlexoConcept() instanceof VirtualModel) {
							for (FMLRTModelSlot ms : ((VirtualModel) object.getRootOwner().getFlexoConcept())
									.getModelSlots(FMLRTModelSlot.class)) {
								// System.out.println("modelSlot " + ms + " vm=" + ms.getAddressedVirtualModel());
								if (object.getFlexoConceptType().getVirtualModel().isAssignableFrom(ms.getAccessedVirtualModel())) {
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
				extends FixProposal<BindingIsRequiredAndMustBeValid<SelectFlexoConceptInstance>, SelectFlexoConceptInstance> {

			public UseLocalVirtualModelInstance() {
				super("sets_virtual_model_instance_to_'virtualModelInstance'_(local_virtual_model_instance)");
			}

			@Override
			protected void fixAction() {
				SelectFlexoConceptInstance action = getValidable();
				action.setReceiver(new DataBinding<>("virtualModelInstance"));
			}
		}

		protected static class UseFMLRTModelSlot
				extends FixProposal<BindingIsRequiredAndMustBeValid<SelectFlexoConceptInstance>, SelectFlexoConceptInstance> {

			private final FMLRTModelSlot modelSlot;

			public UseFMLRTModelSlot(FMLRTModelSlot modelSlot) {
				super("sets_virtual_model_instance_to_'" + modelSlot.getName() + "'");
				this.modelSlot = modelSlot;
			}

			@Override
			protected void fixAction() {
				SelectFlexoConceptInstance action = getValidable();
				action.setReceiver(new DataBinding<>(modelSlot.getName()));
			}
		}

	}

}
