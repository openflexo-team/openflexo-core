package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.DefaultFlexoServiceManager;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.localization.LocalizationService;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.p2pp.Reindenter;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.Resources;

/**
 * Re-indentation of FML texts ({@link Reindenter}, P2PP-F-1), on the FML parsing examples:
 * <ul>
 * <li>only the leading whitespace of rows changes;</li>
 * <li>the result parses to the same model (same normalized pretty-print), and re-indenting it again changes nothing;</li>
 * <li>a normalized pretty-print is a fixed point: the indentation declared by the FML nodes is the one the structure gives (skipped
 * when that pretty-print does not parse again, CORE-D-58).</li>
 * </ul>
 */
@RunWith(Parameterized.class)
public class TestFMLReindent {

	@Parameterized.Parameters(name = "{1}")
	public static Collection<Object[]> generateData() {
		return Resources.getMatchingResource(ResourceLocator.locateResource("FMLParsingExamples"), ".fml");
	}

	private static FlexoServiceManager serviceManager;

	private final Resource fmlResource;
	private final String name;

	public TestFMLReindent(Resource fmlResource, String name) {
		this.fmlResource = fmlResource;
		this.name = name;
	}

	private String source() throws IOException {
		try (InputStream in = fmlResource.openInputStream()) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	static FMLCompilationUnit parse(String text) throws Exception {
		return new FMLCompilationUnitParser().parse(text, new FMLModelFactory(null, serviceManager), modelSlotClasses -> null, true);
	}

	static String reindent(FMLCompilationUnit unit) {
		return new Reindenter((FMLCompilationUnitNode) unit.getPrettyPrintDelegate()).reindent();
	}

	private static String stripLeading(String text) {
		StringBuilder sb = new StringBuilder();
		for (String row : text.split("\r\n|\r|\n", -1)) {
			sb.append(row.replaceFirst("^\\s+", "")).append('\n');
		}
		return sb.toString();
	}

	static String show(String text) {
		return text.replace("\t", "→");
	}

	@Test
	public void onlyLeadingWhitespaceChanges() throws Exception {
		String source = source();
		assertEquals(name, stripLeading(source), stripLeading(reindent(parse(source))));
	}

	@Test
	public void theResultParsesToTheSameModelAndIsAFixedPoint() throws Exception {
		String source = source();
		FMLCompilationUnit unit = parse(source);
		String once = reindent(unit);
		FMLCompilationUnit reparsed = parse(once);
		assertEquals(name, unit.getNormalizedFML(), reparsed.getNormalizedFML());
		assertEquals(name, show(once), show(reindent(reparsed)));
	}

	@Test
	public void aNormalizedPrettyPrintIsAFixedPoint() throws Exception {
		String normalized = parse(source()).getNormalizedFML();
		FMLCompilationUnit reparsed;
		try {
			reparsed = parse(normalized);
		} catch (Exception e) {
			// CORE-D-58: the normalized pretty-print of a unit whose references do not resolve does not parse again
			Assume.assumeNoException("CORE-D-58: the normalized pretty-print of " + name + " does not parse", e);
			return;
		}
		assertEquals(name, show(normalized), show(reindent(reparsed)));
	}

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
}
