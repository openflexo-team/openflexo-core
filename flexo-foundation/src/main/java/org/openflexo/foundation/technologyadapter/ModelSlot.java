/*
 * (c) Copyright 2010-2012 AgileBirds
 * (c) Copyright 2013 Openflexo
 *
 * This file is part of Openflexo.
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

package org.openflexo.foundation.technologyadapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelObject;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.action.CreateVirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.ModelSlotInstanceConfiguration;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;

/**
 * A model slot is a named object providing access to a particular data encoded in a given technology A model slot should be seen as a
 * connector.<br>
 * A model slot formalizes a contract for accessing to a data
 * 
 * It is defined at viewpoint level. <br>
 * A {@link ModelSlotInstance} binds used slots to some data within the project.
 * 
 * @param <RD>
 *            Type of resource data handled by this ModelSlot
 * 
 * @author Sylvain Guerin
 * @see org.openflexo.foundation.fml.ViewPoint
 * @see org.openflexo.foundation.fml.rt.View
 * @see org.openflexo.foundation.fml.rt.ModelSlotInstance
 * */
@ModelEntity(isAbstract = true)
@ImplementationClass(ModelSlot.ModelSlotImpl.class)
@Imports({ @Import(FMLRTModelSlot.class), @Import(TypeAwareModelSlot.class), @Import(FreeModelSlot.class) })
public interface ModelSlot<RD extends ResourceData<RD> & TechnologyObject<?>> extends FlexoRole<RD>, VirtualModelObject {

	@PropertyIdentifier(type = AbstractVirtualModel.class)
	public static final String OWNER_KEY = "owner";

	@PropertyIdentifier(type = boolean.class)
	public static final String IS_REQUIRED_KEY = "isRequired";
	@PropertyIdentifier(type = boolean.class)
	public static final String IS_READ_ONLY_KEY = "isReadOnly";

	@Override
	public FMLModelFactory getFMLModelFactory();

