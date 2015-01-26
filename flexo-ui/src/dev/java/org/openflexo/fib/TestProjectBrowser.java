/**
 * 
 * Copyright (c) 2013-2014, Openflexo
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

import org.openflexo.fib.editor.FIBAbstractEditor;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoEditor.FlexoEditorFactory;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.rm.FlexoResourceManager;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.toolbox.FileResource;

public class TestProjectBrowser extends FIBAbstractEditor {

	public static FileResource PRJ_FILE = new FileResource("Prj/TestBrowser.prj");
	public static FileResource FIB_FILE = new FileResource("Fib/ProjectBrowser.fib");

	protected static final FlexoEditorFactory EDITOR_FACTORY = new FlexoEditorFactory() {
		@Override
		public DefaultFlexoEditor makeFlexoEditor(FlexoProject project) {
			return new DefaultFlexoEditor(project);
		}
	};

	@Override
	public Object[] getData() {
		return FIBAbstractEditor.makeArray(loadProject());
	}

	@Override
	public File getFIBFile() {
		return FIB_FILE;
	}

	public static void main(String[] args) {
		main(TestProjectBrowser.class);
	}

	public static FlexoProject loadProject() {
		FlexoProject project = null;
		FlexoEditor editor;
		File projectFile = PRJ_FILE;
		// File projectFile = new
		// File("C:\\Documents and Settings\\gpolet.DENALI\\Desktop\\FlexoProjects\\test_forFlexoServer_1.1\\test.prj");
		// File projectFile = new File("/Users/sylvain/Documents/TestsFlexo/TestBindingSelector.prj");
		// File projectFile = new File("/Users/sylvain/Documents/TestsFlexo/BA/TestActivityGroup.prj");
		// File projectFile = new File("/Users/sylvain/Documents/TestsFlexo/BA/TestOperatorConversion/TestOperator.prj");
		// File projectFile = new File("/Users/sylvain/Documents/TestsFlexo/BA/TestSPNodesConversion/TestSPNodesConversion.prj");
		// File projectFile = new File("/Users/sylvain/Documents/TestsFlexo/BA/TestInducedEdges.prj");
		// FileResource projectFile = new FileResource("src/dev/resources/TestFGE.prj");
		System.out.println("Found project " + projectFile.getAbsolutePath());
		try {
			editor = FlexoResourceManager.initializeExistingProject(projectFile, EDITOR_FACTORY, null);
			project = editor.getProject();
		} catch (ProjectLoadingCancelledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProjectInitializerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Successfully loaded project " + projectFile.getAbsolutePath());

		return project;
	}

}
