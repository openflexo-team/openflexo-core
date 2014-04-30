package org.openflexo.vpm.controller;

import javax.swing.ImageIcon;

import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.view.FlexoConceptInstance;
import org.openflexo.foundation.view.View;
import org.openflexo.foundation.view.VirtualModelInstance;
import org.openflexo.foundation.viewpoint.ActionScheme;
import org.openflexo.foundation.viewpoint.CloningScheme;
import org.openflexo.foundation.viewpoint.CreationScheme;
import org.openflexo.foundation.viewpoint.DeletionScheme;
import org.openflexo.foundation.viewpoint.FlexoBehaviour;
import org.openflexo.foundation.viewpoint.FlexoBehaviourParameter;
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.FlexoConceptInstanceRole;
import org.openflexo.foundation.viewpoint.FlexoRole;
import org.openflexo.foundation.viewpoint.NavigationScheme;
import org.openflexo.foundation.viewpoint.SynchronizationScheme;
import org.openflexo.foundation.viewpoint.ViewPoint;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.VirtualModelTechnologyAdapter;
import org.openflexo.foundation.viewpoint.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.viewpoint.editionaction.DeleteAction;
import org.openflexo.foundation.viewpoint.editionaction.EditionAction;
import org.openflexo.foundation.viewpoint.editionaction.SelectFlexoConceptInstance;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.icon.VEIconLibrary;
import org.openflexo.icon.VPMIconLibrary;
import org.openflexo.view.EmptyPanel;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.model.FlexoPerspective;
import org.openflexo.vpm.view.StandardFlexoConceptView;
import org.openflexo.vpm.view.ViewPointView;
import org.openflexo.vpm.view.VirtualModelView;

public class VirtualModelTechnologyAdapterController extends TechnologyAdapterController<VirtualModelTechnologyAdapter> {

	@Override
	public Class<VirtualModelTechnologyAdapter> getTechnologyAdapterClass() {
		return VirtualModelTechnologyAdapter.class;
	}

	@Override
	public void initializeActions(ControllerActionInitializer actionInitializer) {
		// Add paste handlers

		actionInitializer.getEditingContext().registerPasteHandler(FlexoConcept.class, new FlexoConceptPasteHandler());
		actionInitializer.getEditingContext().registerPasteHandler(FlexoRole.class, new FlexoConceptPasteHandler());
		actionInitializer.getEditingContext().registerPasteHandler(EditionAction.class, new ActionContainerPasteHandler());
		actionInitializer.getEditingContext().registerPasteHandler(FlexoBehaviour.class, new ActionContainerPasteHandler());
		actionInitializer.getEditingContext().registerPasteHandler(FlexoBehaviourParameter.class, new FlexoBehaviourPasteHandler());
	}

	@Override
	public ImageIcon getTechnologyBigIcon() {
		return VEIconLibrary.VIRTUAL_MODEL_INSTANCE_MEDIUM_ICON;
	}

	/**
	 * Return icon representing underlying technology
	 * 
	 * @return
	 */
	@Override
	public ImageIcon getTechnologyIcon() {
		return VEIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
	}

	/**
	 * Return icon representing a model of underlying technology
	 * 
	 * @return
	 */
	@Override
	public ImageIcon getModelIcon() {
		return VEIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
	}

	/**
	 * Return icon representing a model of underlying technology
	 * 
	 * @return
	 */
	@Override
	public ImageIcon getMetaModelIcon() {
		return VEIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
	}

