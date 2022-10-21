/**
 * 
 * Copyright (c) 2014, Openflexo
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

import org.openflexo.connie.binding.AbstractConstructor;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;

@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractCreationScheme.AbstractCreationSchemeImpl.class)
public abstract interface AbstractCreationScheme extends FlexoBehaviour, AbstractConstructor {

	public boolean isDefaultCreationScheme();

	@Override
	public Type getNewInstanceType();

	public static abstract class AbstractCreationSchemeImpl extends FlexoBehaviourImpl implements AbstractCreationScheme {

		@Override
		public Type getNewInstanceType() {
			return getAnalyzedReturnType();
		}

		@Override
		public Type getAnalyzedReturnType() {
			if (getFlexoConcept() != null)
				return getFlexoConcept().getInstanceType();
			return null;
		}

		@Override
		public boolean isDefaultCreationScheme() {
			return false;
		}
		
		@Override
		public Type getReturnType() {
			return getNewInstanceType();
		}
	}
}
