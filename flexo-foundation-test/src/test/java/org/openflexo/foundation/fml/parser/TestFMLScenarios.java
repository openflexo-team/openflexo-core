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
import org.openflexo.foundation.FlexoServiceManager;
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
		serviceManager = FMLTestServiceManager.create();
	}
}
