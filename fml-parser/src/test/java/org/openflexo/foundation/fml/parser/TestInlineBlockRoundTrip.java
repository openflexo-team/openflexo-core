/**
 *
 * Copyright (c) 2026, Openflexo
 *
 * This file is part of FML-parser, a component of the software infrastructure
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

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.DefaultFlexoServiceManager;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.localization.LocalizationService;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;

/**
 * Round-trip pretty-print regression tests for two defects exposed when reloading a VirtualModel in
 * the editor (which applies the preserving {@code getFMLPrettyPrint()}, not the normalized form):
 * <ul>
 * <li>an inline single-statement block {@code { return null; }} was reformatted as
 * {@code { return null; \n}} (the closing brace has a canonical LINE_SEPARATOR prelude that no longer
 * matched the inline whitespace), inflating the file line by line;</li>
 * <li>a fully-qualified static call {@code java.lang.Double.parseDouble(x)} serialized as the
 * unparsable {@code double.parseDouble(x)} (the call used the method return type as receiver).</li>
 * </ul>
 *
 * @author sylvain
 */
public class TestInlineBlockRoundTrip {

	static FlexoServiceManager serviceManager;

	@BeforeClass
	public static void initServiceManager() {
		serviceManager = new DefaultFlexoServiceManager(null, false, true) {
			@Override
			protected LocalizationService createLocalizationService(String relativePath) {
				LocalizationService returned = super.createLocalizationService(relativePath);
				returned.setAutomaticSaving(false);
				return returned;
			}

			@Override
			protected FlexoEditingContext createEditingContext() {
				return FlexoEditingContext.createInstance(false);
			}

			@Override
			protected DefaultFlexoEditor createApplicationEditor() {
				return new DefaultFlexoEditor(null, this);
			}

			@Override
			protected ProjectLoader createProjectLoaderService() {
				return new ProjectLoader();
			}
		};
		serviceManager.getLocalizationService().setAutomaticSaving(false);
		TechnologyAdapterService taService = serviceManager.getTechnologyAdapterService();
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLTechnologyAdapter.class), true);
		taService.activateTechnologyAdapter(taService.getTechnologyAdapter(FMLRTTechnologyAdapter.class), true);
	}

	@Test
	public void inlineBlockAndStaticCallArePreserved() throws Exception {
		String fml = "@URI(\"http://openflexo.org/test/InlineBlockRoundTrip.fml\")\n" //
				+ "public model TestInlineBlock {\n" //
				+ "\n" //
				+ "\tpublic Double parseDoubleFML(String s) {\n" //
				+ "\t\tif (parameters.s == null) { return null; }\n" //
				+ "\t\treturn java.lang.Double.parseDouble(parameters.s);\n" //
				+ "\t}\n" //
				+ "}\n";

		FMLModelFactory factory = new FMLModelFactory(null, serviceManager);
		FMLCompilationUnitParser parser = new FMLCompilationUnitParser();
		FMLCompilationUnit compilationUnit = parser.parse(fml, factory, modelSlotClasses -> null, true);
		assertNotNull(compilationUnit);

		String pp = compilationUnit.getFMLPrettyPrint();
		System.out.println("===== ROUND-TRIP PRETTY-PRINT =====\n" + pp + "\n===================================");

		// Inline single-statement block must stay inline: the closing brace must not gain a newline.
		assertTrue("inline then-block not preserved:\n" + pp, pp.contains("{ return null; }"));
		assertFalse("closing brace pushed onto its own line:\n" + pp, pp.contains("return null; \n"));

		// Static call must serialize with its DECLARING type, not the primitive method return type.
		assertFalse("static call serialized with primitive receiver (double.parseDouble):\n" + pp, pp.contains("double.parseDouble"));
		assertTrue("static call lost its declaring type:\n" + pp, pp.contains("Double.parseDouble("));
	}
}