	/**
	 * Return the VirtualModel in which this ModelSlot is declared
	 */
	@Getter(value = OWNER_KEY, inverse = VirtualModel.MODEL_SLOTS_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public AbstractVirtualModel<?> getOwner();

	@Setter(OWNER_KEY)
	public void setOwner(AbstractVirtualModel<?> virtualModel);

	@Getter(value = IS_REQUIRED_KEY, defaultValue = "false")
	@XMLAttribute
	public boolean getIsRequired();

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

	public TechnologyAdapter getModelSlotTechnologyAdapter();

	public void setModelSlotTechnologyAdapter(TechnologyAdapter technologyAdapter);

	@Override
	public Type getType();

	public List<Class<? extends FlexoRole<?>>> getAvailableFlexoRoleTypes();

	public List<Class<? extends TechnologySpecificAction<?, ?>>> getAvailableEditionActionTypes();

	public List<Class<? extends FetchRequest<?, ?>>> getAvailableFetchRequestActionTypes();

	public List<Class<? extends FlexoBehaviour>> getAvailableFlexoBehaviourTypes();

	/**
	 * Creates and return a new {@link FlexoRole} of supplied class.<br>
	 * This responsability is delegated to the technology-specific {@link ModelSlot} which manages with introspection its own
	 * {@link FlexoRole} types
	 * 
	 * @param flexoRoleClass
	 * @return
	 */
	public abstract <PR extends FlexoRole<?>> PR makeFlexoRole(Class<PR> patternRoleClass);

	/**
	 * Creates and return a new {@link EditionAction} of supplied class.<br>
	 * This responsability is delegated to the technology-specific {@link ModelSlot} which manages with introspection its own
	 * {@link EditionAction} types
	 * 
	 * @param editionActionClass
	 * @return
	 */
	public abstract <EA extends TechnologySpecificAction<?, ?>> EA makeEditionAction(Class<EA> editionActionClass);

	/**
	 * Creates and return a new {@link FetchRequest} of supplied class.<br>
	 * This responsability is delegated to the technology-specific {@link ModelSlot} which manages with introspection its own
	 * {@link FetchRequest} types
	 * 
	 * @param fetchRequestClass
	 * @return
	 */
	public abstract <FR extends FetchRequest<?, ?>> FR makeFetchRequest(Class<FR> fetchRequestClass);

	/**
	 * Return default name for supplied pattern role class
	 * 
	 * @param flexoRoleClass
	 * @return
	 */
	public <PR extends FlexoRole<?>> String defaultFlexoRoleName(Class<PR> flexoRoleClass);

	/**
	 * A Model Slot is responsible for URI mapping
	 * 
	 * @param msInstance
	 * @param o
	 * @return URI as String
	 */

	public abstract String getURIForObject(ModelSlotInstance<? extends ModelSlot<RD>, RD> msInstance, Object o);

	/**
	 * A Model Slot is responsible for URI mapping
	 * 
	 * @param msInstance
	 * @param objectURI
	 * @return the Object
	 */

	public abstract Object retrieveObjectWithURI(ModelSlotInstance<? extends ModelSlot<RD>, RD> msInstance, String objectURI);

	public abstract ModelSlotInstanceConfiguration<? extends ModelSlot<RD>, RD> createConfiguration(CreateVirtualModelInstance action);

	public String getModelSlotDescription();

	public String getModelSlotName();

	public static abstract class ModelSlotImpl<RD extends ResourceData<RD> & TechnologyObject<?>> extends FlexoRoleImpl<RD> implements
			ModelSlot<RD> {

		private static final Logger logger = Logger.getLogger(ModelSlot.class.getPackage().getName());

		private boolean isRequired;
		private boolean isReadOnly;
		private TechnologyAdapter technologyAdapter;

		private List<Class<? extends FlexoRole<?>>> availableFlexoRoleTypes;
		private List<Class<? extends FlexoBehaviour>> availableFlexoBehaviourTypes;
		private List<Class<? extends TechnologySpecificAction<?, ?>>> availableEditionActionTypes;
		private List<Class<? extends FetchRequest<?, ?>>> availableFetchRequestActionTypes;

		@Override
		public AbstractVirtualModel<?> getVirtualModel() {
			return getOwner();
		}

		@Override
		public String getURI() {
			if (getVirtualModel() != null) {
				return getVirtualModel().getURI() + "." + getName();
			}
			return null;
		}

		/**
		 * Creates and return a new {@link FlexoRole} of supplied class.<br>
		 * This responsability is delegated to the technology-specific {@link ModelSlot} which manages with introspection its own
		 * {@link FlexoRole} types
		 * 
		 * @param flexoRoleClass
		 * @return
		 */
		@Override
		public final <PR extends FlexoRole<?>> PR makeFlexoRole(Class<PR> patternRoleClass) {
			FMLModelFactory factory = getFMLModelFactory();
			return factory.newInstance(patternRoleClass);
		}

		@Override
		public AbstractVirtualModel<?> getOwningVirtualModel() {
			return getVirtualModel();
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
			Class[] constructorParams = new Class[0];
			// constructorParams[0] = VirtualModel.VirtualModelBuilder.class;
			try {
				Constructor<A> c = actionClass.getConstructor(constructorParams);
				return c.newInstance(null);
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
				logger.warning("Unexpected InvocationTargetException " + e);
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
		public BindingModel getBindingModel() {
			return viewPoint.getBindingModel();
		}*/

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("ModelSlot " + getName() + " type=" + getClass().getSimpleName() + "\"" + " required=" + getIsRequired()
					+ " readOnly=" + getIsReadOnly() + ";", context);
			return out.toString();
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
		public void setModelSlotTechnologyAdapter(TechnologyAdapter technologyAdapter) {
			this.technologyAdapter = technologyAdapter;
		}

		public abstract Class<? extends TechnologyAdapter> getTechnologyAdapterClass();

		@Override
		public List<Class<? extends FlexoRole<?>>> getAvailableFlexoRoleTypes() {
			if (availableFlexoRoleTypes == null) {
				availableFlexoRoleTypes = computeAvailableFlexoRoleTypes();
			}
			return availableFlexoRoleTypes;
		}

		private List<Class<? extends FlexoRole<?>>> computeAvailableFlexoRoleTypes() {
			availableFlexoRoleTypes = new ArrayList<Class<? extends FlexoRole<?>>>();
			appendDeclarePatternRoles(availableFlexoRoleTypes, getClass());
			return availableFlexoRoleTypes;

			/*Class<?> cl = getClass();
			if (cl.isAnnotationPresent(DeclarePatternRoles.class)) {
				DeclarePatternRoles allPatternRoles = cl.getAnnotation(DeclarePatternRoles.class);
				for (DeclareFlexoRole patternRoleDeclaration : allPatternRoles.value()) {
					availableFlexoRoleTypes.add(patternRoleDeclaration.flexoRoleClass());
				}
			}
			// availableFlexoRoleTypes.add(FlexoConceptPatternRole.class);
			// availableFlexoRoleTypes.add(FlexoModelObjectPatternRole.class);
			// availableFlexoRoleTypes.add(PrimitiveRole.class);
			return availableFlexoRoleTypes;*/
		}

		private void appendDeclarePatternRoles(List<Class<? extends FlexoRole<?>>> aList, Class<?> cl) {
			if (cl.isAnnotationPresent(DeclarePatternRoles.class)) {
				DeclarePatternRoles allPatternRoles = cl.getAnnotation(DeclarePatternRoles.class);
				for (DeclarePatternRole patternRoleDeclaration : allPatternRoles.value()) {
					if (!availableFlexoRoleTypes.contains(patternRoleDeclaration.flexoRoleClass())) {
						availableFlexoRoleTypes.add((Class<FlexoRole<?>>) patternRoleDeclaration.flexoRoleClass());
					}
				}
			}
			if (cl.getSuperclass() != null) {
				appendDeclarePatternRoles(aList, cl.getSuperclass());
			}

			for (Class superInterface : cl.getInterfaces()) {
				appendDeclarePatternRoles(aList, superInterface);
			}

		}

		@Override
		public List<Class<? extends TechnologySpecificAction<?, ?>>> getAvailableEditionActionTypes() {
			if (availableEditionActionTypes == null) {
				availableEditionActionTypes = computeAvailableEditionActionTypes();
			}
			return availableEditionActionTypes;
		}

		private List<Class<? extends TechnologySpecificAction<?, ?>>> computeAvailableEditionActionTypes() {
			availableEditionActionTypes = new ArrayList<Class<? extends TechnologySpecificAction<?, ?>>>();
			appendEditionActionTypes(availableEditionActionTypes, getClass());
			return availableEditionActionTypes;
		}

		private void appendEditionActionTypes(List<Class<? extends TechnologySpecificAction<?, ?>>> aList, Class<?> cl) {
			if (cl.isAnnotationPresent(DeclareEditionActions.class)) {
				DeclareEditionActions allEditionActions = cl.getAnnotation(DeclareEditionActions.class);
				for (DeclareEditionAction editionActionDeclaration : allEditionActions.value()) {
					if (!availableEditionActionTypes.contains(editionActionDeclaration.editionActionClass())) {
						availableEditionActionTypes.add(editionActionDeclaration.editionActionClass());
					}
				}
			}
			if (cl.getSuperclass() != null) {
				appendEditionActionTypes(aList, cl.getSuperclass());
			}
			for (Class superInterface : cl.getInterfaces()) {
				appendEditionActionTypes(aList, superInterface);
			}
		}

		@Override
		public List<Class<? extends FlexoBehaviour>> getAvailableFlexoBehaviourTypes() {
			if (availableFlexoBehaviourTypes == null) {
				availableFlexoBehaviourTypes = computeAvailableFlexoBehaviourTypes();
			}
			return availableFlexoBehaviourTypes;
		}

		private List<Class<? extends FlexoBehaviour>> computeAvailableFlexoBehaviourTypes() {
			availableFlexoBehaviourTypes = new ArrayList<Class<? extends FlexoBehaviour>>();
			appendFlexoBehaviourTypes(availableFlexoBehaviourTypes, getClass());
			return availableFlexoBehaviourTypes;
		}

		private void appendFlexoBehaviourTypes(List<Class<? extends FlexoBehaviour>> aList, Class<?> cl) {
			if (cl.isAnnotationPresent(DeclareFlexoBehaviours.class)) {
				DeclareFlexoBehaviours allFlexoBehaviours = cl.getAnnotation(DeclareFlexoBehaviours.class);
				for (DeclareFlexoBehaviour flexoBehaviourDeclaration : allFlexoBehaviours.value()) {
					if (!availableFlexoBehaviourTypes.contains(flexoBehaviourDeclaration.flexoBehaviourClass())) {
						availableFlexoBehaviourTypes.add(flexoBehaviourDeclaration.flexoBehaviourClass());
					}
				}
			}
			if (cl.getSuperclass() != null) {
				appendFlexoBehaviourTypes(aList, cl.getSuperclass());
			}
			for (Class superInterface : cl.getInterfaces()) {
				appendFlexoBehaviourTypes(aList, superInterface);
			}
		}

		@Override
		public List<Class<? extends FetchRequest<?, ?>>> getAvailableFetchRequestActionTypes() {
			if (availableFetchRequestActionTypes == null) {
				availableFetchRequestActionTypes = computeAvailableFetchRequestActionTypes();
			}
			return availableFetchRequestActionTypes;
		}

		private List<Class<? extends FetchRequest<?, ?>>> computeAvailableFetchRequestActionTypes() {
			availableFetchRequestActionTypes = new ArrayList<Class<? extends FetchRequest<?, ?>>>();
			appendFetchRequestActionTypes(availableFetchRequestActionTypes, getClass());
			return availableFetchRequestActionTypes;
		}

		private void appendFetchRequestActionTypes(List<Class<? extends FetchRequest<?, ?>>> aList, Class<?> cl) {
			if (cl.isAnnotationPresent(DeclareFetchRequests.class)) {
				DeclareFetchRequests allFetchRequestActions = cl.getAnnotation(DeclareFetchRequests.class);
				for (DeclareFetchRequest fetchRequestDeclaration : allFetchRequestActions.value()) {
					if (!availableFetchRequestActionTypes.contains(fetchRequestDeclaration.fetchRequestClass())) {
						availableFetchRequestActionTypes.add(fetchRequestDeclaration.fetchRequestClass());
					}
				}
			}
			if (cl.getSuperclass() != null) {
				appendFetchRequestActionTypes(aList, cl.getSuperclass());
			}
			for (Class superInterface : cl.getInterfaces()) {
				appendFetchRequestActionTypes(aList, superInterface);
			}
		}

		/**
		 * Creates and return a new {@link EditionAction} of supplied class.<br>
		 * This responsability is delegated to the technology-specific {@link ModelSlot} which manages with introspection its own
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
		 * Creates and return a new {@link FetchRequest} of supplied class.<br>
		 * This responsability is delegated to the technology-specific {@link ModelSlot} which manages with introspection its own
		 * {@link FetchRequest} types
		 * 
		 * @param fetchRequestClass
		 * @return
		 */
		@Override
		public final <FR extends FetchRequest<?, ?>> FR makeFetchRequest(Class<FR> fetchRequestClass) {
			FMLModelFactory factory = getFMLModelFactory();
			return factory.newInstance(fetchRequestClass);
		}

		@Override
		public abstract ModelSlotInstanceConfiguration<? extends ModelSlot<RD>, RD> createConfiguration(CreateVirtualModelInstance action);

		/**
		 * A Model Slot is responsible for URI mapping
		 * 
		 * @param msInstance
		 * @param o
		 * @return URI as String
		 */

		@Override
		public abstract String getURIForObject(ModelSlotInstance<? extends ModelSlot<RD>, RD> msInstance, Object o);

		/**
		 * A Model Slot is responsible for URI mapping
		 * 
		 * @param msInstance
		 * @param objectURI
		 * @return the Object
		 */

		@Override
		public abstract Object retrieveObjectWithURI(ModelSlotInstance<? extends ModelSlot<RD>, RD> msInstance, String objectURI);

		/**
		 * Return first found class matching supplied class.<br>
		 * Returned class is generally the specialized class related to a particular technology
		 * 
		 * @param flexoRoleClass
		 * @return
		 */
		public <PR extends FlexoRole<?>> Class<? extends PR> getFlexoRoleClass(Class<PR> patternRoleClass) {
			for (Class<?> patternRoleType : getAvailableFlexoRoleTypes()) {
				if (patternRoleClass.isAssignableFrom(patternRoleType)) {
					return (Class<? extends PR>) patternRoleType;
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
		public <EA extends EditionAction> Class<? extends EA> getEditionActionClass(Class<EA> editionActionClass) {
			for (Class editionActionType : getAvailableEditionActionTypes()) {
				if (editionActionClass.isAssignableFrom(editionActionType)) {
					return editionActionType;
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
		public ActorReference<RD> makeActorReference(RD object, FlexoConceptInstance epi) {
			return null;
		}
	}

}
