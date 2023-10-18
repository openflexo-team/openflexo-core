/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.icon;

import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.AbstractInvariant;
import org.openflexo.foundation.fml.AbstractProperty;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CloningScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.EventListener;
import org.openflexo.foundation.fml.ExpressionProperty;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceRole;
import org.openflexo.foundation.fml.FlexoEnum;
import org.openflexo.foundation.fml.FlexoEnumValue;
import org.openflexo.foundation.fml.FlexoEvent;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.GetProperty;
import org.openflexo.foundation.fml.GetSetProperty;
import org.openflexo.foundation.fml.JavaRole;
import org.openflexo.foundation.fml.NavigationScheme;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.SynchronizationScheme;
import org.openflexo.foundation.fml.TechnologySpecificFlexoBehaviour;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.ExpressionIterationAction;
import org.openflexo.foundation.fml.controlgraph.IncrementalIterationAction;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.controlgraph.WhileAction;
import org.openflexo.foundation.fml.editionaction.AbstractAssignationAction;
import org.openflexo.foundation.fml.editionaction.AddClassInstance;
import org.openflexo.foundation.fml.editionaction.AddToListAction;
import org.openflexo.foundation.fml.editionaction.DeleteAction;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.editionaction.FetchRequestCondition;
import org.openflexo.foundation.fml.editionaction.LogAction;
import org.openflexo.foundation.fml.editionaction.NotifyProgressAction;
import org.openflexo.foundation.fml.editionaction.NotifyPropertyChangedAction;
import org.openflexo.foundation.fml.editionaction.RemoveFromListAction;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.foundation.fml.inspector.InspectorEntry;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.FinalizeMatching;
import org.openflexo.foundation.fml.rt.editionaction.FireEventAction;
import org.openflexo.foundation.fml.rt.editionaction.InitiateMatching;
import org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.MatchingCriteria;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectVirtualModelInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.rm.ResourceLocator;
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

	public static final ImageIconResource FML_BIG_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FML/FML_64x64.png"));
	public static final ImageIconResource FML_ICON = new ImageIconResource(ResourceLocator.locateResource("Icons/FML/FML_16x16.png"));

	public static final ImageIconResource VIRTUAL_MODEL_LIBRARY_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/VirtualModelLibrary.png"));

	public static final ImageIconResource TECHNOLOGY_ADAPTER_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Common/TechnologyAdapter.png"));
	public static final ImageIconResource TECHNOLOGY_ADAPTER_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Common/TechnologyAdapter_32x32.png"));
	public static final ImageIconResource TECHNOLOGY_ADAPTER_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Common/TechnologyAdapter_64x64.png"));

	public static final ImageIconResource VIRTUAL_MODEL_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/VirtualModel_16x16.png"));
	public static final ImageIconResource VIRTUAL_MODEL_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/VirtualModel_32x32.png"));
	public static final ImageIconResource VIRTUAL_MODEL_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/VirtualModel_64x64.png"));
	public static final ImageIconResource VIRTUAL_MODEL_SMALL_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/VirtualModel_8x8.png"));

	public static final IconMarker VIRTUAL_MODEL_MARKER = new IconMarker(VIRTUAL_MODEL_SMALL_ICON, 6, 0);
	public static final IconMarker VIRTUAL_MODEL_BIG_MARKER = new IconMarker(VIRTUAL_MODEL_MEDIUM_ICON, 32, 32);

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

	public static final ImageIconResource FLEXO_EVENT_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/EventIcon.png"));
	public static final ImageIconResource FLEXO_EVENT_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/EventIcon_32x32.png"));
	public static final ImageIconResource FLEXO_EVENT_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/EventIcon_64x64.png"));

	public static final ImageIconResource FLEXO_ENUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/FlexoEnum_16x16.png"));
	public static final ImageIconResource FLEXO_ENUM_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/FlexoEnum_32x32.png"));
	public static final ImageIconResource FLEXO_ENUM_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/FlexoEnum_64x64.png"));

	public static final ImageIconResource FLEXO_ENUM_VALUE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/FlexoEnumValue_16x16.png"));
	public static final ImageIconResource FLEXO_ENUM_VALUE_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/FlexoEnumValue_32x32.png"));
	public static final ImageIconResource FLEXO_ENUM_VALUE_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/FlexoEnumValue_64x64.png"));

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
	public static final ImageIconResource CREATION_SCHEME_MEDIUM_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/CreationSchemeIcon_32x32.png"));
	public static final ImageIconResource CREATION_SCHEME_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/CreationSchemeIcon_64x64.png"));

	public static final ImageIconResource DELETION_SCHEME_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/DeletionSchemeIcon.png"));
	public static final ImageIconResource NAVIGATION_SCHEME_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/NavigationSchemeIcon.png"));
	public static final ImageIcon EVENT_LISTENER_ICON = IconFactory.getImageIcon(ACTION_SCHEME_ICON, IconLibrary.NOTIFY_MARKER);

	public static final ImageIconResource FLEXO_CONCEPT_PARAMETER_BIG_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/ParameterIcon_64x64.png"));
	public static final ImageIconResource FLEXO_CONCEPT_PARAMETER_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/ParameterIcon.png"));
	public static final ImageIconResource FLEXO_CONCEPT_ACTION_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/ActionIcon.png"));
	public static final ImageIconResource LOCALIZATION_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/LocalizationIcon.png"));
	public static final ImageIconResource UNKNOWN_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VE/UnknownIcon.gif"));

	public static final ImageIconResource JAVA_ROLE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/JavaRoleIcon.png"));
	public static final ImageIconResource STRING_PRIMITIVE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/StringPrimitiveIcon.gif"));
	public static final ImageIconResource MULTI_STRING_PRIMITIVE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/MultiStringPrimitiveIcon.gif"));
	public static final ImageIconResource INTEGER_PRIMITIVE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/IntegerPrimitiveIcon.gif"));
	public static final ImageIconResource DOUBLE_PRIMITIVE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/DoublePrimitiveIcon.gif"));
	public static final ImageIconResource BOOLEAN_PRIMITIVE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/BooleanPrimitiveIcon.gif"));
	public static final ImageIconResource DATE_PRIMITIVE_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/DatePrimitiveIcon.png"));

	public static final ImageIconResource EXPRESSION_ACTION_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/ExpressionActionIcon.png"));
	public static final ImageIconResource LOG_ACTION_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/LogActionIcon.png"));
	public static final ImageIconResource NOTIFY_PROGRESS_ACTION_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/NotifyProgressActionIcon.png"));
	public static final ImageIconResource NOTIFY_PROPERTY_CHANGED_ACTION_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/NotifyPropertyChangedActionIcon.png"));
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

	public static final ImageIconResource DATA_BINDING_ICON = new ImageIconResource(
			ResourceLocator.locateResource("Icons/Model/VPM/LinkIcon_16x16.png"));

	// Markers
	public static final IconMarker ABSTRACT_MARKER = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Model/VPM/Markers/Abstract.png")), 14, 0);
	public static final IconMarker EMPTY_MARKER = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Model/VPM/Markers/Empty.png")), 14, 0);
	public static final IconMarker OVERRIDES_MARKER = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Model/VPM/Markers/Overrides.png")), 14, 10);
	public static final IconMarker PROPERTY_MARKER = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Model/VPM/Markers/Property.png")), 8, 8);
	public static final IconMarker MODEL_SLOT_ICON_MARKER = new IconMarker(
			new ImageIconResource(ResourceLocator.locateResource("Icons/Model/VPM/ModelSlot.png")), 2, 6);

	public static final ImageIcon ABSTRACT_PROPERTY_ICON = IconFactory.getImageIcon(FLEXO_ROLE_ICON, ABSTRACT_MARKER);
	public static final ImageIcon EXPRESSION_PROPERTY_ICON = IconFactory.getImageIcon(EXPRESSION_ACTION_ICON, PROPERTY_MARKER);
	public static final ImageIcon GET_SET_PROPERTY_ICON = IconFactory.getImageIcon(FLEXO_BEHAVIOUR_ICON, PROPERTY_MARKER);

	public static ImageIcon iconForObject(FMLObject object) {

		ImageIcon returned = _iconForObject(object);
		if (object instanceof FlexoConcept) {
			if (((FlexoConcept) object).isAbstract()) {
				return IconFactory.getImageIcon(returned, ABSTRACT_MARKER);
			}
		}
		if (object instanceof FlexoBehaviour) {
			if (((FlexoBehaviour) object).isAbstract()) {
				return IconFactory.getImageIcon(returned, ABSTRACT_MARKER);
			}
			else if (((FlexoBehaviour) object).overrides()) {
				return IconFactory.getImageIcon(returned, OVERRIDES_MARKER);
			}
			else {
				return IconFactory.getImageIcon(returned, EMPTY_MARKER);
			}
		}
		if (object instanceof FlexoProperty) {
			if (((FlexoProperty) object).overrides()) {
				return IconFactory.getImageIcon(returned, OVERRIDES_MARKER);
			}
		}
		return returned;
	}

	private static ImageIcon _iconForObject(FMLObject object) {
		if (object == null) {
			return null;
		}

		if (object instanceof FMLCompilationUnit) {
			return FML_ICON;
		}
		else if (object instanceof VirtualModel) {
			return VIRTUAL_MODEL_ICON;
		}
		else if (object instanceof ModelSlot) {
			TechnologyAdapterController<?> tac = getTechnologyAdapterController(((ModelSlot) object).getModelSlotTechnologyAdapter());
			if (tac != null) {
				// return IconFactory.getImageIcon(tac.getTechnologyIcon(), MODEL_SLOT_ICON_MARKER);
				return IconFactory.getImageIcon(tac.getIconForModelSlot((Class<? extends ModelSlot<?>>) object.getClass()),
						MODEL_SLOT_ICON_MARKER);
			}
			return MODEL_SLOT_ICON;
		}
		else if (object instanceof JavaRole) {
			return FMLIconLibrary.JAVA_ROLE_ICON;
		}
		else if (object instanceof PrimitiveRole) {
			if (((PrimitiveRole) object).getPrimitiveType() != null) {
				switch (((PrimitiveRole) object).getPrimitiveType()) {
					case String:
						return FMLIconLibrary.STRING_PRIMITIVE_ICON;
					case Date:
						return FMLIconLibrary.DATE_PRIMITIVE_ICON;
					case Integer:
						return FMLIconLibrary.INTEGER_PRIMITIVE_ICON;
					case Long:
						return FMLIconLibrary.INTEGER_PRIMITIVE_ICON;
					case Double:
						return FMLIconLibrary.DOUBLE_PRIMITIVE_ICON;
					case Float:
						return FMLIconLibrary.DOUBLE_PRIMITIVE_ICON;
					case Boolean:
						return FMLIconLibrary.BOOLEAN_PRIMITIVE_ICON;
				}
			}
			return FMLIconLibrary.UNKNOWN_ICON;
		}
		else if (object instanceof FlexoConceptInstanceRole) {
			return FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON;
		}
		else if (object instanceof FlexoRole /*&& ((FlexoRole) object).getModelSlot() != null*/) {
			TechnologyAdapterController<?> tac = getTechnologyAdapterController(((FlexoRole) object).getRoleTechnologyAdapter());
			if (tac != null) {
				return tac.getIconForFlexoRole((Class<? extends FlexoRole<?>>) object.getClass());
			}
		}
		else if (object instanceof FlexoConceptInspector) {
			return INSPECT_ICON;
		}
		else if (object instanceof AbstractInvariant) {
			return CONSTRAINT_ICON;
		}
		else if (object instanceof FetchRequestCondition) {
			return CONSTRAINT_ICON;
		}
		else if (object instanceof MatchingCriteria) {
			return CONSTRAINT_ICON;
		}
		else if (object instanceof EditionAction) {
			if ((object instanceof TechnologySpecificAction)
					&& (((TechnologySpecificAction<?, ?>) object).getModelSlotTechnologyAdapter() != null)) {
				TechnologyAdapterController<?> tac = getTechnologyAdapterController(
						((TechnologySpecificAction<?, ?>) object).getModelSlotTechnologyAdapter());
				if (tac != null) {
					ImageIcon returned = tac.getIconForEditionAction((Class<? extends TechnologySpecificAction<?, ?>>) object.getClass());
					if (returned != null) {
						return returned;
					}
					else {
						return tac.getTechnologyIcon();
					}
				}
				else {
					return UNKNOWN_ICON;
				}
			}
			else if (object instanceof AddFlexoConceptInstance) {
				return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.DUPLICATE);
			}
			else if (object instanceof SelectFlexoConceptInstance) {
				return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.IMPORT);
			}
			else if (object instanceof InitiateMatching) {
				return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.SYNC);
			}
			else if (object instanceof MatchFlexoConceptInstance) {
				return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.SYNC);
			}
			else if (object instanceof FinalizeMatching) {
				return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CONCEPT_INSTANCE_ICON, IconLibrary.SYNC);
			}
			else if (object instanceof SelectVirtualModelInstance) {
				return IconFactory.getImageIcon(FMLRTIconLibrary.VIRTUAL_MODEL_INSTANCE_ICON, IconLibrary.IMPORT);
			}
			else if (object instanceof AddToListAction) {
				return IconFactory.getImageIcon(LIST_ICON, IconLibrary.POSITIVE_MARKER);
			}
			else if (object instanceof RemoveFromListAction) {
				return IconFactory.getImageIcon(LIST_ICON, IconLibrary.NEGATIVE_MARKER);
			}
			else if (object instanceof ExpressionAction) {
				return EXPRESSION_ACTION_ICON;
			}
			else if (object instanceof AddClassInstance) {
				return IconFactory.getImageIcon(FMLRTIconLibrary.FLEXO_CLASS_INSTANCE_ICON, IconLibrary.DUPLICATE);
			}
			else if (object instanceof LogAction) {
				return LOG_ACTION_ICON;
			}
			else if (object instanceof NotifyProgressAction) {
				return NOTIFY_PROGRESS_ACTION_ICON;
			}
			else if (object instanceof NotifyPropertyChangedAction) {
				return NOTIFY_PROPERTY_CHANGED_ACTION_ICON;
			}
			else if (object instanceof FireEventAction) {
				return IconFactory.getImageIcon(FLEXO_EVENT_ICON, IconLibrary.NEW_MARKER);
			}
			else if (object instanceof AbstractAssignationAction) {
				return iconForObject(((AbstractAssignationAction) object).getAssignableAction());
			}
			else if (object instanceof ConditionalAction) {
				return CONDITIONAL_ACTION_ICON;
			}
			else if (object instanceof IterationAction) {
				return ITERATION_ACTION_ICON;
			}
			else if (object instanceof ExpressionIterationAction) {
				return ITERATION_ACTION_ICON;
			}
			else if (object instanceof WhileAction) {
				return ITERATION_ACTION_ICON;
			}
			else if (object instanceof IncrementalIterationAction) {
				return ITERATION_ACTION_ICON;
			}
			else if (object instanceof DeleteAction) {
				FlexoProperty<?> pr = ((DeleteAction<?>) object).getAssignedFlexoProperty();
				if (pr != null) {
					ImageIcon baseIcon = iconForObject(pr);
					return IconFactory.getImageIcon(baseIcon, DELETE);
				}
				return DELETE_ICON;
			}
			return UNKNOWN_ICON;
		}
		else if (object instanceof FlexoEvent) {
			return FLEXO_EVENT_ICON;
		}
		else if (object instanceof FlexoEnum) {
			return FLEXO_ENUM_ICON;
		}
		else if (object instanceof FlexoEnumValue) {
			return FLEXO_ENUM_VALUE_ICON;
		}
		else if (object instanceof FlexoConcept) {
			return FLEXO_CONCEPT_ICON;
		}
		else if (object instanceof FlexoBehaviourParameter) {
			return FLEXO_CONCEPT_PARAMETER_ICON;
		}
		else if (object instanceof FlexoBehaviour) {
			if (object instanceof ActionScheme) {
				return ACTION_SCHEME_ICON;
			}
			else if (object instanceof SynchronizationScheme) {
				return IconFactory.getImageIcon(VIRTUAL_MODEL_ICON, IconLibrary.SYNC);
			}
			else if (object instanceof CloningScheme) {
				return CLONING_SCHEME_ICON;
			}
			else if (object instanceof CreationScheme) {
				return CREATION_SCHEME_ICON;
			}
			else if (object instanceof NavigationScheme) {
				return NAVIGATION_SCHEME_ICON;
			}
			else if (object instanceof DeletionScheme) {
				return DELETION_SCHEME_ICON;
			}
			else if (object instanceof EventListener) {
				return FMLIconLibrary.EVENT_LISTENER_ICON;
			}
			else if (object instanceof TechnologySpecificFlexoBehaviour) {
				TechnologyAdapterController<?> tac = getTechnologyAdapterController(
						((TechnologySpecificFlexoBehaviour) object).getSpecificTechnologyAdapter());
				if (tac != null) {
					ImageIcon returned = tac.getIconForFlexoBehaviour((Class<? extends FlexoBehaviour>) object.getClass());
					if (returned != null) {
						return returned;
					}
					else {
						return tac.getTechnologyIcon();
					}
				}
			}
		}
		else if (object instanceof AbstractProperty) {
			return ABSTRACT_PROPERTY_ICON;
		}
		else if (object instanceof ExpressionProperty) {
			return EXPRESSION_PROPERTY_ICON;
		}
		else if (object instanceof GetProperty) {
			return GET_SET_PROPERTY_ICON;
		}
		else if (object instanceof GetSetProperty) {
			return GET_SET_PROPERTY_ICON;
		}
		else if (object instanceof FlexoRole) {
			return FLEXO_ROLE_ICON;
		}
		else if (object instanceof InspectorEntry) {
			InspectorEntry entry = (InspectorEntry) object;
			if (TypeUtils.isString(entry.getType())) {
				return FMLIconLibrary.STRING_PRIMITIVE_ICON;
			}
			if (TypeUtils.isBoolean(entry.getType())) {
				return FMLIconLibrary.BOOLEAN_PRIMITIVE_ICON;
			}
			if (TypeUtils.isInteger(entry.getType()) || TypeUtils.isLong(entry.getType()) || TypeUtils.isShort(entry.getType())
					|| TypeUtils.isByte(entry.getType())) {
				return FMLIconLibrary.INTEGER_PRIMITIVE_ICON;
			}
			if (TypeUtils.isFloat(entry.getType()) || TypeUtils.isDouble(entry.getType())) {
				return FMLIconLibrary.DOUBLE_PRIMITIVE_ICON;
			}
			return FLEXO_CONCEPT_PARAMETER_ICON;
		}
		else if (object instanceof TechnologyObject) {
			return FlexoController.statelessIconForTechnologyObject((TechnologyObject<?>) object);
		}
		logger.warning("No icon for " + object.getClass());
		return UNKNOWN_ICON;
	}

	public static ImageIcon iconForObject(CompilationUnitResource object) {
		return FML_ICON;
	}

	public static <TA extends TechnologyAdapter<TA>> ImageIcon iconForModelSlot(TA ta) {
		TechnologyAdapterController<TA> tac = getTechnologyAdapterController(ta);
		if (tac != null) {
			return IconFactory.getImageIcon(tac.getTechnologyIcon(), MODEL_SLOT_ICON_MARKER);
		}
		return MODEL_SLOT_ICON;
	}

}
