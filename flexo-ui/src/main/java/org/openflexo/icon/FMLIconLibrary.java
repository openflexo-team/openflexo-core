/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.icon;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.antar.binding.TypeUtils;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CloningScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptConstraint;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.NavigationScheme;
import org.openflexo.foundation.fml.OntologicObjectRole;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.TechnologySpecificFlexoBehaviour;
import org.openflexo.foundation.fml.ViewPoint;
import org.openflexo.foundation.fml.ViewPointLocalizedDictionary;
import org.openflexo.foundation.fml.ViewPointObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.editionaction.AddToListAction;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.ConditionalAction;
import org.openflexo.foundation.fml.editionaction.DataPropertyAssertion;
import org.openflexo.foundation.fml.editionaction.DeclareFlexoRole;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.ExecutionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequestCondition;
import org.openflexo.foundation.fml.editionaction.FetchRequestIterationAction;
import org.openflexo.foundation.fml.editionaction.IterationAction;
import org.openflexo.foundation.fml.editionaction.ObjectPropertyAssertion;
import org.openflexo.foundation.fml.editionaction.RemoveFromListAction;
import org.openflexo.foundation.fml.inspector.CheckboxInspectorEntry;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.foundation.fml.inspector.FloatInspectorEntry;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
import org.openflexo.foundation.fml.inspector.IntegerInspectorEntry;
import org.openflexo.foundation.fml.inspector.TextAreaInspectorEntry;
import org.openflexo.foundation.fml.inspector.TextFieldInspectorEntry;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.MatchingCriteria;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.ImageIconResource;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.TechnologyAdapterController;

/**
 * Utility class containing all icons used in context of FML technology adapter
 * 
 * @author sylvain
 * 
 */
public class FMLIconLibrary extends IconLibrary {

	private static final Logger logger = Logger.getLogger(FMLIconLibrary.class.getPackage().getName());

	public static final ImageIconResource VIEWPOINT_LIBRARY_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/ViewPointLibrary.png"));

	public static final ImageIconResource VIEWPOINT_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/ViewPoint.png"));
	public static final ImageIconResource VIEWPOINT_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/ViewPoint_32x32.png"));
	public static final ImageIconResource VIEWPOINT_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/ViewPoint_64x64.png"));

	public static final ImageIconResource VIRTUAL_MODEL_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/VirtualModel.png"));
	public static final ImageIconResource VIRTUAL_MODEL_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/VirtualModel_32x32.png"));
	public static final ImageIconResource VIRTUAL_MODEL_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/VirtualModel_64x64.png"));

	public static final ImageIconResource MODEL_SLOT_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/ModelSlot.png"));
	public static final ImageIconResource MODEL_SLOT_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/ModelSlot32.png"));
	public static final ImageIconResource MODEL_SLOT_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/ModelSlot64.png"));

	public static final ImageIconResource FLEXO_CONCEPT_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/FlexoConcept.png"));
	public static final ImageIconResource FLEXO_CONCEPT_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/FlexoConcept_32x32.png"));
	public static final ImageIconResource FLEXO_CONCEPT_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/FlexoConcept_64x64.png"));

	public static final ImageIconResource FLEXO_ROLE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/FlexoRole.png"));
	public static final ImageIconResource FLEXO_ROLE_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/FlexoRole_32x32.png"));
	public static final ImageIconResource FLEXO_ROLE_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/FlexoRole_64x64.png"));

	public static final ImageIconResource FLEXO_BEHAVIOUR_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/FlexoBehaviour.png"));
	public static final ImageIconResource FLEXO_BEHAVIOUR_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/FlexoBehaviour_32x32.png"));
	public static final ImageIconResource FLEXO_BEHAVIOUR_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/FlexoBehaviour_64x64.png"));

	public static final ImageIconResource ACTION_SCHEME_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/ActionSchemeIcon.png"));
	public static final ImageIconResource SYNCHRONIZATION_SCHEME_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/SynchronizationSchemeIcon.png"));
	public static final ImageIconResource CLONING_SCHEME_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/CloningSchemeIcon.png"));
	public static final ImageIconResource CREATION_SCHEME_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/CreationSchemeIcon.png"));
	public static final ImageIconResource DELETION_SCHEME_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/DeletionSchemeIcon.png"));
	public static final ImageIconResource NAVIGATION_SCHEME_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/NavigationSchemeIcon.png"));
	public static final ImageIconResource FLEXO_CONCEPT_PARAMETER_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/ParameterIcon.png"));
	public static final ImageIconResource FLEXO_CONCEPT_ACTION_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/ActionIcon.png"));
	public static final ImageIconResource LOCALIZATION_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/LocalizationIcon.png"));
	public static final ImageIconResource UNKNOWN_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/UnknownIcon.gif"));

