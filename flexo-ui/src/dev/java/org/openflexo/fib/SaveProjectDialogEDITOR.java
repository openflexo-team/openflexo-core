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
import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.openflexo.components.SaveProjectsDialog;
import org.openflexo.fib.editor.FIBAbstractEditor;
import org.openflexo.foundation.FlexoProject;

public class SaveProjectDialogEDITOR extends FIBAbstractEditor {

	@Override
	public Object[] getData() {
		List<FlexoProject> projects = new ArrayList<FlexoProject>();
		for (int i = 1; i < 6; i++) {
			FlexoProject project = Mockito.mock(FlexoProject.class);
			Mockito.when(project.getName()).thenReturn("test-project-" + i);
			Mockito.when(project.getProjectDirectory()).thenReturn(new File(System.getProperty("user.home"), "test-project-" + i));
			projects.add(project);
		}
		return FIBAbstractEditor.makeArray(new SaveProjectsDialog.ProjectList(projects));
	}

	@Override
	public File getFIBFile() {
		return SaveProjectsDialog.FIB_FILE;
	}

	public static void main(String[] args) {
		main(SaveProjectDialogEDITOR.class);
	}
}
