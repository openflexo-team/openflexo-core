/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller;

import java.lang.reflect.Type;

import javax.swing.ImageIcon;

import org.openflexo.components.widget.FIBTechnologyBrowser;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.fml.controller.action.AddParentFlexoConceptInitializer;
import org.openflexo.fml.controller.action.AddReturnStatementActionInitializer;
import org.openflexo.fml.controller.action.AddToListActionInitializer;
import org.openflexo.fml.controller.action.AddUseDeclarationInitializer;
import org.openflexo.fml.controller.action.AssignActionInitializer;
import org.openflexo.fml.controller.action.CreateAbstractPropertyInitializer;
import org.openflexo.fml.controller.action.CreateContainedVirtualModelInitializer;
import org.openflexo.fml.controller.action.CreateEditionActionInitializer;
import org.openflexo.fml.controller.action.CreateExpressionPropertyInitializer;
import org.openflexo.fml.controller.action.CreateFlexoBehaviourInitializer;
import org.openflexo.fml.controller.action.CreateFlexoConceptInitializer;
import org.openflexo.fml.controller.action.CreateFlexoConceptInstanceRoleInitializer;
import org.openflexo.fml.controller.action.CreateFlexoEventInitializer;
import org.openflexo.fml.controller.action.CreateGenericBehaviourParameterInitializer;
import org.openflexo.fml.controller.action.CreateGetSetPropertyInitializer;
import org.openflexo.fml.controller.action.CreateIndividualRoleInitializer;
import org.openflexo.fml.controller.action.CreateInspectorEntryInitializer;
import org.openflexo.fml.controller.action.CreateModelSlotInitializer;
import org.openflexo.fml.controller.action.CreatePrimitiveRoleInitializer;
import org.openflexo.fml.controller.action.CreateTechnologyRoleInitializer;
import org.openflexo.fml.controller.action.CreateTopLevelVirtualModelInitializer;
import org.openflexo.fml.controller.action.DeclareNewVariableActionInitializer;
import org.openflexo.fml.controller.action.DeleteFlexoConceptObjectsInitializer;
import org.openflexo.fml.controller.action.DeleteVirtualModelInitializer;
import org.openflexo.fml.controller.action.ShowFMLRepresentationInitializer;
import org.openflexo.fml.controller.view.StandardFlexoConceptView;
import org.openflexo.fml.controller.view.FMLLocalizedDictionaryView;
import org.openflexo.fml.controller.view.VirtualModelView;
import org.openflexo.fml.controller.widget.FIBViewPointLibraryBrowser;
import org.openflexo.fml.controller.widget.FlexoConceptInstanceTypeEditor;
import org.openflexo.fml.controller.widget.FlexoResourceTypeEditor;
import org.openflexo.fml.controller.widget.VirtualModelInstanceTypeEditor;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CloningScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.EventListener;
import org.openflexo.foundation.fml.FMLLocalizedDictionary;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoBehaviourParameter.WidgetType;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoEvent;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.NavigationScheme;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.WidgetContext;
import org.openflexo.foundation.fml.action.DeleteFlexoConceptObjects;
import org.openflexo.foundation.fml.binding.FMLBindingFactory;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectVirtualModelInstance;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceType;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.UseModelSlotDeclaration;
import org.openflexo.gina.controller.CustomTypeEditor;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.model.FIBWidget;
import org.openflexo.gina.model.widget.FIBCustom;
import org.openflexo.gina.utils.InspectorGroup;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.view.EmptyPanel;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.model.FlexoPerspective;

public class FMLTechnologyAdapterController extends TechnologyAdapterController<FMLTechnologyAdapter> {

	private InspectorGroup fmlInspectors;

	@Override
	public Class<FMLTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	@Override
	protected void initializeInspectors(FlexoController controller) {
		fmlInspectors = controller.loadInspectorGroup("FML", getTechnologyAdapter().getLocales(), controller.getCoreInspectorGroup());
	}

	@Override
	public InspectorGroup getTechnologyAdapterInspectorGroup() {
		return fmlInspectors;
	}

	@Override
	protected CustomTypeEditor<?> makeCustomTypeEditor(Class<? extends CustomType> typeClass) {
		if (typeClass.equals(FlexoResourceType.class)) {
			return new FlexoResourceTypeEditor(getServiceManager());
		}
		else if (typeClass.equals(VirtualModelInstanceType.class)) {
			return new VirtualModelInstanceTypeEditor(getServiceManager());
		}
		else if (typeClass.equals(FlexoConceptInstanceType.class)) {
			return new FlexoConceptInstanceTypeEditor(getServiceManager());
		}
		return super.makeCustomTypeEditor(typeClass);
	}