	public static final ImageIconResource DECLARE_PATTERN_ROLE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/DeclarePatternRoleIcon.png"));
	public static final ImageIconResource CONDITIONAL_ACTION_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/ConditionalActionIcon.png"));
	public static final ImageIconResource ITERATION_ACTION_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/IterationActionIcon.png"));
	public static final ImageIconResource CONSTRAINT_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/ConstraintIcon.png"));

	public static final ImageIconResource CHECKBOX_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/CheckBoxIcon.png"));
	public static final ImageIconResource LIST_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/Model/VPM/ListIcon.png"));
	public static final ImageIconResource SPINNER_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/SpinnerIcon.png"));
	public static final ImageIconResource TEXT_AREA_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/TextAreaIcon.png"));
	public static final ImageIconResource TEXT_FIELD_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/TextFieldIcon.png"));

	public static final IconMarker MODEL_SLOT_ICON_MARKER = new IconMarker(new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/ModelSlot.png")), 2, 6);

	public static ImageIcon iconForObject(ViewPointObject object) {

		if (object instanceof FlexoRole && ((FlexoRole) object).getModelSlot() != null) {
			TechnologyAdapterController<?> tac = getTechnologyAdapterController(((FlexoRole) object).getModelSlot().getTechnologyAdapter());
			if (tac != null) {
				return tac.getIconForPatternRole((Class<? extends FlexoRole<?>>) object.getClass());
			}
		}
		if (object instanceof ViewPoint) {
			return VIEWPOINT_ICON;
		} else if (object instanceof VirtualModel) {
			return VIRTUAL_MODEL_ICON;
		} else if (object instanceof ModelSlot) {
			TechnologyAdapterController<?> tac = getTechnologyAdapterController(((ModelSlot) object).getTechnologyAdapter());
			if (tac != null) {
				return IconFactory.getImageIcon(tac.getTechnologyIcon(), MODEL_SLOT_ICON_MARKER);
			}
			return MODEL_SLOT_ICON;
		} else if (object instanceof FlexoConceptInspector) {
			return INSPECT_ICON;
		} else if (object instanceof FlexoConceptConstraint) {
			return CONSTRAINT_ICON;
		} else if (object instanceof FetchRequestCondition) {
			return CONSTRAINT_ICON;
		} else if (object instanceof MatchingCriteria) {
			return CONSTRAINT_ICON;
		} else if (object instanceof DataPropertyAssertion) {
			return FlexoController.statelessIconForTechnologyObject(((DataPropertyAssertion) object).getOntologyProperty());
		} else if (object instanceof ObjectPropertyAssertion) {
			return FlexoController.statelessIconForTechnologyObject(((ObjectPropertyAssertion) object).getOntologyProperty());
		} else if (object instanceof EditionAction) {
			if (object instanceof AddFlexoConceptInstance) {
				return IconFactory.getImageIcon(FLEXO_CONCEPT_ICON, IconLibrary.DUPLICATE);
			} else if (object instanceof SelectFlexoConceptInstance) {
				return IconFactory.getImageIcon(FLEXO_CONCEPT_ICON, IconLibrary.IMPORT);
			} else if (object instanceof MatchFlexoConceptInstance) {
				return IconFactory.getImageIcon(FLEXO_CONCEPT_ICON, IconLibrary.SYNC);
			} else if (object instanceof AddToListAction) {
				return IconFactory.getImageIcon(LIST_ICON, IconLibrary.POSITIVE_MARKER);
			} else if (object instanceof RemoveFromListAction) {
				return IconFactory.getImageIcon(LIST_ICON, IconLibrary.NEGATIVE_MARKER);
			} else if (object instanceof DeclareFlexoRole) {
				return DECLARE_PATTERN_ROLE_ICON;
			} else if (object instanceof AssignationAction) {
				return DECLARE_PATTERN_ROLE_ICON;
			} else if (object instanceof ExecutionAction) {
				return ACTION_SCHEME_ICON;
			} else if (object instanceof ConditionalAction) {
				return CONDITIONAL_ACTION_ICON;
			} else if (object instanceof IterationAction) {
				return ITERATION_ACTION_ICON;
			} else if (object instanceof FetchRequestIterationAction) {
				return ITERATION_ACTION_ICON;
			} else if (object instanceof DeleteAction) {
				FlexoRole pr = ((DeleteAction) object).getFlexoRole();
				if (pr != null) {
					ImageIcon baseIcon = iconForObject(pr);
					return IconFactory.getImageIcon(baseIcon, DELETE);
				}
				return DELETE_ICON;
			} else if (((EditionAction) object).getModelSlot() != null) {
				TechnologyAdapterController<?> tac = getTechnologyAdapterController(((EditionAction) object).getModelSlot()
						.getTechnologyAdapter());
				if (tac != null) {
					ImageIcon returned = tac.getIconForEditionAction((Class<? extends EditionAction<?, ?>>) object.getClass());
					if (returned != null) {
						return returned;
					} else {
						return tac.getTechnologyIcon();
					}
				}
			}
			return UNKNOWN_ICON;
		} else if (object instanceof FlexoConcept) {
			return FLEXO_CONCEPT_ICON;
		} else if (object instanceof FlexoBehaviourParameter) {
			return FLEXO_CONCEPT_PARAMETER_ICON;
		} else if (object instanceof FlexoBehaviour) {
			if (object instanceof ActionScheme) {
				return ACTION_SCHEME_ICON;
			} else if (object instanceof SynchronizationScheme) {
				return IconFactory.getImageIcon(VIRTUAL_MODEL_ICON, IconLibrary.SYNC);
			} else if (object instanceof CloningScheme) {
				return CLONING_SCHEME_ICON;
			} else if (object instanceof CreationScheme) {
				return CREATION_SCHEME_ICON;
			} else if (object instanceof NavigationScheme) {
				return NAVIGATION_SCHEME_ICON;
			} else if (object instanceof DeletionScheme) {
				return DELETION_SCHEME_ICON;
			} else if (object instanceof TechnologySpecificFlexoBehaviour) {
				TechnologyAdapterController<?> tac = getTechnologyAdapterController(((TechnologySpecificFlexoBehaviour) object)
						.getTechnologyAdapter());
				if (tac != null) {
					ImageIcon returned = tac.getIconForFlexoBehaviour((Class<? extends FlexoBehaviour>) object.getClass());
					if (returned != null) {
						return returned;
					} else {
						return tac.getTechnologyIcon();
					}
				}
			}
		} else if (object instanceof FlexoConceptInstanceRole) {
			return FLEXO_CONCEPT_ICON;
		} else if (object instanceof PrimitiveRole) {
			return UNKNOWN_ICON;
		} else if (object instanceof OntologicObjectRole && ((OntologicObjectRole<?>) object).getModelSlot() != null) {
			TechnologyAdapterController<?> tac = getTechnologyAdapterController(((OntologicObjectRole<?>) object).getModelSlot()
					.getTechnologyAdapter());
			if (tac != null) {
				Type accessedType = ((OntologicObjectRole<?>) object).getType();
				Class accessedTypeBaseClass = TypeUtils.getBaseClass(accessedType);
				return tac.getIconForTechnologyObject(accessedTypeBaseClass);
			}
		} else if (object instanceof ViewPointLocalizedDictionary) {
			return LOCALIZATION_ICON;
		} else if (object instanceof InspectorEntry) {
			if (object instanceof CheckboxInspectorEntry) {
				return CHECKBOX_ICON;
			} else if (object instanceof FloatInspectorEntry) {
				return SPINNER_ICON;
			} else if (object instanceof IntegerInspectorEntry) {
				return SPINNER_ICON;
			} else if (object instanceof TextAreaInspectorEntry) {
				return TEXT_AREA_ICON;
			} else if (object instanceof TextFieldInspectorEntry) {
				return TEXT_FIELD_ICON;
			}
		} else if (object instanceof TechnologyObject) {
			return FlexoController.statelessIconForTechnologyObject((TechnologyObject<?>) object);
		}
		logger.warning("No icon for " + object.getClass());
		return UNKNOWN_ICON;
	}

	public static ImageIcon iconForObject(ViewPointResource object) {
		return VIEWPOINT_ICON;
	}

	public static ImageIcon iconForObject(VirtualModelResource object) {
		return VIRTUAL_MODEL_ICON;
	}

	public static ImageIcon iconForModelSlot(TechnologyAdapter ta) {
		TechnologyAdapterController<?> tac = getTechnologyAdapterController(ta);
		if (tac != null) {
			return IconFactory.getImageIcon(tac.getTechnologyIcon(), MODEL_SLOT_ICON_MARKER);
		}
		return MODEL_SLOT_ICON;
	}

}
