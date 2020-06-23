/**
 * 
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo.foundation.technologyadapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.editionaction.AbstractFetchRequest;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Import;
import org.openflexo.pamela.annotations.Imports;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;

/**
 * A model slot is a named object providing access to a particular data encoded in a given technology<br>
 * A model slot should be seen as a connector to a data.<br>
 * A model slot formalizes a contract for accessing to a data
 * 
 * It is defined at FML level. <br>
 * A {@link ModelSlotInstance} binds used slots to some data
 * 
 * @param <RD>
 *            Type of resource data handled by this ModelSlot
 * 
 * @author Sylvain Guerin
 * @see org.openflexo.foundation.fml.ViewPoint
 * @see org.openflexo.foundation.fml.rt.View
 * @see org.openflexo.foundation.fml.rt.ModelSlotInstance
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(ModelSlot.ModelSlotImpl.class)
@Imports({ @Import(FMLRTModelSlot.class), @Import(TypeAwareModelSlot.class), @Import(FreeModelSlot.class) })
public interface ModelSlot<RD extends ResourceData<RD> & TechnologyObject<?>> extends FlexoRole<RD>, ModelSlotObject<RD> {

	@PropertyIdentifier(type = VirtualModel.class)
	public static final String OWNER_KEY = "owner";

	@PropertyIdentifier(type = boolean.class)
	public static final String IS_REQUIRED_KEY = "isRequired";
	@PropertyIdentifier(type = boolean.class)
	public static final String IS_READ_ONLY_KEY = "isReadOnly";

	@Override
	public FMLModelFactory getFMLModelFactory();

	@Override
	@Getter(value = IS_REQUIRED_KEY, defaultValue = "false")
	@XMLAttribute
	public boolean getIsRequired();

	@Override
	@Setter(IS_REQUIRED_KEY)
	public void setIsRequired(boolean isRequired);

	@Getter(value = IS_READ_ONLY_KEY, defaultValue = "false")
	@XMLAttribute
	public boolean getIsReadOnly();

	@Setter(IS_READ_ONLY_KEY)
	public void setIsReadOnly(boolean isReadOnly);

	/**
	 * Instantiate new action of required type<br>
	 * 
	 * @param actionClass
	 *            class of EditionAction to be instantiated
	 * @return
	 */
	public <A extends TechnologySpecificAction<?, ?>> A createAction(Class<A> actionClass);

	@Override
	public TechnologyAdapter getModelSlotTechnologyAdapter();

	public void setModelSlotTechnologyAdapter(TechnologyAdapter<?> technologyAdapter);

	@Override
	public Type getType();

	public List<Class<? extends FlexoRole<?>>> getAvailableFlexoRoleTypes();

	public List<Class<? extends EditionAction>> getAvailableEditionActionTypes();

	public List<Class<? extends AbstractFetchRequest<?, ?, ?, ?>>> getAvailableAbstractFetchRequestActionTypes();

	public List<Class<? extends FetchRequest<?, ?, ?>>> getAvailableFetchRequestActionTypes();

	public List<Class<? extends FlexoBehaviour>> getAvailableFlexoBehaviourTypes();

	/**
	 * Creates and return a new {@link FlexoRole} of supplied class.<br>
	 * This responsibility is delegated to the technology-specific {@link ModelSlot} which manages with introspection its own
	 * {@link FlexoRole} types
	 * 
	 * @param flexoRoleClass
	 * @return
	 */
	public abstract <PR extends FlexoRole<?>> PR makeFlexoRole(Class<PR> flexoRoleClass);

	/**
	 * Creates and return a new {@link EditionAction} of supplied class.<br>
	 * This responsibility is delegated to the technology-specific {@link ModelSlot} which manages with introspection its own
	 * {@link EditionAction} types
	 * 
	 * @param editionActionClass
	 * @return
	 */
	public abstract <EA extends TechnologySpecificAction<?, ?>> EA makeEditionAction(Class<EA> editionActionClass);

	/**
	 * Creates and return a new {@link AbstractFetchRequest} of supplied class.<br>
	 * This responsibility is delegated to the technology-specific {@link ModelSlot} which manages with introspection its own
	 * {@link AbstractFetchRequest} types
	 * 
	 * @param fetchRequestClass
	 * @return
	 */
	public abstract <FR extends AbstractFetchRequest<?, ?, ?, ?>> FR makeFetchRequest(Class<FR> fetchRequestClass);

	/**
	 * Return default name for supplied pattern property class
	 * 
	 * @param flexoRoleClass
	 * @return
	 */
	public <PR extends FlexoRole<?>> String defaultFlexoRoleName(Class<PR> flexoRoleClass);

	/**
	 * A Model Slot is responsible for URI mapping
	 * 
	 * @param resourceData
	 * @param o
	 * @return URI as String
	 */
	// TODO: deprecate ?
	public abstract String getURIForObject(RD resourceData, Object o);

	/**
	 * A Model Slot is responsible for URI mapping
	 * 
	 * @param resourceData
	 * @param objectURI
	 * @return the Object
	 */
	// TODO: deprecate ?
	public abstract Object retrieveObjectWithURI(RD resourceData, String objectURI);

	public String getModelSlotDescription();

	// Use with caution, this is not the name of model slot, but a displayable name
	@Deprecated
	public String getModelSlotName();

	@Override
	public ModelSlotInstance<?, RD> makeActorReference(RD object, FlexoConceptInstance epi);

	public static abstract class ModelSlotImpl<RD extends ResourceData<RD> & TechnologyObject<?>> extends FlexoRoleImpl<RD>
			implements ModelSlot<RD> {

		private static final Logger logger = Logger.getLogger(ModelSlot.class.getPackage().getName());

		private boolean isRequired;
		private boolean isReadOnly;
		private TechnologyAdapter<?> technologyAdapter;

		/*private List<Class<? extends FlexoRole<?>>> availableFlexoRoleTypes;
		private List<Class<? extends FlexoBehaviour>> availableFlexoBehaviourTypes;
		private List<Class<? extends TechnologySpecificAction<?, ?, ?>>> availableEditionActionTypes;
		private List<Class<? extends FetchRequest<?, ?, ?>>> availableFetchRequestActionTypes;
		private List<Class<? extends FlexoBehaviourParameter>> availableFlexoBehaviourParameterTypes;
		private List<Class<? extends InspectorEntry>> availableInspectorEntryTypes;*/

		@Override
		public ModelSlot<RD> getModelSlot() {
			return this;
		}

		/**
		 * Creates and return a new {@link FlexoRole} of supplied class.<br>
		 * This responsibility is delegated to the technology-specific {@link ModelSlot} which manages with introspection its own
		 * {@link FlexoRole} types
		 * 
		 * @param flexoRoleClass
		 * @return
		 */
		@Override
		public <PR extends FlexoRole<?>> PR makeFlexoRole(Class<PR> flexoRoleClass) {
			FMLModelFactory factory = getFMLModelFactory();
			return factory.newInstance(flexoRoleClass);
		}

		@Override
		public abstract Type getType();

		/**
		 * Instantiate new action of required type<br>
		 * Default implementation. Override when required.
		 * 
		 * @param actionClass
		 * @return
		 */
		@Override
		public <A extends TechnologySpecificAction<?, ?>> A createAction(Class<A> actionClass) {
			Class<?>[] constructorParams = new Class[0];
			// constructorParams[0] = VirtualModel.VirtualModelBuilder.class;
			try {
				Constructor<A> c = actionClass.getConstructor(constructorParams);
				return c.newInstance();
			} catch (SecurityException e) {
				logger.warning("Unexpected SecurityException " + e);
				e.printStackTrace();
				return null;
			} catch (NoSuchMethodException e) {
				logger.warning("Unexpected NoSuchMethodException " + e);
				e.printStackTrace();
				return null;
			} catch (IllegalArgumentException e) {
				logger.warning("Unexpected IllegalArgumentException " + e);
				e.printStackTrace();
				return null;
			} catch (InstantiationException e) {
				logger.warning("Unexpected InstantiationException " + e);
				e.printStackTrace();
				return null;
			} catch (IllegalAccessException e) {
				logger.warning("Unexpected IllegalAccessException " + e);
				e.printStackTrace();
				return null;
			} catch (InvocationTargetException e) {
				logger.warning("Unexpected InvocationTargetException " + e);
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public boolean getIsReadOnly() {
			return isReadOnly;
		}

		@Override
		public void setIsReadOnly(boolean isReadOnly) {
			this.isReadOnly = isReadOnly;
		}

		@Override
		public boolean getIsRequired() {
			return isRequired;
		}

		@Override
		public void setIsRequired(boolean isRequired) {
			this.isRequired = isRequired;
		}

		/*@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("ModelSlot " + getName() + " as "
					+ (getModelSlotTechnologyAdapter() != null ? getModelSlotTechnologyAdapter().getIdentifier() : "???") + "::"
					+ getImplementedInterface().getSimpleName() + " " + getFMLRepresentationForConformToStatement() + "required="
					+ getIsRequired() + " readOnly=" + getIsReadOnly() + ";", context);
			return out.toString();
		}*/

		@Deprecated
		protected String getFMLRepresentationForConformToStatement() {
			return "";
		}

		@Override
		public TechnologyAdapter getModelSlotTechnologyAdapter() {
			// Try to dynamically retrieve TechnologyAdapter if ServiceManager is accessible from here
			if (technologyAdapter == null && getServiceManager() != null && getServiceManager().getTechnologyAdapterService() != null) {
				technologyAdapter = getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(getTechnologyAdapterClass());
			}
			return technologyAdapter;
		}

		@Override
		public void setModelSlotTechnologyAdapter(TechnologyAdapter<?> technologyAdapter) {
			this.technologyAdapter = technologyAdapter;
		}

		public abstract Class<? extends TechnologyAdapter> getTechnologyAdapterClass();

		@Override
		public List<Class<? extends FlexoRole<?>>> getAvailableFlexoRoleTypes() {
			if (getTechnologyAdapterService() != null) {
				return getTechnologyAdapterService().getAvailableFlexoRoleTypes(getClass());
			}
			return Collections.emptyList();
		}

		@Override
		public List<Class<? extends EditionAction>> getAvailableEditionActionTypes() {
			if (getTechnologyAdapterService() != null) {
				return getTechnologyAdapterService().getAvailableEditionActionTypes(getClass());
			}
			return Collections.emptyList();
		}

		@Override
		public List<Class<? extends FlexoBehaviour>> getAvailableFlexoBehaviourTypes() {
			if (getTechnologyAdapterService() != null) {
				return getTechnologyAdapterService().getAvailableFlexoBehaviourTypes(getClass());
			}
			return Collections.emptyList();
		}

		@Override
		public List<Class<? extends AbstractFetchRequest<?, ?, ?, ?>>> getAvailableAbstractFetchRequestActionTypes() {
			if (getTechnologyAdapterService() != null) {
				return getTechnologyAdapterService().getAvailableAbstractFetchRequestActionTypes(getClass());
			}
			return Collections.emptyList();
		}

		@Override
		public List<Class<? extends FetchRequest<?, ?, ?>>> getAvailableFetchRequestActionTypes() {
			if (getTechnologyAdapterService() != null) {
				return getTechnologyAdapterService().getAvailableFetchRequestActionTypes(getClass());
			}
			return Collections.emptyList();
		}

		/**
		 * Creates and return a new {@link EditionAction} of supplied class.<br>
		 * This responsibility is delegated to the technology-specific {@link ModelSlot} which manages with introspection its own
		 * {@link EditionAction} types
		 * 
		 * @param editionActionClass
		 * @return
		 */
		@Override
		public final <EA extends TechnologySpecificAction<?, ?>> EA makeEditionAction(Class<EA> editionActionClass) {
			FMLModelFactory factory = getFMLModelFactory();
			return factory.newInstance(editionActionClass);
		}

		/**
		 * Creates and return a new {@link AbstractFetchRequest} of supplied class.<br>
		 * This responsibility is delegated to the technology-specific {@link ModelSlot} which manages with introspection its own
		 * {@link AbstractFetchRequest} types
		 * 
		 * @param fetchRequestClass
		 * @return
		 */
		@Override
		public final <FR extends AbstractFetchRequest<?, ?, ?, ?>> FR makeFetchRequest(Class<FR> fetchRequestClass) {
			FMLModelFactory factory = getFMLModelFactory();
			return factory.newInstance(fetchRequestClass);
		}

		/*@Override
		public abstract ModelSlotInstanceConfiguration<? extends ModelSlot<RD>, RD> createConfiguration(
				FlexoConceptInstance flexoConceptInstance, FlexoResourceCenter<?> rc);*/

		/**
		 * A Model Slot is responsible for URI mapping
		 * 
		 * @param resourceData
		 * @param o
		 * @return URI as String
		 */
		// TODO: deprecate ?
		@Override
		public abstract String getURIForObject(RD resourceData, Object o);

		/**
		 * A Model Slot is responsible for URI mapping
		 * 
		 * @param resourceData
		 * @param objectURI
		 * @return the Object
		 */
		// TODO: deprecate ?
		@Override
		public abstract Object retrieveObjectWithURI(RD resourceData, String objectURI);

		/**
		 * Return first found class matching supplied class.<br>
		 * Returned class is generally the specialized class related to a particular technology
		 * 
		 * @param flexoRoleClass
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public <PR extends FlexoRole<?>> Class<? extends PR> getFlexoRoleClass(Class<PR> flexoRoleClass) {
			for (Class<?> flexoRoleType : getAvailableFlexoRoleTypes()) {
				if (flexoRoleClass.isAssignableFrom(flexoRoleType)) {
					return (Class<? extends PR>) flexoRoleType;
				}
			}
			return null;
		}

		/**
		 * Return first found class matching supplied class.<br>
		 * Returned class is generally the specialized class related to a particular technology
		 * 
		 * @param flexoRoleClass
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public <EA extends EditionAction> Class<? extends EA> getEditionActionClass(Class<EA> editionActionClass) {
			for (Class<? extends EditionAction> editionActionType : getAvailableEditionActionTypes()) {
				if (editionActionClass.isAssignableFrom(editionActionType)) {
					return (Class<? extends EA>) editionActionType;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + ":" + getName();
		}

		@Override
		public String getModelSlotDescription() {
			return getModelSlotTechnologyAdapter().getName();
		}

		@Override
		public String getModelSlotName() {
			if (getFMLModelFactory() != null) {
				if (getFMLModelFactory().getModelEntityForInstance(this) != null) {
					return getFMLModelFactory().getModelEntityForInstance(this).getImplementedInterface().getSimpleName();
				}
			}
			return getClass().getSimpleName();
		}

		/**
		 * Encodes the default cloning strategy
		 * 
		 * @return
		 */
		@Override
		public RoleCloningStrategy defaultCloningStrategy() {
			return RoleCloningStrategy.Reference;
		}

		@Override
		public boolean defaultBehaviourIsToBeDeleted() {
			return false;
		}

		@Override
		public boolean isReadOnly() {
			return false;
		}

		private VirtualModel getVirtualModel() {
			if (getFlexoConcept() instanceof VirtualModel) {
				return (VirtualModel) getFlexoConcept();
			}
			if (getFlexoConcept() != null) {
				return getFlexoConcept().getOwner();
			}
			return null;
		}

		@Override
		public boolean delete(Object... context) {
			/*FMLControlGraphVisitor cgVisitor = controlGraph -> {
				if (controlGraph instanceof TechnologySpecificAction
						&& ((TechnologySpecificAction<?, ?>) controlGraph).getInferedModelSlot() == ModelSlotImpl.this) {
					// Unused TechnologySpecificAction<?, ?, ?> action = (TechnologySpecificAction<?, ?, ?>) controlGraph;
					// nullify model slot for action
					// action.setModelSlot(null);
				}
			};*/

			VirtualModel virtualModel = getVirtualModel();
			if (virtualModel != null) {
				for (FlexoRole<?> role : virtualModel.getAccessibleRoles()) {
					if (role.getModelSlot() == this) {
						// nullify model slot for role
						role.setModelSlot(null);
					}
				}

				/*
				// Also iterate on all behaviours, and find EditionAction that are declared with this model slot
				for (FlexoBehaviour behaviour : virtualModel.getFlexoBehaviours()) {
					if (behaviour.getControlGraph() != null) {
						behaviour.getControlGraph().accept(cgVisitor);
					}
				}
				// Also iterate on all behaviours of all inner FlexoConcept, and find EditionAction that are declared with this model slot
				for (FlexoConcept concept : virtualModel.getFlexoConcepts()) {
					for (FlexoBehaviour behaviour : concept.getFlexoBehaviours()) {
						if (behaviour.getControlGraph() != null) {
							behaviour.getControlGraph().accept(cgVisitor);
						}
					}
				}
				// Also iterate on GetProperty
				for (GetProperty<?> property : virtualModel.getAccessibleProperties(GetProperty.class)) {
					if (property.getGetControlGraph() != null) {
						property.getGetControlGraph().accept(cgVisitor);
					}
					if (property instanceof GetSetProperty) {
						if (((GetSetProperty<?>) property).getSetControlGraph() != null) {
							((GetSetProperty<?>) property).getSetControlGraph().accept(cgVisitor);
						}
					}
				}*/
			}

			return super.delete(context);
		}

		@Override
		public Class<? extends TechnologyAdapter> getRoleTechnologyAdapterClass() {
			return getModelSlotTechnologyAdapter().getClass();
		}

		@Override
		public void finalizeDeserialization() {
			if (getVirtualModel() != null && getFMLModelFactory() != null) {
				Class<? extends ModelSlot<?>> modelSlotClass = getFMLModelFactory().getModelEntityForInstance(this)
						.getImplementedInterface();
				if (!getVirtualModel().uses(modelSlotClass)) {
					getVirtualModel().declareUse(modelSlotClass);
				}
			}
			super.finalizeDeserialization();
		}
	}

}
