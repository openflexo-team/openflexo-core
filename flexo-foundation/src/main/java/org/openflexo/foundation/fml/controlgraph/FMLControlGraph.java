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
import org.openflexo.antar.binding.BindingVariable;
import org.openflexo.antar.binding.DataBinding;
import org.openflexo.foundation.fml.FMLRepresentationContext;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.VirtualModelModelFactory;
import org.openflexo.foundation.fml.binding.ControlGraphBindingModel;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;

/**
 * Abstract definition of a control graph
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(FMLControlGraph.FMLControlGraphImpl.class)
@Imports({ @Import(EditionAction.class), @Import(EmptyControlGraph.class), @Import(Sequence.class) })
public abstract interface FMLControlGraph extends FlexoConceptObject {

	@PropertyIdentifier(type = FMLControlGraphOwner.class)
	public static final String OWNER_KEY = "owner";
	@PropertyIdentifier(type = String.class)
	public static final String OWNER_CONTEXT_KEY = "ownerContext";

	@Getter(value = OWNER_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public FMLControlGraphOwner getOwner();

	@Setter(OWNER_KEY)
	public void setOwner(FMLControlGraphOwner owner);

	@Getter(value = OWNER_CONTEXT_KEY)
	@XMLAttribute
	public String getOwnerContext();

	@Setter(OWNER_CONTEXT_KEY)
	public void setOwnerContext(String context);

	@Override
	public String getFMLRepresentation(FMLRepresentationContext context);

	public void sequentiallyAppend(FMLControlGraph controlGraph);

	/**
	 * Return {@link BindingModel} to be used in the context of this {@link FMLControlGraph}<br>
	 * This is the {@link BindingModel} to be used to decode {@link DataBinding} which are defined in the context of this
	 * {@link FMLControlGraph}
	 * 
	 * @return
	 */
	@Override
	public BindingModel getBindingModel();

	/**
	 * Return {@link BindingModel} infered from this {@link FMLControlGraph}<br>
	 * The infered BindingModel might be the {@link BindingModel} given by {@link #getBindingModel()}, but can also redefine this one by
	 * supporting new {@link BindingVariable} which are defined in the scope of current {@link FMLControlGraph}
	 * 
	 * @return
	 */
	public BindingModel getInferedBindingModel();

	public FMLControlGraphOwner getRootOwner();

	public static abstract class FMLControlGraphImpl extends FlexoConceptObjectImpl implements FMLControlGraph {

		private ControlGraphBindingModel<?> bindingModel;

		@Override
		public ControlGraphBindingModel<?> getBindingModel() {
			if (bindingModel == null) {
				bindingModel = makeBindingModel();
			}
			return bindingModel;
		}

		protected ControlGraphBindingModel<?> makeBindingModel() {
			return new ControlGraphBindingModel(this);
		}

		@Override
		public ControlGraphBindingModel<?> getInferedBindingModel() {
			return getBindingModel();
		}

		@Override
		public FlexoConcept getFlexoConcept() {
			if (getOwner() != null) {
				return getOwner().getFlexoConcept();
			}
			return null;
		}

		@Override
		public void sequentiallyAppend(FMLControlGraph controlGraph) {

			FMLControlGraphOwner owner = getOwner();
			String ownerContext = getOwnerContext();
			VirtualModelModelFactory factory = getVirtualModelFactory();
			// Following statement is really important, we need first to "disconnect" actual control graph
			owner.setControlGraph(null, ownerContext);
			// Then we create the sequence
			Sequence sequence = factory.newSequence(this, controlGraph);
			sequence.setOwnerContext(ownerContext);
			owner.setControlGraph(sequence, ownerContext);

		}

		@Override
		public FMLControlGraphOwner getRootOwner() {
			if (getOwner() instanceof FMLControlGraph) {
				return ((FMLControlGraph) getOwner()).getRootOwner();
			}
			return getOwner();
		}
	}
}
