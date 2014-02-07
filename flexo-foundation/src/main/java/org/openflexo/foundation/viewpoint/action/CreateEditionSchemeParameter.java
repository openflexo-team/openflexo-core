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
import org.openflexo.foundation.viewpoint.EditionScheme;
import org.openflexo.foundation.viewpoint.EditionSchemeObject;
import org.openflexo.foundation.viewpoint.EditionSchemeParameter;
import org.openflexo.foundation.viewpoint.ViewPointObject;
import org.openflexo.foundation.viewpoint.VirtualModelModelFactory;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;

public class CreateEditionSchemeParameter extends FlexoAction<CreateEditionSchemeParameter, EditionSchemeObject, ViewPointObject> {

	private static final Logger logger = Logger.getLogger(CreateEditionSchemeParameter.class.getPackage().getName());

	public static FlexoActionType<CreateEditionSchemeParameter, EditionSchemeObject, ViewPointObject> actionType = new FlexoActionType<CreateEditionSchemeParameter, EditionSchemeObject, ViewPointObject>(
			"create_edition_scheme_parameter", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateEditionSchemeParameter makeNewAction(EditionSchemeObject focusedObject, Vector<ViewPointObject> globalSelection,
				FlexoEditor editor) {
			return new CreateEditionSchemeParameter(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(EditionSchemeObject object, Vector<ViewPointObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(EditionSchemeObject object, Vector<ViewPointObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateEditionSchemeParameter.actionType, EditionScheme.class);
	}

	private String parameterName;
	public String description;
	public Class<? extends EditionSchemeParameter> editionSchemeParameterClass;

	private EditionSchemeParameter newParameter;

	CreateEditionSchemeParameter(EditionSchemeObject focusedObject, Vector<ViewPointObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);

	}

	public EditionScheme getEditionScheme() {
		if (getFocusedObject() != null) {
			return getFocusedObject().getEditionScheme();
		}
		return null;
	}

	public String getParameterName() {
		if (StringUtils.isEmpty(parameterName) && editionSchemeParameterClass != null) {
			return getEditionScheme().getAvailableParameterName(editionSchemeParameterClass.getSimpleName());
		}
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {
		logger.info("Add edition scheme, name=" + getParameterName() + " type=" + editionSchemeParameterClass);

		if (editionSchemeParameterClass != null) {

			VirtualModelModelFactory factory = getFocusedObject().getVirtualModelFactory();
			newParameter = factory.newInstance(editionSchemeParameterClass);
			newParameter.setName(getParameterName());
			getEditionScheme().addToParameters(newParameter);
		}

	}

	public EditionSchemeParameter getNewParameter() {
		return newParameter;
	}

	private String validityMessage = EMPTY_NAME;

	private static final String DUPLICATED_NAME = FlexoLocalization.localizedForKey("this_name_is_already_used_please_choose_an_other_one");
	private static final String EMPTY_NAME = FlexoLocalization.localizedForKey("edition_scheme_must_have_an_non_empty_and_unique_name");

	public String getValidityMessage() {
		return validityMessage;
	}

	@Override
	public boolean isValid() {
		if (StringUtils.isEmpty(getParameterName())) {
			validityMessage = EMPTY_NAME;
			return false;
		} else if (getEditionScheme().getParameter(getParameterName()) != null) {
			validityMessage = DUPLICATED_NAME;
			return false;
		} else {
			validityMessage = "";
			return true;
		}
	}
}