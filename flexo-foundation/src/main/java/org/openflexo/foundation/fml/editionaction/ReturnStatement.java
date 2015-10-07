/**
 * 
 * Copyright (c) 2015, Openflexo
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

package org.openflexo.foundation.fml.editionaction;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FMLRepresentationContext.FMLRepresentationOutput;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext;
import org.openflexo.foundation.fml.rt.RunTimeEvaluationContext.ReturnException;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.XMLElement;

/**
 * Return statement<br>
 * Note that the root control graph in which this action is embedded should be typed with a compatible type
 * 
 * 
 * @author sylvain
 *
 * @param <T>
 */
@ModelEntity
@ImplementationClass(ReturnStatement.ReturnActionImpl.class)
@XMLElement
public interface ReturnStatement<T> extends AbstractAssignationAction<T> {

	public static abstract class ReturnActionImpl<T> extends AbstractAssignationActionImpl<T>implements ReturnStatement<T> {

		private static final Logger logger = Logger.getLogger(ReturnStatement.class.getPackage().getName());

		@Override
		public Type getInferedType() {
			return getAssignableType();
		}

		@Override
		public T execute(RunTimeEvaluationContext evaluationContext) throws ReturnException, FlexoException {
			T value = getAssignationValue(evaluationContext);
			throw new ReturnException(value);
		}

		@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			out.append("return " + (getAssignableAction() != null ? getAssignableAction().getFMLRepresentation() : "") + ";", context);
			return out.toString();
		}

		@Override
		public String getStringRepresentation() {
			if (getAssignableAction() != null) {
				return getHeaderContext() + "return " + getAssignableAction().getStringRepresentation();
			}
			else {
				return getHeaderContext() + "return" + " = ???";
			}
		}

	}
}
