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
 * Every statement of the grammar is built into the control graph without a semantic issue, and the text printed from the model
 * (normalized, without the parsed text) parses again; a statement that cannot be built is reported (CORE-D-46). The execution of the
 * same statements is checked by {@code TestStatementLowering.fmlscript}.
 */
public class TestStatementLowering {

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

	private static final String STATEMENTS = "@URI(\"http://openflexo.org/test/StatementLoweringParser.fml\")\n" //
			+ "public model StatementLoweringParser {\n" //
			+ "\tint counter;\n" //
			+ "\tpublic int nestedIf() {\n" //
			+ "\t\tif (true)\n" //
			+ "\t\t\tif (false) return 1; else return 2;\n" //
			+ "\t\telse return 3;\n" //
			+ "\t\treturn 4;\n" //
			+ "\t}\n" //
			+ "\tpublic int arms() {\n" //
			+ "\t\tint n = 0;\n" //
			+ "\t\tif (false) while (false) n = 1; else n = 2;\n" //
			+ "\t\tif (true) for (int i = 0; i < 3; i++) n = n + 1; else n = 100;\n" //
			+ "\t\tif (true) ; else n = 0;\n" //
			+ "\t\t;\n" //
			+ "\t\treturn n;\n" //
			+ "\t}\n" //
			+ "\tpublic int loops() {\n" //
			+ "\t\tint n = 0;\n" //
			+ "\t\tdo {\n" //
			+ "\t\t\tn = n + 1;\n" //
			+ "\t\t} while (n < 3);\n" //
			+ "\t\tfor (int i = 0; ; i++) {\n" //
			+ "\t\t\tif (i == 4) {\n" //
			+ "\t\t\t\treturn i;\n" //
			+ "\t\t\t}\n" //
			+ "\t\t}\n" //
			+ "\t\treturn n;\n" //
			+ "\t}\n" //
			+ "\tpublic int assignments() {\n" //
			+ "\t\tint n = 5;\n" //
			+ "\t\tn += 2;\n" //
			+ "\t\tn *= 3;\n" //
			+ "\t\tn++;\n" //
			+ "\t\t--n;\n" //
			+ "\t\tcounter -= 1;\n" //
			+ "\t\treturn n;\n" //
			+ "\t}\n" //
			+ "}\n";

	private static FMLCompilationUnitParser parse(String fml) throws Exception {
		FMLCompilationUnitParser parser = new FMLCompilationUnitParser();
		FMLCompilationUnit compilationUnit = parser.parse(fml, new FMLModelFactory(null, serviceManager), modelSlotClasses -> null, true);
		assertNotNull(compilationUnit);
		return parser;
	}

	@Test
	public void everyStatementIsBuiltWithoutIssue() throws Exception {
		FMLCompilationUnitParser parser = parse(STATEMENTS);
		assertEquals("Semantic issues: " + parser.getFMLCompilationUnitNode().getSemanticAnalysisIssues(), 0,
				parser.getFMLCompilationUnitNode().getSemanticAnalysisIssues().size());
	}

	@Test
	public void normalizedTextParsesAgain() throws Exception {
		FMLCompilationUnit compilationUnit = parse(STATEMENTS).getFMLCompilationUnitNode().getModelObject();
		String normalized = compilationUnit.getPrettyPrintDelegate()
				.getNormalizedRepresentation(compilationUnit.getPrettyPrintDelegate().makePrettyPrintContext());
		System.out.println("===== normalized =====\n" + normalized);
		assertTrue("do ... while printed back:\n" + normalized, normalized.contains("do {"));
		assertTrue("compound assignment printed back:\n" + normalized, normalized.contains("n += 2"));
		FMLCompilationUnitParser reparsed = parse(normalized);
		assertEquals("Semantic issues after reparse: " + reparsed.getFMLCompilationUnitNode().getSemanticAnalysisIssues(), 0,
				reparsed.getFMLCompilationUnitNode().getSemanticAnalysisIssues().size());
	}

	@Test
	public void parsedTextParsesAgain() throws Exception {
		FMLCompilationUnit compilationUnit = parse(STATEMENTS).getFMLCompilationUnitNode().getModelObject();
		String printed = compilationUnit.getFMLPrettyPrint();
		System.out.println("===== printed =====\n" + printed);
		FMLCompilationUnitParser reparsed = parse(printed);
		assertEquals(0, reparsed.getFMLCompilationUnitNode().getSemanticAnalysisIssues().size());
	}

	@Test
	public void unbuildableStatementIsReported() throws Exception {
		String fml = "@URI(\"http://openflexo.org/test/StatementLoweringIssue.fml\")\n" //
				+ "public model StatementLoweringIssue {\n" //
				+ "\tpublic int noDeclaration() {\n" //
				+ "\t\tint i = 0;\n" //
				+ "\t\tfor (i = 0; i < 3; i++) {\n" //
				+ "\t\t}\n" //
				+ "\t\treturn i;\n" //
				+ "\t}\n" //
				+ "}\n";
		FMLCompilationUnitParser parser = parse(fml);
		assertTrue("A for loop without declaration must be reported",
				parser.getFMLCompilationUnitNode().getSemanticAnalysisIssues().size() > 0);
	}
}
