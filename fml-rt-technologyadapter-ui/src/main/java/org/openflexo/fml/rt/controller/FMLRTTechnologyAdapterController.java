/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Fml-rt-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.rt.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.components.widget.FIBTechnologyBrowser;
import org.openflexo.fml.rt.controller.action.ActionSchemeActionInitializer;
import org.openflexo.fml.rt.controller.action.CreateBasicVirtualModelInstanceInitializer;
import org.openflexo.fml.rt.controller.action.CreateFlexoConceptInstanceInitializer;
import org.openflexo.fml.rt.controller.action.DeleteVirtualModelInstanceInitializer;
import org.openflexo.fml.rt.controller.action.MoveVirtualModelInstanceInitializer;
import org.openflexo.fml.rt.controller.action.NavigationSchemeActionInitializer;
import org.openflexo.fml.rt.controller.action.OpenVirtualModelInstanceInitializer;
import org.openflexo.fml.rt.controller.action.SynchronizationSchemeActionInitializer;
import org.openflexo.fml.rt.controller.validation.FMLRTValidateActionizer;
import org.openflexo.fml.rt.controller.view.VirtualModelInstanceView;
import org.openflexo.fml.rt.controller.widget.FIBVirtualModelInstanceRepositoriesBrowser;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CloningScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.EventListener;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.NavigationScheme;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.editionaction.AddClassInstance;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTValidationModel;
import org.openflexo.foundation.fml.rt.FMLRTValidationReport;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstance;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceRepository;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.AddVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.CreateTopLevelVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.ExecuteFML;
import org.openflexo.foundation.fml.rt.editionaction.ExecuteFlexoBehaviour;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectVirtualModelInstance;
import org.openflexo.foundation.fml.rt.rm.AbstractVirtualModelInstanceResource;
import org.openflexo.foundation.resource.ResourceData;
import org.openflexo.foundation.task.Progress;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterResource;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.gina.utils.InspectorGroup;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.icon.FMLRTIconLibrary;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationModel;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.view.EmptyPanel;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.TechnologyAdapterPluginController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * Technology-specific controller provided by {@link FMLRTTechnologyAdapter}<br>
 * 
 * @author sylvain
 *
 */
public class FMLRTTechnologyAdapterController extends TechnologyAdapterController<FMLRTTechnologyAdapter> {

	static final Logger logger = Logger.getLogger(FlexoController.class.getPackage().getName());

	private FMLRTValidationModel validationModel;
	private Map<VirtualModelInstance, FMLRTValidationReport> validationReports = new HashMap<>();

