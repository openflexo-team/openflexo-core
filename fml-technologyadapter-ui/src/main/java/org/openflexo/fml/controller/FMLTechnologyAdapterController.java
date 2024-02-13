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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
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
import org.openflexo.fml.controller.action.CreateContextualEditionActionInitializer;
import org.openflexo.fml.controller.action.CreateEditionActionInitializer;
import org.openflexo.fml.controller.action.CreateExpressionPropertyInitializer;
import org.openflexo.fml.controller.action.CreateFlexoBehaviourInitializer;
import org.openflexo.fml.controller.action.CreateFlexoConceptInitializer;
import org.openflexo.fml.controller.action.CreateFlexoConceptInstanceRoleInitializer;
import org.openflexo.fml.controller.action.CreateFlexoEnumInitializer;
import org.openflexo.fml.controller.action.CreateFlexoEnumValueInitializer;
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
import org.openflexo.fml.controller.action.DeleteCompilationUnitInitializer;
import org.openflexo.fml.controller.action.DeleteFlexoConceptObjectsInitializer;
import org.openflexo.fml.controller.action.DuplicateVirtualModelInitializer;
import org.openflexo.fml.controller.action.GenerateCreationSchemeInitializer;
import org.openflexo.fml.controller.action.GenerateUnimplementedPropertiesAndBehavioursInitializer;
import org.openflexo.fml.controller.action.MoveVirtualModelToContainerVirtualModelInitializer;
import org.openflexo.fml.controller.action.MoveVirtualModelToDirectoryInitializer;
import org.openflexo.fml.controller.action.RenameCompilationUnitInitializer;
import org.openflexo.fml.controller.action.RenameFlexoConceptInitializer;
import org.openflexo.fml.controller.validation.ValidateActionizer;
import org.openflexo.fml.controller.view.StandardCompilationUnitView;
import org.openflexo.fml.controller.widget.FIBCompilationUnitBrowser;
import org.openflexo.fml.controller.widget.FIBVirtualModelLibraryBrowser;
import org.openflexo.fml.controller.widget.FlexoConceptInstanceTypeEditor;
import org.openflexo.fml.controller.widget.FlexoConceptTypeEditor;
import org.openflexo.fml.controller.widget.FlexoEnumTypeEditor;
import org.openflexo.fml.controller.widget.FlexoResourceTypeEditor;
import org.openflexo.fml.controller.widget.VirtualModelInstanceTypeEditor;
import org.openflexo.fml.rstasupport.FMLLanguageSupport;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CloningScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.EventListener;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FMLValidationModel;
import org.openflexo.foundation.fml.FMLValidationReport;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter.WidgetType;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoEnum;
import org.openflexo.foundation.fml.FlexoEnumType;
import org.openflexo.foundation.fml.FlexoEnumValue;
import org.openflexo.foundation.fml.FlexoEvent;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.NavigationScheme;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.UseModelSlotDeclaration;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.WidgetContext;
import org.openflexo.foundation.fml.action.DeleteFlexoConceptObjects;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelSlot;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.ta.CreateContainedVirtualModel;
import org.openflexo.foundation.fml.ta.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.ta.CreateFlexoConcept;
import org.openflexo.foundation.fml.ta.CreateFlexoConceptInstanceRole;
import org.openflexo.foundation.fml.ta.CreatePrimitiveRole;
import org.openflexo.foundation.fml.ta.CreateTopLevelVirtualModel;
import org.openflexo.foundation.fml.ta.FMLDataBindingRole;
import org.openflexo.foundation.fml.ta.FlexoBehaviourRole;
import org.openflexo.foundation.fml.ta.FlexoConceptInstanceRoleRole;
import org.openflexo.foundation.fml.ta.FlexoConceptRole;
import org.openflexo.foundation.fml.ta.FlexoConceptType;
import org.openflexo.foundation.fml.ta.FlexoPropertyRole;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceType;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.gina.controller.CustomTypeEditor;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.model.FIBWidget;
import org.openflexo.gina.model.widget.FIBCustom;
import org.openflexo.gina.utils.InspectorGroup;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationModel;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.toolbox.StringUtils;
import org.openflexo.view.EmptyPanel;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * Technology-specific controller provided by {@link FMLTechnologyAdapter}<br>
 * 
 * @author sylvain
 *
 */
