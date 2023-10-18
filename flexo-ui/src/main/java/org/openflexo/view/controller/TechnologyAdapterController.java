/**
 * 
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.view.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.openflexo.ApplicationContext;
import org.openflexo.components.widget.FIBTechnologyBrowser;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.WidgetContext;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.ExpressionIterationAction;
import org.openflexo.foundation.fml.controlgraph.IncrementalIterationAction;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.controlgraph.WhileAction;
import org.openflexo.foundation.fml.editionaction.AddClassInstance;
import org.openflexo.foundation.fml.editionaction.AddToListAction;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.editionaction.LogAction;
import org.openflexo.foundation.fml.editionaction.NotifyProgressAction;
import org.openflexo.foundation.fml.editionaction.NotifyPropertyChangedAction;
import org.openflexo.foundation.fml.editionaction.RemoveFromListAction;
import org.openflexo.foundation.fml.rt.action.FlexoBehaviourAction;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.AddVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.CreateTopLevelVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.FinalizeMatching;
import org.openflexo.foundation.fml.rt.editionaction.FireEventAction;
import org.openflexo.foundation.fml.rt.editionaction.InitiateMatching;
import org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectUniqueFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectUniqueVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectVirtualModelInstance;
import org.openflexo.foundation.nature.ProjectNature;
import org.openflexo.foundation.nature.ProjectNatureService;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.gina.controller.CustomTypeEditor;
import org.openflexo.gina.model.FIBComponent.HorizontalScrollBarPolicy;
import org.openflexo.gina.model.FIBComponent.VerticalScrollBarPolicy;
import org.openflexo.gina.model.FIBModelFactory;
import org.openflexo.gina.model.FIBWidget;
import org.openflexo.gina.model.widget.FIBCheckBox;
import org.openflexo.gina.model.widget.FIBCheckboxList;
import org.openflexo.gina.model.widget.FIBDate;
import org.openflexo.gina.model.widget.FIBDropDown;
import org.openflexo.gina.model.widget.FIBLabel;
import org.openflexo.gina.model.widget.FIBNumber;
import org.openflexo.gina.model.widget.FIBNumber.NumberType;
import org.openflexo.gina.model.widget.FIBRadioButtonList;
import org.openflexo.gina.model.widget.FIBTextArea;
import org.openflexo.gina.model.widget.FIBTextField;
import org.openflexo.gina.utils.InspectorGroup;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.module.FlexoModule;
import org.openflexo.module.ModuleLoader;
import org.openflexo.pamela.validation.ValidationModel;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.prefs.TechnologyAdapterPreferences;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * This class represents a technology-specific controller provided by a {@link TechnologyAdapter}<br>
 * A {@link TechnologyAdapterController} works above conceptual layer provided by a {@link TechnologyAdapter}, and manages all tooling
 * dedicated to technology-specific management of a {@link TechnologyAdapter}<br>
 * This controller makes the bindings between Openflexo controllers/editors layer and the {@link TechnologyAdapter}
 * 
 * @author sylvain
 * 
 */
public abstract class TechnologyAdapterController<TA extends TechnologyAdapter<TA>> {
	private TechnologyAdapterControllerService technologyAdapterControllerService;

	private final Map<Class<? extends CustomType>, CustomTypeEditor> customTypeEditors = new LinkedHashMap<>();

	private List<TechnologyAdapterPluginController<TA>> plugins = new ArrayList<>();

	/**
	 * Returns applicable {@link ProjectNatureService}
	 * 
	 * @return
	 */
	public TechnologyAdapterControllerService getTechnologyAdapterControllerService() {
		return technologyAdapterControllerService;
	}

	/**
	 * Sets applicable {@link ProjectNatureService}
	 * 
	 * @param technologyAdapterService
	 */
	public void setTechnologyAdapterService(TechnologyAdapterControllerService technologyAdapterControllerService) {
		this.technologyAdapterControllerService = technologyAdapterControllerService;
	}

