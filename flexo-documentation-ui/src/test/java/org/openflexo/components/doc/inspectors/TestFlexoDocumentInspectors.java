/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Openflexo-technology-adapters-ui, a component of the software infrastructure 
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

package org.openflexo.components.doc.inspectors;

import org.junit.Test;
import org.openflexo.gina.test.GenericFIBInspectorTestCase;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class TestFlexoDocumentInspectors extends GenericFIBInspectorTestCase {

	/*
	 * Use this method to print all
	 * Then copy-paste 
	 */

	public static void main(String[] args) {
		System.out.println(generateInspectorTestCaseClass(((FileResourceImpl) ResourceLocator.locateResource("Inspectors/DocX")).getFile(),
				"Inspectors/DocX/"));
	}

	@Test
	public void testDocXDocumentInspector() {
		validateFIB("Inspectors/DocX/DocXDocument.inspector");
	}

	@Test
	public void testDocXDocumentResourceInspector() {
		validateFIB("Inspectors/DocX/DocXDocumentResource.inspector");
	}

	@Test
	public void testDocXFragmentRoleInspector() {
		validateFIB("Inspectors/DocX/DocXFragmentRole.inspector");
	}

	@Test
	public void testDocXImageRoleInspector() {
		validateFIB("Inspectors/DocX/DocXImageRole.inspector");
	}

	@Test
	public void testDocXModelSlotInspector() {
		validateFIB("Inspectors/DocX/DocXModelSlot.inspector");
	}

	@Test
	public void testDocXTableRoleInspector() {
		validateFIB("Inspectors/DocX/DocXTableRole.inspector");
	}

	@Test
	public void testAddDocXFragmentInspector() {
		validateFIB("Inspectors/DocX/EditionAction/AddDocXFragment.inspector");
	}

	@Test
	public void testSelectGeneratedDocXFragmentInspector() {
		validateFIB("Inspectors/DocX/EditionAction/SelectGeneratedDocXFragment.inspector");
	}

	@Test
	public void testSelectGeneratedDocXTableInspector() {
		validateFIB("Inspectors/DocX/EditionAction/SelectGeneratedDocXTable.inspector");
	}

	@Test
	public void testSelectGeneratedDocXTImageInspector() {
		validateFIB("Inspectors/DocX/EditionAction/SelectGeneratedDocXTImage.inspector");
	}

}
