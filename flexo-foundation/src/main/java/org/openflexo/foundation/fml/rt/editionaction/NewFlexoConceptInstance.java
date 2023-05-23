/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexodiagram, a component of the software infrastructure 
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

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.fml.annotations.UsageExample;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.CreationSchemeAction;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.ta.FlexoConceptType;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * This edition primitive allows to dynamically instantiate a {@link FlexoConceptInstance}
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(NewFlexoConceptInstance.NewFlexoConceptInstanceImpl.class)
@XMLElement
@FML(
		value = "NewInstance",
		description = "<html>This edition primitive allows to dynamically instantiate a FlexoConceptInstance</html>",
		examples = { @UsageExample(
				example = "myInstance = NewInstance(conceptType=aConceptType,container=object,arg0=anArgument);",
				description = "Instantiate a new instance of concept referenced by ’aConceptType’ with supplied container and arguments, and assign this new instance to ‘myInstance’") }/*,
																																															references = { @SeeAlso(AClass.class)}*/)
public interface NewFlexoConceptInstance extends AbstractAddFlexoConceptInstance<FlexoConceptInstance, FMLRTVirtualModelInstance> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONCEPT_TYPE_KEY = "conceptType";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String CONTAINER_KEY = "container";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String ARGUMENTS_KEY = "args";

	@Override
	@Getter(value = CONTAINER_KEY)
	@FMLAttribute(value = CONTAINER_KEY, required = false, description = "<html>container for newly created concept instance</html>")
	public DataBinding<FlexoConceptInstance> getContainer();

	@Override
	@Setter(CONTAINER_KEY)
	public void setContainer(DataBinding<FlexoConceptInstance> container);

	@Override
	@Getter(value = CONCEPT_TYPE_KEY)
	@FMLAttribute(value = CONCEPT_TYPE_KEY, required = true, description = "<html>concept type to be instantiated</html>")
	public DataBinding<FlexoConcept> getDynamicFlexoConceptType();

	@Override
	@Setter(CONCEPT_TYPE_KEY)
	public void setDynamicFlexoConceptType(DataBinding<FlexoConcept> dynamicFlexoConceptType);

	@Getter(value = ARGUMENTS_KEY)
	@FMLAttribute(value = ARGUMENTS_KEY, required = false, description = "<html>list of arguments</html>")
	public DataBinding<List> getArguments();

	@Setter(ARGUMENTS_KEY)
	public void setArguments(DataBinding<List> args);

	public static abstract class NewFlexoConceptInstanceImpl extends
			AbstractAddFlexoConceptInstanceImpl<FlexoConceptInstance, FMLRTVirtualModelInstance> implements NewFlexoConceptInstance {

		private static final Logger logger = Logger.getLogger(NewFlexoConceptInstance.class.getPackage().getName());

		private DataBinding<List> arguments;

		@Override
		public DataBinding<List> getArguments() {
			if (arguments == null) {
				arguments = new DataBinding<>(this, List.class, DataBinding.BindingDefinitionType.GET);
				arguments.setBindingName(ARGUMENTS_KEY);
				arguments.setDeclaredType(List.class);
			}
			return arguments;
		}

		@Override
		public void setArguments(DataBinding<List> arguments) {
			if (arguments != null) {
				arguments.setOwner(this);
				arguments.setBindingName(ARGUMENTS_KEY);
				arguments.setDeclaredType(List.class);
				arguments.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
			}
			this.arguments = arguments;
		}

		@Override
		public FlexoConceptInstance execute(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {
			return super.execute(evaluationContext);
		}

		@Override
		public boolean getDynamicInstantiation() {
			return true;
		}

		@Override
		public FMLRTVirtualModelInstance getVirtualModelInstance(RunTimeEvaluationContext evaluationContext) {
			FMLRTVirtualModelInstance returned = super.getVirtualModelInstance(evaluationContext);
			if (returned == null) {
				FlexoConceptInstance container = getContainer(evaluationContext);
				if (container != null) {
					return (FMLRTVirtualModelInstance) container.getVirtualModelInstance();
				}
			}
			return returned;
		}

		@Override
		public boolean isReceiverMandatory() {
			return false;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<FMLRTVirtualModelInstance> getVirtualModelInstanceClass() {
			return FMLRTVirtualModelInstance.class;
		}

		@Override
		protected Class<? extends FlexoConcept> getDynamicFlexoConceptTypeType() {
			return FlexoConcept.class;
		}

		@Override
		public Type getAssignableType() {
			if (getDynamicFlexoConceptType() != null) {
				Type conceptType = getDynamicFlexoConceptType().getAnalyzedType();
				if (conceptType instanceof FlexoConceptType) {
					return ((FlexoConceptType) conceptType).getType();
				}
			}
			return super.getAssignableType();
		}

		@Override
		protected FlexoConceptInstance makeNewFlexoConceptInstance(RunTimeEvaluationContext evaluationContext)
				throws FMLExecutionException {
			FlexoConceptInstance container = null;
			FMLRTVirtualModelInstance vmi = getVirtualModelInstance(evaluationContext);
			FlexoConcept instantiatedFlexoConcept = retrieveFlexoConcept(evaluationContext);
			if (instantiatedFlexoConcept.getApplicableContainerFlexoConcept() != null) {
				container = getContainer(evaluationContext);
				if (container == null) {
					logger.warning("null container while creating new concept " + instantiatedFlexoConcept);
					return null;
				}
			}

			return vmi.makeNewFlexoConceptInstance(instantiatedFlexoConcept, container);
		}

		@Override
		protected CreationScheme findBestCreationSchemeForDynamicInstantiation(RunTimeEvaluationContext evaluationContext)
				throws FMLExecutionException {

			FlexoConcept instantiatedFlexoConcept = retrieveFlexoConcept(evaluationContext);
			List<?> arguments;
			try {
				arguments = getArguments().getBindingValue(evaluationContext);
			} catch (TypeMismatchException | NullReferenceException | ReflectiveOperationException e) {
				e.printStackTrace();
				throw new FMLExecutionException(e);
			}
			if (arguments == null) {
				arguments = Collections.emptyList();
			}

			if (instantiatedFlexoConcept != null) {
				for (CreationScheme creationScheme : instantiatedFlexoConcept.getAccessibleCreationSchemes()) {
					if (creationScheme.getParameters().size() == arguments.size()) {
						boolean allParametersMatch = true;
						for (int i = 0; i < creationScheme.getParameters().size(); i++) {
							FlexoBehaviourParameter p = creationScheme.getParameters().get(i);
							if (!TypeUtils.isOfType(arguments.get(i), p.getArgumentType())) {
								allParametersMatch = false;
							}
						}
						if (allParametersMatch) {
							return creationScheme;
						}
					}
				}
			}

			return super.findBestCreationSchemeForDynamicInstantiation(evaluationContext);
		}

		@Override
		protected boolean _performExecuteCreationScheme(CreationScheme creationScheme, FlexoConceptInstance newInstance,
				VirtualModelInstance<?, ?> vmInstance, RunTimeEvaluationContext evaluationContext) {
			if (evaluationContext instanceof FlexoBehaviourAction) {
				CreationSchemeAction creationSchemeAction = new CreationSchemeAction(creationScheme, vmInstance, null,
						(FlexoBehaviourAction<?, ?, ?>) evaluationContext);
				creationSchemeAction.initWithFlexoConceptInstance(newInstance);
				try {
					List<?> arguments = getArguments().getBindingValue(evaluationContext);
					for (int i = 0; i < creationScheme.getParameters().size(); i++) {
						FlexoBehaviourParameter p = creationScheme.getParameters().get(i);
						creationSchemeAction.setParameterValue(p, arguments.get(i));
					}
					creationSchemeAction.doAction();

					return creationSchemeAction.hasActionExecutionSucceeded();
				} catch (TypeMismatchException | NullReferenceException | ReflectiveOperationException e) {
					e.printStackTrace();
					return false;
				}
			}
			logger.warning("Unexpected: " + evaluationContext);
			Thread.dumpStack();
			return false;
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getArguments().rebuild();
			getContainer().rebuild();
			getDynamicFlexoConceptType().rebuild();
		}

	}

}
