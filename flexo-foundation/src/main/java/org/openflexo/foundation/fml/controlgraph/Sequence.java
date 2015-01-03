/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.fml.controlgraph;

import org.openflexo.antar.binding.BindingModel;
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
 * Encodes a sequence as a sequential definition of two control graphs
 * 
 * @author sylvain
 * 
 */
@ModelEntity
@ImplementationClass(Sequence.SequenceImpl.class)
@XMLElement
public interface Sequence extends FMLControlGraph, FMLControlGraphOwner {

	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String CONTROL_GRAPH1_KEY = "controlGraph1";
	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String CONTROL_GRAPH2_KEY = "controlGraph2";

	@Getter(value = CONTROL_GRAPH1_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	@XMLElement(context = "ControlGraph1_")
	public FMLControlGraph getControlGraph1();

	@Setter(CONTROL_GRAPH1_KEY)
	public void setControlGraph1(FMLControlGraph aControlGraph);

	@Getter(value = CONTROL_GRAPH2_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@Embedded
	@CloningStrategy(StrategyType.CLONE)
	@XMLElement(context = "ControlGraph2_")
	public FMLControlGraph getControlGraph2();

	@Setter(CONTROL_GRAPH2_KEY)
	public void setControlGraph2(FMLControlGraph aControlGraph);

	public static abstract class SequenceImpl extends FMLControlGraphImpl implements Sequence {

		@Override
		public void setControlGraph1(FMLControlGraph aControlGraph) {
			if (aControlGraph != null) {
				aControlGraph.setOwnerContext(CONTROL_GRAPH1_KEY);
			}
			performSuperSetter(CONTROL_GRAPH1_KEY, aControlGraph);
		}

		@Override
		public void setControlGraph2(FMLControlGraph aControlGraph) {
			if (aControlGraph != null) {
				aControlGraph.setOwnerContext(CONTROL_GRAPH2_KEY);
			}
			performSuperSetter(CONTROL_GRAPH2_KEY, aControlGraph);
		}

		@Override
		public void sequentiallyAppend(FMLControlGraph controlGraph) {

			getControlGraph2().sequentiallyAppend(controlGraph);
		}

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			if (CONTROL_GRAPH1_KEY.equals(ownerContext)) {
				return getControlGraph1();
			} else if (CONTROL_GRAPH2_KEY.equals(ownerContext)) {
				return getControlGraph2();
			}
			return null;
		}

		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {

			if (CONTROL_GRAPH1_KEY.equals(ownerContext)) {
				setControlGraph1(controlGraph);
			} else if (CONTROL_GRAPH2_KEY.equals(ownerContext)) {
				setControlGraph2(controlGraph);
			}
		}

		@Override
		public BindingModel getBaseBindingModel(FMLControlGraph controlGraph) {
			if (controlGraph == getControlGraph1()) {
				return getBindingModel();
			} else if (controlGraph == getControlGraph2()) {
				return getControlGraph1().getInferedBindingModel();
			}
			return null;
		}

		/*@Override
		public void setOwner(FMLControlGraphOwner owner) {
			System.out.println("BEGIN / Sequence, on set le owner de " + this + " avec " + owner);
			performSuperSetter(OWNER_KEY, owner);
			System.out.println("END / Sequence, on a sette le owner de " + this + " avec " + owner);
		}*/

	}
}
