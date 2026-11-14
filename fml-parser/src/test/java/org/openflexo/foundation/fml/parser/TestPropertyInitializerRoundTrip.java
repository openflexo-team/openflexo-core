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
import org.openflexo.foundation.fml.PropertyCardinality;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.localization.LocalizationService;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;

/**
 * A property declaration with an initializer — an expression, or an edition action — is printed back as text that parses again, from
 * the parsed text and normalized, and keeps its initializer (CORE-D-51, CORE-D-52).
 */
public class TestPropertyInitializerRoundTrip {

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

	// select needs the FML@RT technology: its use declaration is written (CORE-D-31)
	private static final String FML = "use org.openflexo.foundation.fml.rt.FMLRTModelSlot as FMLRT;\n" //
			+ "\n" //
			+ "@URI(\"http://openflexo.org/test/PropertyInitializerRoundTrip.fml\")\n" //
			+ "public model PropertyInitializerRoundTrip {\n" //
			+ "\tpublic concept Person {\n" //
			+ "\t}\n" //
			+ "\tpublic concept Holder {\n" //
			+ "\t\tPart first = new Part();\n" //
			+ "\t\tPerson selected = select unique Person from container;\n" //
			+ "\t\tObject[0,*] objects;\n" //
			+ "\t\tpublic concept Part {\n" //
			+ "\t\t}\n" //
			+ "\t}\n" //
			+ "}\n";

	private static FMLCompilationUnitParser parse(String fml) throws Exception {
		FMLCompilationUnitParser parser = new FMLCompilationUnitParser();
		assertNotNull(parser.parse(fml, new FMLModelFactory(null, serviceManager), modelSlotClasses -> null, true));
		return parser;
	}

	private static void checkProperties(FMLCompilationUnit compilationUnit) {
		FlexoConcept holder = compilationUnit.getVirtualModel().getFlexoConcept("Holder");
		assertNotNull(holder);
		FlexoRole<?> first = (FlexoRole<?>) holder.getDeclaredProperty("first");
		FlexoRole<?> selected = (FlexoRole<?>) holder.getDeclaredProperty("selected");
		FlexoRole<?> objects = (FlexoRole<?>) holder.getDeclaredProperty("objects");
		assertTrue("the initializer of first is kept", first.getDefaultValue() != null && first.getDefaultValue().isSet());
		assertNotNull("the edition action initializing selected is kept", selected.getInitializer());
		assertEquals(PropertyCardinality.ZeroMany, objects.getCardinality());
	}

	@Test
	public void printedTextParsesAgain() throws Exception {
		FMLCompilationUnit compilationUnit = parse(FML).getFMLCompilationUnitNode().getModelObject();
		checkProperties(compilationUnit);
		String printed = compilationUnit.getFMLPrettyPrint();
		System.out.println("===== printed =====\n" + printed);
		assertEquals(FML, printed);
		checkProperties(parse(printed).getFMLCompilationUnitNode().getModelObject());
	}

	@Test
	public void normalizedTextParsesAgain() throws Exception {
		FMLCompilationUnit compilationUnit = parse(FML).getFMLCompilationUnitNode().getModelObject();
		String normalized = compilationUnit.getPrettyPrintDelegate()
				.getNormalizedRepresentation(compilationUnit.getPrettyPrintDelegate().makePrettyPrintContext());
		System.out.println("===== normalized =====\n" + normalized);
		assertTrue("initializer action printed back:\n" + normalized, normalized.contains("selected = select unique Person from container;"));
		checkProperties(parse(normalized).getFMLCompilationUnitNode().getModelObject());
	}

	/** Deleting the initializer action leaves the role without initializer, and the text parses again */
	@Test
	public void deletedInitializerIsGone() throws Exception {
		FMLCompilationUnit compilationUnit = parse(FML).getFMLCompilationUnitNode().getModelObject();
		FlexoRole<?> selected = (FlexoRole<?>) compilationUnit.getVirtualModel().getFlexoConcept("Holder").getDeclaredProperty("selected");
		selected.getInitializer().delete();
		assertTrue("no initializer left: " + selected.getInitializer(), selected.getInitializer() == null);
		String printed = compilationUnit.getFMLPrettyPrint();
		System.out.println("===== after deletion =====\n" + printed);
		assertTrue(printed, !printed.contains("select unique") && !printed.contains("selected ="));
		parse(printed);
	}
}
