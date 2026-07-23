/**
 *
 * Copyright (c) 2019, Openflexo
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

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.DefaultFlexoServiceManager;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.localization.LocalizationService;
import org.openflexo.foundation.project.ProjectLoader;

/**
 * Ensures that <b>property-level</b> FML meta-data (annotations such as <code>@Property(...)</code>) survive a normalized pretty-print.
 *
 * <p>
 * Historically only model- and concept-level annotations were emitted by the pretty-printer ({@code VirtualModelNode} /
 * {@code FlexoConceptNode}); property nodes dropped their meta-data. The fix adds the meta-data emission to the common base
 * {@code FlexoPropertyNode}. This test guards against a regression of that behaviour.
 * </p>
 */
public class TestPropertyMetaDataPrettyPrint {

	private static FlexoServiceManager serviceManager;

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
	}

	@Test
	public void propertyLevelAnnotationsAreEmitted() throws Exception {
		String fml = "model MyModel {\n" //
				+ "\t@Table(\"CLIENT\")\n" //
				+ "\tconcept Client {\n" //
				+ "\t\t@Property(column=\"NAME\")\n" //
				+ "\t\tString name;\n" //
				+ "\t}\n" //
				+ "}\n";

		FMLModelFactory factory = new FMLModelFactory(null, serviceManager);
		FMLCompilationUnitParser parser = new FMLCompilationUnitParser();
		FMLCompilationUnit cu = parser.parse(fml, factory, (modelSlotClasses) -> null, true);

		String prettyPrint = cu.getFMLPrettyPrint();
		System.out.println(prettyPrint);

		// Concept-level annotation was already emitted before the fix
		assertTrue("Missing concept-level @Table in:\n" + prettyPrint, prettyPrint.contains("@Table(\"CLIENT\")"));
		// Property-level annotation is what the fix restores
		assertTrue("Missing property-level @Property in:\n" + prettyPrint, prettyPrint.contains("@Property(column=\"NAME\")"));
	}
}
