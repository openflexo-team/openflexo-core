/**
 * 
 * Copyright (c) 2019, Openflexo
 * 
 * This file is part of FML-parser, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.parser;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.UseModelSlotDeclaration;
import org.openflexo.foundation.fml.parser.node.TIdentifier;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * @author sylvain
 * 
 */
public class RoleFactory extends SemanticsAnalyzerFactory {

	private static final Logger logger = Logger.getLogger(RoleFactory.class.getPackage().getName());
	private Class<? extends FlexoRole<?>> roleClass;

	public RoleFactory(MainSemanticsAnalyzer analyzer) {
		super(analyzer);
	}

	public Class<? extends FlexoRole<?>> getRoleClass(TIdentifier roleIdentifier) {
		for (UseModelSlotDeclaration useModelSlotDeclaration : getAnalyzer().getCompilationUnit().getUseDeclarations()) {
			roleClass = getRoleClass(roleIdentifier, useModelSlotDeclaration.getModelSlotClass());
			if (roleClass != null) {
				return roleClass;
			}
		}
		return null;
	}

	public Class<? extends FlexoRole<?>> getRoleClass(TIdentifier taIdentifier, TIdentifier roleIdentifier) {
		Class<? extends ModelSlot<?>> modelSlotClass = getModelSlotClass(taIdentifier);
		if (modelSlotClass != null) {
			return getRoleClass(roleIdentifier, modelSlotClass);
		}
		return null;
	}

	private Class<? extends ModelSlot<?>> getModelSlotClass(TIdentifier taIdentifier) {
		for (UseModelSlotDeclaration useModelSlotDeclaration : getAnalyzer().getCompilationUnit().getUseDeclarations()) {
			if (taIdentifier.getText().equals(useModelSlotDeclaration.getAbbrev())) {
				return useModelSlotDeclaration.getModelSlotClass();
			}
		}
		return null;
	}

	private Class<? extends FlexoRole<?>> getRoleClass(TIdentifier roleIdentifier, Class<? extends ModelSlot<?>> modelSlotClass) {

		if (roleIdentifier.getText().equals(modelSlotClass.getSimpleName())) {
			return modelSlotClass;
		}

		for (Class<? extends FlexoRole<?>> roleClass : getServiceManager().getTechnologyAdapterService()
				.getAvailableFlexoRoleTypes(modelSlotClass)) {
			if (roleIdentifier.getText().equals(roleClass.getSimpleName())) {
				return roleClass;
			}
		}

		return null;
	}

	public String serializeTAId(ModelSlot<?> modelSlot) {
		return serializeTAId((Class) modelSlot.getClass());
	}

	public String serializeTAId(Class<? extends ModelSlot<?>> modelSlotClass) {
		for (UseModelSlotDeclaration useModelSlotDeclaration : getAnalyzer().getCompilationUnit().getUseDeclarations()) {
			if (useModelSlotDeclaration.getModelSlotClass().isAssignableFrom(modelSlotClass)) {
				return useModelSlotDeclaration.getAbbrev();
			}
		}
		return null;
	}

	public String serializeTAId(FlexoRole<?> role) {
		Class<? extends ModelSlot<?>> modelSlotClass = getModelSlotClass(role);
		return serializeTAId(modelSlotClass);

	}

	public Class<? extends ModelSlot<?>> getModelSlotClass(FlexoRole<?> role) {
		if (role instanceof ModelSlot) {
			return (Class<? extends ModelSlot<?>>) role.getClass();
		}
		for (TechnologyAdapter<?> ta : getAnalyzer().getServiceManager().getTechnologyAdapterService().getTechnologyAdapters()) {
			for (Class<? extends ModelSlot<?>> modelSlotClass : ta.getAvailableModelSlotTypes()) {
				for (Class<? extends FlexoRole<?>> roleClass : getAnalyzer().getServiceManager().getTechnologyAdapterService()
						.getAvailableFlexoRoleTypes(modelSlotClass)) {
					if (roleClass.isAssignableFrom(role.getClass())) {
						return modelSlotClass;
					}
				}

			}
		}
		return null;
	}

}
