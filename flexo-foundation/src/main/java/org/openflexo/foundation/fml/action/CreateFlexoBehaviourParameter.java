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
import java.security.URIParameter;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.CheckboxParameter;
import org.openflexo.foundation.fml.ClassParameter;
import org.openflexo.foundation.fml.DropDownParameter;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourObject;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConceptInstanceParameter;
import org.openflexo.foundation.fml.FloatParameter;
import org.openflexo.foundation.fml.IndividualParameter;
import org.openflexo.foundation.fml.IntegerParameter;
import org.openflexo.foundation.fml.ListParameter;
import org.openflexo.foundation.fml.TechnologyObjectParameter;
import org.openflexo.foundation.fml.TextAreaParameter;
import org.openflexo.foundation.fml.TextFieldParameter;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.toolbox.StringUtils;

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

	public static final Class[] AVAILABLE_TYPES = new Class[] { TextFieldParameter.class, TextAreaParameter.class, IntegerParameter.class,
			FloatParameter.class, ListParameter.class, CheckboxParameter.class, DropDownParameter.class, ClassParameter.class,
			FlexoConceptInstanceParameter.class, IndividualParameter.class, TechnologyObjectParameter.class, URIParameter.class };

	private String parameterName;
	private String description;
	private String defaultParameterValue;
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
		logger.info("Add edition scheme, name=" + getParameterName() + " type=" + flexoBehaviourParameterClass);

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
