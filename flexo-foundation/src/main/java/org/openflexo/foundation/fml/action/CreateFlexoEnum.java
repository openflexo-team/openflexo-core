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
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.foundation.action.TechnologySpecificFlexoAction;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoConceptObject;
import org.openflexo.foundation.fml.FlexoEnum;
import org.openflexo.foundation.fml.InconsistentFlexoConceptHierarchyException;
import org.openflexo.foundation.fml.InnerConceptsFacet;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.toolbox.StringUtils;

public class CreateFlexoEnum extends AbstractCreateFlexoConcept<CreateFlexoEnum, FlexoConceptObject, FMLObject>
		implements TechnologySpecificFlexoAction<FMLTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(CreateFlexoEnum.class.getPackage().getName());

	public static FlexoActionFactory<CreateFlexoEnum, FlexoConceptObject, FMLObject> actionType = new FlexoActionFactory<CreateFlexoEnum, FlexoConceptObject, FMLObject>(
			"flexo_enum", FlexoActionFactory.newMenu, FlexoActionFactory.defaultGroup, FlexoActionFactory.ADD_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateFlexoEnum makeNewAction(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
			return new CreateFlexoEnum(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FlexoConceptObject object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FlexoConceptObject object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateFlexoEnum.actionType, VirtualModel.class);
		FlexoObjectImpl.addActionForClass(CreateFlexoEnum.actionType, InnerConceptsFacet.class);
	}

	private String newFlexoEnumName;
	private String newFlexoEnumDescription;
	private FlexoEnum newFlexoEnum;

	public boolean switchNewlyCreatedFlexoConcept = false;

	CreateFlexoEnum(FlexoConceptObject focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public Class<? extends FMLTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	@Override
	protected void doAction(Object context)
			throws NotImplementedException, InvalidParameterException, InconsistentFlexoConceptHierarchyException {

		FMLModelFactory factory = getFocusedObject().getFMLModelFactory();

		newFlexoEnum = factory.newFlexoEnum();
		newFlexoEnum.setName(getNewFlexoEnumName());

		VirtualModel virtualModel = getFocusedObject().getOwningVirtualModel();

		virtualModel.addToFlexoConcepts(newFlexoEnum);

		/*performSetParentConcepts();
		performCreateProperties();
		performCreateBehaviours();
		performCreateInspectors();*/
		performPostProcessings();

	}

	@Override
	public FlexoEnum getNewFlexoConcept() {
		return newFlexoEnum;
	}

	public String getNewFlexoEnumName() {
		return newFlexoEnumName;
	}

	public void setNewFlexoEnumName(String newFlexoEnumName) {
		if ((newFlexoEnumName == null && this.newFlexoEnumName != null)
				|| (newFlexoEnumName != null && !newFlexoEnumName.equals(this.newFlexoEnumName))) {
			String oldValue = this.newFlexoEnumName;
			this.newFlexoEnumName = newFlexoEnumName;
			getPropertyChangeSupport().firePropertyChange("newFlexoEnumName", oldValue, newFlexoEnumName);
		}
	}

	public String getNewFlexoEnumDescription() {
		return newFlexoEnumDescription;
	}

	public void setNewFlexoEnumDescription(String newFlexoEnumDescription) {
		if ((newFlexoEnumDescription == null && this.newFlexoEnumDescription != null)
				|| (newFlexoEnumDescription != null && !newFlexoEnumDescription.equals(this.newFlexoEnumDescription))) {
			String oldValue = this.newFlexoEnumDescription;
			this.newFlexoEnumDescription = newFlexoEnumDescription;
			getPropertyChangeSupport().firePropertyChange("newFlexoEnumDescription", oldValue, newFlexoEnumDescription);
		}
	}

	@Override
	public boolean isValid() {
		if (StringUtils.isEmpty(newFlexoEnumName)) {
			return false;
		}
		else if (getFocusedObject().getDeclaringCompilationUnit().getFlexoConcept(newFlexoEnumName) != null) {
			return false;
		}
		return true;
	}

	@Override
	public int getExpectedProgressSteps() {
		return 10;
	}

}
