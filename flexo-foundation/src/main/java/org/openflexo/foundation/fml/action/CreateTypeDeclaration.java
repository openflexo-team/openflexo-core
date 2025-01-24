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

import java.lang.reflect.Type;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.FlexoAction;
import org.openflexo.foundation.action.FlexoActionFactory;
import org.openflexo.foundation.action.TechnologySpecificFlexoAction;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.InconsistentFlexoConceptHierarchyException;
import org.openflexo.foundation.fml.TypeDeclaration;
import org.openflexo.foundation.technologyadapter.ModelSlot;

/**
 * This action allows to explicitely declare use of a {@link ModelSlot} class in a Virtual Model
 * 
 * @author sylvain
 *
 */
public class CreateTypeDeclaration extends FlexoAction<CreateTypeDeclaration, FMLCompilationUnit, FMLObject>
		implements TechnologySpecificFlexoAction<FMLTechnologyAdapter> {

	private static final Logger logger = Logger.getLogger(CreateTypeDeclaration.class.getPackage().getName());

	public static FlexoActionFactory<CreateTypeDeclaration, FMLCompilationUnit, FMLObject> actionType = new FlexoActionFactory<CreateTypeDeclaration, FMLCompilationUnit, FMLObject>(
			"create_type_declaration", FlexoActionFactory.advancedGroup, FlexoActionFactory.NORMAL_ACTION_TYPE) {

		/**
		 * Factory method
		 */
		@Override
		public CreateTypeDeclaration makeNewAction(FMLCompilationUnit focusedObject, Vector<FMLObject> globalSelection,
				FlexoEditor editor) {
			return new CreateTypeDeclaration(focusedObject, globalSelection, editor);
		}

		@Override
		public boolean isVisibleForSelection(FMLCompilationUnit object, Vector<FMLObject> globalSelection) {
			return object != null;
		}

		@Override
		public boolean isEnabledForSelection(FMLCompilationUnit object, Vector<FMLObject> globalSelection) {
			return isVisibleForSelection(object, globalSelection);
		}

	};

	static {
		FlexoObjectImpl.addActionForClass(CreateTypeDeclaration.actionType, FMLCompilationUnit.class);
	}

	private String abbrev;
	private Type referencedType;

	private CreateTypeDeclaration(FMLCompilationUnit focusedObject, Vector<FMLObject> globalSelection, FlexoEditor editor) {
		super(actionType, focusedObject, globalSelection, editor);
	}

	@Override
	public Class<? extends FMLTechnologyAdapter> getTechnologyAdapterClass() {
		return FMLTechnologyAdapter.class;
	}

	private TypeDeclaration typeDeclaration;

	@Override
	protected void doAction(Object context) throws InconsistentFlexoConceptHierarchyException {
		logger.info("Create TypeDeclaration");
		TypeDeclaration newTypeDeclaration = getFocusedObject().getFMLModelFactory().newTypeDeclaration();
		newTypeDeclaration.setAbbrev(getAbbrev());
		newTypeDeclaration.setReferencedType(getReferencedType());
		getFocusedObject().addToTypeDeclarations(newTypeDeclaration);
		getFocusedObject().setIsModified();
	}

	public TypeDeclaration getNewTypeDeclaration() {
		return typeDeclaration;
	}

	public String getAbbrev() {
		return abbrev;
	}

	public void setAbbrev(String abbrev) {
		if ((abbrev == null && this.abbrev != null) || (abbrev != null && !abbrev.equals(this.abbrev))) {
			String oldValue = this.abbrev;
			this.abbrev = abbrev;
			getPropertyChangeSupport().firePropertyChange("abbrev", oldValue, abbrev);
		}
	}

	public Type getReferencedType() {
		return referencedType;
	}

	public void setReferencedType(Type referencedType) {
		if ((referencedType == null && this.referencedType != null)
				|| (referencedType != null && !referencedType.equals(this.referencedType))) {
			Type oldValue = this.referencedType;
			this.referencedType = referencedType;
			getPropertyChangeSupport().firePropertyChange("referencedType", oldValue, referencedType);
		}
	}

}
