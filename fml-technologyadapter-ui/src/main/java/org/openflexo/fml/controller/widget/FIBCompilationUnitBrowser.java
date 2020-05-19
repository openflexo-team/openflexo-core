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

import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.gina.model.FIBContainer;
import org.openflexo.gina.swing.view.widget.JFIBBrowserWidget;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.view.FIBBrowserView;
import org.openflexo.view.controller.FlexoController;

/**
 * Browser allowing to browse through a {@link VirtualModel}<br>
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public class FIBCompilationUnitBrowser extends FIBBrowserView<FMLCompilationUnit> {
	static final Logger logger = Logger.getLogger(FIBCompilationUnitBrowser.class.getPackage().getName());

	public static final Resource FIB_FILE = ResourceLocator.locateResource("Fib/Widget/FIBCompilationUnitBrowser.fib");

	public FIBCompilationUnitBrowser(FMLCompilationUnit compilationUnit, FlexoController controller) {
		super(compilationUnit, controller, FIB_FILE,
				controller != null ? controller.getTechnologyAdapter(FMLTechnologyAdapter.class).getLocales() : null);
	}

	public void setCompilationUnit(FMLCompilationUnit compilationUnit) {
		setRootObject(compilationUnit);
		getFIBController().setViewMode(getFIBController().getViewMode(), true);
	}

	@Override
	public FIBCompilationUnitBrowserFIBController getFIBController() {
		return (FIBCompilationUnitBrowserFIBController) super.getFIBController();
	}

	public JFIBBrowserWidget<?> getFIBBrowserWidget() {
		return (JFIBBrowserWidget<?>) getFIBController().viewForComponent(retrieveFIBBrowser((FIBContainer) getFIBComponent()));
	}

	// Please uncomment this for a live test
	// Never commit this uncommented since it will not compile on continuous build
	// To have icon, you need to choose "Test interface" in the editor (otherwise, flexo controller is not insanciated in EDIT mode)
	/*public static void main(String[] args) {
	
		try {
			FlexoLoggingManager.initialize(-1, true, null, Level.INFO, null);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		TestApplicationContext testApplicationContext = new TestApplicationContext(
				new FileResource("src/test/resources/TestResourceCenter"));
		final VirtualModelLibrary viewPointLibrary = testApplicationContext.getViewPointLibrary();
	
		ViewPointResource vpRes = viewPointLibrary
				.getViewPointResource("http://www.agilebirds.com/openflexo/ViewPoints/Basic/BasicOntology.owl");
		final DiagramSpecification basicOntologyEditorDS = vpRes.getViewPoint().getDefaultDiagramSpecification();
	
		FIBAbstractEditor editor = new FIBAbstractEditor() {
			@Override
			public Object[] getData() {
				return makeArray(basicOntologyEditorDS);
			}
	
			@Override
			public File getFIBFile() {
				return FIB_FILE;
			}
	
			@Override
			public FIBController makeNewController(FIBComponent component) {
				return new FlexoFIBController(component);
			}
		};
		editor.launch();
	
	}*/

}
