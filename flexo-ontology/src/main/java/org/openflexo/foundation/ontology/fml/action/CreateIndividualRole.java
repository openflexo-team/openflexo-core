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

package org.openflexo.foundation.ontology.fml.action;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoConceptStructuralFacet;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.action.AbstractCreateFlexoRole;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.fml.IndividualRole;
import org.openflexo.foundation.ontology.nature.FlexoOntologyVirtualModelNature;
import org.openflexo.foundation.ontology.technologyadapter.FlexoOntologyModelSlot;
import org.openflexo.foundation.technologyadapter.ModelSlot;

/**
 * Action allowing to create a {@link IndividualRole}<br>
 * 
 * To be valid, such action:
 * <ul>
 * <li>must be configured with a {@link FlexoConceptObject} as focused object</li>
 * <li>must declare a valid property name</li>
 * <li>must declare a valid cardinality</li>
 * <li>may declare a valid {@link FlexoOntologyModelSlot}</li>
 * <li>may declare a valid description</li>
 * </ul>
 */
public class CreateIndividualRole extends AbstractCreateFlexoRole<CreateIndividualRole, FlexoOntologyModelSlot<?, ?, ?>> {

	private static final Logger logger = Logger.getLogger(CreateIndividualRole.class.getPackage().getName());

	private final List<Class<? extends FlexoRole<?>>> vmAvailableFlexoRoleTypes = null;

	public static FlexoActionFactory<CreateIndividualRole, FlexoConceptObject, FMLObject> actionType = new FlexoActionFactory<CreateIndividualRole, FlexoConceptObject, FMLObject>(
			"create_individual_role", FlexoActionFactory.newPropertyMenu, FlexoActionFactory.defaultGroup,
			FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateIndividualRole makeNewAction(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateIndividualRole(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConceptObject object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConceptObject object, Vector<FMLObject> globalSelection) {
			return object != null && FlexoOntologyVirtualModelNature.INSTANCE.hasNature(object.getOwningVirtualModel());
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateIndividualRole.actionType, FlexoConcept.class);
		FlexoObjectImpl.addActionForClass(CreateIndividualRole.actionType, FlexoConceptStructuralFacet.class);
	}

	CreateIndividualRole(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	private Class<? extends IndividualRole<?>> flexoRoleClass;

	public List<Class<? extends IndividualRole<?>>> getAvailableFlexoRoleTypes() {
		if (getModelSlot() != null) {
			List<Class<? extends IndividualRole<?>>> returned = new ArrayList<>();
			List<Class<? extends FlexoRole<?>>> allRoles = getModelSlot().getAvailableFlexoRoleTypes();
			for (Class<? extends FlexoRole<?>> roleClass : allRoles) {
				if (IndividualRole.class.isAssignableFrom(roleClass)) {
					returned.add((Class<? extends IndividualRole<?>>) roleClass);
				}
			}
			return returned;
		}
		return null;
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException, InvalidNameException {
		// logger.info("Add flexo role, flexoRoleClass=" + flexoRoleClass);
		// logger.info("modelSlot = " + getModelSlot());

		if (getFlexoRoleClass() != null) {
			if (getModelSlot() != null) {
				newFlexoRole = getModelSlot().makeFlexoRole(getFlexoRoleClass());
				newFlexoRole.setModelSlot(getModelSlot());
			}
			else {
				FMLModelFactory factory = getFocusedObject().getFMLModelFactory();
				newFlexoRole = factory.newInstance(getFlexoRoleClass());
			}

			if (newFlexoRole != null) {

				((IndividualRole<?>) newFlexoRole).setOntologicType(getIndividualType());

				newFlexoRole.setRoleName(getRoleName());
				newFlexoRole.setCardinality(getCardinality());
				newFlexoRole.setModelSlot(getModelSlot());

				finalizeDoAction(context);
			}
		}

		else {
			throw new InvalidParameterException("No FlexoRole class defined");
		}

	}

	@Override
	public List<FlexoOntologyModelSlot<?, ?, ?>> getAvailableModelSlots() {

		if (getFocusedObject() instanceof VirtualModel) {
			return (List) ((VirtualModel) getFocusedObject()).getModelSlots(FlexoOntologyModelSlot.class);
		}
		else if (getFocusedObject() != null && getFocusedObject().getOwningVirtualModel() != null) {
			return (List) getFocusedObject().getOwningVirtualModel().getModelSlots(FlexoOntologyModelSlot.class);
		}
		return null;
	}

	@Override
	public void setModelSlot(FlexoOntologyModelSlot<?, ?, ?> modelSlot) {
		super.setModelSlot(modelSlot);
		getPropertyChangeSupport().firePropertyChange("availableFlexoRoleTypes", null, getAvailableFlexoRoleTypes());
		if (getFlexoRoleClass() != null && !getAvailableFlexoRoleTypes().contains(getFlexoRoleClass())) {
			if (getAvailableFlexoRoleTypes().size() > 0) {
				setFlexoRoleClass(getAvailableFlexoRoleTypes().get(0));
			}
			else {
				setFlexoRoleClass(null);
			}
		}
		if (modelSlot != null && getFlexoRoleClass() == null && getAvailableFlexoRoleTypes().size() > 0) {
			setFlexoRoleClass(getAvailableFlexoRoleTypes().get(0));
		}
	}

	@Override
	public Class<? extends IndividualRole<?>> getFlexoRoleClass() {
		return flexoRoleClass;
	}

	public void setFlexoRoleClass(Class<? extends IndividualRole<?>> flexoRoleClass) {
		this.flexoRoleClass = flexoRoleClass;

		// The default model slot may change
		ModelSlot<?> oldModelSlot = getModelSlot();
		defaultModelSlot = retrieveDefaultModelSlot();
		getPropertyChangeSupport().firePropertyChange("modelSlot", oldModelSlot, getModelSlot());

		getPropertyChangeSupport().firePropertyChange("roleName", null, getRoleName());
		getPropertyChangeSupport().firePropertyChange("propertyName", null, getRoleName());
		getPropertyChangeSupport().firePropertyChange("flexoRoleClass", flexoRoleClass != null ? null : false, flexoRoleClass);
	}

	private IFlexoOntologyClass<?> individualType;

	public IFlexoOntologyClass<?> getIndividualType() {
		return individualType;
	}

	public void setIndividualType(IFlexoOntologyClass<?> individualType) {
		this.individualType = individualType;
		getPropertyChangeSupport().firePropertyChange("individualType", individualType != null ? null : false, individualType);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class<FlexoOntologyModelSlot<?, ?, ?>> getModelSlotType() {
		return (Class) FlexoOntologyModelSlot.class;
	}

}
