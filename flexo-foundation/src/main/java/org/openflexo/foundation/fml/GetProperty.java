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

import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLElement;

/**
 * A {@link GetProperty} is a particular implementation of a {@link FlexoProperty} allowing to access data using a typed control graph<br>
 * Access to data is read-only
 * 
 * 
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(GetProperty.GetPropertyImpl.class)
@XMLElement
public abstract interface GetProperty<T> extends FlexoProperty<T> {

	// @PropertyIdentifier(type = Type.class)
	// public static final String TYPE_KEY = "type";

	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String GET_CONTROL_GRAPH_KEY = "getControlGraph";

	@Getter(value = GET_CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	@XMLElement(context = "GetControlGraph_")
	@Embedded
	public FMLControlGraph getGetControlGraph();

	@Setter(GET_CONTROL_GRAPH_KEY)
	public void setGetControlGraph(FMLControlGraph aControlGraph);

	/*@Override
	@Getter(value = TYPE_KEY, ignoreType = true)
	@XMLAttribute
	public Type getType();

	@Setter(TYPE_KEY)
	public void setType(Type type);*/

	public static abstract class GetPropertyImpl<T> extends FlexoPropertyImpl<T> implements GetProperty<T> {

		// private static final Logger logger = Logger.getLogger(FlexoRole.class.getPackage().getName());

		@Override
		public boolean isReadOnly() {
			return true;
		}

		@Override
		public Type getType() {
			/*if (getGetControlGraph() != null) {
				return getGetControlGraph().get
			}*/
			return Object.class;
		}

		@Override
		public boolean defaultBehaviourIsToBeDeleted() {
			return false;
		}

	}

}
