/**
 *
 * Copyright (c) 2014-2026, Openflexo
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

package org.openflexo.foundation.fml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.action.RenameCompilationUnit;
import org.openflexo.foundation.fml.action.RenameFlexoConcept;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.resource.FlexoResourceCenterService;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.FileUtils;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * CORE-F-4: renaming a concept whose <code>.fib</code> / <code>.inspector</code> is still resolved by the naming convention alone must
 * not orphan it. Both actions that can change a {@link FlexoConcept}'s name - {@link RenameFlexoConcept} for a nested concept,
 * {@link RenameCompilationUnit} for the VirtualModel itself, which IS a FlexoConcept - freeze that resolution into an explicit
 * <code>@UI</code> / <code>@Inspector</code> annotation before the name actually changes; see
 * {@link FlexoConcept#freezeConventionalUIComponentNames()}.
 *
 * <p>
 * Uses the same <code>TestContainerUI.fml</code> fixture as {@link TestContainerUIComponents}, but works on a COPY of it, in a temporary
 * directory resource center, and never on <code>flexo-test-resources</code> itself. {@link RenameCompilationUnit} really renames the
 * container and its file on disk, and that resource center is not always read-only: Gradle reads it from the packaged jar, but Eclipse
 * reads it from the project's <code>bin/main</code> directory, where the rename succeeded and left a <code>RenamedContainer.fml</code>
 * in place of the fixture, failing every later test that loads it (CORE-D-72).
 */
@RunWith(OrderedRunner.class)
public class TestRenameFlexoConcept extends OpenflexoTestCase {

	private static final String FIXTURE_URI = "http://openflexo.org/test/TestResourceCenter/TestContainerUI.fml";
	private static final String FIXTURE_PATH = "TestResourceCenter/FML/TestContainerUI.fml";
	private static final String TEST_RESOURCES_RC_URI = "http://openflexo.org/test/flexo-test-resources";

	private static VirtualModel virtualModel;
	private static FlexoEditor editor;

	@Test
	@TestOrder(1)
	public void test0LoadFixture() throws IOException {

		instanciateTestServiceManager();
		editor = new DefaultFlexoEditor(null, serviceManager);

		// The fixture keeps its @URI in the copy: unregister the resource center of flexo-test-resources first, so that only the copy
		// declares it
		FlexoResourceCenterService rcService = serviceManager.getResourceCenterService();
		FlexoResourceCenter<?> testResources = rcService.getFlexoResourceCenter(TEST_RESOURCES_RC_URI);
		assertNotNull("No resource center " + TEST_RESOURCES_RC_URI, testResources);
		rcService.removeFromResourceCenters(testResources);

		DirectoryResourceCenter copyRC = makeNewDirectoryResourceCenter();
		Resource fixture = ResourceLocator.locateResource(FIXTURE_PATH);
		assertNotNull("No fixture " + FIXTURE_PATH, fixture);
		File staging = Files.createTempDirectory("TestRenameFlexoConcept").toFile();
		try {
			File container = new File(staging, "TestContainerUI.fml");
			container.mkdirs();
			FileUtils.copyResourceToDir(fixture, container);
			copyIntoResourceCenter(container, copyRC);
		} finally {
			FileUtils.deleteDir(staging);
		}

		CompilationUnitResource resource = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(FIXTURE_URI);
		assertNotNull("No compilation unit for " + FIXTURE_URI, resource);

		virtualModel = resource.getCompilationUnit().getVirtualModel();
		assertNotNull(virtualModel);
	}

	/**
	 * Renaming a concept whose <code>.fib</code>/<code>.inspector</code> are still resolved by convention alone must not lose them: the
	 * rename action freezes the resolution into an explicit annotation first.
	 */
	@Test
	@TestOrder(2)
	public void test1RenamingAConceptPreservesItsConventionalComponents() {

		FlexoConcept simple = virtualModel.getFlexoConcept("Simple");
		assertNotNull(simple);
		assertTrue("The fixture lost its conventional components",
				simple.hasMetaData(FlexoConcept.UI_METADATA) == false && simple.getUIComponentResource() != null);

		RenameFlexoConcept action = RenameFlexoConcept.actionType.makeNewAction(simple, null, editor);
		action.setNewFlexoConceptName("Renamed");
		assertTrue(action.isValid());
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());