	/**
	 * Return TechnologyAdapter
	 * 
	 * @return
	 */
	public final TA getTechnologyAdapter() {
		return technologyAdapterControllerService.getServiceManager().getService(TechnologyAdapterService.class)
				.getTechnologyAdapter(getTechnologyAdapterClass());
	}

	/**
	 * Return TechnologyAdapter class
	 * 
	 * @return
	 */
	public abstract Class<TA> getTechnologyAdapterClass();

	/**
	 * Return the locales relative to this technology
	 * 
	 * @return
	 */
	public LocalizedDelegate getLocales() {
		return getTechnologyAdapter().getLocales();
	}

	/**
	 * Called to activate the {@link TechnologyAdapterController} We do it for all loaded modules. This means that all features and GUIs
	 * available with this technology adapter will be made available to module<br>
	 * 
	 * From a technical point of view, we first initialize inspectors and then actions
	 */
	public void activate() {
		if (getServiceManager() != null) {
			ModuleLoader moduleLoader = getServiceManager().getModuleLoader();
			if (moduleLoader != null) {
				for (FlexoModule<?> module : moduleLoader.getLoadedModuleInstances()) {
					activate(module);
				}
			}
			// Here we iterate on all technology browsers that have been built for this TechnologyAdapter
			// We just have initialized some new actions, that have to be reflected in already existing browsers
			for (FIBTechnologyBrowser<TA> b : technologyBrowsers) {
				b.initializeFIBComponent();
			}

			// initTechnologySpecificTypeEditors(getServiceManager().getTechnologyAdapterService());
		}
		isActivated = true;
	}

	/**
	 * Called to activate the {@link TechnologyAdapter}
	 */
	public void disactivate() {
		isActivated = false;
	}

	/**
	 * Called to activate the {@link TechnologyAdapterController} We do it for all loaded modules. This means that all features and GUIs
	 * available with this technology adapter will be made available to module<br>
	 * 
	 * From a technical point of view, we first initialize inspectors and then actions
	 */
	public void activate(FlexoModule<?> module) {
		FlexoController controller = module.getFlexoController();
		if (controller != null) {
			initializeInspectors(controller);
			initializeActions(controller.getControllerActionInitializer());
			if (module.activateAdvancedActions(getTechnologyAdapter())) {
				initializeAdvancedActions(controller.getControllerActionInitializer());
			}
			activateActivablePlugins(module);
		}
	}

	/**
	 * Called to activate the {@link TechnologyAdapter}
	 */
	public void disactivate(FlexoModule<?> module) {
	}

	private boolean isActivated = false;

	public boolean isActivated() {
		return isActivated;
	}

	/**
	 * Initialize actions for supplied module using supplied {@link ControllerActionInitializer}
	 * 
	 * @param actionInitializer
	 */
	protected abstract void initializeActions(ControllerActionInitializer actionInitializer);

	/**
	 * Overrides when required
	 * 
	 * @param actionInitializer
	 */
	public void initializeAdvancedActions(ControllerActionInitializer actionInitializer) {
	}

	/**
	 * Initialize inspectors for supplied module using supplied {@link FlexoController}
	 * 
	 * @param controller
	 */
	protected abstract void initializeInspectors(FlexoController controller);

	/**
	 * Return inspector group for this technology
	 * 
	 * @return
	 */
	public abstract InspectorGroup getTechnologyAdapterInspectorGroup();

	/**
	 * Return inspector group for FML technology
	 * 
	 * @return
	 */
	public InspectorGroup getFMLTechnologyAdapterInspectorGroup() {
		for (TechnologyAdapterController<?> tac : getTechnologyAdapterControllerService().getLoadedAdapterControllers()) {
			if (tac.getTechnologyAdapter() instanceof FMLTechnologyAdapter) {
				return tac.getTechnologyAdapterInspectorGroup();
			}
		}
		return null;
	}

	public ApplicationContext getServiceManager() {
		return (ApplicationContext) getTechnologyAdapter().getTechnologyAdapterService().getServiceManager();
	}

