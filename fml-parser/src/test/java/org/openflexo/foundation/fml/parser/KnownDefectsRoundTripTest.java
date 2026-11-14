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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Assume;
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
 * Known defects of KNOWN_DEFECTS.md about the text rebuilt from the model. Each test asserts the intended behaviour and fails
 * until its defect is fixed. Skipped unless the environment variable {@code OPENFLEXO_KNOWN_DEFECTS} is set:
 * 
 * <pre>
 * OPENFLEXO_KNOWN_DEFECTS=1 ./gradlew :fml-parser:cleanTest :fml-parser:test --tests '*KnownDefects*'
 * </pre>
 */
public class KnownDefectsRoundTripTest {

	static FlexoServiceManager serviceManager;

	@BeforeClass
	public static void initServiceManager() {
		Assume.assumeTrue("Known defects are run only when OPENFLEXO_KNOWN_DEFECTS is set", System.getenv("OPENFLEXO_KNOWN_DEFECTS") != null);
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

	private static void assertPrintedBack(String body) throws Exception {
		String fml = "@URI(\"http://openflexo.org/test/KnownDefectsUnbraced.fml\")\npublic model KnownDefectsUnbraced {\n\tpublic int m() {\n"
				+ "\t\tint n = 0;\n" + body + "\t\treturn n;\n\t}\n}\n";
		FMLCompilationUnit compilationUnit = new FMLCompilationUnitParser().parse(fml, new FMLModelFactory(null, serviceManager),
				modelSlotClasses -> null, true);
		assertEquals("CORE-D-56: the text printed back from the parsed text differs", fml, compilationUnit.getFMLPrettyPrint());
	}

	/**
	 * CORE-D-56: a statement written without braces is printed back, from the parsed text, as it was written.
	 */
	@Test
	public void coreD56_unbracedIfElseIsPrintedBackAsWritten() throws Exception {
		assertPrintedBack("\t\tif (n > 0) n = 1; else n = 2;\n");
	}

	@Test
	public void coreD56_unbracedWhileIsPrintedBackAsWritten() throws Exception {
		assertPrintedBack("\t\twhile (n < 3) n = n + 1;\n");
	}

	@Test
	public void coreD56_unbracedDoWhileIsPrintedBackAsWritten() throws Exception {
		assertPrintedBack("\t\tdo n = n + 1; while (n < 3);\n");
	}

	@Test
	public void coreD56_nestedUnbracedIfElseIsPrintedBackAsWritten() throws Exception {
		assertPrintedBack("\t\tif (n > 0)\n\t\t\tif (n > 1) n = 1; else n = 2;\n\t\telse n = 3;\n");
	}
}
