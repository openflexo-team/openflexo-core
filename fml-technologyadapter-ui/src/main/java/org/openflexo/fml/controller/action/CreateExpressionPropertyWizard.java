/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.action;

import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.action.CreateExpressionProperty;
import org.openflexo.gina.annotation.FIBPanel;
import org.openflexo.view.controller.FlexoController;

public class CreateExpressionPropertyWizard extends AbstractCreateFlexoPropertyWizard<CreateExpressionProperty> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CreateExpressionPropertyWizard.class.getPackage().getName());

	private static final String NO_EXPRESSION = "please_define_an_expression";
	private static final String INVALID_EXPRESSION = "expression_is_not_valid";

	public CreateExpressionPropertyWizard(CreateExpressionProperty action, FlexoController controller) {
		super(action, controller);
	}

	@Override
	protected DescribeExpressionProperty makeDescriptionStep() {
		return new DescribeExpressionProperty();
	}

	@Override
	public String getWizardTitle() {
		return getAction().getLocales().localizedForKey("create_expression_property");
	}

	@Override
	public DescribeExpressionProperty getDescribeProperty() {
		return (DescribeExpressionProperty) super.getDescribeProperty();
	}

	/**
	 * This step is used to set new property parameters
	 * 
	 * @author sylvain
	 * 
	 */
	@FIBPanel("Fib/Wizard/CreateFMLElement/DescribeExpressionProperty.fib")
	public class DescribeExpressionProperty extends DescribeProperty implements Bindable {

		@Override
		public CreateExpressionProperty getAction() {
			return super.getAction();
		}

		@Override
		public String getTitle() {
			return getAction().getLocales().localizedForKey("describe_expression_property");
		}

		@Override
		public boolean isValid() {

			if (!super.isValid()) {
				return false;
			}

			if (getExpression() == null || !getExpression().isSet()) {
				setIssueMessage(getAction().getLocales().localizedForKey(NO_EXPRESSION), IssueMessageType.ERROR);
				return false;
			}
			else if (getExpression() != null && getExpression().isSet() && !getExpression().isValid()) {
				setIssueMessage(getAction().getLocales().localizedForKey(INVALID_EXPRESSION), IssueMessageType.ERROR);
				return false;
			}

			return true;
		}

		private DataBinding<?> expression;

		public DataBinding<?> getExpression() {
			if (expression == null) {
				expression = new DataBinding<>(this, Object.class, DataBinding.BindingDefinitionType.GET);
				expression.setBindingName("expression");
				expression.setMandatory(true);

			}
			return expression;
		}

		public void setExpression(DataBinding<?> expression) {
			if (expression != null) {
				this.expression = new DataBinding<>(expression.toString(), this, Object.class, DataBinding.BindingDefinitionType.GET);
				expression.setBindingName("expression");
				expression.setMandatory(true);
			}
			getAction().setExpression(expression);
			getPropertyChangeSupport().firePropertyChange("expression", null, expression);
			checkValidity();
		}

		@Override
		public BindingModel getBindingModel() {
			return getAction().getFlexoConcept().getBindingModel();
		}

		@Override
		public BindingFactory getBindingFactory() {
			return getAction().getFlexoConcept().getBindingFactory();
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
			checkValidity();
		}

		@Override
		public void notifiedBindingDecoded(DataBinding<?> dataBinding) {
		}
	}

}
