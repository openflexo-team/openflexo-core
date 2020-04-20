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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.TechnologySpecificFlexoAction;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.Visibility;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour.BehaviourParameterEntry;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.toolbox.StringUtils;

public class GenerateCreationScheme extends FlexoAction<GenerateCreationScheme, FlexoConcept, FMLObject>
		implements TechnologySpecificFlexoAction<FMLTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(GenerateCreationScheme.class.getPackage().getName());

	public static FlexoActionFactory<GenerateCreationScheme, FlexoConcept, FMLObject> actionType = new FlexoActionFactory<GenerateCreationScheme, FlexoConcept, FMLObject>(
			"creation_scheme_using_properties", FlexoActionFactory.generateMenu, FlexoActionFactory.defaultGroup,
			FlexoActionFactory.NORMAL_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public GenerateCreationScheme makeNewAction(FlexoConcept focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new GenerateCreationScheme(focusedObject, globalSelection, editor);
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
		FlexoObjectImpl.addActionForClass(GenerateCreationScheme.actionType, FlexoConcept.class);
	}

	private String newCreationSchemeName;
	private Visibility visibility;
	private List<FlexoProperty<?>> propertiesToConsider;
	private List<FlexoProperty<?>> selectedProperties;

	GenerateCreationScheme(FlexoConcept focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		newCreationSchemeName = "create";
		visibility = Visibility.Public;
		propertiesToConsider = getFocusedObject().retrieveAccessibleProperties(false);
		selectedProperties = new ArrayList<>();
		selectedProperties.addAll(propertiesToConsider);
	}

	@Override
	protected void doAction(Object context) {

		logger.info("Generate CreationScheme" + getNewCreationSchemeName());
		// System.out.println("Properties to consider: " + getPropertiesToConsider());
		// System.out.println("selected: " + getSelectedProperties());

		CreateFlexoBehaviour createCreationScheme = CreateFlexoBehaviour.actionType.makeNewEmbeddedAction(getFocusedObject(), null, this);
		createCreationScheme.setFlexoBehaviourName(getNewCreationSchemeName());
		createCreationScheme.setFlexoBehaviourClass(CreationScheme.class);
		for (FlexoProperty<?> property : getSelectedProperties()) {
			BehaviourParameterEntry newEntry = createCreationScheme.newParameterEntry();
			newEntry.setParameterName(property.getName());
			newEntry.setParameterType(property.getType());
		}
		createCreationScheme.doAction();
		CreationScheme creationScheme = (CreationScheme) createCreationScheme.getNewFlexoBehaviour();
		for (FlexoProperty<?> property : getSelectedProperties()) {
			CreateEditionAction assignAction = CreateEditionAction.actionType.makeNewEmbeddedAction(creationScheme.getControlGraph(), null,
					this);
			assignAction.setEditionActionClass(ExpressionAction.class);
			assignAction.setAssignation(new DataBinding<>(property.getName()));
			assignAction.doAction();
			AssignationAction<?> createRightMember = (AssignationAction<?>) assignAction.getNewEditionAction();
			((ExpressionAction<?>) createRightMember.getAssignableAction())
					.setExpression(new DataBinding<>("parameters." + property.getName()));
		}

	}

	@Override
	public Class<? extends FMLTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	public FMLTechnologyAdapter getFMLTechnologyAdapter() {
		return getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
	}

	public String getNewCreationSchemeName() {
		return newCreationSchemeName;
	}

	public void setNewCreationSchemeName(String newCreationSchemeName) {
		this.newCreationSchemeName = newCreationSchemeName;
		getPropertyChangeSupport().firePropertyChange("newCreationSchemeName", null, newCreationSchemeName);
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public void setVisibility(Visibility visibility) {
		if ((visibility == null && this.visibility != null) || (visibility != null && !visibility.equals(this.visibility))) {
			Visibility oldValue = this.visibility;
			this.visibility = visibility;
			getPropertyChangeSupport().firePropertyChange("visibility", oldValue, visibility);
		}
	}

	public List<FlexoProperty<?>> getSelectedProperties() {
		return selectedProperties;
	}

	public List<FlexoProperty<?>> getPropertiesToConsider() {
		return propertiesToConsider;
	}

	@Override
	public boolean isValid() {
		if (StringUtils.isEmpty(getNewCreationSchemeName())) {
			return false;
		}
		if (getFocusedObject().getFlexoBehaviour(getNewCreationSchemeName()) != null) {
			return false;
		}
		return true;
	}

}
