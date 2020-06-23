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

package org.openflexo.foundation.fml.controlgraph;

import org.openflexo.connie.BindingModel;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.pamela.annotations.CloningStrategy;
import org.openflexo.pamela.annotations.Embedded;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.CloningStrategy.StrategyType;

/**
 * A default implementation for an object "owning" a {@link FMLControlGraph}
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(DefaultFMLControlGraphOwner.DefaultFMLControlGraphOwnerImpl.class)
public abstract interface DefaultFMLControlGraphOwner extends FMLControlGraphOwner {

	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String CONTROL_GRAPH_KEY = "controlGraph";
	@PropertyIdentifier(type = FlexoConceptObject.class)
	public static final String CONCEPT_OBJECT_KEY = "conceptObject";

	@Getter(value = CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	@Embedded
	public FMLControlGraph getControlGraph();

	@Setter(CONTROL_GRAPH_KEY)
	public void setControlGraph(FMLControlGraph aControlGraph);

	@Getter(CONCEPT_OBJECT_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public FlexoConceptObject getConceptObject();

	@Setter(CONCEPT_OBJECT_KEY)
	public void setConceptObject(FlexoConceptObject owner);

	public static abstract class DefaultFMLControlGraphOwnerImpl extends FlexoConceptObjectImpl implements DefaultFMLControlGraphOwner {

		/**
		 * Return control graph identified by supplied owner's context
		 * 
		 * @param ownerContext
		 * @return
		 */
		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			return getControlGraph();
		}

		/**
		 * Sets control graph identified by supplied owner's context
		 * 
		 * @param controlGraph
		 * @param ownerContext
		 */
		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {
			setControlGraph(controlGraph);
		}

		/**
		 * Return base BindingModel to be used for supplied control graph
		 * 
		 * @param controlGraph
		 * @return
		 */
		@Override
		public BindingModel getBaseBindingModel(FMLControlGraph controlGraph) {
			return getBindingModel();
		}

		/**
		 * This method will apply reduction rules to the current control graph<br>
		 * This means that adequate structural modifications will be performed to reduce the complexity of this control graph owner<br>
		 * (unnecessary EmptyControlGraph will be removed, for example)
		 */
		@Override
		public void reduce() {
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			if (getConceptObject() != null) {
				return getConceptObject().getFlexoConcept();
			}
			return null;
		}

		@Override
		public String getURI() {
			return null;
		}

		@Override
		public BindingModel getBindingModel() {
			if (getConceptObject() instanceof FMLControlGraph) {
				return ((FMLControlGraph) getConceptObject()).getInferedBindingModel();
			}
			return getConceptObject().getBindingModel();
		}
	}
}
