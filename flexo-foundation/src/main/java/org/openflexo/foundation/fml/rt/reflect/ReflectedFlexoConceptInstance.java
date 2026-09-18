/*
 * Copyright (c) 2013-2017, Openflexo
 *
 * This file is part of Flexo-foundation, a component of the software infrastructure
 * developed at Openflexo.
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
 *           Additional permission under GNU GPL version 3 section 7
 *           If you modify this Program, or any covered work, by linking or
 *           combining it with software containing parts covered by the terms
 *           of EPL 1.0, the licensors of this Program grant you additional permission
 *           to convey the resulting work.
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

package org.openflexo.foundation.fml.rt.reflect;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.rt.FlexoConceptInstance;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;

/**
 * A {@link FlexoConceptInstance} reflecting a given object (the support object) as a {@link FlexoConceptInstance}
 * 
 * @param <S>
 *            type of support object
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(ReflectedFlexoConceptInstance.ReflectedFlexoConceptInstanceImpl.class)
public interface ReflectedFlexoConceptInstance<S> extends FlexoConceptInstance {

	String SUPPORT_OBJECT_KEY = "supportObject";

	/**
	 * Return {@link Row} support object
	 * 
	 * @return
	 */
	@Getter(value = SUPPORT_OBJECT_KEY, ignoreType = true)
	public S getSupportObject();

	/**
	 * Sets {@link Row} support object
	 * 
	 * @return
	 */
	@Setter(SUPPORT_OBJECT_KEY)
	public void setSupportObject(S supportObject);

	/**
	 * Default implementation for {@link ReflectedFlexoConceptInstance}
	 * 
	 * @author sylvain
	 *
	 */
	abstract class ReflectedFlexoConceptInstanceImpl<S> extends FlexoConceptInstanceImpl implements ReflectedFlexoConceptInstance<S> {

		@SuppressWarnings("unused")
		private static final Logger logger = FlexoLogger.getLogger(ReflectedFlexoConceptInstance.class.getPackage().toString());

		@Override
		public ReflectedVirtualModelInstance<?, ?, ?, ?> getVirtualModelInstance() {
			return (ReflectedVirtualModelInstance<?, ?, ?, ?>) super.getVirtualModelInstance();
		}

	}
}