	/**
	 * Return icon representing underlying technology, required size is 32x32
	 * 
	 * @return
	 */
	public abstract ImageIcon getTechnologyBigIcon();

	/**
	 * Return icon representing underlying technology, required size is 16x16
	 * 
	 * @return
	 */
	public abstract ImageIcon getTechnologyIcon();

	/**
	 * Return icon representing a model of underlying technology
	 * 
	 * @return
	 */
	public abstract ImageIcon getModelIcon();

	/**
	 * Return icon representing a model of underlying technology
	 * 
	 * @return
	 */
	public abstract ImageIcon getMetaModelIcon();

	/**
	 * Return icon representing supplied {@link TechnologyObject}
	 * 
	 * @param object
	 * @return
	 */
	public ImageIcon getIconForTechnologyObject(TechnologyObject<?> object) {
		if (object != null) {
			return getIconForTechnologyObject((Class<? extends TechnologyObject<?>>) object.getClass());
		}
		return null;
	}

	/**
	 * Return icon representing supplied {@link TechnologyObject} class
	 * 
	 * @param object
	 * @return
	 */
	public abstract ImageIcon getIconForTechnologyObject(Class<? extends TechnologyObject<?>> objectClass);

	/**
	 * Return icon representing supplied model slot class
	 * 
	 * @param object
	 * @return
	 */
	public ImageIcon getIconForModelSlot(Class<? extends ModelSlot<?>> modelSlotClass) {
		return getTechnologyIcon();
	}

	/**
	 * Return icon representing supplied flexo role class
	 * 
	 * @param object
	 * @return
	 */
	public abstract ImageIcon getIconForFlexoRole(Class<? extends FlexoRole<?>> flexoRoleClass);

