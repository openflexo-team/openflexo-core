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
import org.openflexo.foundation.viewpoint.FlexoConcept;
import org.openflexo.foundation.viewpoint.FlexoConceptBehaviouralFacet;
import org.openflexo.foundation.viewpoint.FlexoConceptObject;
import org.openflexo.foundation.viewpoint.EditionScheme;
import org.openflexo.foundation.viewpoint.ViewPointObject;
import org.openflexo.foundation.viewpoint.VirtualModelModelFactory;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;

public class CreateEditionScheme extends FlexoAction<CreateEditionScheme, FlexoConceptObject, ViewPointObject> {

	private static final Logger logger = Logger.getLogger(CreateEditionScheme.class.getPackage().getName());

	public static FlexoActionType<CreateEditionScheme, FlexoConceptObject, ViewPointObject> actionType = new FlexoActionType<CreateEditionScheme, FlexoConceptObject, ViewPointObject>(
			"create_edition_scheme", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateEditionScheme makeNewAction(FlexoConceptObject focusedObject, Vector<ViewPointObject> globalSelection,
				FlexoEditor editor) {
			return new CreateEditionScheme(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConceptObject object, Vector<ViewPointObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConceptObject object, Vector<ViewPointObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateEditionScheme.actionType, FlexoConcept.class);
		FlexoObjectImpl.addActionForClass(CreateEditionScheme.actionType, FlexoConceptBehaviouralFacet.class);
	}

	private String editionSchemeName;
	public String description;
	public Class<? extends EditionScheme> editionSchemeClass;

	private EditionScheme newEditionScheme;

	CreateEditionScheme(FlexoConceptObject focusedObject, Vector<ViewPointObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);

	}

	public FlexoConcept getFlexoConcept() {
		if (getFocusedObject() != null) {
			return getFocusedObject().getFlexoConcept();
		}
		return null;
	}

	public String getEditionSchemeName() {
		if (StringUtils.isEmpty(editionSchemeName) && editionSchemeClass != null) {
			return getFlexoConcept().getAvailableEditionSchemeName(editionSchemeClass.getSimpleName());
		}
		return editionSchemeName;
	}

	public void setEditionSchemeName(String editionSchemeName) {
		this.editionSchemeName = editionSchemeName;
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {
		logger.info("Add edition scheme, name=" + getEditionSchemeName() + " type=" + editionSchemeClass);

		if (editionSchemeClass != null) {

			VirtualModelModelFactory factory = getFocusedObject().getVirtualModelFactory();
			newEditionScheme = factory.newInstance(editionSchemeClass);
			newEditionScheme.setName(getEditionSchemeName());
			getFlexoConcept().addToEditionSchemes(newEditionScheme);
		}

	}

	public EditionScheme getNewEditionScheme() {
		return newEditionScheme;
	}

	private String validityMessage = EMPTY_NAME;

	private static final String DUPLICATED_NAME = FlexoLocalization.localizedForKey("this_name_is_already_used_please_choose_an_other_one");
	private static final String EMPTY_NAME = FlexoLocalization.localizedForKey("edition_scheme_must_have_an_non_empty_and_unique_name");

	public String getValidityMessage() {
		return validityMessage;
	}

	@Override
	public boolean isValid() {
		if (StringUtils.isEmpty(getEditionSchemeName())) {
			validityMessage = EMPTY_NAME;
			return false;
		} else if (getFlexoConcept().getPatternRole(getEditionSchemeName()) != null) {
			validityMessage = DUPLICATED_NAME;
			return false;
		} else {
			validityMessage = "";
			return true;
		}
	}
}