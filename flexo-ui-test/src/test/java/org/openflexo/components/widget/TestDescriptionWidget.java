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

package org.openflexo.components.widget;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.gina.test.SwingGraphicalContextDelegate;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

/**
 * Test the structural and behavioural features of {@link FIBDescriptionWidget}
 * 
 * @author sylvain
 * 
 */
public class TestDescriptionWidget extends OpenflexoTestCase {

	private static SwingGraphicalContextDelegate gcDelegate;

	@BeforeClass
	public static void setupClass() {
		instanciateTestServiceManager();
		initGUI();
	}

	@Test
	public void test1InstanciateWidget() throws ModelDefinitionException {

		ModelFactory factory = new FMLModelFactory(null, serviceManager);
		VirtualModel anObject = factory.newInstance(VirtualModel.class);
		FIBDescriptionWidget descriptionWidget = new FIBDescriptionWidget(anObject);
		gcDelegate.addTab("FIBDescriptionWidget", descriptionWidget.getController());
	}

	public static void initGUI() {
		gcDelegate = new SwingGraphicalContextDelegate(TestDescriptionWidget.class.getSimpleName());
	}

	@AfterClass
	public static void waitGUI() {
		gcDelegate.waitGUI();
	}

	@Before
	public void setUp() {
		gcDelegate.setUp();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		gcDelegate.tearDown();
	}

}
