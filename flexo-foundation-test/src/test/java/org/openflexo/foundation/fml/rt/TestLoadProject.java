/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexodiagram, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.rt;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.project.FlexoProjectResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * This unit test is intented to test OBP2Analysis using a {@link OBP2ModelSlot}
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestLoadProject extends OpenflexoProjectAtRunTimeTestCase {

	public static final String PROJECT_URI = "http://www.openflexo.org/projects/2020/4/TestSingleInheritance_1585907148412.prj";

	private static FlexoEditor editor;
	private static VirtualModel virtualModel;
	private static FMLRTVirtualModelInstance vmi;

	@Test
	@TestOrder(1)
	public void testLoadProject() throws ModelDefinitionException, IOException, ResourceLoadingCancelledException, FlexoException {
		instanciateTestServiceManager();
		System.out.println("SM=" + serviceManager);
		FlexoResourceCenterService rcService = serviceManager.getResourceCenterService();
		for (FlexoResourceCenter<?> resourceCenter : rcService.getResourceCenters()) {
			System.out.println("> resourceCenter " + resourceCenter + " uri=" + resourceCenter.getDefaultBaseURI());
			for (FlexoResource<?> resource : resourceCenter.getAllResources()) {
				System.out.println(" > " + resource.getURI());
			}
		}

		FlexoProjectResource<?> projectResource = (FlexoProjectResource) serviceManager.getResourceManager().getResource(PROJECT_URI);

		System.out.println("projectResource=" + projectResource);

		if (projectResource.getIODelegate().getSerializationArtefact() instanceof File) {

			File projectDirectory = ((File) projectResource.getIODelegate().getSerializationArtefact()).getParentFile();
			System.out.println("projectDirectory=" + projectDirectory);

			editor = loadProject(projectDirectory);
			FlexoProject<?> project = editor.getProject();

			System.out.println("Toutes les resources du projet:");
			for (FlexoResource<?> resource : project.getAllResources()) {
				System.out.println(" > " + resource.getURI());
			}

			CompilationUnitResource virtualModelResource = (CompilationUnitResource) project.getResource(PROJECT_URI + "/Vm.fml");
			assertNotNull(virtualModelResource);

			virtualModel = virtualModelResource.getCompilationUnit().getVirtualModel();
		}
	}

	/*@Test
	@TestOrder(2)
	public void testCreateVirtualModelInstance() throws SaveResourceException {
	
		log("testCreateVirtualModelInstance()");
	
		CreateBasicVirtualModelInstance action = CreateBasicVirtualModelInstance.actionType
				.makeNewAction(editor.getProject().getVirtualModelInstanceRepository().getRootFolder(), null, editor);
		action.setNewVirtualModelInstanceName("MyVMI");
		action.setNewVirtualModelInstanceTitle("Test creation of a new VMI");
		action.setVirtualModel(virtualModel);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		vmi = action.getNewVirtualModelInstance();
		assertNotNull(vmi);
		assertNotNull(vmi.getResource());
	}*/

}