		assertEquals("Renamed", simple.getName());

		// The rename froze the convention into an explicit annotation, naming the SAME files as before
		assertEquals("Simple.fib", simple.getSingleMetaData(FlexoConcept.UI_METADATA, String.class));
		assertEquals("Simple.inspector", simple.getSingleMetaData(FlexoConcept.INSPECTOR_METADATA, String.class));

		// So resolution survives the rename - it would otherwise look for the non-existent Renamed.fib / Renamed.inspector
		assertResolvesTo("Simple.fib", simple.getUIComponentResource());
		assertResolvesTo("Simple.inspector", simple.getInspectorComponentResource());
	}

	/**
	 * A VirtualModel is a FlexoConcept and gets its own view the same way (<code>Xxx.fml/Xxx.fib</code>);
	 * {@link RenameCompilationUnit#doAction} renames it too (<code>FMLCompilationUnit.setName()</code> delegates to
	 * <code>getVirtualModel().setName()</code>), and must freeze it exactly like {@link RenameFlexoConcept} does - BEFORE that
	 * delegation, so the freeze does not depend on the rest of the rename succeeding.
	 *
	 * <p>
	 * The fixture is a writable copy (see the class javadoc), so the rename itself succeeds too: the container and its FML file are
	 * renamed on disk, and the components it ships - still named <code>TestContainerUI.*</code> - keep resolving through the frozen
	 * annotations.
	 */
	@Test
	@TestOrder(3)
	public void test2RenamingTheCompilationUnitFreezesTheVirtualModelsOwnComponentsFirst() {

		assertTrue("The fixture lost its conventional components",
				virtualModel.hasMetaData(FlexoConcept.UI_METADATA) == false && virtualModel.getUIComponentResource() != null);

		FMLCompilationUnit compilationUnit = virtualModel.getDeclaringCompilationUnit();
		RenameCompilationUnit action = RenameCompilationUnit.actionType.makeNewAction(compilationUnit, null, editor);
		action.setNewCompilationUnitName("RenamedContainer");
		action.setDefaultURI(true);
		assertTrue(action.isValid());
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		assertEquals("RenamedContainer", virtualModel.getName());

		File renamedContainer = new File(getResourceCenter().getRootDirectory(), "RenamedContainer.fml");
		assertTrue("Container not renamed on disk", new File(renamedContainer, "RenamedContainer.fml").isFile());
		assertFalse(new File(getResourceCenter().getRootDirectory(), "TestContainerUI.fml").exists());

		assertEquals("TestContainerUI.fib", virtualModel.getSingleMetaData(FlexoConcept.UI_METADATA, String.class));
		assertEquals("TestContainerUI.inspector", virtualModel.getSingleMetaData(FlexoConcept.INSPECTOR_METADATA, String.class));

		assertResolvesTo("TestContainerUI.fib", virtualModel.getUIComponentResource());
		assertResolvesTo("TestContainerUI.inspector", virtualModel.getInspectorComponentResource());
		assertTrue(new File(renamedContainer, "TestContainerUI.fib").isFile());
	}

	/** The fixture of flexo-test-resources was never touched: only its copy was renamed. */
	@Test
	@TestOrder(4)
	public void test3TheSharedFixtureIsUntouched() {
		assertNotNull(ResourceLocator.locateResource(FIXTURE_PATH + "/TestContainerUI.fml"));
		assertTrue(ResourceLocator.locateResource("TestResourceCenter/FML/RenamedContainer.fml") == null);
	}

	/**
	 * Assert supplied resource is the artefact simply named <code>expectedName</code>. Only the last element of the relative path may be
	 * compared: its base differs between a file-based and a jar-based resource center.
	 */
	private static void assertResolvesTo(String expectedName, Resource resolved) {
		assertNotNull("Resolved to nothing, expected " + expectedName, resolved);
		String path = resolved.getRelativePath();
		assertTrue("Expected an artefact named " + expectedName + ", got " + path, path.endsWith("/" + expectedName));
	}
}
