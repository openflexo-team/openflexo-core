/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.action;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.TechnologySpecificFlexoAction;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour.BehaviourParameterEntry;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.editionaction.ReturnStatement;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceModelSlot;

public class GenerateUnimplementedPropertiesAndBehaviours
		extends FlexoAction<GenerateUnimplementedPropertiesAndBehaviours, FlexoConcept, FMLObject>
		implements TechnologySpecificFlexoAction<FMLTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(GenerateUnimplementedPropertiesAndBehaviours.class.getPackage().getName());

	public static FlexoActionFactory<GenerateUnimplementedPropertiesAndBehaviours, FlexoConcept, FMLObject> actionType = new FlexoActionFactory<GenerateUnimplementedPropertiesAndBehaviours, FlexoConcept, FMLObject>(
			"generate_unimplemented_properties_and_behaviours", FlexoActionFactory.generateMenu, FlexoActionFactory.defaultGroup,
			FlexoActionFactory.NORMAL_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public GenerateUnimplementedPropertiesAndBehaviours makeNewAction(FlexoConcept focusedObject, Vector<FMLObject> globalSelection,
				FlexoEditor editor) {
			return new GenerateUnimplementedPropertiesAndBehaviours(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConcept object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConcept object, Vector<FMLObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(GenerateUnimplementedPropertiesAndBehaviours.actionType, FlexoConcept.class);
	}

	private List<FlexoProperty<?>> propertiesToConsider;
	private List<FlexoProperty<?>> selectedProperties;

	private List<FlexoBehaviour> behavioursToConsider;
	private List<FlexoBehaviour> selectedBehaviours;

	GenerateUnimplementedPropertiesAndBehaviours(FlexoConcept focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		List<FlexoProperty<?>> allProperties = getFocusedObject().retrieveAccessibleProperties(false);
		propertiesToConsider = new ArrayList<>();
		for (FlexoProperty<?> property : allProperties) {
			if (property.getFlexoConcept() != getFocusedObject()) {
				propertiesToConsider.add(property);
			}
		}
		selectedProperties = new ArrayList<>();
		for (FlexoProperty<?> property : propertiesToConsider) {
			if (property.isAbstract()) {
				selectedProperties.add(property);
			}
		}

		List<FlexoBehaviour> allBehaviours = getFocusedObject().getAccessibleFlexoBehaviours(false);
		behavioursToConsider = new ArrayList<>();
		for (FlexoBehaviour behaviour : allBehaviours) {
			if (behaviour.getFlexoConcept() != getFocusedObject()) {
				behavioursToConsider.add(behaviour);
			}
		}
		selectedBehaviours = new ArrayList<>();
		for (FlexoBehaviour behaviour : behavioursToConsider) {
			if (behaviour.isAbstract()) {
				selectedBehaviours.add(behaviour);
			}
		}
	}

	@Override
	protected void doAction(Object context) {

		logger.info("GenerateUnimplementedPropertiesAndBehaviours" + selectedBehaviours);

		System.out.println("selectedProperties=" + selectedProperties);
		System.out.println("selectedBehaviours=" + selectedBehaviours);

		AbstractCreateFlexoProperty<?> action = null;

		for (FlexoProperty<?> flexoProperty : selectedProperties) {
			Type type = flexoProperty.getType();
			if (TypeUtils.isPrimitive(type)) {
				CreatePrimitiveRole createPrimitive = CreatePrimitiveRole.actionType.makeNewEmbeddedAction(getFocusedObject(), null, this);
				action = createPrimitive;
				createPrimitive.setRoleName(flexoProperty.getName());
				createPrimitive.setCardinality(flexoProperty.getCardinality());
				if (TypeUtils.isString(type)) {
					createPrimitive.setPrimitiveType(PrimitiveType.String);
				}
				if (TypeUtils.isDate(type)) {
					createPrimitive.setPrimitiveType(PrimitiveType.Date);
				}
				if (TypeUtils.isBoolean(type)) {
					createPrimitive.setPrimitiveType(PrimitiveType.Boolean);
				}
				if (TypeUtils.isInteger(type) || TypeUtils.isLong(type) || TypeUtils.isShort(type) || TypeUtils.isByte(type)) {
					createPrimitive.setPrimitiveType(PrimitiveType.Integer);
				}
				if (TypeUtils.isFloat(type)) {
					createPrimitive.setPrimitiveType(PrimitiveType.Float);
				}
				if (TypeUtils.isDouble(type)) {
					createPrimitive.setPrimitiveType(PrimitiveType.Double);
				}
			}
			else if (type instanceof VirtualModelInstanceType) {
				CreateModelSlot createModelSlot = CreateModelSlot.actionType.makeNewEmbeddedAction(getFocusedObject(), null, this);
				action = createModelSlot;
				createModelSlot.setModelSlotName(flexoProperty.getName());
				createModelSlot.setTechnologyAdapter(getFMLRTTechnologyAdapter());
				createModelSlot.setModelSlotClass(FMLRTVirtualModelInstanceModelSlot.class);
				createModelSlot.setVmRes((CompilationUnitResource) ((VirtualModelInstanceType) type).getVirtualModel().getResource());
			}
			else if (type instanceof FlexoConceptInstanceType) {
				CreateFlexoConceptInstanceRole createFCIRole = CreateFlexoConceptInstanceRole.actionType
						.makeNewEmbeddedAction(getFocusedObject(), null, this);
				action = createFCIRole;
				createFCIRole.setPropertyName(flexoProperty.getName());
				createFCIRole.setFlexoConceptInstanceType(((FlexoConceptInstanceType) type).getFlexoConcept());
			}

			if (action != null) {
				System.out.println("Executing action " + action + " valid=" + action.isValid());
				action.doAction();
			}
		}

		for (FlexoBehaviour behaviour : selectedBehaviours) {
			CreateFlexoBehaviour createFlexoBehaviour = CreateFlexoBehaviour.actionType.makeNewEmbeddedAction(getFocusedObject(), null,
					this);
			createFlexoBehaviour.setFlexoBehaviourName(behaviour.getName());
			createFlexoBehaviour.setFlexoBehaviourClass((Class<? extends FlexoBehaviour>) behaviour.getImplementedInterface());
			for (FlexoBehaviourParameter parameter : behaviour.getParameters()) {
				BehaviourParameterEntry newEntry = createFlexoBehaviour.newParameterEntry();
				newEntry.setParameterName(parameter.getName());
				newEntry.setParameterType(parameter.getType());
			}
			createFlexoBehaviour.doAction();
			FlexoBehaviour newBehaviour = createFlexoBehaviour.getNewFlexoBehaviour();

			CreateEditionAction assignAction = CreateEditionAction.actionType.makeNewEmbeddedAction(newBehaviour.getControlGraph(), null,
					this);
			assignAction.setEditionActionClass(ExpressionAction.class);
			assignAction.setReturnStatement(true);
			assignAction.doAction();
			ReturnStatement<?> createRightMember = (ReturnStatement<?>) assignAction.getNewEditionAction();
			((ExpressionAction<?>) createRightMember.getAssignableAction()).setExpression(new DataBinding<>("null"));
		}

	}

	@Override
	public Class<? extends FMLTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	public FMLTechnologyAdapter getFMLTechnologyAdapter() {
		return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
	}

	public FMLRTTechnologyAdapter getFMLRTTechnologyAdapter() {
		return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLRTTechnologyAdapter.class);
	}

	public List<FlexoProperty<?>> getPropertiesToConsider() {
		return propertiesToConsider;
	}

	public List<FlexoProperty<?>> getSelectedProperties() {
		return selectedProperties;
	}

	public List<FlexoBehaviour> getSelectedBehaviours() {
		return selectedBehaviours;
	}

	public List<FlexoBehaviour> getBehavioursToConsider() {
		return behavioursToConsider;
	}

	@Override
	public boolean isValid() {
		return true;
	}

}
