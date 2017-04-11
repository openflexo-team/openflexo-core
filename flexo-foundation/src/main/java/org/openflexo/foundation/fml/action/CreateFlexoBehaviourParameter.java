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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.CheckboxParameter;
import org.openflexo.foundation.fml.DropDownParameter;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourObject;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConceptInstanceParameter;
import org.openflexo.foundation.fml.FlexoResourceParameter;
import org.openflexo.foundation.fml.FlexoVMIResourceParameter;
import org.openflexo.foundation.fml.FloatParameter;
import org.openflexo.foundation.fml.IntegerParameter;
import org.openflexo.foundation.fml.ListParameter;
import org.openflexo.foundation.fml.TextAreaParameter;
import org.openflexo.foundation.fml.TextFieldParameter;
import org.openflexo.foundation.fml.URIParameter;
import org.openflexo.toolbox.StringUtils;

@Deprecated
public class CreateFlexoBehaviourParameter extends FlexoAction<CreateFlexoBehaviourParameter, FlexoBehaviourObject, FMLObject> {

	private static final Logger logger = Logger.getLogger(CreateFlexoBehaviourParameter.class.getPackage().getName());

	public static FlexoActionType<CreateFlexoBehaviourParameter, FlexoBehaviourObject, FMLObject> actionType = new FlexoActionType<CreateFlexoBehaviourParameter, FlexoBehaviourObject, FMLObject>(
			"create_behaviour_parameter", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateFlexoBehaviourParameter makeNewAction(FlexoBehaviourObject focusedObject, Vector<FMLObject> globalSelection,
				FlexoEditor editor) {
			return new CreateFlexoBehaviourParameter(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoBehaviourObject object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoBehaviourObject object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateFlexoBehaviourParameter.actionType, FlexoBehaviour.class);
	}

	private String parameterName;
	private String description;
	private Class<? extends FlexoBehaviourParameter> flexoBehaviourParameterClass;

	private FlexoBehaviourParameter newParameter;

	CreateFlexoBehaviourParameter(FlexoBehaviourObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);

	}

	public FlexoBehaviour getFlexoBehaviour() {
		if (getFocusedObject() != null) {
			return getFocusedObject().getFlexoBehaviour();
		}
		return null;
	}

	public String getParameterName() {
		if (StringUtils.isEmpty(parameterName) && flexoBehaviourParameterClass != null) {
			return getFlexoBehaviour().getAvailableParameterName(flexoBehaviourParameterClass.getSimpleName());
		}
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {
		logger.info("Add FlexoBehaviourParameter, name=" + getParameterName() + " type=" + flexoBehaviourParameterClass);

		if (flexoBehaviourParameterClass != null) {
			FMLModelFactory factory = getFocusedObject().getFMLModelFactory();
			newParameter = factory.newInstance(flexoBehaviourParameterClass);
			newParameter.setName(getParameterName());
			getFlexoBehaviour().addToParameters(newParameter);
		}

	}

	public FlexoBehaviourParameter getNewParameter() {
		return newParameter;
	}

	@Override
	public boolean isValid() {
		if (StringUtils.isEmpty(getParameterName())) {
			return false;
		}
		else if (getFlexoBehaviour().getParameter(getParameterName()) != null) {
			return false;
		}
		return true;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if ((description == null && this.description != null) || (description != null && !description.equals(this.description))) {
			String oldValue = this.description;
			this.description = description;
			getPropertyChangeSupport().firePropertyChange("description", oldValue, description);
		}
	}

	public Class<? extends FlexoBehaviourParameter> getFlexoBehaviourParameterClass() {
		return flexoBehaviourParameterClass;
	}

	public void setFlexoBehaviourParameterClass(Class<? extends FlexoBehaviourParameter> flexoBehaviourParameterClass) {
		if (flexoBehaviourParameterClass != this.flexoBehaviourParameterClass) {
			Class<? extends FlexoBehaviourParameter> oldValue = this.flexoBehaviourParameterClass;
			this.flexoBehaviourParameterClass = flexoBehaviourParameterClass;
			getPropertyChangeSupport().firePropertyChange("flexoBehaviourParameterClass", oldValue, flexoBehaviourParameterClass);
		}
	}

	private List<Class<? extends FlexoBehaviourParameter>> availableParameterTypes;

	public List<Class<? extends FlexoBehaviourParameter>> getAvailableParameterTypes() {
		if (availableParameterTypes == null) {
			availableParameterTypes = computeAvailableParameterTypes();
		}
		return availableParameterTypes;
	}

	// TODO: this code is duplicated in createFlexoBehaviourWizard, it needs refactoring to avoid any mistake
	private List<Class<? extends FlexoBehaviourParameter>> computeAvailableParameterTypes() {
		availableParameterTypes = new ArrayList<Class<? extends FlexoBehaviourParameter>>();
		availableParameterTypes.add(TextFieldParameter.class);
		availableParameterTypes.add(TextAreaParameter.class);
		availableParameterTypes.add(CheckboxParameter.class);
		availableParameterTypes.add(DropDownParameter.class);
		availableParameterTypes.add(FloatParameter.class);
		availableParameterTypes.add(IntegerParameter.class);
		availableParameterTypes.add(ListParameter.class);
		availableParameterTypes.add(URIParameter.class);
		availableParameterTypes.add(FlexoResourceParameter.class);
		availableParameterTypes.add(FlexoConceptInstanceParameter.class);
		availableParameterTypes.add(FlexoVMIResourceParameter.class);
		/*if (getFocusedObject() != null && getFocusedObject().getOwningVirtualModel() != null
				&& getFocusedObject().getOwningVirtualModel().getModelSlots() != null) {
			for (ModelSlot<?> ms : getFocusedObject().getOwningVirtualModel().getModelSlots()) {
				for (Class<? extends FlexoBehaviourParameter> paramType : ms.getAvailableFlexoBehaviourParameterTypes()) {
					if (!availableParameterTypes.contains(paramType)) {
						availableParameterTypes.add(paramType);
					}
				}
			}
		}
		if (getFocusedObject().getFlexoConcept() instanceof AbstractVirtualModel) {
			for (ModelSlot<?> ms : ((AbstractVirtualModel<?>) getFocusedObject().getFlexoConcept()).getModelSlots()) {
				for (Class<? extends FlexoBehaviourParameter> paramType : ms.getAvailableFlexoBehaviourParameterTypes()) {
					if (!availableParameterTypes.contains(paramType)) {
						availableParameterTypes.add(paramType);
					}
				}
			}
		}*/
		return availableParameterTypes;
	}

}
