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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.test.OpenflexoProjectAtRunTimeTestCase;

/**
 * This unit test is intented to test project creation facilities
 * 
 * @author sylvain
 * 
 */
public class TestCreateProjectInResourceCenter extends OpenflexoProjectAtRunTimeTestCase {

	private static DirectoryResourceCenter resourceCenter;

	@Test
	public void testCreateProject() throws IOException {

		instanciateTestServiceManager();

		resourceCenter = makeNewDirectoryResourceCenter();
		assertNotNull(resourceCenter);
		System.out.println("ResourceCenter= " + resourceCenter);

		FlexoEditor editor = createProjectInResourceCenter("TestProject", resourceCenter);
		FlexoProject<File> project = (FlexoProject<File>) editor.getProject();
		System.out.println("Created project " + project.getProjectDirectory());
		assertTrue(project.getProjectDirectory().exists());
		assertTrue(project.getResource().getIODelegate().exists());
		assertFalse(project.isStandAlone());
		assertEquals(project.getRootFolder().getSerializationArtefact(), project.getProjectDirectory());
		assertEquals(project.getBaseArtefact().getParentFile(), project.getProjectDirectory());
		assertSame(resourceCenter, project.getResourceCenter());
		assertTrue(project.getDelegateResourceCenter() instanceof DirectoryResourceCenter);
		assertEquals(project.getProjectDirectory(), ((DirectoryResourceCenter) project.getDelegateResourceCenter()).getRootDirectory());
	}

}
