/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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

package org.openflexo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.logging.Logger;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.gina.test.TestApplicationContext;
import org.openflexo.module.ModuleLoader;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.test.UITest;
import org.openflexo.view.controller.TechnologyAdapterControllerService;

@RunWith(OrderedRunner.class)
public class TestServiceManager extends OpenflexoTestCase {

	protected static final Logger logger = Logger.getLogger(TestServiceManager.class.getPackage().getName());

	private static ApplicationContext testApplicationContext;
	private static FlexoResourceCenter<?> resourceCenter;

	/**
	 * Instanciate test ApplicationContext
	 */
	@Test
	@TestOrder(1)
	@Category(UITest.class)
	public void test0UseTestApplicationContext() {
		log("test0UseTestApplicationContext()");
		testApplicationContext = new TestApplicationContext();
		resourceCenter = testApplicationContext.getResourceCenterService().getResourceCenters().get(0);
		logger.info("resource center=" + resourceCenter);

		logger.info("services: " + testApplicationContext.getRegisteredServices());

		assertNotNull(testApplicationContext.getService(ProjectLoader.class));
		assertNotNull(testApplicationContext.getService(ModuleLoader.class));
		assertNotNull(testApplicationContext.getService(FlexoResourceCenterService.class));
		assertNotNull(testApplicationContext.getService(TechnologyAdapterService.class));
		assertNotNull(testApplicationContext.getService(TechnologyAdapterControllerService.class));

	}

	/**
	 * Try to load a module
	 */
	@Test
	@TestOrder(2)
	@Category(UITest.class)
	public void test1ModuleLoading() {
		ModuleLoader moduleLoader = testApplicationContext.getModuleLoader();
		assertEquals(moduleLoader, testApplicationContext.getService(ModuleLoader.class));

		/*
		 * UserType.setCurrentUserType(UserType.MAINTAINER);
		 * 
		 * try { ExternalModule loadedModule =
		 * moduleLoader.getVPMModuleInstance(); if (loadedModule != null) {
		 * fail(); } // This module is not in the classpath, normal } catch
		 * (ModuleLoadingException e) { fail(); }
		 */
	}
}
