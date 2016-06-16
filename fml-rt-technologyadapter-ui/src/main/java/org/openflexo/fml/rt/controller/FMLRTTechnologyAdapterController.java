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

import javax.swing.ImageIcon;

import org.openflexo.components.widget.FIBTechnologyBrowser;
import org.openflexo.fml.rt.controller.action.ActionSchemeActionInitializer;
import org.openflexo.fml.rt.controller.action.CreateBasicVirtualModelInstanceInitializer;
import org.openflexo.fml.rt.controller.action.CreateViewInitializer;
import org.openflexo.fml.rt.controller.action.DeleteViewInitializer;
import org.openflexo.fml.rt.controller.action.DeleteVirtualModelInstanceInitializer;
import org.openflexo.fml.rt.controller.action.MoveViewInitializer;
import org.openflexo.fml.rt.controller.action.NavigationSchemeActionInitializer;
import org.openflexo.fml.rt.controller.action.OpenVirtualModelInstanceInitializer;
import org.openflexo.fml.rt.controller.action.SynchronizationSchemeActionInitializer;
import org.openflexo.fml.rt.controller.view.ViewModuleView;
import org.openflexo.fml.rt.controller.view.VirtualModelInstanceView;
import org.openflexo.fml.rt.controller.widget.FIBViewLibraryBrowser;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CloningScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.NavigationScheme;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.foundation.fml.rt.View;
import org.openflexo.foundation.fml.rt.ViewLibrary;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.AddSubView;
import org.openflexo.foundation.fml.rt.editionaction.AddVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectVirtualModelInstance;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
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

public class FMLRTTechnologyAdapterController extends TechnologyAdapterController<FMLRTTechnologyAdapter> {

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

		// View library perspective
		new CreateViewInitializer(actionInitializer);
		new MoveViewInitializer(actionInitializer);

		new DeleteViewInitializer(actionInitializer);
		new CreateBasicVirtualModelInstanceInitializer(actionInitializer);
		new DeleteVirtualModelInstanceInitializer(actionInitializer);

		new ActionSchemeActionInitializer(actionInitializer);
		new SynchronizationSchemeActionInitializer(actionInitializer);
		new NavigationSchemeActionInitializer(actionInitializer);

		new OpenVirtualModelInstanceInitializer(actionInitializer);

		// Add paste handlers
		actionInitializer.getEditingContext().registerPasteHandler(new VirtualModelInstancePasteHandler());
	}

	@Override
	public ImageIcon getTechnologyBigIcon() {
		return FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_MEDIUM_ICON;
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
		if (View.class.isAssignableFrom(objectClass)) {
			return FMLRTIconLibrary.VIEW_ICON;
		}
		else if (VirtualModelInstance.class.isAssignableFrom(objectClass)) {
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
		if (AddVirtualModelInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON, IconLibrary.DUPLICATE);
		}
		if (AddSubView.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.VIEW_ICON, IconLibrary.DUPLICATE);
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
			return IconFactory.getImageIcon(FMLIconLibrary.ACTION_SCHEME_ICON);
		}
		else if (DeletionScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.DELETE_ICON);
		}
		else if (CreationScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.CREATION_SCHEME_ICON);
		}
		else if (NavigationScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.NAVIGATION_SCHEME_ICON);
		}
		else if (SynchronizationScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.SYNCHRONIZATION_SCHEME_ICON);
		}
		else if (CloningScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.CLONING_SCHEME_ICON);
		}
		return super.getIconForFlexoBehaviour(flexoBehaviourClass);
	}

	@Override
	public boolean hasModuleViewForObject(TechnologyObject<FMLRTTechnologyAdapter> object, FlexoController controller) {

		if (object instanceof View) {
			return true;
		}
		else if (object instanceof VirtualModelInstance) {
			return true;
		} /*else if (object instanceof FlexoConceptInstance) {
			// NO module view yet
			}*/
		return false;
	}

	@Override
	public String getWindowTitleforObject(TechnologyObject<FMLRTTechnologyAdapter> object, FlexoController controller) {
		if (object instanceof ViewLibrary) {
			return getLocales().localizedForKey("view_library");
		}
		if (object instanceof VirtualModelInstance) {
			return ((VirtualModelInstance) object).getTitle();
		}
		if (object instanceof View) {
			return ((View) object).getName();
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
	public ModuleView<?> createModuleViewForObject(TechnologyObject<FMLRTTechnologyAdapter> object, FlexoController controller,
			FlexoPerspective perspective) {

		if (object instanceof View) {
			View view = (View) object;
			return new ViewModuleView(view, controller, perspective);
		}
		else if (object instanceof VirtualModelInstance) {
			VirtualModelInstance vmi = (VirtualModelInstance) object;
			return new VirtualModelInstanceView(vmi, controller, perspective);
		}
		else if (object instanceof FlexoConceptInstance) {
			// NO module view yet
		}

		return new EmptyPanel<TechnologyObject<FMLRTTechnologyAdapter>>(controller, perspective, object);
	}

	@Override
	protected FIBTechnologyBrowser<FMLRTTechnologyAdapter> buildTechnologyBrowser(FlexoController controller) {
		return new FIBViewLibraryBrowser(getTechnologyAdapter(), controller);
	}

}
