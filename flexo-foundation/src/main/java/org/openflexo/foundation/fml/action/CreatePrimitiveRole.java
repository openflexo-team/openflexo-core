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

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoConceptStructuralFacet;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.PrimitiveRole.PrimitiveType;
import org.openflexo.foundation.technologyadapter.ModelSlot;

/**
 * Action allowing to create a {@link PrimitiveRole}<br>
 * 
 * To be valid, such action:
 * <ul>
 * <li>must be configured with a {@link FlexoConceptObject} as focused object</li>
 * <li>must declare a valid property name</li>
 * <li>must declare a valid cardinality</li>
 * <li>must declare a valid primitive type</li>
 * <li>may declare a valid description</li>
 * </ul>
 */
public class CreatePrimitiveRole extends AbstractCreateFlexoRole<CreatePrimitiveRole, ModelSlot<?>> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreatePrimitiveRole.class.getPackage().getName());

	public static FlexoActionType<CreatePrimitiveRole, FlexoConceptObject, FMLObject> actionType = new FlexoActionType<CreatePrimitiveRole, FlexoConceptObject, FMLObject>(
			"create_primitive_role", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreatePrimitiveRole makeNewAction(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreatePrimitiveRole(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConceptObject object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConceptObject object, Vector<FMLObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreatePrimitiveRole.actionType, FlexoConcept.class);
		FlexoObjectImpl.addActionForClass(CreatePrimitiveRole.actionType, FlexoConceptStructuralFacet.class);
	}

	private PrimitiveType primitiveType = PrimitiveType.String;

	CreatePrimitiveRole(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {
		// logger.info("Add flexo role, flexoRoleClass=" + flexoRoleClass);
		// logger.info("modelSlot = " + getModelSlot());

		FMLModelFactory factory = getFocusedObject().getFMLModelFactory();
		newFlexoRole = factory.newInstance(PrimitiveRole.class);

		newFlexoRole.setRoleName(getRoleName());
		newFlexoRole.setCardinality(getCardinality());
		newFlexoRole.setModelSlot(getModelSlot());

		((PrimitiveRole<?>) newFlexoRole).setPrimitiveType(getPrimitiveType());

		finalizeDoAction(context);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class<PrimitiveRole<?>> getFlexoRoleClass() {
		return (Class) PrimitiveRole.class;
	}

	@Override
	public List<ModelSlot<?>> getAvailableModelSlots() {
		return null;
	}

	@Override
	public Class<? extends ModelSlot<?>> getModelSlotType() {
		return null;
	}

	public PrimitiveType getPrimitiveType() {
		return primitiveType;
	}

	public void setPrimitiveType(PrimitiveType primitiveType) {
		this.primitiveType = primitiveType;
		getPropertyChangeSupport().firePropertyChange("primitiveType", null, primitiveType);
	}

}
