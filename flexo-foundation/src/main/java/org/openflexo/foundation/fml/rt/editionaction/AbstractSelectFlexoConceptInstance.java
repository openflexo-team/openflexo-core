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
import org.openflexo.connie.expr.Expression;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoConceptInstanceType.DefaultFlexoConceptInstanceTypeFactory;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.binding.FetchRequestConditionSelectedBindingVariable;
import org.openflexo.foundation.fml.editionaction.AbstractFetchRequest;
import org.openflexo.foundation.fml.editionaction.FetchRequestCondition;
import org.openflexo.foundation.fml.expr.FMLBooleanBinaryOperator;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * Generic {@link AbstractFetchRequest} allowing to retrieve a selection of some {@link FlexoConceptInstance} matching some conditions and a
 * given {@link FlexoConcept}.<br>
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractSelectFlexoConceptInstance.AbstractSelectFlexoConceptInstanceImpl.class)
public interface AbstractSelectFlexoConceptInstance<VMI extends VirtualModelInstance<VMI, FMLRTTechnologyAdapter>, AT>
		extends AbstractFetchRequest<FMLRTModelSlot<VMI, FMLRTTechnologyAdapter>, VMI, FlexoConceptInstance, AT> {

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

	public FlexoConceptInstanceType getType();

	public void setType(FlexoConceptInstanceType type);

	public static abstract class AbstractSelectFlexoConceptInstanceImpl<VMI extends VirtualModelInstance<VMI, FMLRTTechnologyAdapter>, AT>
			extends AbstractFetchRequestImpl<FMLRTModelSlot<VMI, FMLRTTechnologyAdapter>, VMI, FlexoConceptInstance, AT>
			implements AbstractSelectFlexoConceptInstance<VMI, AT> {

		protected static final Logger logger = FlexoLogger.getLogger(AbstractSelectFlexoConceptInstance.class.getPackage().getName());

		private FlexoConceptInstanceType type;

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
				container = new DataBinding<>(this, FlexoConceptInstance.class, BindingDefinitionType.GET);
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
			String whereClauses = getWhereClausesFMLRepresentation();
			return "(type=" + (getFlexoConceptType() != null ? getFlexoConceptType().getName() : "null")
					+ (whereClauses != null ? "," + whereClauses : "") + ")";
		}

		@Override
		public FlexoConceptInstanceType getFetchedType() {
			return getType();
		}

		@Override
		public String _getFlexoConceptTypeURI() {
			if (type != null) {
				return type.getConceptURI();
			}
			return null;
		}

		private boolean isFetching = false;

		@Override
		public void _setFlexoConceptTypeURI(String flexoConceptURI) {
			type = new FlexoConceptInstanceType(flexoConceptURI, new DefaultFlexoConceptInstanceTypeFactory(getTechnologyAdapter()) {
				@Override
				public FlexoConcept resolveFlexoConcept(FlexoConceptInstanceType typeToResolve) {
					if (!isFetching && getAddressedVirtualModel() != null) {
						isFetching = true;
						FlexoConcept flexoConceptType = getAddressedVirtualModel().getFlexoConcept(typeToResolve.getConceptURI());
						isFetching = false;
						return flexoConceptType;
					}
					return null;
				}
			});
		}

		@Override
		public FlexoConcept getFlexoConceptType() {

			if (type != null) {
				if (!type.isResolved()) {
					type.resolve();
				}
				if (type.getFlexoConcept() != null) {
					return type.getFlexoConcept();
				}
			}
			return null;
		}

		@Override
		public void setFlexoConceptType(FlexoConcept flexoConceptType) {
			if (flexoConceptType != getFlexoConceptType()) {
				FlexoConcept oldValue = getFlexoConceptType();
				if (flexoConceptType != null) {
					type = flexoConceptType.getInstanceType();
				}
				else {
					type = null;
				}
				getPropertyChangeSupport().firePropertyChange("flexoConceptType", oldValue, flexoConceptType);
			}
		}

		@Override
		public FlexoConceptInstanceType getType() {
			return type;
		}

		@Override
		public void setType(FlexoConceptInstanceType type) {
			if ((type == null && this.type != null) || (type != null && !type.equals(this.type))) {
				FlexoConceptInstanceType oldValue = this.type;
				this.type = type;
				getPropertyChangeSupport().firePropertyChange("type", oldValue, type);
			}
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
				if (getReceiver().isValid()) {
					isAnalyzingContainer = true;
					Type vmiType = getReceiver().getAnalyzedType();
					isAnalyzingContainer = false;
					if (vmiType instanceof VirtualModelInstanceType) {
						return ((VirtualModelInstanceType) vmiType).getVirtualModel();
					}
				}
			}
			// I could not find VM, trying to "guess" (TODO: remove this hack ?)
			if (getFlexoConcept() instanceof VirtualModel) {
				return (VirtualModel) getFlexoConcept();
			}
			if (getInferedModelSlot() != null) {
				return getInferedModelSlot().getAccessedVirtualModel();
			}
			return getOwningVirtualModel();
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
				} catch (ReflectiveOperationException e) {
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
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		private List<FlexoConceptInstance> getIndexedMatchingList(FetchRequestCondition indexableCondition, VirtualModelInstance<?, ?> vmi,
				RunTimeEvaluationContext evaluationContext)
				throws TypeMismatchException, NullReferenceException, ReflectiveOperationException {
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
				return Collections.emptyList();
			}

			return Collections.emptyList();
		}

		@Override
		protected List<FlexoConceptInstance> performExecute(RunTimeEvaluationContext evaluationContext) {

			VirtualModelInstance<?, ?> vmi = getVirtualModelInstance(evaluationContext);
			FlexoConceptInstance container = getContainer(evaluationContext);

			if (vmi == null) {
				if (container instanceof VirtualModelInstance) {
					vmi = (VirtualModelInstance<?, ?>) container;
				}
				else if (container != null) {
					vmi = container.getOwningVirtualModelInstance();
				}
			}

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
					} catch (ReflectiveOperationException e) {
						e.printStackTrace();
					}
				}

				// Otherwise, we do it brute force !!!

				List<FlexoConceptInstance> fciList = null;
				if (container instanceof VirtualModelInstance) {
					fciList = ((VirtualModelInstance<?, ?>) container).getFlexoConceptInstances(getFlexoConceptType());
				}
				else {
					fciList = container.getEmbeddedFlexoConceptInstances(getFlexoConceptType());
				}
				// System.out.println("Unfiltered FCI list for " + getFlexoConceptType() + " : " + fciList);
				return filterWithConditions(fciList, evaluationContext);
			}
			logger.warning(
					getStringRepresentation() + " : Cannot find virtual model instance on which to apply SelectFlexoConceptInstance");
			/*
			logger.warning("evaluationContext=" + evaluationContext);
			logger.warning("isSet=" + getVirtualModelInstance().isSet());
			logger.warning("isValid=" + getVirtualModelInstance().isValid());
			logger.warning("fci=" + evaluationContext.getFlexoConceptInstance());
			logger.warning("vmi=" + evaluationContext.getVirtualModelInstance());
			 */
			// logger.warning(getOwner().getFMLRepresentation());
			return null;
		}

		public boolean isIndexable(FlexoConceptInstance container) {
			// Temporary desactivate indexes caching
			/*if (container instanceof FMLRTVirtualModelInstance && getConditions().size() > 0) {
				for (FetchRequestCondition condition : getConditions()) {
					if (!isIndexableCondition(condition)) {
						return false;
					}
				}
				return true;
			}*/
			return false;

		}

		private static boolean isIndexableCondition(FetchRequestCondition condition) {
			return getIndexableTerm(condition) != null;
		}

		private static Expression getIndexableTerm(FetchRequestCondition condition) {
			if (condition.getCondition() != null && condition.getCondition().getExpression() instanceof BinaryOperatorExpression) {
				BinaryOperatorExpression binaryExpression = (BinaryOperatorExpression) condition.getCondition().getExpression();
				Expression leftTerm = binaryExpression.getLeftArgument();
				boolean leftTermUsesSelectedBindingVariable = expressionUsesSelectedBindingVariable(leftTerm);
				Expression rightTerm = binaryExpression.getRightArgument();
				boolean rightTermUsesSelectedBindingVariable = expressionUsesSelectedBindingVariable(rightTerm);
				if (binaryExpression.getOperator() == FMLBooleanBinaryOperator.EQUALS) {
					if (leftTermUsesSelectedBindingVariable) {
						if (rightTermUsesSelectedBindingVariable) {
							return null;
						}
						return leftTerm;
					}
					if (rightTermUsesSelectedBindingVariable) {
						return rightTerm;
					}
					return null;
				}
			}
			return null;
		}

		private static Expression getOppositeTerm(FetchRequestCondition condition) {
			if (condition.getCondition() != null && condition.getCondition().getExpression() instanceof BinaryOperatorExpression) {
				BinaryOperatorExpression binaryExpression = (BinaryOperatorExpression) condition.getCondition().getExpression();
				Expression leftTerm = binaryExpression.getLeftArgument();
				boolean leftTermUsesSelectedBindingVariable = expressionUsesSelectedBindingVariable(leftTerm);
				Expression rightTerm = binaryExpression.getRightArgument();
				boolean rightTermUsesSelectedBindingVariable = expressionUsesSelectedBindingVariable(rightTerm);
				if (binaryExpression.getOperator() == FMLBooleanBinaryOperator.EQUALS) {
					if (leftTermUsesSelectedBindingVariable) {
						if (rightTermUsesSelectedBindingVariable)
							return null;
						return rightTerm;
					}
					if (rightTermUsesSelectedBindingVariable)
						return leftTerm;
					return null;
				}
			}
			return null;
		}

		private static boolean expressionUsesSelectedBindingVariable(Expression exp) {
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

		@Override
		public boolean isReceiverMandatory() {
			return false;
		}
	}

	@DefineValidationRule
	@SuppressWarnings({ "rawtypes" })
	public static class SelectFlexoConceptInstanceMustAddressAFlexoConceptType
			extends ValidationRule<SelectFlexoConceptInstanceMustAddressAFlexoConceptType, AbstractSelectFlexoConceptInstance> {
		public SelectFlexoConceptInstanceMustAddressAFlexoConceptType() {
			super(AbstractSelectFlexoConceptInstance.class, "select_flexo_concept_instance_action_must_address_a_valid_flexo_concept_type");
		}

		@Override
		public ValidationIssue<SelectFlexoConceptInstanceMustAddressAFlexoConceptType, AbstractSelectFlexoConceptInstance> applyValidation(
				AbstractSelectFlexoConceptInstance action) {
			if (action.getFlexoConceptType() == null) {
				return new ValidationError<>(this, action, "select_flexo_concept_instance_action_doesn't_define_any_flexo_concept_type");
			}
			return null;
		}
	}

	/*@DefineValidationRule
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static class VirtualModelInstanceBindingIsRequiredAndMustBeValid
			extends BindingIsRequiredAndMustBeValid<AbstractSelectFlexoConceptInstance> {
		public VirtualModelInstanceBindingIsRequiredAndMustBeValid() {
			super("'receiver'_binding_is_not_valid", AbstractSelectFlexoConceptInstance.class);
		}
	
		@Override
		public DataBinding<VirtualModelInstance<?, ?>> getBinding(AbstractSelectFlexoConceptInstance object) {
			return object.getReceiver();
		}
	
		@Override
		public ValidationIssue<BindingIsRequiredAndMustBeValid<AbstractSelectFlexoConceptInstance>, AbstractSelectFlexoConceptInstance> applyValidation(
				AbstractSelectFlexoConceptInstance object) {
			ValidationIssue<BindingIsRequiredAndMustBeValid<AbstractSelectFlexoConceptInstance>, AbstractSelectFlexoConceptInstance> returned = super.applyValidation(
					object);
			if (returned instanceof UndefinedRequiredBindingIssue) {
				((UndefinedRequiredBindingIssue) returned).addToFixProposals(new UseLocalVirtualModelInstance());
			}
			else {
				DataBinding<VirtualModelInstance<?, ?>> binding = getBinding(object);
				if (binding.getAnalyzedType() instanceof VirtualModelInstanceType && object.getFlexoConceptType() != null) {
					if (object.getFlexoConceptType().getOwner() != ((VirtualModelInstanceType) binding.getAnalyzedType())
							.getVirtualModel()) {
						returned = new ValidationError(this, object, "incompatible_virtual_model_type");
						// Attempt to find some solutions...
	
						if (object.getOwningVirtualModel() != null) {
							for (FMLRTModelSlot ms : object.getOwningVirtualModel().getModelSlots(FMLRTModelSlot.class)) {
								// System.out.println("modelSlot " + ms + " vm=" + ms.getAddressedVirtualModel());
								if (object.getFlexoConceptType().getOwner().isAssignableFrom(ms.getAccessedVirtualModel())) {
									((ValidationError) returned).addToFixProposals(new UseFMLRTModelSlot(ms));
								}
							}
						}
	
						if (object.getRootOwner().getFlexoConcept() instanceof VirtualModel) {
							for (FMLRTModelSlot ms : ((VirtualModel) object.getRootOwner().getFlexoConcept())
									.getModelSlots(FMLRTModelSlot.class)) {
								// System.out.println("modelSlot " + ms + " vm=" + ms.getAddressedVirtualModel());
								if (object.getFlexoConceptType().getOwner().isAssignableFrom(ms.getAccessedVirtualModel())) {
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
				FixProposal<BindingIsRequiredAndMustBeValid<AbstractSelectFlexoConceptInstance>, AbstractSelectFlexoConceptInstance> {
	
			public UseLocalVirtualModelInstance() {
				super("sets_virtual_model_instance_to_'virtualModelInstance'_(local_virtual_model_instance)");
			}
	
			@Override
			protected void fixAction() {
				AbstractSelectFlexoConceptInstance action = getValidable();
				action.setReceiver(new DataBinding<>("virtualModelInstance"));
			}
		}
	
		protected static class UseFMLRTModelSlot extends
				FixProposal<BindingIsRequiredAndMustBeValid<AbstractSelectFlexoConceptInstance>, AbstractSelectFlexoConceptInstance> {
	
			private final FMLRTModelSlot modelSlot;
	
			public UseFMLRTModelSlot(FMLRTModelSlot modelSlot) {
				super("sets_virtual_model_instance_to_'" + modelSlot.getName() + "'");
				this.modelSlot = modelSlot;
			}
	
			@Override
			protected void fixAction() {
				AbstractSelectFlexoConceptInstance action = getValidable();
				action.setReceiver(new DataBinding<>(modelSlot.getName()));
			}
		}
	
	}*/

}
