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
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.model.annotations.ModelEntity;

/**
 * Represents an object that "owns" a control graph
 * 
 * @author sylvain
 * 
 */
@ModelEntity(isAbstract = true)
// @ImplementationClass(FMLControlGraphOwner.FMLControlGraphOwnerImpl.class)
public abstract interface FMLControlGraphOwner extends FlexoConceptObject {

	/**
	 * Return control graph identified by supplied owner's context
	 * 
	 * @param ownerContext
	 * @return
	 */
	public FMLControlGraph getControlGraph(String ownerContext);

	/**
	 * Sets control graph identified by supplied owner's context
	 * 
	 * @param controlGraph
	 * @param ownerContext
	 */
	public void setControlGraph(FMLControlGraph controlGraph, String ownerContext);

	/**
	 * Return base BindingModel to be used for supplied control graph
	 * 
	 * @param controlGraph
	 * @return
	 */
	public BindingModel getBaseBindingModel(FMLControlGraph controlGraph);

	/**
	 * This method will apply reduction rules to the current control graph<br>
	 * This means that adequate structural modifications will be performed to reduce the complexity of this control graph owner<br>
	 * (unnecessary EmptyControlGraph will be removed, for example)
	 */
	public void reduce();

	/*public static abstract class FMLControlGraphOwnerImpl extends FlexoConceptObjectImpl implements FMLControlGraphOwner {

	}*/
}