	@Override
	protected void initializeActions(ControllerActionInitializer actionInitializer) {

		new CreateTopLevelVirtualModelInitializer(actionInitializer);
		new CreateModelSlotInitializer(actionInitializer);
		new CreateContainedVirtualModelInitializer(actionInitializer);
		new DeleteVirtualModelInitializer(actionInitializer);

		new AddUseDeclarationInitializer(actionInitializer);

		new CreateTechnologyRoleInitializer(actionInitializer);
		new CreatePrimitiveRoleInitializer(actionInitializer);
		new CreateFlexoConceptInstanceRoleInitializer(actionInitializer);
		new CreateIndividualRoleInitializer(actionInitializer);
		new CreateExpressionPropertyInitializer(actionInitializer);
		new CreateGetSetPropertyInitializer(actionInitializer);
		new CreateAbstractPropertyInitializer(actionInitializer);

		new CreateEditionActionInitializer(actionInitializer);
		new CreateFlexoBehaviourInitializer(actionInitializer);
		new CreateFlexoConceptInitializer(actionInitializer);
		new CreateFlexoEventInitializer(actionInitializer);
		// new CreateFlexoBehaviourParameterInitializer(actionInitializer);
		new CreateGenericBehaviourParameterInitializer(actionInitializer);
		new CreateInspectorEntryInitializer(actionInitializer);
		// new DeleteFlexoConceptInitializer(actionInitializer);
		// new DuplicateFlexoConceptInitializer(actionInitializer);
		new ShowFMLRepresentationInitializer(actionInitializer);

		new DeclareNewVariableActionInitializer(actionInitializer);
		new AssignActionInitializer(actionInitializer);
		new AddToListActionInitializer(actionInitializer);
		new AddReturnStatementActionInitializer(actionInitializer);

		if (actionInitializer.getActionInitializer(DeleteFlexoConceptObjects.actionType) == null) {
			new DeleteFlexoConceptObjectsInitializer(actionInitializer);
		}
		else {
			// Already have an module specific initializer, skip DeleteFlexoConceptObjectsInitializer
		}

		new AddParentFlexoConceptInitializer(actionInitializer);

		// Add paste handlers
		actionInitializer.getEditingContext().registerPasteHandler(new VirtualModelPasteHandler());
		actionInitializer.getEditingContext().registerPasteHandler(new FlexoConceptPasteHandler());
		actionInitializer.getEditingContext().registerPasteHandler(new FlexoPropertyPasteHandler());
		actionInitializer.getEditingContext().registerPasteHandler(new FlexoBehaviourPasteHandler());
		actionInitializer.getEditingContext().registerPasteHandler(new FMLControlGraphPasteHandler());
		actionInitializer.getEditingContext().registerPasteHandler(new BehaviorPasteHandler());

		FlexoActionType.newVirtualModelMenu.setSmallIcon(FMLIconLibrary.VIRTUAL_MODEL_ICON);
		FlexoActionType.newPropertyMenu.setSmallIcon(FMLIconLibrary.FLEXO_ROLE_ICON);
	}

	@Override
	public ImageIcon getTechnologyBigIcon() {
		return FMLIconLibrary.VIEWPOINT_MEDIUM_ICON;
	}

	/**
	 * Return icon representing underlying technology
	 * 
	 * @return Icon representing underlying technology
	 */
	@Override
	public ImageIcon getTechnologyIcon() {
		return FMLIconLibrary.VIEWPOINT_ICON;
	}

	/**
	 * Return icon representing a model of underlying technology
	 * 
	 * @return
	 */
	@Override
	public ImageIcon getModelIcon() {
		return FMLIconLibrary.VIEWPOINT_ICON;
	}

	/**
	 * Return icon representing a meta model of underlying technology
	 * 
	 * @return icon representing a meta model of underlying technology
	 */
	@Override
	public ImageIcon getMetaModelIcon() {
		return FMLIconLibrary.VIEWPOINT_ICON;
	}

