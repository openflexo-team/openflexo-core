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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.RepositoryFolder;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Closing a project with the project loader, then opening it again in the same service manager: its resources must be there once, not
 * twice (CORE-D-93).
 *
 * @author sylvain
 */
@RunWith(OrderedRunner.class)
public class TestReopenProjectNoDuplicates extends OpenflexoProjectAtRunTimeTestCase {

	private static final String VM_NAME = "Reopened";

	private static FlexoEditor editor;
	private static FlexoProject<File> project;
	private static File projectDirectory;
	private static int resourceCount;
	private static int rootCount;
	private static List<String> browsedBefore;

	private static List<String> resourceNames(FlexoProject<File> p) {
		List<String> returned = new ArrayList<>();
		for (FlexoResource<?> r : p.getAllResources()) {
			returned.add(r.getClass().getSimpleName() + ":" + r.getName());
		}
		Collections.sort(returned);
		return returned;
	}

	/**
	 * What a browser lists: the root folder of each resource center, so a project must have exactly one
	 */
	private static List<String> rootsOf(FlexoProject<File> p) {
		List<String> returned = new ArrayList<>();
		for (FlexoResourceCenter<?> rc : serviceManager.getResourceCenterService().getResourceCenters()) {
			if (rc.getRootFolder() != null && rc.getRootFolder().getSerializationArtefact() != null
					&& rc.getRootFolder().getSerializationArtefact().equals(p.getProjectDirectory())) {
				returned.add(rc.getClass().getSimpleName() + "@" + System.identityHashCode(rc));
			}
		}
		return returned;
	}

	/**
	 * What the resource browser shows under the project: the resources and sub-folders of its root folder, recursively
	 */
	private static List<String> browsed(FlexoProject<File> p) {
		List<String> returned = new ArrayList<>();
		collect(p.getRootFolder(), "", returned);
		Collections.sort(returned);
		return returned;
	}

	private static void collect(RepositoryFolder<?, ?> folder, String path, List<String> into) {
		if (folder == null) {
			return;
		}
		for (FlexoResource<?> r : folder.getResources()) {
			into.add(path + "/" + r.getClass().getSimpleName() + ":" + r.getName());
		}
		for (RepositoryFolder<?, ?> child : folder.getChildren()) {
			into.add(path + "/[" + child.getName() + "]");
			collect(child, path + "/" + child.getName(), into);
		}
	}

	@Test
	@TestOrder(1)
	public void testCreateProjectWithAVirtualModel() throws SaveResourceException, ModelDefinitionException {
		editor = createStandaloneProject("TestReopenProjectNoDuplicates");
		project = (FlexoProject<File>) editor.getProject();
		projectDirectory = project.getProjectDirectory();

		FMLTechnologyAdapter fmlTA = serviceManager.getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		CompilationUnitResource resource = fmlTA.getCompilationUnitResourceFactory().makeTopLevelCompilationUnitResource(VM_NAME,
				project.getProjectURI() + "/" + VM_NAME + ".fml", fmlTA.getVirtualModelRepository(project).getRootFolder(), true);
		assertNotNull(resource);
		resource.save();
		project.save();
		resourceCount = project.getAllResources().size();
		System.out.println("Before closing: " + resourceNames(project));
		System.out.println("ROOTS before closing: " + rootsOf(project));
		rootCount = rootsOf(project).size();
		browsedBefore = browsed(project);
		System.out.println("BROWSED before: " + browsedBefore);
	}

	/**
	 * As the application does: a new service manager, the project opened from its directory (not created in this session), and its
	 * resources taken as the reference
	 */
	@Test
	@TestOrder(2)
	public void testOpenFromDirectoryInANewServiceManager() {
		instanciateTestServiceManager();
		editor = loadProject(projectDirectory);
		project = (FlexoProject<File>) editor.getProject();
		resourceCount = project.getAllResources().size();
		browsedBefore = browsed(project);
		rootCount = rootsOf(project).size();
		System.out.println("BROWSED from directory: " + browsedBefore);
	}

	@Test
	@TestOrder(3)
	public void testCloseAndReopenWithTheProjectLoader() {
		serviceManager.getProjectLoaderService().closeProject(project);
		editor = loadProject(projectDirectory);
		project = (FlexoProject<File>) editor.getProject();
		System.out.println("After reopening: " + resourceNames(project));
		System.out.println("ROOTS after reopening: " + rootsOf(project));
		assertEquals("What the browser shows changed after reopening", browsedBefore, browsed(project));
		assertEquals("Root folders listed for the project: " + rootsOf(project), rootCount, rootsOf(project).size());
		assertEquals("The reopened project holds its resources more (or less) than once: " + resourceNames(project), resourceCount,
				project.getAllResources().size());
		FMLTechnologyAdapter fmlTA = serviceManager.getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		assertEquals(1, fmlTA.getVirtualModelRepository(project).getAllResources().stream().filter(r -> VM_NAME.equals(r.getName()))
				.count());
	}

	@Test
	@TestOrder(4)
	public void testCloseAndReopenAgain() {
		serviceManager.getProjectLoaderService().closeProject(project);
		editor = loadProject(projectDirectory);
		project = (FlexoProject<File>) editor.getProject();
		assertEquals(resourceNames(project).toString(), resourceCount, project.getAllResources().size());
		assertEquals("What the browser shows changed after reopening again", browsedBefore, browsed(project));
	}
}
