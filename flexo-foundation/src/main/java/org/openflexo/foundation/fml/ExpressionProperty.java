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
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

/**
 * A {@link ExpressionProperty} is a particular implementation of a {@link FlexoProperty} allowing to access data using an expression<br>
 * Access to data is read-only or read-write depending on getExpression settable property
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
	public static final String GET_EXPRESSION_KEY = "getExpression";

	@Getter(value = GET_EXPRESSION_KEY)
	@XMLAttribute
	public DataBinding<? super T> getGetExpression();

	@Setter(GET_EXPRESSION_KEY)
	public void setGetExpression(DataBinding<? super T> assignation);

	public static abstract class ExpressionPropertyImpl<T> extends FlexoPropertyImpl<T> implements ExpressionProperty<T> {

		// private static final Logger logger = Logger.getLogger(FlexoRole.class.getPackage().getName());

		private DataBinding<? super T> getExpression;

		@Override
		public boolean isReadOnly() {
			return !getGetExpression().isSettable();
		}

		@Override
		public boolean defaultBehaviourIsToBeDeleted() {
			return false;
		}

		@Override
		public DataBinding<? super T> getGetExpression() {
			if (getExpression == null) {
				getExpression = new DataBinding<Object>(this, Object.class, DataBinding.BindingDefinitionType.GET_SET);
				getExpression.setBindingName("getExpression");
				getExpression.setMandatory(true);

			}
			return getExpression;
		}

		@Override
		public void setGetExpression(DataBinding<? super T> getExpression) {
			if (getExpression != null) {
				this.getExpression = new DataBinding<Object>(getExpression.toString(), this, Object.class,
						DataBinding.BindingDefinitionType.GET_SET);
				getExpression.setBindingName("getExpression");
				getExpression.setMandatory(true);
			}
			notifiedBindingChanged(getExpression);
		}

		@Override
		public Type getType() {
			if (getGetExpression() != null) {
				return getGetExpression().getAnalyzedType();
			}
			return Object.class;
		}
	}

}
