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

package org.openflexo.foundation.fml.controlgraph;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.binding.IterationActionBindingModel;
import org.openflexo.foundation.fml.editionaction.EditionAction;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Embedded;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractIterationAction.AbstractIterationActionImpl.class)
public interface AbstractIterationAction extends ControlStructureAction, FMLControlGraphOwner {

	@PropertyIdentifier(type = String.class)
	public static final String ITERATOR_NAME_KEY = "iteratorName";
	@PropertyIdentifier(type = FMLControlGraph.class)
	public static final String CONTROL_GRAPH_KEY = "controlGraph";

	@Getter(value = ITERATOR_NAME_KEY)
	@XMLAttribute
	public String getIteratorName();

	@Setter(ITERATOR_NAME_KEY)
	public void setIteratorName(String iteratorName);

	public Type getItemType();

	/**
	 * Returns the control graph on which we iterate
	 * 
	 * @return
	 */
	@Getter(value = CONTROL_GRAPH_KEY, inverse = FMLControlGraph.OWNER_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	@XMLElement(context = "ControlGraph_")
	@Embedded
	public FMLControlGraph getControlGraph();

	@Setter(CONTROL_GRAPH_KEY)
	public void setControlGraph(FMLControlGraph aControlGraph);

	public static abstract class AbstractIterationActionImpl extends ControlStructureActionImpl implements AbstractIterationAction {

		private static final Logger logger = Logger.getLogger(AbstractIterationAction.class.getPackage().getName());

		private String iteratorName = "item";

		@Override
		public String getIteratorName() {
			return iteratorName;
		}

		@Override
		public void setIteratorName(String iteratorName) {
			if (this.iteratorName == null || !this.iteratorName.equals(iteratorName)) {
				String oldValue = this.iteratorName;
				this.iteratorName = iteratorName;
				// rebuildInferedBindingModel();
				getPropertyChangeSupport().firePropertyChange(ITERATOR_NAME_KEY, oldValue, iteratorName);
			}
		}

		@Override
		protected IterationActionBindingModel makeInferedBindingModel() {
			return new IterationActionBindingModel(this);
		}

		@Deprecated
		@Override
		public void addToActions(EditionAction anAction) {
			// FMLControlGraphConverter.addToActions(this, CONTROL_GRAPH_KEY, anAction);
		}

		@Deprecated
		@Override
		public void removeFromActions(EditionAction anAction) {
			// FMLControlGraphConverter.removeFromActions(this, CONTROL_GRAPH_KEY, anAction);
		}

		@Override
		public void reduce() {
			if (getControlGraph() instanceof FMLControlGraphOwner) {
				((FMLControlGraphOwner) getControlGraph()).reduce();
			}
		}

		@Override
		public FMLControlGraph getControlGraph(String ownerContext) {
			if (CONTROL_GRAPH_KEY.equals(ownerContext)) {
				return getControlGraph();
			}
			return null;
		}

		@Override
		public void setControlGraph(FMLControlGraph controlGraph, String ownerContext) {

			if (CONTROL_GRAPH_KEY.equals(ownerContext)) {
				setControlGraph(controlGraph);
			}
		}

		@Override
		public void setControlGraph(FMLControlGraph aControlGraph) {
			if (aControlGraph != null) {
				aControlGraph.setOwnerContext(CONTROL_GRAPH_KEY);
			}
			performSuperSetter(CONTROL_GRAPH_KEY, aControlGraph);
		}

		@Override
		public void setOwner(FMLControlGraphOwner owner) {
			performSuperSetter(OWNER_KEY, owner);
			if (getControlGraph() != null) {
				getControlGraph().getBindingModel().setBaseBindingModel(getBaseBindingModel(getControlGraph()));
			}
		}

		@Override
		public Type getInferedType() {
			if (getControlGraph() != null) {
				return getControlGraph().getInferedType();
			}
			return Void.class;
		}

		@Override
		public void accept(FMLControlGraphVisitor visitor) {
			super.accept(visitor);
			if (getControlGraph() != null) {
				getControlGraph().accept(visitor);
			}
		}

	}

}
