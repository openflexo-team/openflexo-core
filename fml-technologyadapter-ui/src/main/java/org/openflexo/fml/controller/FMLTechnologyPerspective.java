/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller;

import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.openflexo.fml.controller.view.FMLCompilationUnitView;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.icon.FMLIconLibrary;
import org.openflexo.view.ModuleView;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.GenericPerspective;

/**
 * A perspective specializing {@link GenericPerspective} by representing {@link VirtualModel} with FML textual language
 * 
 * @author sylvain
 * 
 * @param <TA>
 */
public class FMLTechnologyPerspective extends GenericPerspective {

	static final Logger logger = Logger.getLogger(FMLTechnologyPerspective.class.getPackage().getName());

	public FMLTechnologyPerspective(FlexoController controller) {
		super(controller);
	}

	@Override
	public String getName() {
		return "textual_FML_perspective";
	}

	@Override
	public ImageIcon getActiveIcon() {
		return FMLIconLibrary.FML_ICON;
	}

	/*@Override
	@SuppressWarnings("unchecked")
	public boolean hasModuleViewForObject(FlexoObject object) {
		if (object instanceof FMLCompilationUnit) {
			return true;
		}
		return super.hasModuleViewForObject(object);
	}*/
	
	@Override
	public boolean isRepresentableInModuleView(FlexoObject object) {
		if (object instanceof FMLObject && ((FMLObject)object).getDeclaringCompilationUnit() != null) {
			return true;
		}
		return super.isRepresentableInModuleView(object);
	}
	
	@Override
	public FlexoObject getRepresentableMasterObject(FlexoObject object) {
		if (object instanceof FMLObject && ((FMLObject)object).getDeclaringCompilationUnit() != null) {
			return ((FMLObject)object).getDeclaringCompilationUnit();
		}
		return super.getRepresentableMasterObject(object);
	}

	@Override
	public ModuleView<?> createModuleViewForMasterObject(FlexoObject object) {
		if (object instanceof FMLCompilationUnit) {
			CompilationUnitResource resource = (CompilationUnitResource) ((FMLCompilationUnit) object).getResource();
			return new FMLCompilationUnitView(resource, getController(), this);
		}
		/*if (object instanceof FMLObject) {
			CompilationUnitResource resource = ((FMLObject) object).getDeclaringCompilationUnitResource();
			return new FMLCompilationUnitView(resource, getController(), this);
		}*/
		return super.createModuleViewForMasterObject(object);
	}

}
