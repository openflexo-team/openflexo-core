package org.openflexo.fml.rt.controller;

import javax.swing.ImageIcon;

import org.openflexo.fml.rt.controller.action.ActionSchemeActionInitializer;
import org.openflexo.fml.rt.controller.action.CreateBasicVirtualModelInstanceInitializer;
import org.openflexo.fml.rt.controller.action.CreateViewInitializer;
import org.openflexo.fml.rt.controller.action.DeleteViewInitializer;
import org.openflexo.fml.rt.controller.action.DeleteVirtualModelInstanceInitializer;
import org.openflexo.fml.rt.controller.action.MoveViewInitializer;
import org.openflexo.fml.rt.controller.action.NavigationSchemeActionInitializer;
import org.openflexo.fml.rt.controller.action.SynchronizationSchemeActionInitializer;
import org.openflexo.fml.rt.controller.view.ViewModuleView;
import org.openflexo.fml.rt.controller.view.VirtualModelInstanceView;
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
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
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

	@Override
	public void initializeActions(ControllerActionInitializer actionInitializer) {

		// View library perspective
		new CreateViewInitializer(actionInitializer);
		new MoveViewInitializer(actionInitializer);

		new DeleteViewInitializer(actionInitializer);
		new CreateBasicVirtualModelInstanceInitializer(actionInitializer);
		new DeleteVirtualModelInstanceInitializer(actionInitializer);

		new ActionSchemeActionInitializer(actionInitializer);
		new SynchronizationSchemeActionInitializer(actionInitializer);
		new NavigationSchemeActionInitializer(actionInitializer);

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
	 * @return
	 */
	@Override
	public ImageIcon getTechnologyIcon() {
		return FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
	}

	/**
	 * Return icon representing a model of underlying technology
	 * 
	 * @return
	 */
	@Override
	public ImageIcon getModelIcon() {
		return FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
	}

	/**
	 * Return icon representing a model of underlying technology
	 * 
	 * @return
	 */
	@Override
	public ImageIcon getMetaModelIcon() {
		return FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
	}

	/**
	 * Return icon representing supplied ontology object
	 * 
	 * @param object
	 * @return
	 */
	@Override
	public ImageIcon getIconForTechnologyObject(Class<? extends TechnologyObject<FMLRTTechnologyAdapter>> objectClass) {
		if (View.class.isAssignableFrom(objectClass)) {
			return FMLRTIconLibrary.VIEW_ICON;
		} else if (VirtualModelInstance.class.isAssignableFrom(objectClass)) {
			return FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
		} else if (FlexoConceptInstance.class.isAssignableFrom(objectClass)) {
			return FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON;
		}
		return IconFactory.getImageIcon(FMLRTIconLibrary.OPENFLEXO_NOTEXT_16, IconLibrary.QUESTION);
	}

	/**
	 * Return icon representing supplied pattern role
	 * 
	 * @param object
	 * @return
	 */
	@Override
	public ImageIcon getIconForPatternRole(Class<? extends FlexoRole<?>> patternRoleClass) {
		if (FlexoConceptInstanceRole.class.isAssignableFrom(patternRoleClass)) {
			return FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON;
		}
		return null;
	}

	@Override
	public ImageIcon getIconForEditionAction(Class<? extends EditionAction<?, ?>> editionActionClass) {
		if (AddFlexoConceptInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.DUPLICATE);
		} else if (SelectFlexoConceptInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.IMPORT);
		} else if (DeleteAction.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.DELETE);
		}
		return super.getIconForEditionAction(editionActionClass);
	}

	@Override
	public ImageIcon getIconForFlexoBehaviour(Class<? extends FlexoBehaviour> flexoBehaviourClass) {
		if (ActionScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.ACTION_SCHEME_ICON);
		} else if (DeletionScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.DELETE_ICON);
		} else if (CreationScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.CREATION_SCHEME_ICON);
		} else if (NavigationScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.NAVIGATION_SCHEME_ICON);
		} else if (SynchronizationScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.SYNCHRONIZATION_SCHEME_ICON);
		} else if (CloningScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return IconFactory.getImageIcon(FMLIconLibrary.CLONING_SCHEME_ICON);
		}
		return super.getIconForFlexoBehaviour(flexoBehaviourClass);
	}

	@Override
	public boolean hasModuleViewForObject(TechnologyObject<FMLRTTechnologyAdapter> object, FlexoController controller) {

		if (object instanceof View) {
			return true;
		} else if (object instanceof VirtualModelInstance) {
			return true;
		} /*else if (object instanceof FlexoConceptInstance) {
			// NO module view yet
			}*/
		return false;
	}

	@Override
	public String getWindowTitleforObject(TechnologyObject<FMLRTTechnologyAdapter> object, FlexoController controller) {
		return object.toString();
	}

	/**
	 * Return a newly created ModuleView for supplied technology object, if this TechnologyAdapter controller service support ModuleView
	 * rendering
	 * 
	 * @param object
	 * @return
	 */
	@Override
	public ModuleView<?> createModuleViewForObject(TechnologyObject<FMLRTTechnologyAdapter> object, FlexoController controller,
			FlexoPerspective perspective) {

		if (object instanceof View) {
			View view = (View) object;
			return new ViewModuleView(view, controller, perspective);
		} else if (object instanceof VirtualModelInstance) {
			VirtualModelInstance vmi = (VirtualModelInstance) object;
			return new VirtualModelInstanceView(vmi, controller, perspective);
		} else if (object instanceof FlexoConceptInstance) {
			// NO module view yet
		}

		return new EmptyPanel<TechnologyObject<FMLRTTechnologyAdapter>>(controller, perspective, object);
	}

}