public class FMLTechnologyAdapterController extends TechnologyAdapterController<FMLTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(FMLTechnologyAdapterController.class.getPackage().getName());

	private InspectorGroup fmlInspectors;

	private FMLValidationModel validationModel;
	private Map<FMLCompilationUnit, FMLValidationReport> validationReports = new HashMap<>();

	private FIBCompilationUnitBrowser compilationUnitBrowser;

	private FMLLanguageSupport fmlLanguageSupport;

	@Override
	public Class<FMLTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	@Override
	public void activate() {
		super.activate();
		logger.info("Activating FMLLanguageSupport in FMLTechnologyAdapterController");
		LanguageSupportFactory.get().addLanguageSupport(FMLLanguageSupport.SYNTAX_STYLE_FML, FMLLanguageSupport.class.getName());
		fmlLanguageSupport = (FMLLanguageSupport) LanguageSupportFactory.get().getSupportFor(FMLLanguageSupport.SYNTAX_STYLE_FML);
		fmlLanguageSupport.setFMLTechnologyAdapterController(this);
		try {
			fmlLanguageSupport.getJarManager().addCurrentJreClassFileSource();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		FoldParserManager.get().addFoldParserMapping(FMLLanguageSupport.SYNTAX_STYLE_FML, new CurlyFoldParser(true, true));
	}

	@Override
	public void disactivate() {
		super.disactivate();
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
		else if (typeClass.equals(FlexoEnumType.class)) {
			return new FlexoEnumTypeEditor(getServiceManager());
		}
		else if (typeClass.equals(FlexoConceptInstanceType.class)) {
			return new FlexoConceptInstanceTypeEditor(getServiceManager());
		}
		else if (typeClass.equals(FlexoConceptType.class)) {
			return new FlexoConceptTypeEditor(getServiceManager());
		}
		return super.makeCustomTypeEditor(typeClass);
	}

	@Override
	protected void initializeActions(ControllerActionInitializer actionInitializer) {

		// Add paste handlers
		actionInitializer.getEditingContext().registerPasteHandler(new RepositoryFolderPasteHandler());
		actionInitializer.getEditingContext().registerPasteHandler(new VirtualModelPasteHandler());
		actionInitializer.getEditingContext().registerPasteHandler(new FlexoConceptPasteHandler());
		actionInitializer.getEditingContext().registerPasteHandler(new FlexoPropertyPasteHandler());
		actionInitializer.getEditingContext().registerPasteHandler(new FlexoBehaviourPasteHandler());
		actionInitializer.getEditingContext().registerPasteHandler(new FMLControlGraphPasteHandler());
		actionInitializer.getEditingContext().registerPasteHandler(new BehaviorPasteHandler());

		compilationUnitBrowser = new FIBCompilationUnitBrowser(null, actionInitializer.getController());

	}

	@Override
	public void initializeAdvancedActions(ControllerActionInitializer actionInitializer) {

		new ValidateActionizer(this, actionInitializer);

		new CreateTopLevelVirtualModelInitializer(actionInitializer);
		new CreateModelSlotInitializer(actionInitializer);
		new CreateContainedVirtualModelInitializer(actionInitializer);
		new DeleteCompilationUnitInitializer(actionInitializer);

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
		new CreateFlexoEnumInitializer(actionInitializer);
		new CreateFlexoEnumValueInitializer(actionInitializer);
		new CreateGenericBehaviourParameterInitializer(actionInitializer);
		new CreateInspectorEntryInitializer(actionInitializer);

		new CreateContextualEditionActionInitializer(actionInitializer);

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

		new MoveVirtualModelToDirectoryInitializer(actionInitializer);
		new MoveVirtualModelToContainerVirtualModelInitializer(actionInitializer);
		new RenameCompilationUnitInitializer(actionInitializer);
		new DuplicateVirtualModelInitializer(actionInitializer);

		new RenameFlexoConceptInitializer(actionInitializer);
		new GenerateCreationSchemeInitializer(actionInitializer);
		new GenerateUnimplementedPropertiesAndBehavioursInitializer(actionInitializer);

		FlexoActionFactory.newVirtualModelMenu.setSmallIcon(FMLIconLibrary.VIRTUAL_MODEL_ICON);
		FlexoActionFactory.newPropertyMenu.setSmallIcon(FMLIconLibrary.FLEXO_ROLE_ICON);
		FlexoActionFactory.newBehaviourMenu.setSmallIcon(FMLIconLibrary.FLEXO_BEHAVIOUR_ICON);
	}

	@Override
	public ImageIcon getTechnologyBigIcon() {
		return FMLIconLibrary.VIRTUAL_MODEL_BIG_ICON;
	}

	/**
	 * Return icon representing underlying technology
	 * 
	 * @return Icon representing underlying technology
	 */
	@Override
	public ImageIcon getTechnologyIcon() {
		return FMLIconLibrary.VIRTUAL_MODEL_ICON;
	}

	/**
	 * Return icon representing a model of underlying technology
	 * 
	 * @return
	 */
	@Override
	public ImageIcon getModelIcon() {
		return FMLIconLibrary.VIRTUAL_MODEL_ICON;
	}

	/**
	 * Return icon representing a meta model of underlying technology
	 * 
	 * @return icon representing a meta model of underlying technology
	 */
	@Override
	public ImageIcon getMetaModelIcon() {
		return FMLIconLibrary.VIRTUAL_MODEL_ICON;
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
		else if (FlexoEnum.class.isAssignableFrom(objectClass)) {
			return FMLIconLibrary.FLEXO_ENUM_ICON;
		}
		else if (FlexoEnumValue.class.isAssignableFrom(objectClass)) {
			return FMLIconLibrary.FLEXO_ENUM_VALUE_ICON;
		}
		else if (FlexoConcept.class.isAssignableFrom(objectClass)) {
			return FMLIconLibrary.FLEXO_CONCEPT_ICON;
		}
		else if (UseModelSlotDeclaration.class.isAssignableFrom(objectClass)) {
			return FMLIconLibrary.IMPORT_ICON;
		}
		else if (FMLRTVirtualModelInstance.class.isAssignableFrom(objectClass)) {
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
	 * Return icon representing supplied role class
	 * 
	 * @param patternRoleClass
	 * @return icon representing supplied role class
	 */
	@Override
	public ImageIcon getIconForFlexoRole(Class<? extends FlexoRole<?>> flexoRoleClass) {
		if (FlexoConceptRole.class.isAssignableFrom(flexoRoleClass)) {
			return FMLIconLibrary.FLEXO_CONCEPT_ICON;
		}
		if (FlexoConceptInstanceRoleRole.class.isAssignableFrom(flexoRoleClass)) {
			return FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON;
		}
		if (FlexoPropertyRole.class.isAssignableFrom(flexoRoleClass)) {
			return FMLIconLibrary.FLEXO_ROLE_ICON;
		}
		if (FlexoBehaviourRole.class.isAssignableFrom(flexoRoleClass)) {
			return FMLIconLibrary.FLEXO_BEHAVIOUR_ICON;
		}
		if (FMLDataBindingRole.class.isAssignableFrom(flexoRoleClass)) {
			return FMLIconLibrary.DATA_BINDING_ICON;
		}
		return null;
	}

	@Override
	public ImageIcon getIconForEditionAction(Class<? extends EditionAction> editionActionClass) {

		if (CreateFlexoConcept.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_CONCEPT_ICON, IconLibrary.DUPLICATE);
		}
		if (CreateTopLevelVirtualModel.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.VIRTUAL_MODEL_ICON, IconLibrary.DUPLICATE);
		}
		if (CreateContainedVirtualModel.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.VIRTUAL_MODEL_ICON, IconLibrary.DUPLICATE);
		}
		if (CreatePrimitiveRole.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_ROLE_ICON, IconLibrary.DUPLICATE);
		}
		if (CreateFlexoConceptInstanceRole.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.DUPLICATE);
		}
		if (CreateFlexoBehaviour.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.ACTION_SCHEME_ICON, IconLibrary.DUPLICATE);
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

	/*@Override
	public boolean hasModuleViewForObject(TechnologyObject<FMLTechnologyAdapter> object, FlexoController controller,
			FlexoPerspective perspective) {
	
		if (object instanceof FMLCompilationUnit) {
			return true;
		}
		//if (object instanceof FlexoConcept) {
		//	return true;
		//}
		// TODO not applicable
		return false;
	}*/

	@Override
	public String getWindowTitleforObject(TechnologyObject<FMLTechnologyAdapter> object, FlexoController controller) {
		if (object instanceof FMLCompilationUnit) {
			return ((FMLCompilationUnit) object).getVirtualModel().getName();
		}
		if (object instanceof FlexoConcept) {
			return ((FlexoConcept) object).getName();
		}
		if (object != null) {
			return object.toString();
		}
		return "null";
	}

	@Override
	public boolean isRepresentableInModuleView(TechnologyObject<FMLTechnologyAdapter> object) {
		return (object instanceof FMLObject && ((FMLObject) object).getDeclaringCompilationUnit() != null);
	}

	@Override
	public FlexoObject getRepresentableMasterObject(TechnologyObject<FMLTechnologyAdapter> object) {
		if (object instanceof FMLObject) {
			return ((FMLObject) object).getDeclaringCompilationUnit();
		}
		return null;
	}

	/**
	 * Return a newly created ModuleView for supplied technology object, if this TechnologyAdapter controller service support ModuleView
	 * rendering
	 * 
	 * @param object
	 * @return newly created ModuleView for supplied technology object
	 */
	@Override
	public ModuleView<?> createModuleViewForMasterObject(TechnologyObject<FMLTechnologyAdapter> object, FlexoController controller,
			FlexoPerspective perspective) {
		if (object instanceof FMLCompilationUnit) {
			return new StandardCompilationUnitView((FMLCompilationUnit) object, controller, perspective);
		}
		/*if (object instanceof FlexoConcept) {
			FlexoConcept ep = (FlexoConcept) object;
			return new StandardFlexoConceptView(ep, controller, perspective);
		}*/

		return new EmptyPanel<>(controller, perspective, object);
	}

	@Override
	protected FIBTechnologyBrowser<FMLTechnologyAdapter> buildTechnologyBrowser(FlexoController controller) {
		return new FIBVirtualModelLibraryBrowser(getTechnologyAdapter(), controller);
	}

	@Override
	public FIBWidget makeWidget(WidgetContext widgetContext, FlexoBehaviourAction<?, ?, ?> action, FIBModelFactory fibModelFactory,
			String variableName, boolean[] expand) {
		if (widgetContext.getWidget() == WidgetType.CUSTOM_WIDGET) {
			if (widgetContext.getType() instanceof VirtualModelInstanceType) {
				return makeVirtualModelInstanceSelector(widgetContext, fibModelFactory, variableName);
			}
			else if (widgetContext.getType() instanceof FlexoConceptInstanceType) {
				return makeFlexoConceptInstanceSelector(widgetContext, fibModelFactory, variableName);
			}
			else if (widgetContext.getType() instanceof FlexoResourceType) {
				return makeFlexoResourceSelector(widgetContext, fibModelFactory, variableName);
			}
			else if (widgetContext.getType() instanceof FlexoConceptType) {
				return makeFlexoConceptSelector(widgetContext, fibModelFactory, variableName);
			}
			else if (widgetContext.getType().equals(FlexoConcept.class)) {
				return makeFlexoConceptSelector(widgetContext, fibModelFactory, variableName);
			}
			else if (widgetContext.getType().equals(DataBinding.class)) {
				return makeDataBindingSelector(widgetContext, fibModelFactory, variableName);
			}
		}
		return super.makeWidget(widgetContext, action, fibModelFactory, variableName, expand);
	}

	/*private boolean needsVirtualModelInstanceContext(WidgetContext object) {
		return object instanceof FlexoBehaviourParameter
				&& ((FlexoBehaviourParameter) object).getFlexoBehaviour() instanceof CreationScheme;
	}*/

	private static String getContainerBinding(WidgetContext widgetContext, String variableName) {
		return variableName + "."
				+ (StringUtils.isNotEmpty(widgetContext.getFlexoConceptInstanceAccess())
						? widgetContext.getFlexoConceptInstanceAccess() + "."
						: "")
				+ widgetContext.getContainer().toString();
	}

	private static FIBWidget makeFlexoResourceSelector(final WidgetContext widgetContext, FIBModelFactory fibModelFactory,
			String variableName) {
		FIBCustom resourceSelector = fibModelFactory.newFIBCustom();
		resourceSelector.setBindingFactory(widgetContext.getBindingFactory());
		Class<?> resourceSelectorClass;
		try {
			resourceSelectorClass = Class.forName("org.openflexo.components.widget.FIBResourceSelector");
			resourceSelector.setComponentClass(resourceSelectorClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		resourceSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(resourceSelector,
				new DataBinding<>("component.resourceCenter"), new DataBinding<>("controller.editor.project"), true));
		resourceSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(resourceSelector,
				new DataBinding<>("component.serviceManager"), new DataBinding<>("controller.flexoController.applicationContext"), true));
		resourceSelector
				.addToAssignments(fibModelFactory.newFIBCustomAssignment(resourceSelector, new DataBinding<>("component.expectedType"),
						new DataBinding<>(variableName + "." + widgetContext.getWidgetDefinitionAccess() + ".type"), true));
		return resourceSelector;

	}

	private static FIBWidget makeFlexoConceptInstanceSelector(final WidgetContext widgetContext, FIBModelFactory fibModelFactory,
			String variableName) {

		FIBCustom fciSelector = fibModelFactory.newFIBCustom();
		fciSelector.setBindingFactory(widgetContext.getBindingFactory());
		Class<?> fciSelectorClass;
		try {
			fciSelectorClass = Class.forName("org.openflexo.fml.rt.controller.widget.FIBFlexoConceptInstanceSelector");
			fciSelector.setComponentClass(fciSelectorClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		fciSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(fciSelector, new DataBinding<>("component.project"),
				new DataBinding<>("controller.editor.project"), true));

		String containerBinding = getContainerBinding(widgetContext, variableName);
		DataBinding<?> container = widgetContext.getContainer();
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
				new DataBinding<>(variableName + "." + widgetContext.getWidgetDefinitionAccess() + ".type"), true));

		return fciSelector;

	}

	private static FIBWidget makeVirtualModelInstanceSelector(final WidgetContext widgetContext, FIBModelFactory fibModelFactory,
			String variableName) {
		FIBCustom vmiSelector = fibModelFactory.newFIBCustom();
		vmiSelector.setBindingFactory(widgetContext.getBindingFactory());
		Class<?> fciSelectorClass;
		try {
			fciSelectorClass = Class.forName("org.openflexo.fml.rt.controller.widget.FIBVirtualModelInstanceSelector");
			vmiSelector.setComponentClass(fciSelectorClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		vmiSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(vmiSelector, new DataBinding<>("component.project"),
				new DataBinding<>("controller.editor.project"), true));

		String containerBinding = getContainerBinding(widgetContext, variableName);
		DataBinding<?> container = widgetContext.getContainer();
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
		// else {
		// No container defined, set service manager
		vmiSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(vmiSelector, new DataBinding<>("component.serviceManager"),
				new DataBinding<>("controller.flexoController.applicationContext"), true));
		// }

		// vmiSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(vmiSelector, new DataBinding<Object>("component.view"),
		// new DataBinding<Object>(containerBinding), true));

		vmiSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(vmiSelector, new DataBinding<>("component.expectedType"),
				new DataBinding<>(variableName + "." + widgetContext.getWidgetDefinitionAccess() + ".type"), true));
		return vmiSelector;
	}

	private static FIBWidget makeFlexoConceptSelector(final WidgetContext widgetContext, FIBModelFactory fibModelFactory,
			String variableName) {

		FIBCustom fcSelector = fibModelFactory.newFIBCustom();
		fcSelector.setBindingFactory(widgetContext.getBindingFactory());
		Class<?> fciSelectorClass;
		try {
			fciSelectorClass = Class.forName("org.openflexo.fml.controller.widget.FIBFlexoConceptSelector");
			fcSelector.setComponentClass(fciSelectorClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		// fciSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(fciSelector, new DataBinding<>("component.resourceCenter"),
		// new DataBinding<>("controller.editor.project"), true));

		String containerBinding = getContainerBinding(widgetContext, variableName);
		DataBinding<?> container = widgetContext.getContainer();
		if (container != null && container.isSet() && container.isValid()) {
			Type containerType = container.getAnalyzedType();
			if (containerType instanceof VirtualModelInstanceType) {
				fcSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(fcSelector, new DataBinding<>("component.virtualModel"),
						new DataBinding<>(containerBinding), true));
			}
			else if (TypeUtils.isTypeAssignableFrom(FlexoResourceCenter.class, containerType)) {
				fcSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(fcSelector,
						new DataBinding<>("component.resourceCenter"), new DataBinding<>(containerBinding), true));
			}
		}
		else {

			// No container defined, set service manager
			fcSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(fcSelector, new DataBinding<>("component.serviceManager"),
					new DataBinding<>("controller.flexoController.applicationContext"), true));
		}

		fcSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(fcSelector, new DataBinding<>("component.expectedType"),
				new DataBinding<>(variableName + "." + widgetContext.getWidgetDefinitionAccess() + ".type"), true));

		return fcSelector;

	}

	private static FIBWidget makeDataBindingSelector(final WidgetContext widgetContext, FIBModelFactory fibModelFactory,
			String variableName) {

		FIBCustom fciSelector = fibModelFactory.newFIBCustom();
		fciSelector.setBindingFactory(widgetContext.getBindingFactory());
		Class<?> fciSelectorClass;
		try {
			fciSelectorClass = Class.forName("org.openflexo.gina.swing.utils.BindingSelector");
			fciSelector.setComponentClass(fciSelectorClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		// fciSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(fciSelector, new DataBinding<>("component.bindable"),
		// new DataBinding<>("controller.editor.project"), true));

		String containerBinding = getContainerBinding(widgetContext, variableName);
		DataBinding<?> container = widgetContext.getContainer();
		System.out.println("widgetContext : " + widgetContext);
		System.out.println("containerBinding : " + containerBinding);
		System.out.println("container : " + container);
		if (container != null && container.isSet() && container.isValid()) {
			Type containerType = container.getAnalyzedType();
			System.out.println("containerType : " + containerType);
			fciSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(fciSelector, new DataBinding<>("component.bindable"),
					new DataBinding<>(containerBinding), true));
			if (containerType instanceof VirtualModelInstanceType) {
				fciSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(fciSelector,
						new DataBinding<>("component.virtualModel"), new DataBinding<>(containerBinding), true));
			}
			else if (TypeUtils.isTypeAssignableFrom(FlexoResourceCenter.class, containerType)) {
				fciSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(fciSelector,
						new DataBinding<>("component.resourceCenter"), new DataBinding<>(containerBinding), true));
			}
		}
		/*else {
		
			// No container defined, set service manager
			fciSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(fciSelector, new DataBinding<>("component.serviceManager"),
					new DataBinding<>("controller.flexoController.applicationContext"), true));
		}*/

		// fciSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(fciSelector, new DataBinding<>("component.expectedType"),
		// new DataBinding<>(variableName + "." + widgetContext.getWidgetDefinitionAccess() + ".type"), true));

		return fciSelector;

	}

	private static FIBWidget makeDefaultFlexoConceptSelector(final WidgetContext widgetContext, FIBModelFactory fibModelFactory,
			String variableName) {

		FIBCustom flexoConceptSelector = fibModelFactory.newFIBCustom();
		flexoConceptSelector.setBindingFactory(widgetContext.getBindingFactory());
		Class<?> fciSelectorClass;
		try {
			fciSelectorClass = Class.forName("org.openflexo.fml.controller.widget.FIBFlexoConceptSelector");
			flexoConceptSelector.setComponentClass(fciSelectorClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		flexoConceptSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(flexoConceptSelector,
				new DataBinding<>("component.project"), new DataBinding<>("controller.editor.project"), true));

		String containerBinding = getContainerBinding(widgetContext, variableName);
		DataBinding<?> container = widgetContext.getContainer();
		if (container != null && container.isSet() && container.isValid()) {
			Type containerType = container.getAnalyzedType();
			if (TypeUtils.isTypeAssignableFrom(VirtualModel.class, containerType)) {
				flexoConceptSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(flexoConceptSelector,
						new DataBinding<>("component.virtualModel"), new DataBinding<>(containerBinding), true));
			}
			else if (TypeUtils.isTypeAssignableFrom(FlexoResourceCenter.class, containerType)) {
				flexoConceptSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(flexoConceptSelector,
						new DataBinding<>("component.resourceCenter"), new DataBinding<>(containerBinding), true));
			}
		}
		else {

			// No container defined, set service manager
			flexoConceptSelector.addToAssignments(
					fibModelFactory.newFIBCustomAssignment(flexoConceptSelector, new DataBinding<>("component.virtualModelLibrary"),
							new DataBinding<>("controller.flexoController.applicationContext.virtualModelLibrary"), true));
		}

		// flexoConceptSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(flexoConceptSelector, new
		// DataBinding<>("component.expectedType"),
		// new DataBinding<>(variableName + "." + widgetContext.getWidgetDefinitionAccess() + ".type"), true));

		return flexoConceptSelector;

	}

	/* Unused 
	private static FIBWidget makeViewSelector(final WidgetContext widgetContext, FIBModelFactory fibModelFactory, String variableName) {
		FIBCustom viewSelector = fibModelFactory.newFIBCustom();
		viewSelector.setBindingFactory(widgetContext.getBindingFactory());
		Class<?> fciSelectorClass;
		try {
			fciSelectorClass = Class.forName("org.openflexo.fml.rt.controller.widget.FIBViewSelector");
			viewSelector.setComponentClass(fciSelectorClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		viewSelector.addToAssignments(fibModelFactory.newFIBCustomAssignment(viewSelector, new DataBinding<>("component.project"),
				new DataBinding<>("controller.editor.project"), true));
	
		String containerBinding = getContainerBinding(widgetContext, variableName);
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
				new DataBinding<>(variableName + "." + widgetContext.getWidgetDefinitionAccess() + ".type"), true));
		return viewSelector;
	}
	 */

	@Override
	public void resourceLoading(TechnologyAdapterResource<?, FMLTechnologyAdapter> resource) {
		// logger.info("RESOURCE LOADED: " + resource);

		if (resource instanceof CompilationUnitResource) {
			FMLCompilationUnit cu = ((CompilationUnitResource) resource).getLoadedCompilationUnit();
			buildFMLValidationReport(cu);
		}
	}

	/*private FMLValidationReport makeValidationReport(FMLCompilationUnit compilationUnit) {
		FMLValidationReport validationReport = null;
		try {
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Validating compilation unit " + compilationUnit);
			}
			Progress.progress(getLocales().localizedForKey("validating_compilation_unit..."));
			validationReport = (FMLValidationReport) getFMLValidationModel().validate(compilationUnit);
			validationReports.put(vm, validationReport);
			if (logger.isLoggable(Level.INFO)) {
				logger.info("End validating compilation unit " + compilationUnit);
				logger.info("Errors=" + validationReport.getAllErrors().size());
				for (ValidationError<?, ?> e : validationReport.getAllErrors()) {
					logger.info(" > " + validationReport.getValidationModel().localizedIssueMessage(e) + " details="
							+ validationReport.getValidationModel().localizedIssueDetailedInformations(e));
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return validationReport;
	}*/

	@Override
	public void resourceUnloaded(TechnologyAdapterResource<?, FMLTechnologyAdapter> resource) {
		logger.warning("RESOURCE UNLOADED not fully implemented: " + resource);

		if (resource instanceof CompilationUnitResource) {
			FMLCompilationUnit cu = ((CompilationUnitResource) resource).getLoadedCompilationUnit();
			if (cu != null) {
				validationReports.remove(cu);
			}
		}
	}

	public FMLValidationModel getFMLValidationModel() {
		if (validationModel == null) {
			validationModel = getServiceManager().getVirtualModelLibrary().getFMLValidationModel();
		}
		return validationModel;
	}

	@Override
	public ValidationModel getValidationModel(Class<? extends ResourceData<?>> resourceDataClass) {
		if (FMLCompilationUnit.class.isAssignableFrom(resourceDataClass)) {
			return getFMLValidationModel();
		}
		return null;
	}

	private FMLValidationReport buildFMLValidationReport(FMLCompilationUnit cu) {
		FMLValidationReport validationReport = null;
		try {
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Validating compilation unit " + cu);
			}
			Progress.progress(getLocales().localizedForKey("validating_virtual_model..."));
			validationReport = (FMLValidationReport) getFMLValidationModel().validate(cu);
			if (cu != null) {
				validationReports.put(cu, validationReport);
			}
			if (logger.isLoggable(Level.INFO)) {
				logger.info("End validating virtual model " + cu);
				logger.info("Errors=" + validationReport.getAllErrors().size());
				for (ValidationError<?, ?> e : validationReport.getAllErrors()) {
					logger.info(" > " + validationReport.getValidationModel().localizedIssueMessage(e) + " details="
							+ validationReport.getValidationModel().localizedIssueDetailedInformations(e));
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return validationReport;
	}

	public ValidationReport getValidationReport(ResourceData<?> resourceData) {
		if (resourceData instanceof FMLCompilationUnit) {
			ValidationReport returned = validationReports.get(resourceData);
			if (returned == null) {
				returned = buildFMLValidationReport((FMLCompilationUnit) resourceData);
			}
			return returned;
		}
		return buildFMLValidationReport(null);
	}

	@Override
	public ValidationReport getValidationReport(ResourceData<?> resourceData, boolean createWhenNotExistent) {
		if (resourceData instanceof FMLCompilationUnit) {
			ValidationReport returned = validationReports.get(resourceData);
			if (returned == null && createWhenNotExistent) {
				return buildFMLValidationReport((FMLCompilationUnit) resourceData);
			}
			return returned;
		}
		return null;
	}

	public FIBCompilationUnitBrowser getCompilationUnitBrowser() {
		return compilationUnitBrowser;
	}

	@Override
	public Class<FMLPreferences> getPreferencesClass() {
		return FMLPreferences.class;
	}

}
