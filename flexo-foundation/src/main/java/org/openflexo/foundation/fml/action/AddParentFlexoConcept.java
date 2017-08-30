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

package org.openflexo.foundation.fml.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.InconsistentFlexoConceptHierarchyException;
import org.openflexo.foundation.fml.action.AbstractCreateFlexoConcept.ParentFlexoConceptEntry;

/**
 * This action allows to declare a new parent FlexoConcept for a given FlexoConcept
 * 
 * @author sylvain
 *
 */
@SuppressWarnings("serial")
public class AddParentFlexoConcept extends FlexoAction<AddParentFlexoConcept, FlexoConcept, FMLObject> {

	private static final Logger logger = Logger.getLogger(AddParentFlexoConcept.class.getPackage().getName());

	public static FlexoActionFactory<AddParentFlexoConcept, FlexoConcept, FMLObject> actionType = new FlexoActionFactory<AddParentFlexoConcept, FlexoConcept, FMLObject>(
			"add_parent_flexo_concept", FlexoActionFactory.defaultGroup, FlexoActionFactory.NORMAL_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public AddParentFlexoConcept makeNewAction(FlexoConcept focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new AddParentFlexoConcept(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConcept object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConcept object, Vector<FMLObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(AddParentFlexoConcept.actionType, FlexoConcept.class);
	}

	private final List<ParentFlexoConceptEntry> parentFlexoConceptEntries;

	AddParentFlexoConcept(FlexoConcept focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
		parentFlexoConceptEntries = new ArrayList<AbstractCreateFlexoConcept.ParentFlexoConceptEntry>();
		for (FlexoConcept parentConcept : getFocusedObject().getParentFlexoConcepts()) {
			addToParentConcepts(parentConcept);
		}
	}

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

	@Override
	protected void doAction(Object context) throws InconsistentFlexoConceptHierarchyException {
		logger.info("Add parent concepts");

		for (ParentFlexoConceptEntry entry : getParentFlexoConceptEntries()) {
			if (!getFocusedObject().getParentFlexoConcepts().contains(entry.getParentConcept())) {
				getFocusedObject().addToParentFlexoConcepts(entry.getParentConcept());
			}
		}

	}

}
