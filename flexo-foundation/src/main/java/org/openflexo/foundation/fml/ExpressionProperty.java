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

package org.openflexo.foundation.fml;

import java.lang.reflect.Type;

import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

/**
 * A {@link ExpressionProperty} is a particular implementation of a {@link FlexoProperty} allowing to access data using an expression<br>
 * Access to data is read-only or read-write depending on expression settable property
 * 
 * 
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(ExpressionProperty.ExpressionPropertyImpl.class)
@XMLElement
public abstract interface ExpressionProperty<T> extends FlexoProperty<T> {

	@PropertyIdentifier(type = DataBinding.class)
	public static final String EXPRESSION_KEY = "expression";

	@Getter(value = EXPRESSION_KEY)
	@XMLAttribute
	public DataBinding<? super T> getExpression();

	@Setter(EXPRESSION_KEY)
	public void setExpression(DataBinding<? super T> expression);

	public static abstract class ExpressionPropertyImpl<T> extends FlexoPropertyImpl<T> implements ExpressionProperty<T> {

		// private static final Logger logger = Logger.getLogger(FlexoRole.class.getPackage().getName());

		private DataBinding<? super T> expression;

		/**
		 * Return flag indicating whether this property is abstract
		 * 
		 * @return
		 */
		@Override
		public boolean isAbstract() {
			return false;
		}

		@Override
		public boolean isReadOnly() {
			return !getExpression().isSettable();
		}

		@Override
		public boolean defaultBehaviourIsToBeDeleted() {
			return false;
		}

		@Override
		public DataBinding<? super T> getExpression() {
			if (expression == null) {
				expression = new DataBinding<Object>(this, Object.class, DataBinding.BindingDefinitionType.GET);
				expression.setBindingName("expression");
				expression.setMandatory(true);

			}
			return expression;
		}

		@Override
		public void setExpression(DataBinding<? super T> expression) {
			if (expression != null) {
				this.expression = new DataBinding<Object>(expression.toString(), this, Object.class, DataBinding.BindingDefinitionType.GET);
				this.expression.setBindingName("expression");
				this.expression.setMandatory(true);
			}
			notifiedBindingChanged(expression);
		}

		private boolean isAnalysingType = false;

		@Override
		public Type getType() {

			if (isAnalysingType) {
				return Object.class;
			}
			else {
				isAnalysingType = true;

				/*System.out.println("Le type de " + getExpression() + " c'est quoi ?");
				System.out.println("BM=" + getBindingModel());
				System.out.println("valid=" + getExpression().isValid());
				System.out.println("return: " + getExpression().getAnalyzedType())*/

				// getBindingModel();

				if (getExpression() != null && getExpression().isValid()) {
					Type returned = getExpression().getAnalyzedType();
					// System.out.println("je retourne " + returned);
					// System.out.println("valid=" + getExpression().isValid());

					isAnalysingType = false;
					return returned;
				}

				isAnalysingType = false;
			}

			return Object.class;
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("FlexoProperty " + getName() + " " + getExpression() + " as " + getTypeDescription() + " cardinality="
					+ getCardinality() + ";", context);
			return out.toString();
		}

	}

}
