/**
 * 
 * Copyright (c) 2013-2015, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.fib;

import java.io.File;

import org.openflexo.ApplicationContext;
import org.openflexo.ApplicationData;
import org.openflexo.components.WelcomeDialog;
import org.openflexo.fib.editor.FIBAbstractEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.rm.FlexoProject.FlexoProjectReferenceLoader;
import org.openflexo.foundation.technologyadapter.InformationSpace;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.utils.DefaultProjectLoadingHandler;
import org.openflexo.foundation.utils.ProjectLoadingHandler;
import org.openflexo.foundation.fml.ViewPointLibrary;
import org.openflexo.foundation.xml.XMLSerializationService;
import org.openflexo.view.controller.InteractiveFlexoEditor;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

public class WelcomePanelEDITOR extends FIBAbstractEditor {

	@Override
	public Object[] getData() {
		ApplicationData applicationData = new ApplicationData(new ApplicationContext() {

			@Override
			protected XMLSerializationService createXMLSerializationService() {
				return XMLSerializationService.createInstance();
			}

			@Override
			public FlexoEditor makeFlexoEditor(FlexoProject project, FlexoServiceManager sm) {
				return new InteractiveFlexoEditor(this, project);
			}

			@Override
			public FlexoEditor createApplicationEditor() {
				return new InteractiveFlexoEditor(this, null);
			}

			@Override
			protected FlexoProjectReferenceLoader createProjectReferenceLoader() {
				return null;
			}

			@Override
			public ProjectLoadingHandler getProjectLoadingHandler(File projectDirectory) {
				return new DefaultProjectLoadingHandler();
			}

			@Override
			protected FlexoResourceCenterService createResourceCenterService() {
				return null;
			}

			@Override
			protected TechnologyAdapterService createTechnologyAdapterService(FlexoResourceCenterService resourceCenterService) {
				return null;
			}

			@Override
			protected TechnologyAdapterControllerService createTechnologyAdapterControllerService() {
				return null;
			}

			@Override
			protected ViewPointLibrary createViewPointLibraryService() {
				return null;
			}

			@Override
			protected InformationSpace createInformationSpace() {
				return null;
			}

		});
		return FIBAbstractEditor.makeArray(applicationData);
	}

	@Override
	public File getFIBFile() {
		return WelcomeDialog.FIB_FILE;
	}

	public static void main(String[] args) {
		main(WelcomePanelEDITOR.class);
	}
}
