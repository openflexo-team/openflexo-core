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

package org.openflexo.foundation.fml.editionaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceFactory;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.model.annotations.DefineValidationRule;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.validation.FixProposal;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationRule;
import org.openflexo.model.validation.ValidationWarning;

/**
 * Abstract edition action allowing to create an empty resource in a {@link FlexoResourceCenter}, at a specified relative path<br>
 * 
 * Note that this class must be subclassed in each {@link TechnologyAdapter}
 * 
 * @author sylvain
 *
 * @param <MS>
 *            type of model slot on which this {@link EditionAction} applies
 * @param <RD>
 *            type of data beeing created by this {@link EditionAction} (resource data)
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractCreateResource.AbstractCreateResourceImpl.class)
public interface AbstractCreateResource<MS extends ModelSlot<RD>, RD extends ResourceData<RD> & TechnologyObject<TA>, TA extends TechnologyAdapter>
		extends TechnologySpecificAction<MS, RD> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String RESOURCE_NAME_KEY = "resourceName";
	@PropertyIdentifier(type = DataBinding.class)
	public static final String RESOURCE_URI_KEY = "resourceURI";
	@PropertyIdentifier(type = FlexoResourceCenter.class)
	public static final String RESOURCE_CENTER_KEY = "resourceCenter";
	@PropertyIdentifier(type = String.class)
	public static final String RELATIVE_PATH_KEY = "relativePath";

	@Getter(value = RESOURCE_NAME_KEY)
	@XMLAttribute
	public DataBinding<String> getResourceName();

	@Setter(RESOURCE_NAME_KEY)
	public void setResourceName(DataBinding<String> resourceName);

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

	@Getter(value = RELATIVE_PATH_KEY)
	@XMLAttribute
	public String getRelativePath();

	@Setter(RELATIVE_PATH_KEY)
	public void setRelativePath(String relativePath);

	public static abstract class AbstractCreateResourceImpl<MS extends ModelSlot<RD>, RD extends ResourceData<RD> & TechnologyObject<TA>, TA extends TechnologyAdapter>
			extends TechnologySpecificActionImpl<MS, RD>implements AbstractCreateResource<MS, RD, TA> {

		private static final Logger logger = Logger.getLogger(AbstractCreateResource.class.getPackage().getName());

		private DataBinding<String> resourceName;
		private DataBinding<String> resourceURI;

		@Override
		public FlexoProperty<RD> getAssignedFlexoProperty() {
			return super.getAssignedFlexoProperty();
		}

		public String getResourceName(RunTimeEvaluationContext evaluationContext) {

			if (getResourceName() != null && getResourceName().isSet() && getResourceName().isValid()) {
				try {
					return getResourceName().getBindingValue(evaluationContext);
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

		public String getResourceURI(RunTimeEvaluationContext evaluationContext) {
			if (getResourceURI() != null && getResourceURI().isSet() && getResourceURI().isValid()) {
				try {
					return getResourceURI().getBindingValue(evaluationContext);
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

		public FlexoResourceCenter<?> getResourceCenter(RunTimeEvaluationContext evaluationContext) {
			try {
				return getResourceCenter().getBindingValue(evaluationContext);
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
		public DataBinding<String> getResourceName() {
			if (resourceName == null) {
				resourceName = new DataBinding<String>(this, String.class, DataBinding.BindingDefinitionType.GET);
				resourceName.setBindingName("resourceName");
			}
			return resourceName;
		}

		@Override
		public void setResourceName(DataBinding<String> diagramName) {
			if (diagramName != null) {
				diagramName.setOwner(this);
				diagramName.setDeclaredType(String.class);
				diagramName.setBindingDefinitionType(DataBinding.BindingDefinitionType.GET);
				diagramName.setBindingName("resourceName");
			}
			this.resourceName = diagramName;
		}

		@Override
		public DataBinding<String> getResourceURI() {
			if (resourceURI == null) {
				resourceURI = new DataBinding<String>(this, String.class, DataBinding.BindingDefinitionType.GET);
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

		private DataBinding<FlexoResourceCenter<?>> resourceCenter;

		@Override
		public DataBinding<FlexoResourceCenter<?>> getResourceCenter() {
			if (resourceCenter == null) {
				resourceCenter = new DataBinding<FlexoResourceCenter<?>>(this, FlexoResourceCenter.class,
						DataBinding.BindingDefinitionType.GET);
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
		public abstract Type getAssignableType();

		protected <I, R extends TechnologyAdapterResource<RD, TA>, RF extends FlexoResourceFactory<R, RD, TA>> R createResource(
				TA technologyAdapter, Class<RF> resourceFactoryClass, FlexoResourceCenter<I> resourceCenter, String resourceName,
				String resourceURI, String relativePath, String extension, boolean createEmptyContents)
						throws SaveResourceException, ModelDefinitionException {

			System.out.println("Creating resource from " + resourceFactoryClass);

			if (technologyAdapter == null) {
				logger.warning("Could not access TechnologyAdapter while creating resource from " + resourceFactoryClass);
				return null;
			}

			return technologyAdapter.createResource(resourceFactoryClass, resourceCenter, resourceName, resourceURI, relativePath,
					extension, createEmptyContents);
		}

	}

	/* Validation Rule to avoid ResourceCenter to be Null/Empty */

	@DefineValidationRule
	public static class ResourceCenterShouldNotBeNull
			extends ValidationRule<ResourceCenterShouldNotBeNull, TechnologySpecificAction<?, ?>> {

		public ResourceCenterShouldNotBeNull() {
			super(TechnologySpecificAction.class, "CreateResource_need_a_rc");
		}

		@Override
		public ValidationIssue<ResourceCenterShouldNotBeNull, TechnologySpecificAction<?, ?>> applyValidation(
				TechnologySpecificAction<?, ?> anAction) {
			DataBinding rcbinding = ((AbstractCreateResource) anAction).getResourceCenter();
			if (rcbinding == null || rcbinding.isNull() || rcbinding.getExpression() == null) {
				SetResourceCenterBeingProjectByDefault fixProposal = new SetResourceCenterBeingProjectByDefault(anAction);
				return new ValidationWarning<ResourceCenterShouldNotBeNull, TechnologySpecificAction<?, ?>>(this, anAction,
						"CreateResource_should_not_have_null_RC", fixProposal);

			}
			return null;
		}

		protected static class SetResourceCenterBeingProjectByDefault
				extends FixProposal<ResourceCenterShouldNotBeNull, TechnologySpecificAction<?, ?>> {

			private final TechnologySpecificAction<?, ?> action;

			public SetResourceCenterBeingProjectByDefault(TechnologySpecificAction<?, ?> anAction) {
				super("set_rc_defaulting_to_project");
				this.action = anAction;
			}

			@Override
			protected void fixAction() {
				((AbstractCreateResource<?, ?, ?>) action).getResourceCenter().setUnparsedBinding("project");
			}
		}

	}
}