	/**
	 * Return icon representing supplied technology object
	 * 
	 * @param objectClass
	 * @return icon representing supplied technology object
	 */
	@Override
	public ImageIcon getIconForTechnologyObject(Class<? extends TechnologyObject<?>> objectClass) {
		if (VirtualModel.class.isAssignableFrom(objectClass)) {
			return FMLIconLibrary.VIRTUAL_MODEL_ICON;
		}
		else if (FlexoEvent.class.isAssignableFrom(objectClass)) {
			return FMLIconLibrary.FLEXO_EVENT_ICON;
		}
		else if (FlexoConcept.class.isAssignableFrom(objectClass)) {
			return FMLIconLibrary.FLEXO_CONCEPT_ICON;
		}
		else if (UseModelSlotDeclaration.class.isAssignableFrom(objectClass)) {
			return FMLIconLibrary.IMPORT_ICON;
		}
		else if (VirtualModelInstance.class.isAssignableFrom(objectClass)) {
			return FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
		}
		else if (FlexoConceptInstance.class.isAssignableFrom(objectClass)) {
			return FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON;
		}
		return IconFactory.getImageIcon(IconLibrary.OPENFLEXO_NOTEXT_16, IconLibrary.QUESTION);
	}

	/**
	 * Return icon representing supplied model slot class
	 * 
	 * @param object
	 * @return
	 */
	@Override
	public ImageIcon getIconForModelSlot(Class<? extends ModelSlot<?>> modelSlotClass) {
		if (FMLRTVirtualModelInstanceModelSlot.class.isAssignableFrom(modelSlotClass)) {
			return FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
		}
		return super.getIconForModelSlot(modelSlotClass);
	}

	/**
	 * Return icon representing supplied pattern property
	 * 
	 * @param patternRoleClass
	 * @return icon representing supplied pattern property
	 */
	@Override
	public ImageIcon getIconForFlexoRole(Class<? extends FlexoRole<?>> flexoRoleClass) {
		if (FlexoConceptInstanceRole.class.isAssignableFrom(flexoRoleClass)) {
			return FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON;
		}
		return null;
	}

