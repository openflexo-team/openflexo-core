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

import java.security.InvalidParameterException;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoActionType;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.fml.AbstractVirtualModel;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.InconsistentFlexoConceptHierarchyException;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.toolbox.StringUtils;

public class CreateFlexoConcept extends AbstractCreateFlexoConcept<CreateFlexoConcept, AbstractVirtualModel<?>, FMLObject> {

	private static final Logger logger = Logger.getLogger(CreateFlexoConcept.class.getPackage().getName());

	public static FlexoActionType<CreateFlexoConcept, AbstractVirtualModel<?>, FMLObject> actionType = new FlexoActionType<CreateFlexoConcept, AbstractVirtualModel<?>, FMLObject>(
			"add_new_flexo_concept", FlexoActionType.newMenu, FlexoActionType.defaultGroup, FlexoActionType.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateFlexoConcept makeNewAction(AbstractVirtualModel<?> focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateFlexoConcept(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(AbstractVirtualModel<?> object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(AbstractVirtualModel<?> object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateFlexoConcept.actionType, VirtualModel.class);
	}

	private String newFlexoConceptName;
	private String newFlexoConceptDescription;
	private FlexoConcept newFlexoConcept;

	public boolean switchNewlyCreatedFlexoConcept = true;

	CreateFlexoConcept(AbstractVirtualModel<?> focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	protected void doAction(Object context) throws NotImplementedException, InvalidParameterException,
			InconsistentFlexoConceptHierarchyException {

		FMLModelFactory factory = getFocusedObject().getFMLModelFactory();

		newFlexoConcept = factory.newFlexoConcept();
		newFlexoConcept.setName(getNewFlexoConceptName());

		performSetParentConcepts();

		getFocusedObject().addToFlexoConcepts(newFlexoConcept);
	}

	@Override
	public FlexoConcept getNewFlexoConcept() {
		return newFlexoConcept;
	}

	public String getNewFlexoConceptName() {
		return newFlexoConceptName;
	}

	public void setNewFlexoConceptName(String newFlexoConceptName) {
		if ((newFlexoConceptName == null && this.newFlexoConceptName != null)
				|| (newFlexoConceptName != null && !newFlexoConceptName.equals(this.newFlexoConceptName))) {
			String oldValue = this.newFlexoConceptName;
			this.newFlexoConceptName = newFlexoConceptName;
			getPropertyChangeSupport().firePropertyChange("newFlexoConceptName", oldValue, newFlexoConceptName);
		}
	}

	public String getNewFlexoConceptDescription() {
		return newFlexoConceptDescription;
	}

	public void setNewFlexoConceptDescription(String newFlexoConceptDescription) {
		if ((newFlexoConceptDescription == null && this.newFlexoConceptDescription != null)
				|| (newFlexoConceptDescription != null && !newFlexoConceptDescription.equals(this.newFlexoConceptDescription))) {
			String oldValue = this.newFlexoConceptDescription;
			this.newFlexoConceptDescription = newFlexoConceptDescription;
			getPropertyChangeSupport().firePropertyChange("newFlexoConceptDescription", oldValue, newFlexoConceptDescription);
		}
	}

	@Override
	public boolean isValid() {
		if (StringUtils.isEmpty(newFlexoConceptName)) {
			return false;
		} else if (getFocusedObject() instanceof VirtualModel && getFocusedObject().getFlexoConcept(newFlexoConceptName) != null) {
			return false;
		}
		return true;
	}

	@Override
	public int getExpectedProgressSteps() {
		return 10;
	}

}
