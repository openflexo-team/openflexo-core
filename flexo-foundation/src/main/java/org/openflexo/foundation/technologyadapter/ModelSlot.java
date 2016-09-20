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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.GetProperty;
import org.openflexo.foundation.fml.GetSetProperty;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelObject;
import org.openflexo.foundation.fml.annotations.DeclareEditionActions;
import org.openflexo.foundation.fml.annotations.DeclareFetchRequests;
import org.openflexo.foundation.fml.annotations.DeclareFlexoBehaviourParameters;
import org.openflexo.foundation.fml.annotations.DeclareFlexoBehaviours;
import org.openflexo.foundation.fml.annotations.DeclareFlexoRoles;
import org.openflexo.foundation.fml.annotations.DeclareInspectorEntries;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphVisitor;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequest;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
import org.openflexo.foundation.fml.rt.AbstractVirtualModelInstance;
import org.openflexo.foundation.fml.rt.ActorReference;
import org.openflexo.foundation.fml.rt.FMLRTModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.ModelSlotInstance;
import org.openflexo.foundation.fml.rt.action.ModelSlotInstanceConfiguration;
import org.openflexo.foundation.resource.FlexoResourceCenter;
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
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(ModelSlot.ModelSlotImpl.class)
@Imports({ @Import(FMLRTModelSlot.class), @Import(TypeAwareModelSlot.class), @Import(FreeModelSlot.class) })
public interface ModelSlot<RD extends ResourceData<RD> & TechnologyObject<?>>
		extends FlexoRole<RD>, ModelSlotObject<RD>, VirtualModelObject {

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

	@Override
	public TechnologyAdapter getModelSlotTechnologyAdapter();

	public void setModelSlotTechnologyAdapter(TechnologyAdapter technologyAdapter);

	@Override
	public Type getType();

	public List<Class<? extends FlexoRole<?>>> getAvailableFlexoRoleTypes();

	public <R extends FlexoRole<?>> List<Class<? extends R>> getAvailableFlexoRoleTypes(Class<R> roleType);

	public List<Class<? extends TechnologySpecificAction<?, ?>>> getAvailableEditionActionTypes();

	public List<Class<? extends FetchRequest<?, ?>>> getAvailableFetchRequestActionTypes();

	public List<Class<? extends FlexoBehaviour>> getAvailableFlexoBehaviourTypes();

	public List<Class<? extends FlexoBehaviourParameter>> getAvailableFlexoBehaviourParameterTypes();

	public List<Class<? extends InspectorEntry>> getAvailableInspectorEntryTypes();

	/**
	 * Creates and return a new {@link FlexoRole} of supplied class.<br>
	 * This responsability is delegated to the technology-specific {@link ModelSlot} which manages with introspection its own
	 * {@link FlexoRole} types
	 * 
	 * @param flexoRoleClass
	 * @return
	 */
	public abstract <PR extends FlexoRole<?>> PR makeFlexoRole(Class<PR> flexoRoleClass);

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
	 * Return default name for supplied pattern property class
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

	public abstract ModelSlotInstanceConfiguration<? extends ModelSlot<RD>, RD> createConfiguration(
			AbstractVirtualModelInstance<?, ?> virtualModelInstance, FlexoResourceCenter<?> rc);

	public String getModelSlotDescription();

	public String getModelSlotName();

	public static abstract class ModelSlotImpl<RD extends ResourceData<RD> & TechnologyObject<?>> extends FlexoRoleImpl<RD>
			implements ModelSlot<RD> {

		private static final Logger logger = Logger.getLogger(ModelSlot.class.getPackage().getName());

		private boolean isRequired;
		private boolean isReadOnly;
		private TechnologyAdapter technologyAdapter;

		private List<Class<? extends FlexoRole<?>>> availableFlexoRoleTypes;
		private List<Class<? extends FlexoBehaviour>> availableFlexoBehaviourTypes;
		private List<Class<? extends TechnologySpecificAction<?, ?>>> availableEditionActionTypes;
		private List<Class<? extends FetchRequest<?, ?>>> availableFetchRequestActionTypes;
		private List<Class<? extends FlexoBehaviourParameter>> availableFlexoBehaviourParameterTypes;
		private List<Class<? extends InspectorEntry>> availableInspectorEntryTypes;

		@Override
		public AbstractVirtualModel<?> getVirtualModel() {
			return getOwner();
		}

		@Override
		public final FlexoConcept getFlexoConcept() {
			return getOwner();
		}

		@Override
		public ModelSlot<RD> getModelSlot() {
			return this;
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
		public <PR extends FlexoRole<?>> PR makeFlexoRole(Class<PR> flexoRoleClass) {
			FMLModelFactory factory = getFMLModelFactory();
			return factory.newInstance(flexoRoleClass);
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

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("ModelSlot " + getName() + " as " + getModelSlotTechnologyAdapter().getIdentifier() + "::"
					+ getImplementedInterface().getSimpleName() + " " + getFMLRepresentationForConformToStatement() + "required="
					+ getIsRequired() + " readOnly=" + getIsReadOnly() + ";", context);
			return out.toString();
		}

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
		public void setModelSlotTechnologyAdapter(TechnologyAdapter technologyAdapter) {
			this.technologyAdapter = technologyAdapter;
		}

		public abstract Class<? extends TechnologyAdapter> getTechnologyAdapterClass();

		@Override
		public <R extends FlexoRole<?>> List<Class<? extends R>> getAvailableFlexoRoleTypes(Class<R> roleType) {
			List<Class<? extends R>> returned = new ArrayList<Class<? extends R>>();
			for (Class<? extends FlexoRole<?>> roleClass : getAvailableFlexoRoleTypes()) {
				if (roleType.isAssignableFrom(roleClass)) {
					returned.add((Class<? extends R>) roleClass);
				}
			}
			return returned;
		}

		@Override
		public List<Class<? extends FlexoRole<?>>> getAvailableFlexoRoleTypes() {
			if (availableFlexoRoleTypes == null) {
				availableFlexoRoleTypes = computeAvailableFlexoRoleTypes();
			}
			return availableFlexoRoleTypes;
		}

		private List<Class<? extends FlexoRole<?>>> computeAvailableFlexoRoleTypes() {
			availableFlexoRoleTypes = new ArrayList<Class<? extends FlexoRole<?>>>();
			appendDeclareFlexoRoles(availableFlexoRoleTypes, getClass());
			return availableFlexoRoleTypes;

			/*Class<?> cl = getClass();
			if (cl.isAnnotationPresent(DeclareFlexoRoles.class)) {
				DeclareFlexoRoles allPatternRoles = cl.getAnnotation(DeclareFlexoRoles.class);
				for (DeclareFlexoRole patternRoleDeclaration : allPatternRoles.value()) {
					availableFlexoRoleTypes.add(patternRoleDeclaration.flexoRoleClass());
				}
			}
			// availableFlexoRoleTypes.add(FlexoConceptPatternRole.class);
			// availableFlexoRoleTypes.add(FlexoModelObjectPatternRole.class);
			// availableFlexoRoleTypes.add(PrimitiveRole.class);
			return availableFlexoRoleTypes;*/
		}

		private void appendDeclareFlexoRoles(List<Class<? extends FlexoRole<?>>> aList, Class<?> cl) {
			if (cl.isAnnotationPresent(DeclareFlexoRoles.class)) {
				DeclareFlexoRoles allFlexoRoles = cl.getAnnotation(DeclareFlexoRoles.class);
				for (Class<? extends FlexoRole> roleClass : allFlexoRoles.value()) {
					if (!availableFlexoRoleTypes.contains(roleClass)) {
						availableFlexoRoleTypes.add((Class<FlexoRole<?>>) roleClass);
					}
				}
			}
			if (cl.getSuperclass() != null) {
				appendDeclareFlexoRoles(aList, cl.getSuperclass());
			}

			for (Class superInterface : cl.getInterfaces()) {
				appendDeclareFlexoRoles(aList, superInterface);
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
				for (Class<? extends TechnologySpecificAction> editionActionClass : allEditionActions.value()) {
					if (!availableEditionActionTypes.contains(editionActionClass)) {
						availableEditionActionTypes.add((Class<? extends TechnologySpecificAction<?, ?>>) editionActionClass);
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
				for (Class<? extends FlexoBehaviour> flexoBehaviourClass : allFlexoBehaviours.value()) {
					if (!availableFlexoBehaviourTypes.contains(flexoBehaviourClass)) {
						availableFlexoBehaviourTypes.add(flexoBehaviourClass);
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
				for (Class<? extends FetchRequest<?, ?>> fetchRequestClass : allFetchRequestActions.value()) {
					if (!availableFetchRequestActionTypes.contains(fetchRequestClass)) {
						availableFetchRequestActionTypes.add(fetchRequestClass);
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

		@Override
		public List<Class<? extends FlexoBehaviourParameter>> getAvailableFlexoBehaviourParameterTypes() {
			if (availableFlexoBehaviourParameterTypes == null) {
				availableFlexoBehaviourParameterTypes = computeAvailableFlexoBehaviourParameterTypes();
			}
			return availableFlexoBehaviourParameterTypes;
		}

		private List<Class<? extends FlexoBehaviourParameter>> computeAvailableFlexoBehaviourParameterTypes() {
			availableFlexoBehaviourParameterTypes = new ArrayList<Class<? extends FlexoBehaviourParameter>>();
			appendFlexoBehaviourParameterTypes(availableFlexoBehaviourParameterTypes, getClass());
			return availableFlexoBehaviourParameterTypes;
		}

		private void appendFlexoBehaviourParameterTypes(List<Class<? extends FlexoBehaviourParameter>> aList, Class<?> cl) {
			if (cl.isAnnotationPresent(DeclareFlexoBehaviourParameters.class)) {
				DeclareFlexoBehaviourParameters allFlexoBehaviourParameterTypes = cl.getAnnotation(DeclareFlexoBehaviourParameters.class);
				for (Class<? extends FlexoBehaviourParameter> parameterClass : allFlexoBehaviourParameterTypes.value()) {
					if (!availableFlexoBehaviourParameterTypes.contains(parameterClass)) {
						availableFlexoBehaviourParameterTypes.add(parameterClass);
					}
				}
			}
			if (cl.getSuperclass() != null) {
				appendFlexoBehaviourParameterTypes(aList, cl.getSuperclass());
			}
			for (Class superInterface : cl.getInterfaces()) {
				appendFlexoBehaviourParameterTypes(aList, superInterface);
			}
		}

		@Override
		public List<Class<? extends InspectorEntry>> getAvailableInspectorEntryTypes() {
			if (availableInspectorEntryTypes == null) {
				availableInspectorEntryTypes = computeAvailableInspectorEntryTypes();
			}
			return availableInspectorEntryTypes;
		}

		private List<Class<? extends InspectorEntry>> computeAvailableInspectorEntryTypes() {
			availableInspectorEntryTypes = new ArrayList<Class<? extends InspectorEntry>>();
			appendInspectorEntryTypes(availableInspectorEntryTypes, getClass());
			return availableInspectorEntryTypes;
		}

		private void appendInspectorEntryTypes(List<Class<? extends InspectorEntry>> aList, Class<?> cl) {
			if (cl.isAnnotationPresent(DeclareInspectorEntries.class)) {
				DeclareInspectorEntries allInspectorEntries = cl.getAnnotation(DeclareInspectorEntries.class);
				for (Class<? extends InspectorEntry> inspectorEntryClass : allInspectorEntries.value()) {
					if (!availableInspectorEntryTypes.contains(inspectorEntryClass)) {
						availableInspectorEntryTypes.add(inspectorEntryClass);
					}
				}
			}
			if (cl.getSuperclass() != null) {
				appendInspectorEntryTypes(aList, cl.getSuperclass());
			}
			for (Class superInterface : cl.getInterfaces()) {
				appendInspectorEntryTypes(aList, superInterface);
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
		public abstract ModelSlotInstanceConfiguration<? extends ModelSlot<RD>, RD> createConfiguration(
				AbstractVirtualModelInstance<?, ?> virtualModelInstance, FlexoResourceCenter<?> rc);

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

		@Override
		public boolean isReadOnly() {
			return false;
		}

		@Override
		public boolean delete(Object... context) {
			for (FlexoRole<?> role : getVirtualModel().getAccessibleRoles()) {
				if (role.getModelSlot() == this) {
					// nullify model slot for role
					role.setModelSlot(null);
				}
			}

			FMLControlGraphVisitor cgVisitor = new FMLControlGraphVisitor() {
				@Override
				public void visit(FMLControlGraph controlGraph) {
					if (controlGraph instanceof TechnologySpecificAction
							&& ((TechnologySpecificAction<?, ?>) controlGraph).getModelSlot() == ModelSlotImpl.this) {
						TechnologySpecificAction action = (TechnologySpecificAction<?, ?>) controlGraph;
						// nullify model slot for action
						action.setModelSlot(null);
					}
				}
			};

			// Also iterate on all behaviours, and find EditionAction that are declared with this model slot
			for (FlexoBehaviour behaviour : getVirtualModel().getFlexoBehaviours()) {
				if (behaviour.getControlGraph() != null) {
					behaviour.getControlGraph().accept(cgVisitor);
				}
			}
			// Also iterate on all behaviours of all inner FlexoConcept, and find EditionAction that are declared with this model slot
			for (FlexoConcept concept : getVirtualModel().getFlexoConcepts()) {
				for (FlexoBehaviour behaviour : concept.getFlexoBehaviours()) {
					if (behaviour.getControlGraph() != null) {
						behaviour.getControlGraph().accept(cgVisitor);
					}
				}
			}
			// Also iterate on GetProperty
			for (GetProperty<?> property : getVirtualModel().getAccessibleProperties(GetProperty.class)) {
				if (property.getGetControlGraph() != null) {
					property.getGetControlGraph().accept(cgVisitor);
				}
				if (property instanceof GetSetProperty) {
					if (((GetSetProperty<?>) property).getSetControlGraph() != null) {
						((GetSetProperty<?>) property).getSetControlGraph().accept(cgVisitor);
					}
				}
			}

			return super.delete(context);
		}
	}

}
