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

package org.openflexo.fml.controller.widget;

import javax.swing.SwingUtilities;

import org.openflexo.fml.controller.widget.fmleditor.FMLEditor;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.gina.swing.view.widget.JFIBBrowserWidget;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.FIBBrowserView;
import org.openflexo.view.controller.FlexoController;

/**
 * Browser allowing to browse source of a {@link FMLCompilationUnit} with line number semantics<br>
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBCompilationUnitDetailedBrowser extends FIBBrowserView<FMLCompilationUnit> {
	private static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/Widget/FIBCompilationUnitDetailedBrowser.fib");

	private final FMLEditor fmlEditor;

	public FIBCompilationUnitDetailedBrowser(FMLCompilationUnit compilationUnit, FMLEditor fmlEditor, FlexoController controller) {
		super(compilationUnit, controller, FIB_FILE,
				controller != null ? controller.getTechnologyAdapter(FMLTechnologyAdapter.class).getLocales() : null);
		this.fmlEditor = fmlEditor;
		SwingUtilities.invokeLater(() -> {
			if (getFIBView("browser") instanceof JFIBBrowserWidget) {
				JFIBBrowserWidget<FMLObject> browser = (JFIBBrowserWidget<FMLObject>) getFIBView("browser");
				browser.performExpand(compilationUnit.getVirtualModel());
				browser.performExpand(compilationUnit.getVirtualModel().getStructuralFacet());
				browser.performExpand(compilationUnit.getVirtualModel().getBehaviouralFacet());
				browser.performExpand(compilationUnit.getVirtualModel().getInnerConceptsFacet());
			}
		});
	}

	public void makeVisible(FMLObject object) {
		SwingUtilities.invokeLater(() -> {
			if (getFIBView("browser") instanceof JFIBBrowserWidget) {
				JFIBBrowserWidget<FMLObject> browser = (JFIBBrowserWidget<FMLObject>) getFIBView("browser");
				browser.performSelect(object, true);
			}
		});
	}

	public FMLEditor getFMLEditor() {
		return fmlEditor;
	}

	@Override
	public FIBCompilationUnitDetailedBrowserFIBController getFIBController() {
		return (FIBCompilationUnitDetailedBrowserFIBController) super.getFIBController();
	}

	@Override
	protected FIBCompilationUnitDetailedBrowserFIBController createFibController(FIBComponent fibComponent, FlexoController controller,
			LocalizedDelegate locales) {
		FIBCompilationUnitDetailedBrowserFIBController returned = (FIBCompilationUnitDetailedBrowserFIBController) super.createFibController(
				fibComponent, controller, locales);
		returned.setBrowser(this);
		return returned;
	}

}
