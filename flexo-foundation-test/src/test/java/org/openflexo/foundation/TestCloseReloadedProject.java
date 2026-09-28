/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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
package org.openflexo.foundation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Closing a reloaded project holding a compilation unit that was never loaded (CORE-D-27).<br>
 * Such a resource has no URI of its own yet: it computes it from the project when asked - which unregistering it does, while the
 * project resource center is removed. The project must still be attached to its delegate resource center then.
 *
 * @author sylvain
 */
@RunWith(OrderedRunner.class)
public class TestCloseReloadedProject extends OpenflexoProjectAtRunTimeTestCase {

	private static final String VM_NAME = "NeverLoaded";

	private static FlexoEditor editor;
	private static FlexoProject<File> project;

	@Test
	@TestOrder(1)
	public void testCreateProjectWithAVirtualModel() throws SaveResourceException, ModelDefinitionException {
		editor = createStandaloneProject("TestCloseReloadedProject");
		project = (FlexoProject<File>) editor.getProject();

		FMLTechnologyAdapter fmlTA = serviceManager.getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		CompilationUnitResource resource = fmlTA.getCompilationUnitResourceFactory().makeTopLevelCompilationUnitResource(VM_NAME,
				project.getProjectURI() + "/" + VM_NAME + ".fml", fmlTA.getVirtualModelRepository(project).getRootFolder(), true);
		assertNotNull(resource);
		resource.save();
		project.save();
	}

	/**
	 * Reopening in the SAME service manager: the reopened project must find its virtual model again in its own repository
	 */
	@Test
	@TestOrder(2)
	public void testReopenInSameServiceManager() {
		String vmURI = project.getProjectURI() + "/" + VM_NAME + ".fml";
		editor = reloadProject(project);
		project = (FlexoProject<File>) editor.getProject();
		assertNotNull("The reopened project lost its virtual model", serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class).getVirtualModelRepository(project).getResource(vmURI));
	}

	@Test
	@TestOrder(3)
	public void testCloseReloadedProject() {
		// A new service manager, as when the application is started again: resources are built anew from the files, and none is loaded
		File projectDirectory = project.getProjectDirectory();
		instanciateTestServiceManager();
		editor = loadProject(projectDirectory);
		project = (FlexoProject<File>) editor.getProject();

		CompilationUnitResource resource = null;
		for (FlexoResource<?> r : project.getAllResources()) {
			if (r instanceof CompilationUnitResource && VM_NAME.equals(r.getName())) {
				resource = (CompilationUnitResource) r;
			}
		}
		assertNotNull("The virtual model was not found back in the reloaded project", resource);
		assertFalse("The virtual model must not be loaded: only a never loaded resource computes its URI at close", resource.isLoaded());

		// Threw a NullPointerException from CompilationUnitResourceImpl.computeDefaultURI() before CORE-D-27 was fixed
		project.close();

		assertNull("The project data is detached once closed", project.getResource());
		assertTrue(project.getServiceManager() == null
				|| !project.getServiceManager().getResourceCenterService().getResourceCenters().contains(project));
	}
}
