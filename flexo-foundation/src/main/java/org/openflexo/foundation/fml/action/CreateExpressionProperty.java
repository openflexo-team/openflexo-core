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

import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.ExpressionProperty;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoConceptStructuralFacet;

/**
 * Action allowing to create a {@link ExpressionProperty}<br>
 * 
 * To be valid, such action:
 * <ul>
 * <li>must be configured with a {@link FlexoConceptObject} as focused object</li>
 * <li>must declare a valid property name</li>
 * <li>must declare a valid expression</li>
 * <li>may declare a valid description</li>
 * </ul>
 */
public class CreateExpressionProperty extends AbstractCreateFlexoProperty<CreateExpressionProperty> implements Bindable {

	private static final Logger logger = Logger.getLogger(CreateExpressionProperty.class.getPackage().getName());

	public static FlexoActionFactory<CreateExpressionProperty, FlexoConceptObject, FMLObject> actionType = new FlexoActionFactory<CreateExpressionProperty, FlexoConceptObject, FMLObject>(
			"create_expression_property", FlexoActionFactory.newPropertyMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateExpressionProperty makeNewAction(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection,
				FlexoEditor editor) {
			return new CreateExpressionProperty(focusedObject, globalSelection, editor);
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
		FlexoObjectImpl.addActionForClass(CreateExpressionProperty.actionType, FlexoConcept.class);
		FlexoObjectImpl.addActionForClass(CreateExpressionProperty.actionType, FlexoConceptStructuralFacet.class);
	}

	private DataBinding<?> expression;

	private ExpressionProperty<?> newExpressionProperty;

	CreateExpressionProperty(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	public DataBinding<?> getExpression() {
		if (expression == null) {
			expression = new DataBinding<Object>(this, Object.class, DataBinding.BindingDefinitionType.GET);
			expression.setBindingName("expression");
			expression.setMandatory(true);

		}
		return expression;
	}

	public void setExpression(DataBinding<?> expression) {
		if (expression != null) {
			this.expression = new DataBinding<Object>(expression.toString(), this, Object.class, DataBinding.BindingDefinitionType.GET);
			expression.setBindingName("expression");
			expression.setMandatory(true);
		}
		getPropertyChangeSupport().firePropertyChange("expression", null, getExpression());
	}

	@Override
	public ExpressionProperty<?> getNewFlexoProperty() {
		return newExpressionProperty;
	}

	@Override
	protected String getDefaultPropertyName() {
		return "property";
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException {

		FMLModelFactory factory = getFocusedObject().getFMLModelFactory();
		newExpressionProperty = factory.newExpressionProperty();

		if (newExpressionProperty != null) {

			newExpressionProperty.setPropertyName(getPropertyName());
			if (getExpression() != null && getExpression().isSet() && getExpression().isValid()) {
				newExpressionProperty.setExpression((DataBinding) getExpression());
			}

			finalizeDoAction(context);
		}

	}

	private boolean isNotifying = false;

	@Override
	public void notifiedBindingChanged(DataBinding<?> dataBinding) {
		if (isNotifying) {
			return;
		}
		isNotifying = true;
		getPropertyChangeSupport().firePropertyChange("expression", null, getExpression());
		isNotifying = false;
	}

}
