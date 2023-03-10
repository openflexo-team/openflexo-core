/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.controller.view;

import javax.swing.SwingUtilities;

import org.openflexo.fml.controller.CommonFIB;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.gina.swing.view.widget.JFIBBrowserWidget;
import org.openflexo.rm.Resource;
import org.openflexo.view.controller.FlexoController;
import org.openflexo.view.controller.model.FlexoPerspective;

/**
 * This is the module view representing a {@link FMLCompilationUnit}<br>
 * 
 * @author sguerin
 * 
 */
public class StandardCompilationUnitView extends CompilationUnitView {

	public StandardCompilationUnitView(FMLCompilationUnit compilationUnit, FlexoController controller, FlexoPerspective perspective) {
		super(compilationUnit, CommonFIB.COMPILATION_UNIT_VIEW_FIB, controller, perspective);
		// prout();
	}

	public StandardCompilationUnitView(FMLCompilationUnit compilationUnit, Resource fibFile, FlexoController controller,
			FlexoPerspective perspective) {
		super(compilationUnit, fibFile, controller, perspective);
		// prout();
	}

	@Override
	public void willShow() {
		super.willShow();
		getFlexoController().getControllerModel().setRightViewVisible(false);

		SwingUtilities.invokeLater(() -> {
			if (getFIBView("flexoConceptBrowser") instanceof JFIBBrowserWidget) {
				JFIBBrowserWidget<FMLObject> browser = (JFIBBrowserWidget<FMLObject>) getFIBView("flexoConceptBrowser");
				if (getDataObject() instanceof FMLCompilationUnit) {
					FMLCompilationUnit cu = (FMLCompilationUnit) getDataObject();
					browser.performExpand(cu.getVirtualModel());
					browser.performExpand(cu.getVirtualModel().getStructuralFacet());
					browser.performExpand(cu.getVirtualModel().getBehaviouralFacet());
					browser.performExpand(cu.getVirtualModel().getInnerConceptsFacet());
				}
			}
		});
	}
}
