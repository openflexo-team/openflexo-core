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
import org.openflexo.foundation.fml.GetSetProperty;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;

/**
 * Action allowing to create a {@link GetSetProperty}<br>
 * 
 * To be valid, such action:
 * <ul>
 * <li>must be configured with a {@link FlexoConceptObject} as focused object</li>
 * <li>must declare a valid property name</li>
 * <li>may declare a valid description</li>
 * </ul>
 */
public class CreateGetSetProperty extends AbstractCreateFlexoProperty<CreateGetSetProperty> {

	private static final Logger logger = Logger.getLogger(CreateGetSetProperty.class.getPackage().getName());

	public static FlexoActionType<CreateGetSetProperty, FlexoConceptObject, FMLObject> actionType = new FlexoActionType<CreateGetSetProperty, FlexoConceptObject, FMLObject>(
			"create_get_set_property", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateGetSetProperty makeNewAction(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateGetSetProperty(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConceptObject object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConceptObject object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateGetSetProperty.actionType, FlexoConcept.class);
		FlexoObjectImpl.addActionForClass(CreateGetSetProperty.actionType, FlexoConceptStructuralFacet.class);
	}

	private GetSetProperty<?> newGetSetProperty;

	CreateGetSetProperty(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public GetSetProperty<?> getNewFlexoProperty() {
		return newGetSetProperty;
	}

	@Override
	protected String getDefaultPropertyName() {
		return "property";
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {

		FMLModelFactory factory = getFocusedObject().getFMLModelFactory();
		newGetSetProperty = factory.newGetSetProperty();

		if (newGetSetProperty != null) {

			newGetSetProperty.setPropertyName(getPropertyName());
			newGetSetProperty.setGetControlGraph(getGetControlGraph());
			if (getSetControlGraph() != null) {
				newGetSetProperty.setSetControlGraph(getSetControlGraph());
			}
			finalizeDoAction(context);
		}

	}

	private FMLControlGraph getControlGraph;
	private FMLControlGraph setControlGraph;

	public FMLControlGraph getGetControlGraph() {
		return getControlGraph;
	}

	public void setGetControlGraph(FMLControlGraph getControlGraph) {
		if ((getControlGraph == null && this.getControlGraph != null)
				|| (getControlGraph != null && !getControlGraph.equals(this.getControlGraph))) {
			FMLControlGraph oldValue = this.getControlGraph;
			this.getControlGraph = getControlGraph;
			getPropertyChangeSupport().firePropertyChange("getControlGraph", oldValue, getControlGraph);
		}
	}

	public FMLControlGraph getSetControlGraph() {
		return setControlGraph;
	}

	public void setSetControlGraph(FMLControlGraph setControlGraph) {
		if ((setControlGraph == null && this.setControlGraph != null)
				|| (setControlGraph != null && !setControlGraph.equals(this.setControlGraph))) {
			FMLControlGraph oldValue = this.setControlGraph;
			this.setControlGraph = setControlGraph;
			getPropertyChangeSupport().firePropertyChange("setControlGraph", oldValue, setControlGraph);
		}
	}
}
