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
package org.openflexo.foundation.viewpoint.action;

import java.security.InvalidParameterException;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.viewpoint.FlexoBehaviour;
import org.openflexo.foundation.viewpoint.FlexoBehaviourObject;
import org.openflexo.foundation.viewpoint.FlexoBehaviourParameter;
import org.openflexo.foundation.viewpoint.ViewPointObject;
import org.openflexo.foundation.viewpoint.VirtualModelModelFactory;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;

//TODO: rename as CreateFlexoBehaviourParameter
public class CreateEditionSchemeParameter extends FlexoAction<CreateEditionSchemeParameter, FlexoBehaviourObject, ViewPointObject> {

	private static final Logger logger = Logger.getLogger(CreateEditionSchemeParameter.class.getPackage().getName());

	public static FlexoActionType<CreateEditionSchemeParameter, FlexoBehaviourObject, ViewPointObject> actionType = new FlexoActionType<CreateEditionSchemeParameter, FlexoBehaviourObject, ViewPointObject>(
			"create_edition_scheme_parameter", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateEditionSchemeParameter makeNewAction(FlexoBehaviourObject focusedObject, Vector<ViewPointObject> globalSelection,
				FlexoEditor editor) {
			return new CreateEditionSchemeParameter(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoBehaviourObject object, Vector<ViewPointObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoBehaviourObject object, Vector<ViewPointObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateEditionSchemeParameter.actionType, FlexoBehaviour.class);
	}

	private String parameterName;
	private String description;
	private Class<? extends FlexoBehaviourParameter> flexoBehaviourParameterClass;

	private FlexoBehaviourParameter newParameter;

	CreateEditionSchemeParameter(FlexoBehaviourObject focusedObject, Vector<ViewPointObject> globalSelection, FlexoEditor editor) {
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
		logger.info("Add edition scheme, name=" + getParameterName() + " type=" + flexoBehaviourParameterClass);

		if (flexoBehaviourParameterClass != null) {

			VirtualModelModelFactory factory = getFocusedObject().getVirtualModelFactory();
			newParameter = factory.newInstance(flexoBehaviourParameterClass);
			newParameter.setName(getParameterName());
			getFlexoBehaviour().addToParameters(newParameter);
		}

	}

	public FlexoBehaviourParameter getNewParameter() {
		return newParameter;
	}

	private String errorMessage = EMPTY_NAME;

	private static final String DUPLICATED_NAME = FlexoLocalization.localizedForKey("this_name_is_already_used_please_choose_an_other_one");
	private static final String EMPTY_NAME = FlexoLocalization.localizedForKey("edition_behaviour_must_have_an_non_empty_and_unique_name");

	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public boolean isValid() {
		if (StringUtils.isEmpty(getParameterName())) {
			errorMessage = EMPTY_NAME;
			return false;
		} else if (getFlexoBehaviour().getParameter(getParameterName()) != null) {
			errorMessage = DUPLICATED_NAME;
			return false;
		} else {
			errorMessage = "";
			return true;
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Class<? extends FlexoBehaviourParameter> getFlexoBehaviourParameterClass() {
		return flexoBehaviourParameterClass;
	}

	public void setFlexoBehaviourParameterClass(Class<? extends FlexoBehaviourParameter> flexoBehaviourParameterClass) {
		boolean wasValid = isValid();
		this.flexoBehaviourParameterClass = flexoBehaviourParameterClass;
		getPropertyChangeSupport().firePropertyChange("flexoBehaviourParameterClass", null, flexoBehaviourParameterClass);
		getPropertyChangeSupport().firePropertyChange("isValid", wasValid, isValid());
		getPropertyChangeSupport().firePropertyChange("errorMessage", null, getErrorMessage());
	}
}