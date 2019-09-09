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

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.components.ResourceCenterEditorDialog;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.gina.swing.test.FIBDialogGraphicalContextDelegate;
import org.openflexo.gina.test.OpenflexoTestCaseWithGUI;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.test.UITest;

/**
 * Test the ResourceCenterEditor widget
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestResourceCenterEditor extends OpenflexoTestCaseWithGUI {

	private static FIBDialogGraphicalContextDelegate gcDelegate;

	protected static DirectoryResourceCenter resourceCenter;

	@Test
	@TestOrder(1)
	@Category(UITest.class)
	public void testInstanciateTestServiceManager() throws IOException {
		instanciateTestServiceManager();
		resourceCenter = makeNewDirectoryResourceCenter(serviceManager);
		assertNotNull(resourceCenter);
		System.out.println("ResourceCenter= " + resourceCenter);
	}

	@Test
	@TestOrder(2)
	@Category(UITest.class)
	public void testInstanciateWidget() {
		ResourceCenterEditorDialog dialog = ResourceCenterEditorDialog.getResourceCenterEditorDialog(serviceManager, null, true);

		log("instanciated " + dialog);
		System.out.println("rcs= " + serviceManager.getResourceCenterService().getResourceCenters());
		gcDelegate = new FIBDialogGraphicalContextDelegate(dialog, ResourceCenterEditorDialog.RESOURCE_CENTER_EDITOR_FIB);
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