	@Override
	public ImageIcon getIconForEditionAction(Class<? extends EditionAction> editionActionClass) {

		if (AddFlexoConceptInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.DUPLICATE);
		}
		else if (SelectFlexoConceptInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.IMPORT);
		}
		else if (SelectVirtualModelInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON, IconLibrary.IMPORT);
		}
		else if (DeleteAction.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.DELETE);
		}
		return super.getIconForEditionAction(editionActionClass);
	}

	@Override
	public ImageIcon getIconForFlexoBehaviour(Class<? extends FlexoBehaviour> flexoBehaviourClass) {
		if (ActionScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return FMLIconLibrary.ACTION_SCHEME_ICON;
		}
		else if (DeletionScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return FMLIconLibrary.DELETE_ICON;
		}
		else if (CreationScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return FMLIconLibrary.CREATION_SCHEME_ICON;
		}
		else if (NavigationScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return FMLIconLibrary.NAVIGATION_SCHEME_ICON;
		}
		else if (SynchronizationScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return FMLIconLibrary.SYNCHRONIZATION_SCHEME_ICON;
		}
		else if (CloningScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return FMLIconLibrary.CLONING_SCHEME_ICON;
		}
		else if (EventListener.class.isAssignableFrom(flexoBehaviourClass)) {
			return FMLIconLibrary.EVENT_LISTENER_ICON;
		}
		return super.getIconForFlexoBehaviour(flexoBehaviourClass);
	}

	@Override
	public boolean hasModuleViewForObject(TechnologyObject<FMLTechnologyAdapter> object, FlexoController controller) {

		if (object instanceof FlexoConcept || object instanceof FMLLocalizedDictionary) {
			return true;
		}
		// TODO not applicable
		return false;
	}

	@Override
	public String getWindowTitleforObject(TechnologyObject<FMLTechnologyAdapter> object, FlexoController controller) {
		if (object instanceof VirtualModel) {
			return ((VirtualModel) object).getName();
		}
		if (object instanceof FlexoConcept) {
			return ((FlexoConcept) object).getName();
		}
		if (object instanceof FMLLocalizedDictionary) {
			return getLocales().localizedForKey("localized_dictionary_for") + " " + ((FMLLocalizedDictionary) object).getOwner().getName();
		}
		if (object != null) {
			return object.toString();
		}
		return "null";
	}

	/**
	 * Return a newly created ModuleView for supplied technology object, if this TechnologyAdapter controller service support ModuleView
	 * rendering
	 * 
	 * @param object
	 * @return newly created ModuleView for supplied technology object
	 */
	@Override
	public ModuleView<?> createModuleViewForObject(TechnologyObject<FMLTechnologyAdapter> object, FlexoController controller,
			FlexoPerspective perspective) {
		if (object instanceof FMLLocalizedDictionary) {
			return new FMLLocalizedDictionaryView((FMLLocalizedDictionary) object, controller, perspective);
		}
		if (object instanceof FlexoConcept) {
			FlexoConcept ep = (FlexoConcept) object;
			if (ep instanceof VirtualModel) {
				return new VirtualModelView((VirtualModel) ep, controller, perspective);
			}
			else {
				return new StandardFlexoConceptView(ep, controller, perspective);
			}

		}

		return new EmptyPanel<TechnologyObject<FMLTechnologyAdapter>>(controller, perspective, object);
	}

	@Override
	protected FIBTechnologyBrowser<FMLTechnologyAdapter> buildTechnologyBrowser(FlexoController controller) {
		return new FIBViewPointLibraryBrowser(getTechnologyAdapter(), controller);
	}

	@Override
	public FIBWidget makeWidget(WidgetContext parameter, FlexoBehaviourAction<?, ?, ?> action, FIBModelFactory fibModelFactory,
			boolean[] expand) {
		if (parameter.getWidget() == WidgetType.CUSTOM_WIDGET) {
			if (parameter.getType() instanceof VirtualModelInstanceType) {
				return makeVirtualModelInstanceSelector(parameter, fibModelFactory);
			}
			else if (parameter.getType() instanceof FlexoConceptInstanceType) {
				return makeFlexoConceptInstanceSelector(parameter, fibModelFactory);
			}
			else if (parameter.getType() instanceof FlexoResourceType) {
				return makeFlexoResourceSelector(parameter, fibModelFactory);
			}

		}
		return super.makeWidget(parameter, action, fibModelFactory, expand);
	}

	private boolean needsVirtualModelInstanceContext(WidgetContext object) {
		return object instanceof FlexoBehaviourParameter
				&& ((FlexoBehaviourParameter) object).getFlexoBehaviour() instanceof CreationScheme;
	}

	private String getContainerBinding(WidgetContext object) {
		StringBuilder result = new StringBuilder("data.");
		if (needsVirtualModelInstanceContext(object)) {
			result.append(FMLBindingFactory.VIRTUAL_MODEL_INSTANCE);
		}
		else {
			result.append(FMLBindingFactory.FLEXO_CONCEPT_INSTANCE);
		}
		result.append(object.getContainer().toString());
		return result.toString();
	}

	private FIBWidget makeFlexoResourceSelector(final WidgetContext object, FIBModelFactory fibModelFactory) {
		FIBCustom resourceSelector = fibModelFactory.newFIBCustom();
		resourceSelector.setBindingFactory(object.getBindingFactory());
		Class resourceSelectorClass;
		try {
			resourceSelectorClass = Class.forName("org.openflexo.components.widget.FIBResourceSelector");
			resourceSelector.setComponentClass(resourceSelectorClass);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		resourceSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(resourceSelector, new DataBinding<>("component.project"),
				new DataBinding<>("controller.editor.project"), true));
		resourceSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(resourceSelector,
				new DataBinding<>("component.serviceManager"), new DataBinding<>("controller.flexoController.applicationContext"), true));
		resourceSelector
				.addToAssignments(fibModelFactory.newFIBCustomAssignment(resourceSelector, new DataBinding<>("component.expectedType"),
						new DataBinding<>("data.parametersDefinitions." + object.getName() + ".type"), true));
		return resourceSelector;

	}

	private FIBWidget makeFlexoConceptInstanceSelector(final WidgetContext parameter, FIBModelFactory fibModelFactory) {
		FIBCustom fciSelector = fibModelFactory.newFIBCustom();
		fciSelector.setBindingFactory(parameter.getBindingFactory());
		Class fciSelectorClass;
		try {
			fciSelectorClass = Class.forName("org.openflexo.fml.rt.controller.widget.FIBFlexoConceptInstanceSelector");
			fciSelector.setComponentClass(fciSelectorClass);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fciSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(fciSelector, new DataBinding<>("component.project"),
				new DataBinding<>("controller.editor.project"), true));

		String containerBinding = getContainerBinding(parameter);
		DataBinding<?> container = parameter.getContainer();
		if (container != null && container.isSet() && container.isValid()) {
			Type containerType = container.getAnalyzedType();
			if (containerType instanceof VirtualModelInstanceType) {
				fciSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(fciSelector,
						new DataBinding<>("component.virtualModelInstance"), new DataBinding<>(containerBinding), true));
			}
			else if (TypeUtils.isTypeAssignableFrom(FlexoResourceCenter.class, containerType)) {
				fciSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(fciSelector,
						new DataBinding<>("component.resourceCenter"), new DataBinding<>(containerBinding), true));
			}
		}
		else {

			// No container defined, set service manager
			fciSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(fciSelector, new DataBinding<>("component.serviceManager"),
					new DataBinding<>("controller.flexoController.applicationContext"), true));
		}

		fciSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(fciSelector, new DataBinding<>("component.expectedType"),
				new DataBinding<>("data.parametersDefinitions." + parameter.getName() + ".type"), true));
		return fciSelector;

	}

	private FIBWidget makeVirtualModelInstanceSelector(final WidgetContext parameter, FIBModelFactory fibModelFactory) {
		FIBCustom vmiSelector = fibModelFactory.newFIBCustom();
		vmiSelector.setBindingFactory(parameter.getBindingFactory());
		Class fciSelectorClass;
		try {
			fciSelectorClass = Class.forName("org.openflexo.fml.rt.controller.widget.FIBVirtualModelInstanceSelector");
			vmiSelector.setComponentClass(fciSelectorClass);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		vmiSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(vmiSelector, new DataBinding<>("component.project"),
				new DataBinding<>("controller.editor.project"), true));

		String containerBinding = getContainerBinding(parameter);
		DataBinding<?> container = parameter.getContainer();
		if (container != null && container.isSet() && container.isValid()) {
			Type containerType = container.getAnalyzedType();

			/*if (containerType instanceof ViewType) {
				vmiSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(vmiSelector, new DataBinding<>("component.view"),
						new DataBinding<>(containerBinding), true));
			}
			else*/
			if (TypeUtils.isTypeAssignableFrom(FlexoResourceCenter.class, containerType)) {
				vmiSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(vmiSelector,
						new DataBinding<>("component.resourceCenter"), new DataBinding<>(containerBinding), true));
			}
		}
		else {
			// No container defined, set service manager
			vmiSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(vmiSelector, new DataBinding<>("component.serviceManager"),
					new DataBinding<>("controller.flexoController.applicationContext"), true));
		}

		// vmiSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(vmiSelector, new DataBinding<Object>("component.view"),
		// new DataBinding<Object>(containerBinding), true));

		vmiSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(vmiSelector, new DataBinding<>("component.expectedType"),
				new DataBinding<>("data.parametersDefinitions." + parameter.getName() + ".type"), true));
		return vmiSelector;
	}

	private FIBWidget makeViewSelector(final WidgetContext widgetContext, FIBModelFactory fibModelFactory) {
		FIBCustom viewSelector = fibModelFactory.newFIBCustom();
		viewSelector.setBindingFactory(widgetContext.getBindingFactory());
		Class fciSelectorClass;
		try {
			fciSelectorClass = Class.forName("org.openflexo.fml.rt.controller.widget.FIBViewSelector");
			viewSelector.setComponentClass(fciSelectorClass);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		viewSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(viewSelector, new DataBinding<>("component.project"),
				new DataBinding<>("controller.editor.project"), true));

		String containerBinding = getContainerBinding(widgetContext);
		DataBinding<?> container = widgetContext.getContainer();
		if (container != null && container.isSet() && container.isValid()) {
			Type containerType = container.getAnalyzedType();

			if (TypeUtils.isTypeAssignableFrom(FlexoResourceCenter.class, containerType)) {
				viewSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(viewSelector,
						new DataBinding<>("component.resourceCenter"), new DataBinding<>(containerBinding), true));
			}
		}
		else {
			// No container defined, set service manager
			viewSelector
					.addToAssignments(fibModelFactory.newFIBCustomAssignment(viewSelector, new DataBinding<>("component.serviceManager"),
							new DataBinding<>("controller.flexoController.applicationContext"), true));
		}

		// viewSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(viewSelector, new DataBinding<Object>("component.view"),
		// new DataBinding<Object>(containerBinding), true));

		viewSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(viewSelector, new DataBinding<>("component.expectedType"),
				new DataBinding<>("data.parametersDefinitions." + widgetContext.getName() + ".type"), true));
		return viewSelector;
	}
}