	/**
	 * Return icon representing supplied ontology object
	 * 
	 * @param object
	 * @return
	 */
	@Override
	public ImageIcon getIconForTechnologyObject(Class<? extends TechnologyObject<VirtualModelTechnologyAdapter>> objectClass) {
		if (ViewPoint.class.isAssignableFrom(objectClass)) {
			return VPMIconLibrary.VIEWPOINT_ICON;
		} else if (View.class.isAssignableFrom(objectClass)) {
			return VEIconLibrary.VIEW_ICON;
		} else if (VirtualModel.class.isAssignableFrom(objectClass)) {
			return VPMIconLibrary.VIRTUAL_MODEL_ICON;
		} else if (FlexoConcept.class.isAssignableFrom(objectClass)) {
			return VPMIconLibrary.FLEXO_CONCEPT_ICON;
		} else if (VirtualModelInstance.class.isAssignableFrom(objectClass)) {
			return VEIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
		} else if (FlexoConceptInstance.class.isAssignableFrom(objectClass)) {
			return VEIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON;
		}
		return IconFactory.getImageIcon(VEIconLibrary.OPENFLEXO_NOTEXT_16, IconLibrary.QUESTION);
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
			return VEIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON;
		}
		return null;
	}

	@Override
	public ImageIcon getIconForEditionAction(Class<? extends EditionAction<?, ?>> editionActionClass) {
		if (AddFlexoConceptInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(VEIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.DUPLICATE);
		} else if (SelectFlexoConceptInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(VEIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.IMPORT);
		} else if (DeleteAction.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(VEIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.DELETE);
		}
		return super.getIconForEditionAction(editionActionClass);
	}

	@Override
	public ImageIcon getIconForFlexoBehaviour(Class<? extends FlexoBehaviour> flexoBehaviourClass) {
		if (ActionScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return IconFactory.getImageIcon(VPMIconLibrary.ACTION_SCHEME_ICON);
		} else if (DeletionScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return IconFactory.getImageIcon(VPMIconLibrary.DELETE_ICON);
		} else if (CreationScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return IconFactory.getImageIcon(VPMIconLibrary.CREATION_SCHEME_ICON);
		} else if (NavigationScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return IconFactory.getImageIcon(VPMIconLibrary.NAVIGATION_SCHEME_ICON);
		} else if (SynchronizationScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return IconFactory.getImageIcon(VPMIconLibrary.SYNCHRONIZATION_SCHEME_ICON);
		} else if (CloningScheme.class.isAssignableFrom(flexoBehaviourClass)) {
			return IconFactory.getImageIcon(VPMIconLibrary.CLONING_SCHEME_ICON);
		}
		return super.getIconForFlexoBehaviour(flexoBehaviourClass);
	}

	@Override
	public boolean hasModuleViewForObject(TechnologyObject<VirtualModelTechnologyAdapter> object, FlexoController controller) {

		if (object instanceof ViewPoint || object instanceof FlexoConcept) {
			return true;
		}
		// TODO not applicable
		return false;
	}

	@Override
	public String getWindowTitleforObject(TechnologyObject<VirtualModelTechnologyAdapter> object, FlexoController controller) {
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
	public ModuleView<?> createModuleViewForObject(TechnologyObject<VirtualModelTechnologyAdapter> object, FlexoController controller,
			FlexoPerspective perspective) {
		if (object instanceof ViewPoint) {
			return new ViewPointView((ViewPoint) object, controller, perspective);
		}
		if (object instanceof FlexoConcept) {
			FlexoConcept ep = (FlexoConcept) object;
			if (ep instanceof VirtualModel) {
				// if (ep instanceof DiagramSpecification) {
				// return new DiagramSpecificationView(ep, (VPMController) controller);
				// } else {
				return new VirtualModelView(ep, controller, perspective);
				// }
			} else {
				// if (ep.getVirtualModel() instanceof DiagramSpecification) {
				// return new DiagramFlexoConceptView(ep, (VPMController) controller);
				// } else {
				return new StandardFlexoConceptView(ep, controller, perspective);
				// }
			}

		}

		/*if (object instanceof ViewPoint) {
		return new ViewPointView((ViewPoint) object, controller);
		}
		if (object instanceof FlexoConcept) {
		FlexoConcept ep = (FlexoConcept) object;
		if (ep instanceof VirtualModel) {
			//if (ep instanceof DiagramSpecification) {
			//	return new DiagramSpecificationView(ep, (VPMController) controller);
			//} else {
			return new VirtualModelView(ep, (VPMController) controller);
			// }
		} else {
			// if (ep.getVirtualModel() instanceof DiagramSpecification) {
			//	return new DiagramFlexoConceptView(ep, (VPMController) controller);
			// } else {
			return new StandardFlexoConceptView(ep, (VPMController) controller);
			// }
		}

		}*/

		return new EmptyPanel<TechnologyObject<VirtualModelTechnologyAdapter>>(controller, perspective, object);
	}

}
