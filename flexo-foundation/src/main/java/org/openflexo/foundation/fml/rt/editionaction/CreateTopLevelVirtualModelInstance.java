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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FMLMigration;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.annotations.FML;
import org.openflexo.foundation.fml.rt.FMLExecutionException;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResource;
import org.openflexo.foundation.fml.rt.rm.FMLRTVirtualModelInstanceResourceFactory;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.pamela.annotations.DefineValidationRule;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.validation.FixProposal;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationRule;

/**
 * 
 * This action is used to explicitely instanciate a new top-level {@link FMLRTVirtualModelInstance}
 * 
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(CreateTopLevelVirtualModelInstance.CreateTopLevelVirtualModelInstanceImpl.class)
@XMLElement
@FML("CreateTopLevelVirtualModelInstance")
@FMLMigration("ExpressionAction should be used instead")
@Deprecated
public interface CreateTopLevelVirtualModelInstance extends AbstractAddFMLRTVirtualModelInstance {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String RESOURCE_URI_KEY = "resourceURI";
	@PropertyIdentifier(type = FlexoResourceCenter.class)
	public static final String RESOURCE_CENTER_KEY = "resourceCenter";
	@PropertyIdentifier(type = String.class)
	public static final String DYNAMIC_RELATIVE_PATH_KEY = "dynamicRelativePath";

	@Getter(value = RESOURCE_URI_KEY)
	@XMLAttribute
	public DataBinding<String> getResourceURI();

	@Setter(RESOURCE_URI_KEY)
	public void setResourceURI(DataBinding<String> resourceURI);

	@Getter(value = RESOURCE_CENTER_KEY)
	@XMLAttribute
	public DataBinding<FlexoResourceCenter<?>> getResourceCenter();

	@Setter(RESOURCE_CENTER_KEY)
	public void setResourceCenter(DataBinding<FlexoResourceCenter<?>> resourceCenter);

	@Getter(value = DYNAMIC_RELATIVE_PATH_KEY)
	@XMLAttribute
	public DataBinding<String> getDynamicRelativePath();

	@Setter(DYNAMIC_RELATIVE_PATH_KEY)
	public void setDynamicRelativePath(DataBinding<String> relativePath);

	public static abstract class CreateTopLevelVirtualModelInstanceImpl extends AbstractAddFMLRTVirtualModelInstanceImpl
			implements CreateTopLevelVirtualModelInstance {

		private static final Logger logger = Logger.getLogger(CreateTopLevelVirtualModelInstance.class.getPackage().getName());

		private DataBinding<String> dynamicRelativePath;
		private DataBinding<String> resourceURI;

		public <T> T evaluateDataBinding(DataBinding<T> dataBinding, RunTimeEvaluationContext evaluationContext) {
			if (dataBinding != null && dataBinding.isSet() && dataBinding.isValid()) {
				try {
					return dataBinding.getBindingValue(evaluationContext);
				} catch (TypeMismatchException | NullReferenceException | ReflectiveOperationException e) {
					logger.log(Level.WARNING,
							"Can't evaluate data binding " + dataBinding.getBindingName() + " (" + dataBinding.getExpression() + ")");
				}
			}
			return null;
		}

		public String getResourceName(RunTimeEvaluationContext evaluationContext) {
			return evaluateDataBinding(getVirtualModelInstanceName(), evaluationContext);
		}

		public String getResourceTitle(RunTimeEvaluationContext evaluationContext) {
			return evaluateDataBinding(getVirtualModelInstanceTitle(), evaluationContext);
		}

		public String getResourceURI(RunTimeEvaluationContext evaluationContext) {
			return evaluateDataBinding(getResourceURI(), evaluationContext);
		}

		public FlexoResourceCenter<?> getResourceCenter(RunTimeEvaluationContext evaluationContext) {
			return evaluateDataBinding(getResourceCenter(), evaluationContext);
		}

		public String getRelativePath(RunTimeEvaluationContext evaluationContext) {
			if (getDynamicRelativePath().isSet()) {
				return evaluateDataBinding(getDynamicRelativePath(), evaluationContext);
			}
			return "";
		}

		@Override
		public DataBinding<String> getResourceURI() {
			if (resourceURI == null) {
				resourceURI = new DataBinding<>(this, String.class, DataBinding.BindingDefinitionType.GET);
				resourceURI.setBindingName("resourceURI");
			}
			return resourceURI;
		}

		@Override
		public void setResourceURI(DataBinding<String> resourceURI) {
			if (resourceURI != null) {
				resourceURI.setOwner(this);
				resourceURI.setDeclaredType(String.class);
				resourceURI.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				resourceURI.setBindingName("resourceURI");
			}
			this.resourceURI = resourceURI;
		}

		@Override
		public DataBinding<String> getDynamicRelativePath() {
			if (dynamicRelativePath == null) {
				dynamicRelativePath = new DataBinding<>(this, String.class, DataBinding.BindingDefinitionType.GET);
				dynamicRelativePath.setBindingName("dynamicRelativePath");
			}
			return dynamicRelativePath;
		}

		@Override
		public void setDynamicRelativePath(DataBinding<String> dynamicRelativePath) {
			if (dynamicRelativePath != null) {
				dynamicRelativePath.setOwner(this);
				dynamicRelativePath.setDeclaredType(String.class);
				dynamicRelativePath.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				dynamicRelativePath.setBindingName("dynamicRelativePath");
			}
			this.dynamicRelativePath = dynamicRelativePath;
		}

		private DataBinding<FlexoResourceCenter<?>> resourceCenter;

		@Override
		public DataBinding<FlexoResourceCenter<?>> getResourceCenter() {
			if (resourceCenter == null) {
				resourceCenter = new DataBinding<>(this, FlexoResourceCenter.class, DataBinding.BindingDefinitionType.GET);
				resourceCenter.setBindingName("resourceCenter");
			}
			return resourceCenter;
		}

		@Override
		public void setResourceCenter(DataBinding<FlexoResourceCenter<?>> resourceCenter) {
			if (resourceCenter != null) {
				resourceCenter.setOwner(this);
				resourceCenter.setDeclaredType(FlexoResourceCenter.class);
				resourceCenter.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				resourceCenter.setBindingName("resourceCenter");
			}
			this.resourceCenter = resourceCenter;
		}

		@Override
		public boolean isReceiverMandatory() {
			return false;
		}

		@SuppressWarnings("unchecked")
		private <I> RepositoryFolder<FMLRTVirtualModelInstanceResource, I> getRepositoryFolder(RunTimeEvaluationContext evaluationContext)
				throws IOException {

			FMLRTTechnologyAdapter technologyAdapter = getServiceManager().getTechnologyAdapterService()
					.getTechnologyAdapter(FMLRTTechnologyAdapter.class);

			String resourceName = getResourceName(evaluationContext);
			FlexoResourceCenter<I> rc = (FlexoResourceCenter<I>) getResourceCenter(evaluationContext);
			String relativePath = getRelativePath(evaluationContext);

			I artefact = technologyAdapter.retrieveResourceSerializationArtefact(rc, resourceName, relativePath,
					FMLRTVirtualModelInstanceResourceFactory.FML_RT_SUFFIX);
			return technologyAdapter.getGlobalRepository(rc).getParentRepositoryFolder(artefact, true);

		}

		@Override
		protected FMLRTVirtualModelInstance makeNewFlexoConceptInstance(RunTimeEvaluationContext evaluationContext)
				throws FMLExecutionException {

			FMLRTTechnologyAdapter technologyAdapter = getServiceManager().getTechnologyAdapterService()
					.getTechnologyAdapter(FMLRTTechnologyAdapter.class);
			FMLRTVirtualModelInstanceResourceFactory factory = technologyAdapter.getFMLRTVirtualModelInstanceResourceFactory();

			try {

				String resourceName = getResourceName(evaluationContext);
				String resourceTitle = getResourceTitle(evaluationContext);
				String resourceURI = getResourceURI(evaluationContext);
				RepositoryFolder<FMLRTVirtualModelInstanceResource, ?> folder = getRepositoryFolder(evaluationContext);

				VirtualModel instantiatedVirtualModel = retrieveVirtualModel(evaluationContext);

				FMLRTVirtualModelInstanceResource returned = null;
				returned = factory.makeTopLevelFMLRTVirtualModelInstanceResource(resourceName, resourceURI,
						instantiatedVirtualModel.getResource(), folder, true);
				if (returned != null) {
					returned.getLoadedResourceData().setTitle(resourceTitle);
				}

				return returned.getLoadedResourceData();

			} catch (IOException e) {
				throw new FMLExecutionException(e);
			} catch (ModelDefinitionException e) {
				throw new FMLExecutionException(e);
			} catch (SaveResourceException e) {
				throw new FMLExecutionException(e);
			}

		}

		@Override
		public FMLRTVirtualModelInstance execute(RunTimeEvaluationContext evaluationContext) throws FMLExecutionException {

			loadMetaModelWhenRequired();

			if (getDynamicInstantiation() && !getDynamicFlexoConceptType().isValid()) {
				logger.warning("undefined or invalid dynamic concept type while creating new concept");
				return null;
			}

			if (!getDynamicInstantiation() && getFlexoConceptType() == null) {
				logger.warning("null concept type while creating new concept");
				return null;
			}

			if (logger.isLoggable(Level.FINE)) {
				logger.fine("concept=" + (getCreationScheme() != null ? getCreationScheme().getFlexoConcept() : null));
				logger.fine("getCreationScheme()=" + getCreationScheme());
			}

			FMLRTVirtualModelInstance returned = makeNewFlexoConceptInstance(evaluationContext);

			if (!getDynamicInstantiation()) {
				if (executeCreationScheme(returned, null, evaluationContext)) {
					if (logger.isLoggable(Level.FINE)) {
						logger.fine("Successfully performed performAddFlexoConcept " + evaluationContext);
					}
					return returned;
				}
				else {
					logger.warning("Failing execution of creationScheme: " + getCreationScheme());
				}
			}
			else {
				logger.warning("I should find default creationScheme for dynamic instantiation");
				CreationScheme applicableCreationScheme = findBestCreationSchemeForDynamicInstantiation(evaluationContext);
				System.out.println("Found: " + applicableCreationScheme);
				if (applicableCreationScheme != null) {
					if (_performExecuteCreationScheme(applicableCreationScheme, returned, null, evaluationContext)) {
						if (logger.isLoggable(Level.FINE)) {
							logger.fine("Successfully performed performAddFlexoConcept " + evaluationContext);
						}
						return returned;
					}
					else {
						logger.warning("Failing execution of creationScheme: " + applicableCreationScheme);
					}
				}
			}

			return null;
		}

		@Override
		public void revalidateBindings() {
			super.revalidateBindings();
			getResourceCenter().rebuild();
			getDynamicRelativePath().rebuild();
			getResourceURI().rebuild();
		}

	}

	@DefineValidationRule
	public static class ResourceNameIsRequiredAndMustBeValid
			extends BindingIsRecommandedAndShouldBeValid<CreateTopLevelVirtualModelInstance> {
		public ResourceNameIsRequiredAndMustBeValid() {
			super("'resource_uri'_binding_is_recommanded_and_should_be_valid", CreateTopLevelVirtualModelInstance.class);
		}

		@Override
		public DataBinding<String> getBinding(CreateTopLevelVirtualModelInstance object) {
			return object.getResourceURI();
		}

	}

	@DefineValidationRule
	public static class ResourceCenterShouldNotBeNull
			extends ValidationRule<ResourceCenterShouldNotBeNull, CreateTopLevelVirtualModelInstance> {

		public ResourceCenterShouldNotBeNull() {
			super(CreateTopLevelVirtualModelInstance.class, "CreateResource_need_a_rc");
		}

		@Override
		public ValidationIssue<ResourceCenterShouldNotBeNull, CreateTopLevelVirtualModelInstance> applyValidation(
				CreateTopLevelVirtualModelInstance anAction) {
			DataBinding<?> rcbinding = anAction.getResourceCenter();
			if (rcbinding == null || rcbinding.isNull() || rcbinding.getExpression() == null) {
				SetResourceCenterBeingCurrentResourceCenterByDefault fixProposal = new SetResourceCenterBeingCurrentResourceCenterByDefault(
						anAction);
				return new ValidationError<>(this, anAction, "CreateTopLevelVirtualModelInstance_should_not_have_null_resource_center",
						fixProposal);

			}
			return null;
		}

		protected static class SetResourceCenterBeingCurrentResourceCenterByDefault
				extends FixProposal<ResourceCenterShouldNotBeNull, CreateTopLevelVirtualModelInstance> {

			private final CreateTopLevelVirtualModelInstance action;

			public SetResourceCenterBeingCurrentResourceCenterByDefault(CreateTopLevelVirtualModelInstance anAction) {
				super("set_resource_center_to_'this.resourceCenter'");
				this.action = anAction;
			}

			@Override
			protected void fixAction() {
				action.getResourceCenter().setUnparsedBinding("this.resourceCenter");
				action.notifiedBindingChanged(action.getResourceCenter());
			}
		}

	}

}
