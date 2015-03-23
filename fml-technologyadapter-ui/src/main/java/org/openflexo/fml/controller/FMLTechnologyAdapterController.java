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

import javax.swing.ImageIcon;

import org.openflexo.components.widget.FIBTechnologyBrowser;
import org.openflexo.fib.utils.InspectorGroup;
import org.openflexo.fml.controller.action.CreateEditionActionInitializer;
import org.openflexo.fml.controller.action.CreateFlexoBehaviourInitializer;
import org.openflexo.fml.controller.action.CreateFlexoConceptInitializer;
import org.openflexo.fml.controller.action.CreateFlexoRoleInitializer;
import org.openflexo.fml.controller.action.CreateModelSlotInitializer;
import org.openflexo.fml.controller.action.CreateViewPointInitializer;
import org.openflexo.fml.controller.action.CreateVirtualModelInitializer;
import org.openflexo.fml.controller.action.DeleteFlexoConceptInitializer;
import org.openflexo.fml.controller.action.DeleteViewPointInitializer;
import org.openflexo.fml.controller.action.DeleteVirtualModelInitializer;
import org.openflexo.fml.controller.action.DuplicateFlexoConceptInitializer;
import org.openflexo.fml.controller.action.ShowFMLRepresentationInitializer;
import org.openflexo.fml.controller.view.StandardFlexoConceptView;
import org.openflexo.fml.controller.view.ViewPointLocalizedDictionaryView;
import org.openflexo.fml.controller.view.ViewPointView;
import org.openflexo.fml.controller.view.VirtualModelView;
import org.openflexo.fml.controller.widget.FIBViewPointLibraryBrowser;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CloningScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.NavigationScheme;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewPointLocalizedDictionary;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
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
import org.openflexo.localization.FlexoLocalization;
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
		fmlInspectors = controller.loadInspectorGroup("FML", controller.getCoreInspectorGroup());
	}

	@Override
	public InspectorGroup getTechnologyAdapterInspectorGroup() {
		return fmlInspectors;
	}

	@Override
	protected void initializeActions(ControllerActionInitializer actionInitializer) {

		// ViewPoint perspective
		new CreateViewPointInitializer(actionInitializer);
		new DeleteViewPointInitializer(actionInitializer);
		new CreateModelSlotInitializer(actionInitializer);
		new CreateVirtualModelInitializer(actionInitializer);
		new DeleteVirtualModelInitializer(actionInitializer);

		new CreateFlexoRoleInitializer(actionInitializer);
		new CreateExpressionPropertyInitializer(actionInitializer);
		new CreateGetSetPropertyInitializer(actionInitializer);
		new CreateAbstractPropertyInitializer(actionInitializer);

		new CreateEditionActionInitializer(actionInitializer);
		new CreateFlexoBehaviourInitializer(actionInitializer);
		new CreateFlexoConceptInitializer(actionInitializer);
		new DeleteFlexoConceptInitializer(actionInitializer);
		new DuplicateFlexoConceptInitializer(actionInitializer);
		new ShowFMLRepresentationInitializer(actionInitializer);

		// Add paste handlers
		actionInitializer.getEditingContext().registerPasteHandler(new FlexoConceptPasteHandler());
		actionInitializer.getEditingContext().registerPasteHandler(new ActionContainerPasteHandler());
		actionInitializer.getEditingContext().registerPasteHandler(new FlexoBehaviourPasteHandler());
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
		if (ViewPoint.class.isAssignableFrom(objectClass)) {
			return FMLIconLibrary.VIEWPOINT_ICON;
		} else if (View.class.isAssignableFrom(objectClass)) {
			return FMLRTIconLibrary.VIEW_ICON;
		} else if (VirtualModel.class.isAssignableFrom(objectClass)) {
			return FMLIconLibrary.VIRTUAL_MODEL_ICON;
		} else if (FlexoConcept.class.isAssignableFrom(objectClass)) {
			return FMLIconLibrary.FLEXO_CONCEPT_ICON;
		} else if (VirtualModelInstance.class.isAssignableFrom(objectClass)) {
			return FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON;
		} else if (FlexoConceptInstance.class.isAssignableFrom(objectClass)) {
			return FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON;
		}
		return IconFactory.getImageIcon(IconLibrary.OPENFLEXO_NOTEXT_16, IconLibrary.QUESTION);
	}

	/**
	 * Return icon representing supplied pattern property
	 * 
	 * @param patternRoleClass
	 * @return icon representing supplied pattern property
	 */
	@Override
	public ImageIcon getIconForPatternRole(Class<? extends FlexoRole<?>> patternRoleClass) {
		if (FlexoConceptInstanceRole.class.isAssignableFrom(patternRoleClass)) {
			return FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON;
		}
		return null;
	}

	@Override
	public ImageIcon getIconForEditionAction(Class<? extends EditionAction> editionActionClass) {

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
	public boolean hasModuleViewForObject(TechnologyObject<FMLTechnologyAdapter> object, FlexoController controller) {

		if (object instanceof FlexoConcept || object instanceof ViewPointLocalizedDictionary) {
			return true;
		}
		// TODO not applicable
		return false;
	}

	@Override
	public String getWindowTitleforObject(TechnologyObject<FMLTechnologyAdapter> object, FlexoController controller) {
		/*if (object instanceof ViewPointLibrary) {
			return FlexoLocalization.localizedForKey("view_point_library");
		}*/
		if (object instanceof ViewPoint) {
			return ((ViewPoint) object).getName();
		}
		if (object instanceof VirtualModel) {
			return ((VirtualModel) object).getName();
		}
		if (object instanceof FlexoConcept) {
			return ((FlexoConcept) object).getName();
		}
		if (object instanceof ViewPointLocalizedDictionary) {
			return FlexoLocalization.localizedForKey("localized_dictionary_for") + " "
					+ ((ViewPointLocalizedDictionary) object).getViewPoint().getName();
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
		if (object instanceof ViewPoint) {
			return new ViewPointView((ViewPoint) object, controller, perspective);
		}
		if (object instanceof ViewPointLocalizedDictionary) {
			return new ViewPointLocalizedDictionaryView((ViewPointLocalizedDictionary) object, controller, perspective);
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

		return new EmptyPanel<TechnologyObject<FMLTechnologyAdapter>>(controller, perspective, object);
	}

	@Override
	protected FIBTechnologyBrowser<FMLTechnologyAdapter> makeTechnologyBrowser(FlexoController controller) {
		return new FIBViewPointLibraryBrowser(getTechnologyAdapter(), controller);
	}
}
