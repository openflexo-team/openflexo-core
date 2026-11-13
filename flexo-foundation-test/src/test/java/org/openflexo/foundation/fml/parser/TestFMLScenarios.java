package org.openflexo.foundation.fml.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.DefaultFlexoServiceManager;
import org.openflexo.foundation.FlexoEditingContext;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.localization.LocalizationService;
import org.openflexo.foundation.project.ProjectLoader;
import org.openflexo.foundation.technologyadapter.TechnologyAdapterService;
import org.openflexo.p2pp.testing.ResourceFiles;
import org.openflexo.p2pp.testing.scenario.Scenario;
import org.openflexo.p2pp.testing.scenario.ScenarioFile;
import org.openflexo.p2pp.testing.scenario.ScenarioRunner;

/**
 * Runs every scenario of {@code src/test/resources/fml-scenarios/*.p2pt} on FML: a source, then steps of edits (see {@link FMLEdits}) each
 * followed by the pretty-prints expected. Most come from the {@code TestFMLPrettyPrintN} tests, whose source and expected files they carry
 * (the checks of the positions of the nodes stay in those tests).
 */
@RunWith(Parameterized.class)
public class TestFMLScenarios {

	private static FlexoServiceManager serviceManager;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> scenarios() throws IOException {
		List<Object[]> returned = new ArrayList<>();
		for (File f : ResourceFiles.list(TestFMLScenarios.class, "/fml-scenarios", ".p2pt")) {
			for (Scenario s : ScenarioFile.read(f)) {
				returned.add(new Object[] { s.toString(), s });
			}
		}
		return returned;
	}

	private final Scenario scenario;

	public TestFMLScenarios(String name, Scenario scenario) {
		this.scenario = scenario;
	}

	@Test
	public void run() {
		ScenarioRunner.run(new FMLTarget(serviceManager), scenario);
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
