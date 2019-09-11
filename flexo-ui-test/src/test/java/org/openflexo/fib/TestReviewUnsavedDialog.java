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

package org.openflexo.fib;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.ApplicationContext;
import org.openflexo.OpenflexoProjectAtRunTimeTestCaseWithGUI;
import org.openflexo.components.ReviewUnsavedDialog;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.gina.swing.test.FIBDialogGraphicalContextDelegate;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.test.UITest;

/**
 * Test the ReviewUnsavedDialog widget
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestReviewUnsavedDialog extends OpenflexoProjectAtRunTimeTestCaseWithGUI {

	private static FIBDialogGraphicalContextDelegate gcDelegate;

	private static FlexoEditor editor;
	private static FlexoProject<File> project;

	@Test
	@TestOrder(1)
	@Category(UITest.class)
	public void testCreateProject() {
		serviceManager = instanciateTestServiceManager();
		editor = createStandaloneProject("TestProject");
		project = (FlexoProject<File>) editor.getProject();
		System.out.println("Created project " + project.getProjectDirectory());
		assertTrue(project.getProjectDirectory().exists());
	}

	@Test
	@TestOrder(2)
	@Category(UITest.class)
	public void testInstanciateWidget() {
		System.out.println(">>>>>>>>>>>>> serviceManager=" + serviceManager);
		ReviewUnsavedDialog dialog = new ReviewUnsavedDialog((ApplicationContext) serviceManager, serviceManager.getResourceManager());
		log("instanciated " + dialog);
		gcDelegate = new FIBDialogGraphicalContextDelegate(dialog, ReviewUnsavedDialog.FIB_FILE_NAME);
	}

	@Before
	public void setUp() {
		if (gcDelegate != null) {
			gcDelegate.setUp();
		}
	}

	@Override
	@After
	public void tearDown() throws Exception {
		if (gcDelegate != null) {
			gcDelegate.tearDown();
		}
		super.tearDown();
	}

}