	/**
	 * Return icon representing supplied edition action
	 * 
	 * @param object
	 * @return
	 */
	public ImageIcon getIconForEditionAction(Class<? extends EditionAction> editionActionClass) {

		if (AddFlexoConceptInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.DUPLICATE);
		}
		else if (CreateTopLevelVirtualModelInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON, IconLibrary.DUPLICATE);
		}
		else if (AddVirtualModelInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON, IconLibrary.DUPLICATE);
		}
		else if (AddClassInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CLASS_INSTANCE_ICON, IconLibrary.DUPLICATE);
		}
		else if (SelectFlexoConceptInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.IMPORT);
		}
		else if (SelectUniqueFlexoConceptInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.IMPORT);
		}
		else if (SelectVirtualModelInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON, IconLibrary.IMPORT);
		}
		else if (SelectUniqueVirtualModelInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON, IconLibrary.IMPORT);
		}
		else if (InitiateMatching.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.SYNC);
		}
		else if (MatchFlexoConceptInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.SYNC);
		}
		else if (FinalizeMatching.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.SYNC);
		}
		else if (AddToListAction.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.LIST_ICON, IconLibrary.POSITIVE_MARKER);
		}
		else if (RemoveFromListAction.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.LIST_ICON, IconLibrary.NEGATIVE_MARKER);
		}
		else if (DeleteAction.class.isAssignableFrom(editionActionClass)) {
			return FMLIconLibrary.DELETE_ICON;
		}
		else if (ConditionalAction.class.isAssignableFrom(editionActionClass)) {
			return FMLIconLibrary.CONDITIONAL_ACTION_ICON;
		}
		else if (IterationAction.class.isAssignableFrom(editionActionClass)) {
			return FMLIconLibrary.ITERATION_ACTION_ICON;
		}
		else if (ExpressionIterationAction.class.isAssignableFrom(editionActionClass)) {
			return FMLIconLibrary.ITERATION_ACTION_ICON;
		}
		else if (WhileAction.class.isAssignableFrom(editionActionClass)) {
			return FMLIconLibrary.ITERATION_ACTION_ICON;
		}
		else if (IncrementalIterationAction.class.isAssignableFrom(editionActionClass)) {
			return FMLIconLibrary.ITERATION_ACTION_ICON;
		}
		else if (ExpressionAction.class.isAssignableFrom(editionActionClass)) {
			return FMLIconLibrary.EXPRESSION_ACTION_ICON;
		}
		else if (LogAction.class.isAssignableFrom(editionActionClass)) {
			return FMLIconLibrary.LOG_ACTION_ICON;
		}
		else if (NotifyProgressAction.class.isAssignableFrom(editionActionClass)) {
			return FMLIconLibrary.NOTIFY_PROGRESS_ACTION_ICON;
		}
		else if (NotifyPropertyChangedAction.class.isAssignableFrom(editionActionClass)) {
			return FMLIconLibrary.NOTIFY_PROPERTY_CHANGED_ACTION_ICON;
		}
		else if (FireEventAction.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.FLEXO_EVENT_ICON, IconLibrary.NEW_MARKER);
		}
		return null;

	}

	/**
	 * Return icon representing supplied edition scheme
	 * 
	 * @param object
	 * @return
	 */
	public ImageIcon getIconForFlexoBehaviour(Class<? extends FlexoBehaviour> flexoBehaviourClass) {
		return null;
	}

	public abstract String getWindowTitleforObject(TechnologyObject<TA> object, FlexoController controller);

	/**
	 * Return boolean indicating if this TechnologyAdapter controller service support ModuleView rendering for supplied technology object
	 * 
	 * @param object
	 * @return
	 */
	// public abstract boolean hasModuleViewForObject(TechnologyObject<TA> object, FlexoController controller,
	// FlexoPerspective perspective);

	/**
	 * Return a boolean indicating if this {@link FlexoPerspective} handles supplied object by defining a {@link ModuleView} in which
	 * supplied object is representable either as a master object or as an object representable in related view
	 * 
	 * @param object
	 * @return
	 */
	public abstract boolean isRepresentableInModuleView(TechnologyObject<TA> object);

	/**
	 * Return {@link FlexoObject} for which this perspective defines a {@link ModuleView} where supplied object is also representable
	 * 
	 * @param object
	 * @return
	 */
	public abstract FlexoObject getRepresentableMasterObject(TechnologyObject<TA> object);

	/**
	 * Return a newly created {@link ModuleView} for supplied technology object, when supported rendering
	 * 
	 * @param object
	 * @return
	 */
	public abstract ModuleView<?> createModuleViewForMasterObject(TechnologyObject<TA> object, FlexoController controller,
			FlexoPerspective perspective);

	public Resource getFIBPanelForObject(Object anObject) {
		if (anObject != null) {
			return getFIBPanelForClass(anObject.getClass());
		}
		return null;
	}

	private final Map<Class<?>, Resource> fibPanelsForClasses = new HashMap<Class<?>, Resource>() {
		@Override
		public Resource get(Object key) {
			if (containsKey(key)) {
				return super.get(key);
			}
			if (key instanceof Class) {
				Class<?> aClass = (Class<?>) key;
				// System.out.println("Searching FIBPanel for " + aClass);
				if (aClass.getAnnotation(org.openflexo.gina.annotation.FIBPanel.class) != null) {
					// System.out.println("Found annotation " + aClass.getAnnotation(FIBPanel.class));
					String fibPanelName = aClass.getAnnotation(org.openflexo.gina.annotation.FIBPanel.class).value();
					// System.out.println("fibPanelFile=" + fibPanel);
					Resource fibLocation = ResourceLocator.locateResource(fibPanelName);
					if (fibLocation != null) {
						// logger.info("Found " + fibPanel);
						put(aClass, fibLocation);
						return fibLocation;
					}
				}
				put(aClass, null);
				return null;
			}
			return null;
		}
	};

	public Resource getFIBPanelForClass(Class<?> aClass) {
		return TypeUtils.objectForClass(aClass, fibPanelsForClasses);
	}

	// ***************************************************************
	// Management of FlexoProject natures
	// ***************************************************************

	public final boolean hasSpecificFlexoProjectNature(FlexoProject<?> project) {
		return getSpecificProjectNatures(project).size() > 0;
	}

	// Override when required
	public List<? extends ProjectNature> getSpecificProjectNatures(FlexoProject<?> project) {
		return Collections.emptyList();
	}

	// Override when required
	public ModuleView<FlexoProject<?>> createFlexoProjectModuleViewForSpecificNature(FlexoProject<?> project, ProjectNature<?> nature,
			FlexoController controller, FlexoPerspective perspective) {
		return null;
	}

	/**
	 * Internally stores all technology browsers that have been built by this {@link TechnologyAdapterController}
	 */
	private final List<FIBTechnologyBrowser<TA>> technologyBrowsers = new ArrayList<>();

	/**
	 * Make technology browser
	 * 
	 * @param controller
	 * @return
	 */
	public final FIBTechnologyBrowser<TA> makeTechnologyBrowser(FlexoController controller) {
		FIBTechnologyBrowser<TA> returned = buildTechnologyBrowser(controller);
		technologyBrowsers.add(returned);
		return returned;
	}

	/**
	 * Override when required
	 * 
	 * @param controller
	 * @return
	 */
	protected FIBTechnologyBrowser<TA> buildTechnologyBrowser(FlexoController controller) {
		return new FIBTechnologyBrowser<>(getTechnologyAdapter(), controller, getTechnologyAdapter().getLocales());
	}

	/**
	 * Factory method used to instantiate a technology-specific FIBWidget for a given {@link FlexoBehaviourParameter}<br>
	 * Provides a hook to specialize this method in a given technology
	 * 
	 * @param object
	 * @return
	 */
	public FIBWidget makeWidget(final WidgetContext object, FlexoBehaviourAction<?, ?, ?> action, FIBModelFactory fibModelFactory,
			String variableName, boolean[] expand) {
		if (object.getWidget() != null) {
			switch (object.getWidget()) {
				case TEXT_FIELD:
				case URI:
				case LOCALIZED_TEXT_FIELD:
					FIBTextField tf = fibModelFactory.newFIBTextField();
					tf.setName(object.getName() + "TextField");
					return tf;
				case TEXT_AREA:
					FIBTextArea ta = fibModelFactory.newFIBTextArea();
					ta.setName(object.getName() + "TextArea");
					ta.setValidateOnReturn(true); // Avoid too many ontologies manipulations
					ta.setUseScrollBar(true);
					ta.setHorizontalScrollbarPolicy(HorizontalScrollBarPolicy.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					ta.setVerticalScrollbarPolicy(VerticalScrollBarPolicy.VERTICAL_SCROLLBAR_AS_NEEDED);
					expand[1] = true;
					return ta;
				case DATE:
					FIBDate dateWidget = fibModelFactory.newFIBDate();
					dateWidget.setName(object.getName() + "Date");
					return dateWidget;
				case CHECKBOX:
					FIBCheckBox cb = fibModelFactory.newFIBCheckBox();
					cb.setName(object.getName() + "CheckBox");
					return cb;
				case INTEGER:
					FIBNumber number = fibModelFactory.newFIBNumber();
					number.setName(object.getName() + "Number");
					number.setNumberType(NumberType.IntegerType);
					expand[0] = false;
					return number;
				case FLOAT:
					FIBNumber numberF = fibModelFactory.newFIBNumber();
					numberF.setName(object.getName() + "Number");
					numberF.setNumberType(NumberType.DoubleType);
					expand[0] = false;
					return numberF;
				case DROPDOWN:
					FIBDropDown dropDown = fibModelFactory.newFIBDropDown();
					dropDown.setName(object.getName() + "DropDown");
					dropDown.setList(new DataBinding<List<?>>(variableName + "." + object.getWidgetDefinitionAccess() + ".listOfObjects"));
					return dropDown;
				case RADIO_BUTTON:
					FIBRadioButtonList rbList = fibModelFactory.newFIBRadioButtonList();
					rbList.setName(object.getName() + "FIBRadioButtonList");
					rbList.setList(new DataBinding<List<?>>(variableName + "." + object.getWidgetDefinitionAccess() + ".listOfObjects"));
					return rbList;
				case CHECKBOX_LIST:
					FIBCheckboxList cbList = fibModelFactory.newFIBCheckboxList();
					cbList.setName(object.getName() + "CheckboxList");
					cbList.setList(new DataBinding<List<?>>(variableName + "." + object.getWidgetDefinitionAccess() + ".listOfObjects"));
					cbList.setUseScrollBar(true);
					cbList.setHorizontalScrollbarPolicy(HorizontalScrollBarPolicy.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					cbList.setVerticalScrollbarPolicy(VerticalScrollBarPolicy.VERTICAL_SCROLLBAR_AS_NEEDED);
					expand[1] = true;
					return cbList;
				case CUSTOM_WIDGET:
					FIBLabel notFound = fibModelFactory.newFIBLabel("<not_found>");
					notFound.setName(object.getName() + "NotFound");
					return notFound;
				default:
					break;
			}
		}
		return null;
	}

	/**
	 * Provides a hook to handle specific {@link FlexoBehaviourParameter} for a given technology
	 * 
	 * @param availableParameterTypes
	 */
	public void appendSpecificFlexoBehaviourParameters(List<Class<? extends FlexoBehaviourParameter>> availableParameterTypes) {
	}

	protected CustomTypeEditor makeCustomTypeEditor(Class<? extends CustomType> typeClass) {
		return null;
	}

	public <T extends CustomType> CustomTypeEditor<T> getCustomTypeEditor(Class<T> typeClass) {
		CustomTypeEditor<T> returned = customTypeEditors.get(typeClass);
		if (returned == null) {
			returned = makeCustomTypeEditor(typeClass);
			customTypeEditors.put(typeClass, returned);
		}
		return returned;
	}

	public void addToTechnologyAdapterPlugins(TechnologyAdapterPluginController plugin) {
		plugins.add(plugin);
	}

	public void removeFromTechnologyAdapterPlugins(TechnologyAdapterPluginController plugin) {
		plugins.remove(plugin);
	}

	public void resourceLoading(TechnologyAdapterResource<?, TA> resource) {
	}

	public void resourceUnloaded(TechnologyAdapterResource<?, TA> resource) {
	}

	public ValidationModel getValidationModel(Class<? extends ResourceData<?>> resourceDataClass) {
		return null;
	}

	public ValidationReport getValidationReport(ResourceData<?> resourceData, boolean createWhenNotExistent) {
		return null;
	}

	public void activateActivablePlugins() {
		for (FlexoModule<?> flexoModule : getServiceManager().getModuleLoader().getLoadedModuleInstances()) {
			if (flexoModule.isActive()) {
				activateActivablePlugins(flexoModule);
			}
		}
	}

	public void activateActivablePlugins(FlexoModule<?> module) {
		FlexoController controller = module.getFlexoController();
		if (controller != null) {
			for (TechnologyAdapterPluginController<TA> plugin : plugins) {
				if (plugin.isActivable(module)) {
					plugin.activate(module);
				}
			}
		}
	}

	public <P extends TechnologyAdapterPluginController<?>> P getPlugin(Class<P> pluginClass) {
		for (TechnologyAdapterPluginController<TA> plugin : plugins) {
			if (pluginClass.isAssignableFrom(plugin.getClass())) {
				return (P) plugin;
			}
		}
		return null;
	}

	/**
	 * Return list of all activated TechnologyAdapter Plugins
	 * 
	 * @return
	 */
	public List<TechnologyAdapterPluginController<TA>> getPlugins() {
		return plugins;
	}

	public Class<? extends TechnologyAdapterPreferences<TA>> getPreferencesClass() {
		return null;
	}

}