	@Override
	public Class<FMLRTTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLRTTechnologyAdapter.class;
	}

	/**
	 * Initialize inspectors for supplied module using supplied {@link FlexoController}
	 * 
	 * @param controller
	 */
	@Override
	protected void initializeInspectors(FlexoController controller) {

		fmlRTInspectorGroup = controller.loadInspectorGroup("FML-RT", getTechnologyAdapter().getLocales(),
				getFMLTechnologyAdapterInspectorGroup());
		// actionInitializer.getController().getModuleInspectorController()
		// .loadDirectory(ResourceLocator.locateResource("src/main/resources/Inspectors/Fiacre"));
	}

	private InspectorGroup fmlRTInspectorGroup;

	/**
	 * Return inspector group for this technology
	 * 
	 * @return
	 */
	@Override
	public InspectorGroup getTechnologyAdapterInspectorGroup() {
		return fmlRTInspectorGroup;
	}

	@Override
	protected void initializeActions(ControllerActionInitializer actionInitializer) {

		// FMLRTVirtualModelInstance

		new FMLRTValidateActionizer(this, actionInitializer);

		new CreateBasicVirtualModelInstanceInitializer(actionInitializer);
		new DeleteVirtualModelInstanceInitializer(actionInitializer);
		new MoveVirtualModelInstanceInitializer(actionInitializer);

		new CreateFlexoConceptInstanceInitializer(actionInitializer);

		new ActionSchemeActionInitializer(actionInitializer);
		new SynchronizationSchemeActionInitializer(actionInitializer);
		new NavigationSchemeActionInitializer(actionInitializer);

		new OpenVirtualModelInstanceInitializer(actionInitializer);

		// Add paste handlers
		actionInitializer.getEditingContext().registerPasteHandler(new VirtualModelInstancePasteHandler());
	}

	@Override
	public ImageIcon getTechnologyBigIcon() {
		return FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_BIG_ICON;
	}

	/**
	 * Return icon representing underlying technology
	 * 
	 * @return ImageIcon representing underlying technology
	 */
	@Override
	public ImageIcon getTechnologyIcon() {
		return FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
	}

	/**
	 * Return icon representing a model of underlying technology
	 * 
	 * @return ImageIcon representing a model of underlying technology
	 */
	@Override
	public ImageIcon getModelIcon() {
		return FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
	}

	/**
	 * Return icon representing a meta model of underlying technology
	 * 
	 * @return ImageIcon representing a meta model of underlying technology
	 */
	@Override
	public ImageIcon getMetaModelIcon() {
		return FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
	}

	/**
	 * Return icon representing supplied technology object
	 * 
	 * @param objectClass
	 * @return ImageIcon representing supplied technology object
	 */
	@Override
	public ImageIcon getIconForTechnologyObject(Class<? extends TechnologyObject<?>> objectClass) {
		if (FMLRTVirtualModelInstance.class.isAssignableFrom(objectClass)) {
			return FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
		}
		else if (FlexoConceptInstance.class.isAssignableFrom(objectClass)) {
			return FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON;
		}
		return IconFactory.getImageIcon(FMLRTIconLibrary.OPENFLEXO_NOTEXT_16, IconLibrary.QUESTION);
	}

	/**
	 * Return icon representing supplied flexo role class
	 * 
	 * @param patternRoleClass
	 * @return ImageIcon representing supplied pattern property
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
		else if (SelectVirtualModelInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON, IconLibrary.IMPORT);
		}
		else if (DeleteAction.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.DELETE);
		}
		else if (ExecuteFlexoBehaviour.class.isAssignableFrom(editionActionClass)) {
			return FMLIconLibrary.FLEXO_BEHAVIOUR_ICON;
		}
		else if (ExecuteFML.class.isAssignableFrom(editionActionClass)) {
			return FMLIconLibrary.FLEXO_BEHAVIOUR_ICON;
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
	public boolean hasModuleViewForObject(TechnologyObject<FMLRTTechnologyAdapter> object, FlexoController controller,
			FlexoPerspective perspective) {

		for (TechnologyAdapterPluginController<?> plugin : getTechnologyAdapterControllerService().getActivatedPlugins()) {
			if (plugin.hasModuleViewForObject(object)) {
				return true;
			}
		}

		if (object instanceof FMLRTVirtualModelInstance) {
			return true;
		}
		return false;
	}*/

	@Override
	public boolean isRepresentableInModuleView(TechnologyObject<FMLRTTechnologyAdapter> object) {
		
		for (TechnologyAdapterPluginController<?> plugin : getTechnologyAdapterControllerService().getActivatedPlugins()) {
			if (plugin.isRepresentableInModuleView(object)) {
				return true;
			}
		}

		if (object instanceof FMLRTVirtualModelInstance) {
			return true;
		}
		return false;
	}
	
	@Override
	public FlexoObject getRepresentableMasterObject(TechnologyObject<FMLRTTechnologyAdapter> object) {
		for (TechnologyAdapterPluginController<?> plugin : getTechnologyAdapterControllerService().getActivatedPlugins()) {
			if (plugin.isRepresentableInModuleView(object)) {
				return getRepresentableMasterObject(object);
			}
		}

		if (object instanceof FMLRTVirtualModelInstance) {
			return object;
		}
		return null;
	}
	
	@Override
	public String getWindowTitleforObject(TechnologyObject<FMLRTTechnologyAdapter> object, FlexoController controller) {
		if (object instanceof FMLRTVirtualModelInstanceRepository) {
			return getLocales().localizedForKey("virtual_model_instance_repository");
		}
		if (object instanceof FMLRTVirtualModelInstance) {
			return ((FMLRTVirtualModelInstance) object).getTitle();
		}
		return object.toString();
	}

	/**
	 * Return a newly created ModuleView for supplied technology object, if this TechnologyAdapter controller service support ModuleView
	 * rendering
	 * 
	 * @param object
	 * @return newly created ModuleView for supplied technology object
	 */
	@Override
	public ModuleView<?> createModuleViewForMasterObject(TechnologyObject<FMLRTTechnologyAdapter> object, FlexoController controller,
			FlexoPerspective perspective) {

		for (TechnologyAdapterPluginController<?> plugin : getTechnologyAdapterControllerService().getActivatedPlugins()) {
			if (plugin.isRepresentableInModuleView(object)) {
				return plugin.createModuleViewForMasterObject(object, controller, perspective);
			}
		}

		if (object instanceof FMLRTVirtualModelInstance) {
			FMLRTVirtualModelInstance vmi = (FMLRTVirtualModelInstance) object;
			return new VirtualModelInstanceView(vmi, controller, perspective);
		}
		else if (object instanceof FlexoConceptInstance) {
			// NO module view yet
		}

		return new EmptyPanel<>(controller, perspective, object);
	}

	@Override
	protected FIBTechnologyBrowser<FMLRTTechnologyAdapter> buildTechnologyBrowser(FlexoController controller) {
		return new FIBVirtualModelInstanceRepositoriesBrowser(getTechnologyAdapter(), controller);
	}

	@Override
	public Class<FMLRTPreferences> getPreferencesClass() {
		return FMLRTPreferences.class;
	}

	public FMLRTValidationModel getFMLRTValidationModel() {
		if (validationModel == null) {
			validationModel = getServiceManager().getVirtualModelLibrary().getFMLRTValidationModel();
		}
		return validationModel;
	}

	@Override
	public ValidationModel getValidationModel(Class<? extends ResourceData<?>> resourceDataClass) {
		if (VirtualModelInstance.class.isAssignableFrom(resourceDataClass)) {
			return getFMLRTValidationModel();
		}
		return null;
	}

	@Override
	public ValidationReport getValidationReport(ResourceData<?> resourceData, boolean createWhenNotExistent) {
		if (resourceData instanceof VirtualModelInstance) {
			ValidationReport returned = validationReports.get(resourceData);
			if (returned == null && createWhenNotExistent) {
				return makeValidationReport((VirtualModelInstance) resourceData);
			}
			return returned;
		}
		return null;
	}

	private FMLRTValidationReport makeValidationReport(VirtualModelInstance<?, ?> vmi) {
		FMLRTValidationReport validationReport = null;
		try {
			if (logger.isLoggable(Level.INFO)) {
				logger.info("Validating virtual model instance " + vmi);
			}
			Progress.progress(getLocales().localizedForKey("validating_virtual_model_instance..."));
			validationReport = (FMLRTValidationReport) getFMLRTValidationModel().validate(vmi);
			validationReports.put(vmi, validationReport);
			if (logger.isLoggable(Level.INFO)) {
				logger.info("End validating virtual model instance " + vmi);
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

	@Override
	public void resourceLoading(TechnologyAdapterResource<?, FMLRTTechnologyAdapter> resource) {
		// logger.info("RESOURCE LOADED: " + resource);

		if (resource instanceof AbstractVirtualModelInstanceResource) {
			VirtualModelInstance vmi = (VirtualModelInstance) ((AbstractVirtualModelInstanceResource) resource).getLoadedResourceData();
			if (vmi != null) {
				makeValidationReport(vmi);
			}
		}
	}

	@Override
	public void resourceUnloaded(TechnologyAdapterResource<?, FMLRTTechnologyAdapter> resource) {
		logger.warning("RESOURCE UNLOADED not fully implemented: " + resource);

		if (resource instanceof AbstractVirtualModelInstanceResource) {
			VirtualModelInstance vmi = (VirtualModelInstance) ((AbstractVirtualModelInstanceResource) resource).getLoadedResourceData();
			if (vmi != null) {
				validationReports.remove(vmi);
			}
		}
	}

}
