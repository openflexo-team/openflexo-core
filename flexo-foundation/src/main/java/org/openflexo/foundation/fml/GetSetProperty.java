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

import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphOwner;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphVisitor;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;

/**
 * A {@link GetProperty} which can also be written, declared in FML with a {@code get()} block and a {@code set(...)} block (see
 * {@link #getSetControlGraph()}). It is read-only as long as it has no {@code set} block.
 * <p>
 * Example (excerpt of {@code FML/Library.fml} in the {@code flexo-test-resources} test resource center):
 *
 * <pre>
 * String displayName {
 *     String get() {
 *         return "Shelf " + label;
 *     }
 *     set(String value) {
 *         label = value;
 *     }
 * };
 * </pre>
 *
 * @author sylvain
 *
 */
@ModelEntity
@ImplementationClass(GetSetProperty.GetSetPropertyImpl.class)
@XMLElement
public abstract interface GetSetProperty<T> extends GetProperty<T> {

	@PropertyIdentifier(type = String.class)
	public static final String VALUE_VARIABLE_NAME_KEY = "valueVariableName";

	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String SET_CONTROL_GRAPH_KEY = "setControlGraph";

	/**
	 * Return the name of the variable holding the assigned value in the {@code set} block: {@code value}<br>
	 * Note that the parser does not use the parameter name declared in {@code set(...)}: in the body of the {@code set} block, the assigned
	 * value is always accessed as {@code value}, neither through the declared name nor through {@code parameters} (known defect
	 * {@code CORE-D-3}, see {@code KNOWN_DEFECTS.md} in openflexo-core)
	 *
	 * @return
	 */
	@Getter(value = VALUE_VARIABLE_NAME_KEY, defaultValue = "value")
	@XMLAttribute
	public String getValueVariableName();

	@Setter(VALUE_VARIABLE_NAME_KEY)
	public void setValueVariableName(String valueVariableName);

	@Getter(value = SET_CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@CloningStrategy(StrategyType.CLONE)
	@XMLElement(context = "SetControlGraph_")
	@Embedded
	public FMLControlGraph getSetControlGraph();

	@Setter(SET_CONTROL_GRAPH_KEY)
	public void setSetControlGraph(FMLControlGraph aControlGraph);

	public static abstract class GetSetPropertyImpl<T> extends GetPropertyImpl<T> implements GetSetProperty<T> {

		// private static final Logger logger = Logger.getLogger(FlexoRole.class.getPackage().getName());

		@Override
		public boolean isReadOnly() {
			return getSetControlGraph() == null;
		}

		@Override
		public void setSetControlGraph(FMLControlGraph aControlGraph) {
			if (aControlGraph != null) {
				aControlGraph.setOwnerContext(SET_CONTROL_GRAPH_KEY);
			}
			performSuperSetter(SET_CONTROL_GRAPH_KEY, aControlGraph);
		}

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			if (SET_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				return getSetControlGraph();
			}
			return super.getControlGraph(ownerContext);
		}

		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {

			if (SET_CONTROL_GRAPH_KEY.equals(ownerContext)) {
				setSetControlGraph(controlGraph);
			}
			else {
				super.setControlGraph(controlGraph, ownerContext);
			}
		}

		@Override
		public void reduce() {
			super.reduce();
			if (getSetControlGraph() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getSetControlGraph()).reduce();
			}
		}

		@Override
		public void finalizeDeserialization() {

			if (getSetControlGraph() != null) {
				getSetControlGraph().accept(new FMLControlGraphVisitor() {
					@Override
					public void visit(FMLControlGraph controlGraph) {
						controlGraph.finalizeDeserialization();
					}
				});
			}

			super.finalizeDeserialization();

		}

	}

}
