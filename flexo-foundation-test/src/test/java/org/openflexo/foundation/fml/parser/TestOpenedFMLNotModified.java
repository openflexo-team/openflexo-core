/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Cartoeditor, a component of the software infrastructure 
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


package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.TextUpdate;
import org.openflexo.foundation.test.OpenflexoTestCase;

/**
 * Opening a FML in the editor parses its text again and calls <code>updateWith()</code> with the result, exactly as
 * <code>FMLEditorParser.parse</code> does. Nothing was edited: the resource must not be marked modified (each object gets a new pretty-print
 * delegate, which is not an edition).
 */
public class TestOpenedFMLNotModified extends OpenflexoTestCase {

	private CompilationUnitResource resource;
	private FMLCompilationUnit compilationUnit;

	private void load() throws Exception {
		instanciateTestServiceManager();
		VirtualModel virtualModel = serviceManager.getVirtualModelLibrary()
				.getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestVirtualModelA.fml");
		assertNotNull(virtualModel);
		resource = virtualModel.getResource();
		compilationUnit = resource.getCompilationUnit();
		assertNotNull(compilationUnit);
		assertFalse("modified before any edition", resource.isModified());
	}

	/** What the FML editor does at each parse: parse the text again, then merge it into the compilation unit */
	private void parseAndUpdate(String fml, boolean textUnchanged) throws Exception {
		FMLCompilationUnit parsed = new FMLCompilationUnitParser().parse(fml, resource.getFactory(),
				modelSlotClasses -> resource.updateFMLModelFactory(modelSlotClasses), true);
		parsed.setResource(resource);
		try (TextUpdate update = new TextUpdate(resource, textUnchanged)) {
			compilationUnit.updateWith(parsed);
			compilationUnit.getTypingSpace().resolveUnresolvedTypes();
			compilationUnit.setResource(resource);
			compilationUnit.revalidateAllBindings();
		}
	}

	/** The FML is opened in the editor: its text is parsed again, unchanged */
	@Test
	public void openingTheEditorDoesNotMarkTheResourceModified() throws Exception {
		load();
		String text = resource.getRawSource();
		assertNotNull(text);

		parseAndUpdate(text, true);

		assertFalse("the resource is modified by the parse of its own text: " + resource.getURI(), resource.isModified());
		assertFalse("the compilation unit is modified by the parse of its own text", compilationUnit.isModified());
	}

	/** A comment or a blank typed in the editor is an edition: it is saved with the text */
	@Test
	public void aCommentOnlyEditMarksTheResourceModified() throws Exception {
		load();
		String text = resource.getRawSource();

		parseAndUpdate(text + "\n// a comment\n", false);

		assertTrue("a comment typed in the editor must mark the resource modified", resource.isModified());
	}

	@Test
	public void aWhitespaceOnlyEditMarksTheResourceModified() throws Exception {
		load();
		String text = resource.getRawSource();

		parseAndUpdate("\n\n" + text, false);

		assertTrue("a blank typed in the editor must mark the resource modified", resource.isModified());
	}

	/** A resource that was already modified stays so, whatever the text */
	@Test
	public void aModifiedResourceStaysModified() throws Exception {
		load();
		String text = resource.getRawSource();
		compilationUnit.setIsModified();
		assertTrue(resource.isModified());

		parseAndUpdate(text, true);

		assertTrue("the editor parse must not clean an edited resource", resource.isModified());
	}

	/** Typed declarations: the editor updates each of them (an updater) */
	@Test
	public void openingAFMLWithDeclarationsDoesNotMarkTheResourceModified() throws Exception {
		load();
		String fml = "@URI(\"http://openflexo.org/test/TestResourceCenter/TestVirtualModelA.fml\")\n" + "@Version(\"0.1\")\n"
				+ "model TestVirtualModelA {\n" + "	public concept Item {\n" + "		String label;\n" + "	}\n"
				+ "	public int count() {\n" + "		String s = \"a\";\n" + "		int n = 1;\n"
				+ "		List<Item> all = select Item from this;\n" + "		return all.size;\n" + "	}\n" + "}\n";

		// A real edition: the resource is modified
		parseAndUpdate(fml, false);
		assertTrue("an edition must mark the resource modified", resource.isModified());
		compilationUnit.clearIsModified();
		assertFalse(resource.isModified());

		// The editor parses the same text again
		parseAndUpdate(fml, true);
		assertFalse("the resource is modified by the parse of the same text: " + resource.getURI(), resource.isModified());

		// Then a property is added: an edition
		parseAndUpdate(fml.replace("String label;", "String label;\n		String code;"), false);
		assertTrue("an added property must mark the resource modified", resource.isModified());
	}
}
