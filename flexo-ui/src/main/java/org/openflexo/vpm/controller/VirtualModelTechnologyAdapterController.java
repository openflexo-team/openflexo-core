package org.openflexo.vpm.controller;

import javax.swing.ImageIcon;

import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.viewpoint.EditionPattern;
import org.openflexo.foundation.viewpoint.EditionPatternInstancePatternRole;
import org.openflexo.foundation.viewpoint.PatternRole;
import org.openflexo.foundation.viewpoint.ViewPoint;
import org.openflexo.foundation.viewpoint.VirtualModel;
import org.openflexo.foundation.viewpoint.VirtualModelTechnologyAdapter;
import org.openflexo.foundation.viewpoint.editionaction.AddEditionPatternInstance;
import org.openflexo.foundation.viewpoint.editionaction.DeleteAction;
import org.openflexo.foundation.viewpoint.editionaction.EditionAction;
import org.openflexo.foundation.viewpoint.editionaction.SelectEditionPatternInstance;
import org.openflexo.icon.IconFactory;
import org.openflexo.icon.IconLibrary;
import org.openflexo.icon.VEIconLibrary;
import org.openflexo.view.EmptyPanel;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.ControllerActionInitializer;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterController;
import org.openflexo.view.controller.model.FlexoPerspective;
import org.openflexo.vpm.view.StandardEditionPatternView;
import org.openflexo.vpm.view.ViewPointView;
import org.openflexo.vpm.view.VirtualModelView;

public class VirtualModelTechnologyAdapterController extends TechnologyAdapterController<VirtualModelTechnologyAdapter> {

	@Override
	public Class<VirtualModelTechnologyAdapter> getTechnologyAdapterClass() {
		return VirtualModelTechnologyAdapter.class;
	}

	@Override
	public void initializeActions(ControllerActionInitializer actionInitializer) {
	}

	@Override
	public ImageIcon getTechnologyBigIcon() {
		// TODO
		return VEIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
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
	public ImageIcon getIconForTechnologyObject(Class<? extends TechnologyObject> objectClass) {
		return null;
	}

	/**
	 * Return icon representing supplied pattern role
	 * 
	 * @param object
	 * @return
	 */
	@Override
	public ImageIcon getIconForPatternRole(Class<? extends PatternRole<?>> patternRoleClass) {
		if (EditionPatternInstancePatternRole.class.isAssignableFrom(patternRoleClass)) {
			return VEIconLibrary.EDITION_PATTERN_INSTANCE_ICON;
		}
		return null;
	}

	@Override
	public ImageIcon getIconForEditionAction(Class<? extends EditionAction<?, ?>> editionActionClass) {
		if (AddEditionPatternInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(VEIconLibrary.EDITION_PATTERN_INSTANCE_ICON, IconLibrary.DUPLICATE);
		} else if (SelectEditionPatternInstance.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(VEIconLibrary.EDITION_PATTERN_INSTANCE_ICON, IconLibrary.IMPORT);
		} else if (DeleteAction.class.isAssignableFrom(editionActionClass)) {
			return IconFactory.getImageIcon(VEIconLibrary.EDITION_PATTERN_INSTANCE_ICON, IconLibrary.DELETE);
		}
		return super.getIconForEditionAction(editionActionClass);
	}

	@Override
	public boolean hasModuleViewForObject(TechnologyObject<VirtualModelTechnologyAdapter> object, FlexoController controller) {
		if (object instanceof ViewPoint || object instanceof EditionPattern) {
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
		if (object instanceof EditionPattern) {
			EditionPattern ep = (EditionPattern) object;
			if (ep instanceof VirtualModel) {
				// if (ep instanceof DiagramSpecification) {
				// return new DiagramSpecificationView(ep, (VPMController) controller);
				// } else {
				return new VirtualModelView(ep, controller, perspective);
				// }
			} else {
				// if (ep.getVirtualModel() instanceof DiagramSpecification) {
				// return new DiagramEditionPatternView(ep, (VPMController) controller);
				// } else {
				return new StandardEditionPatternView(ep, controller, perspective);
				// }
			}

		}

		/*if (object instanceof ViewPoint) {
		return new ViewPointView((ViewPoint) object, controller);
		}
		if (object instanceof EditionPattern) {
		EditionPattern ep = (EditionPattern) object;
		if (ep instanceof VirtualModel) {
			//if (ep instanceof DiagramSpecification) {
			//	return new DiagramSpecificationView(ep, (VPMController) controller);
			//} else {
			return new VirtualModelView(ep, (VPMController) controller);
			// }
		} else {
			// if (ep.getVirtualModel() instanceof DiagramSpecification) {
			//	return new DiagramEditionPatternView(ep, (VPMController) controller);
			// } else {
			return new StandardEditionPatternView(ep, (VPMController) controller);
			// }
		}

		}*/

		return new EmptyPanel<TechnologyObject<VirtualModelTechnologyAdapter>>(controller, perspective, object);
	}

}
