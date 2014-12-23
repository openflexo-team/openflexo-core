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
package org.openflexo.foundation.fml.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.LongRunningAction;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.toolbox.PropertyChangedSupportDefaultImplementation;

/**
 * Abstract action creating a {@link FlexoConcept} or any of its subclass
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractCreateFlexoConcept<A extends FlexoAction<A, T1, T2>, T1 extends FlexoObject, T2 extends FMLObject>
		extends FlexoAction<A, T1, T2> implements LongRunningAction {

	private static final Logger logger = Logger.getLogger(AbstractCreateFlexoConcept.class.getPackage().getName());

	private final List<ParentFlexoConceptEntry> parentFlexoConceptEntries;

	AbstractCreateFlexoConcept(FlexoActionType<A, T1, T2> actionType, T1 focusedObject, Vector<T2> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		parentFlexoConceptEntries = new ArrayList<AbstractCreateFlexoConcept.ParentFlexoConceptEntry>();
	}

	public abstract FlexoConcept getNewFlexoConcept();

	public List<ParentFlexoConceptEntry> getParentFlexoConceptEntries() {
		return parentFlexoConceptEntries;
	}

	public ParentFlexoConceptEntry newParentFlexoConceptEntry() {
		ParentFlexoConceptEntry returned = new ParentFlexoConceptEntry();
		parentFlexoConceptEntries.add(returned);
		getPropertyChangeSupport().firePropertyChange("parentFlexoConceptEntries", null, returned);
		return returned;
	}

	public void deleteParentFlexoConceptEntry(ParentFlexoConceptEntry parentFlexoConceptEntryToDelete) {
		parentFlexoConceptEntries.remove(parentFlexoConceptEntryToDelete);
		parentFlexoConceptEntryToDelete.delete();
		getPropertyChangeSupport().firePropertyChange("parentFlexoConceptEntries", parentFlexoConceptEntryToDelete, null);
	}

	public ParentFlexoConceptEntry addToParentConcepts(FlexoConcept parentFlexoConcept) {
		ParentFlexoConceptEntry newParentFlexoConceptEntry = new ParentFlexoConceptEntry(parentFlexoConcept);
		parentFlexoConceptEntries.add(newParentFlexoConceptEntry);
		getPropertyChangeSupport().firePropertyChange("parentFlexoConceptEntries", null, newParentFlexoConceptEntry);
		return newParentFlexoConceptEntry;
	}

	protected void performSetParentConcepts() {
		for (ParentFlexoConceptEntry entry : getParentFlexoConceptEntries()) {
			getNewFlexoConcept().addToParentFlexoConcepts(entry.getParentConcept());
		}
	}

	public static class ParentFlexoConceptEntry extends PropertyChangedSupportDefaultImplementation {

		private FlexoConcept parentConcept;

		public ParentFlexoConceptEntry() {
			super();
		}

		public ParentFlexoConceptEntry(FlexoConcept parentConcept) {
			super();
			this.parentConcept = parentConcept;
		}

		public void delete() {
			parentConcept = null;
		}

		public FlexoConcept getParentConcept() {
			return parentConcept;
		}

		public void setParentConcept(FlexoConcept parentConcept) {
			if ((parentConcept == null && this.parentConcept != null)
					|| (parentConcept != null && !parentConcept.equals(this.parentConcept))) {
				FlexoConcept oldValue = this.parentConcept;
				this.parentConcept = parentConcept;
				getPropertyChangeSupport().firePropertyChange("parentConcept", oldValue, parentConcept);
			}
		}
	}
}
